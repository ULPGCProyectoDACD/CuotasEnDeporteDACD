package org.ulpgc.dacd.datamart.control.trainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PythonModelTrainer implements ModelTrainer {
    private final String mlDirectoryName;
    private final String scriptName;
    private final String pythonExePath;
    private final String basePath;

    public PythonModelTrainer(String mlDirectoryName, String scriptName, String basePath) {
        this.mlDirectoryName = mlDirectoryName;
        this.scriptName = scriptName;
        this.basePath = basePath;
        this.pythonExePath = determinePythonPath();
    }

    private String determinePythonPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            System.out.println("💻 Detectado sistema Windows. Configurando rutas...");
            return "venv" + File.separator + "Scripts" + File.separator + "python.exe";
        } else {
            System.out.println("🍎 Detectado sistema Mac/Linux. Configurando rutas...");
            return "venv" + File.separator + "bin" + File.separator + "python";
        }
    }

    @Override
    public void trainModel() {
        System.out.println("Iniciando orquestación del entrenamiento en Python...");
        try {
            Path mlFolder = resolveWorkingDirectory();
            Path pythonExe = mlFolder.resolve(pythonExePath);

            if (!Files.exists(pythonExe)) {
                System.err.println("❌ ERROR: No se encuentra el ejecutable de Python en: " + pythonExe.toAbsolutePath());
                return;
            }
            executePythonScript(mlFolder, pythonExe);
        } catch (Exception e) {
            System.err.println("❌ Error crítico al intentar ejecutar Python: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Path resolveWorkingDirectory() {
        Path baseDir = Paths.get(basePath);
        Path mlFolder = baseDir.resolve(mlDirectoryName);
        return Files.exists(mlFolder) ? mlFolder : baseDir.resolveSibling(mlDirectoryName);
    }

    private void executePythonScript(Path workingDirectory, Path pythonExe) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExe.toString(), scriptName)
                .directory(workingDirectory.toFile())
                .inheritIO();
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        int exitCode = processBuilder.start().waitFor();
        if (exitCode == 0) {
            System.out.println("✅ Entrenamiento completado.");
        } else {
            System.err.println("❌ Hubo un error en Python. Código de salida: " + exitCode);
        }
    }
}