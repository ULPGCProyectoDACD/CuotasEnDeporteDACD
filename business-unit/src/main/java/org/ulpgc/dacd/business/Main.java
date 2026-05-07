package org.ulpgc.dacd.business;

import org.ulpgc.dacd.business.control.PythonModelTrainer;

public class Main {
    public static void main(String[] args) {
        String mlDirectory = "machine-learning";
        String scriptToRun = "main.py";

        PythonModelTrainer trainer = new PythonModelTrainer(mlDirectory, scriptToRun);
        trainer.trainModel();
    }
}