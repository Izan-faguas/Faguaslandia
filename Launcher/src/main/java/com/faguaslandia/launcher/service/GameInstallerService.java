package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mslinks.ShellLink;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

public class GameInstallerService {

    private static final String BASE_DIR =
            System.getProperty("user.home") + "/Faguaslandia/games/";

    private static final String LAUNCHER_EXE =
            System.getProperty("user.home") + "/Faguaslandia/Faguaslandia.exe";

    private final ObjectMapper mapper = new ObjectMapper();

    // ════════════════════════════════════════════════════
    //  INSTALACIÓN
    // ════════════════════════════════════════════════════

    public boolean isInstalled(String gameName) {
        File folder = new File(BASE_DIR + gameName);
        if (!folder.exists() || !folder.isDirectory()) return false;
        return findExe(folder) != null;
    }

    public void install(String gameName, String downloadUrl) {
        try {
            File gameDir = new File(BASE_DIR + gameName);
            if (gameDir.exists()) deleteDirectory(gameDir);
            gameDir.mkdirs();

            String zipPath = gameDir.getAbsolutePath() + "/game.zip";
            downloadFile(downloadUrl, zipPath);

            File zip = new File(zipPath);
            unzip(zipPath, gameDir.getAbsolutePath());
            zip.delete();

            // Crear acceso directo en el escritorio al terminar
            crearAccesoDirecto(gameName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listarArchivos(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            System.out.println(indent + f.getName());
            if (f.isDirectory()) listarArchivos(f, indent + "  ");
        }
    }

    // ════════════════════════════════════════════════════
    //  LANZAMIENTO CON REGISTRO DE SESIÓN
    // ════════════════════════════════════════════════════

    /**
     * Lanza el juego y registra la sesión en el backend.
     * Requiere usuarioId y juegoId para poder llamar a /sesiones/iniciar.
     */
    public void launch(String gameName, Long usuarioId, Long juegoId) {
        try {
            File dir = new File(BASE_DIR + gameName);
            System.out.println("Buscando en: " + dir.getAbsolutePath());
            System.out.println("Existe: " + dir.exists());

            // Listar todos los archivos recursivamente
            listarArchivos(dir, "");

            File exe = findExe(dir);
            System.out.println("EXE encontrado: " + exe);
            if (exe == null) {
                System.out.println("No se encontró .exe para: " + gameName);
                return;
            }

            // Iniciar sesión en el backend
            Long sesionId = iniciarSesion(usuarioId, juegoId);

            // Lanzar el proceso del juego
            Process proceso = new ProcessBuilder(exe.getAbsolutePath())
                    .directory(exe.getParentFile())
                    .start();

            // Vigilar el proceso en background para finalizar la sesión al cerrar
            if (sesionId != null) {
                final long sid = sesionId;
                Thread watcher = new Thread(() -> {
                    try {
                        proceso.waitFor();
                        finalizarSesion(sid);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                });
                watcher.setDaemon(true);
                watcher.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sobrecarga sin sesión — compatibilidad con llamadas antiguas.
     */
    public void launch(String gameName) {
        launch(gameName, null, null);
    }

    // ════════════════════════════════════════════════════
    //  SESIONES
    // ════════════════════════════════════════════════════

    private Long iniciarSesion(Long usuarioId, Long juegoId) {
        if (usuarioId == null || juegoId == null) return null;
        try {
            String json = String.format("{\"usuarioId\":%d,\"juegoId\":%d}", usuarioId, juegoId);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(Config.API_BASE_URL + "/sesiones/iniciar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode node = mapper.readTree(resp.body());
                return node.path("id").asLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void finalizarSesion(long sesionId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(Config.API_BASE_URL + "/sesiones/finalizar/" + sesionId))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            AuthService.getClient().send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════
    //  ACCESO DIRECTO
    // ════════════════════════════════════════════════════

    private void crearAccesoDirecto(String gameName) {
        try {
            File launcher = new File(LAUNCHER_EXE);
            if (!launcher.exists()) {
                System.out.println("Launcher .exe no encontrado, no se crea acceso directo.");
                return;
            }

            String escritorio = FileSystemView
                    .getFileSystemView()
                    .getHomeDirectory()
                    .getAbsolutePath();
            String lnkPath    = escritorio + "/" + gameName + ".lnk";

            ShellLink sl = ShellLink.createLink(LAUNCHER_EXE)
                    .setWorkingDir(new File(LAUNCHER_EXE).getParent())
                    .setCMDArgs("--launch " + gameName);

            // Intentar usar el .exe del juego como icono
            File gameDir = new File(BASE_DIR + gameName);
            File exe = findExe(gameDir);
            if (exe != null) {
                sl.getHeader().setIconIndex(0);
                sl.setIconLocation(exe.getAbsolutePath());
            }

            sl.saveTo(lnkPath);
            System.out.println("Acceso directo creado: " + lnkPath);

        } catch (Exception e) {
            System.out.println("No se pudo crear el acceso directo: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════
    //  DESCARGA Y DESCOMPRESIÓN
    // ════════════════════════════════════════════════════

    private void downloadFile(String url, String outputPath) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();

        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipFilePath);
        zipFile.stream().forEach(entry -> {
            try {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists()) parent.mkdirs();
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        zipFile.close();
    }

    // ════════════════════════════════════════════════════
    //  UTILS
    // ════════════════════════════════════════════════════

    private File findExe(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return null;
        File crashHandler = null;
        for (File f : files) {
            if (f.isDirectory()) {
                File nested = findExe(f);
                if (nested != null) return nested;
            }
            if (f.getName().toLowerCase().endsWith(".exe")) {
                if (f.getName().toLowerCase().contains("crashhandler")) {
                    crashHandler = f;
                    continue;
                }
                return f;
            }
        }
        return crashHandler;
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) deleteDirectory(file);
                else file.delete();
            }
        }
        dir.delete();
    }
}