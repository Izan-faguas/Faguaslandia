package com.faguaslandia.launcher.service;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GameInstallerService {

    private static final String BASE_DIR =
            System.getProperty("user.home") + "/Faguaslandia/games/";

    public boolean isInstalled(String gameName) {
        File folder = new File(BASE_DIR + gameName);
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        File exe = findExe(folder);
        return exe != null;
    }

    public void install(String gameName, String downloadUrl) {
        try {
            File gameDir = new File(BASE_DIR + gameName);

            if (gameDir.exists()) {
                deleteDirectory(gameDir);
            }

            gameDir.mkdirs();

            String zipPath = gameDir.getAbsolutePath() + "/game.zip";

            System.out.println("Descargando: " + downloadUrl);
            System.out.println("ZIP destino: " + zipPath);

            downloadFile(downloadUrl, zipPath);

            File zip = new File(zipPath);

            System.out.println("ZIP existe: " + zip.exists());
            System.out.println("Tamaño ZIP: " + zip.length() + " bytes");

            unzip(zipPath, gameDir.getAbsolutePath());

            zip.delete();

            System.out.println("Instalación completada");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void downloadFile(String url, String outputPath) throws IOException {

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {

        File dir = new File(destDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipFilePath);

        zipFile.stream().forEach(entry -> {
            try {
                File newFile = new File(destDir, entry.getName());

                System.out.println("Extrayendo: " + entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();

                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (
                            InputStream is = zipFile.getInputStream(entry);
                            FileOutputStream fos = new FileOutputStream(newFile)
                    ) {
                        byte[] buffer = new byte[8192];
                        int len;

                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        zipFile.close();

        System.out.println("ZIP extraído correctamente");
    }

    public void launch(String gameName) {
        try {
            File dir = new File(BASE_DIR + gameName);

            File exe = findExe(dir);

            if (exe != null) {
                new ProcessBuilder(exe.getAbsolutePath()).start();
            } else {
                System.out.println("No se encontró .exe");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

                String name = f.getName().toLowerCase();

                if (name.contains("crashhandler")) {
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

                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        dir.delete();
    }
}