package com.faguaslandia.config;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
public class Descompresor {

    public static void descomprimirZip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    // Crear carpetas padre
                    if (newFile.getParent() != null) {
                        Files.createDirectories(newFile.getParent());
                    }
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        System.out.println("Descompresión completa en: " + destDir.toString());
    }
}
