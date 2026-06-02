package com.fulvian.utils;

public class TestDataHelper {

    private TestDataHelper() {
        // Utility class, tidak perlu dibuat object
    }

    public static String randomProductName() {
        return "Produk Test " + System.currentTimeMillis();
    }

    public static String randomBarcode() {
        return "BRG-" + System.currentTimeMillis();
    }

    public static String defaultCategory() {
        return "Umum";
    }

    public static String defaultPrice() {
        return "10000";
    }

    public static String defaultStock() {
        return "10";
    }
}