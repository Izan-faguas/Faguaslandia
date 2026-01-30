#!/bin/bash
# Ruta a JavaFX
JAVA_FX="$HOME/Escritorio/javafx-sdk-17.0.18/lib"

# Ruta al JAR (en otra carpeta)
APP_JAR="$HOME/FaguaslandiaLauncher/launcher-1.0.0.jar"

# Ejecutar el launcher
java --module-path "$JAVA_FX" --add-modules javafx.controls,javafx.fxml -jar "$APP_JAR"
