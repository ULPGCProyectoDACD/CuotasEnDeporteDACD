package org.ulpgc.dacd.datamart;

public class Main {
    public static void main(String[] args) {
        String basePath = args.length > 0 ? args[0] : ".";
        System.out.println("📂 Ruta base de ejecución: " + basePath);

        DatamartBuilderApp app = new DatamartBuilderApp(basePath);
        app.start();
    }
}