package org.ulpgc.dacd.business;

public class Main {
    public static void main(String[] args) {
        String basePath = args.length > 0 ? args[0] : ".";
        System.out.println("📂 Ruta base de ejecución: " + basePath);

        BusinessUnitApp app = new BusinessUnitApp(basePath);
        app.start();
    }
}