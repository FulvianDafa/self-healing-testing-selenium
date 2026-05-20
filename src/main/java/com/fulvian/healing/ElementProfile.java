package com.fulvian.healing;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

/**
 * Snapshot atribut elemen target SEBELUM locator gagal.
 * Digunakan SimilarityEngine sebagai referensi perbandingan.
 *
 * Kenapa perlu class ini?
 * Saat locator gagal, kita tidak punya referensi elemen asli.
 * Solusinya: simpan dulu profil elemen (teks, id, class, posisi)
 * dari informasi yang kita tahu sebelum test berjalan.
 */
public class ElementProfile {

    public final String expectedText;   // teks yang terlihat user di layar
    public final String originalLocatorValue; // nilai locator asli (misal: "inputProductBtn")
    public final String locatorType;    // "id", "class", "xpath", "css"

    // Posisi elemen TERAKHIR yang diketahui (opsional, bisa null kalau pertama kali)
    public final Point  lastKnownLocation;

    // -------------------------------------------------------
    // Constructor lengkap (pakai ini kalau punya posisi terakhir)
    // -------------------------------------------------------
    public ElementProfile(String expectedText,
                          String originalLocatorValue,
                          String locatorType,
                          Point  lastKnownLocation) {
        this.expectedText         = expectedText         == null ? "" : expectedText.trim();
        this.originalLocatorValue = originalLocatorValue == null ? "" : originalLocatorValue.trim();
        this.locatorType          = locatorType          == null ? "id" : locatorType.toLowerCase();
        this.lastKnownLocation    = lastKnownLocation;
    }

    // -------------------------------------------------------
    // Constructor ringkas (tanpa posisi — paling sering dipakai)
    // -------------------------------------------------------
    public ElementProfile(String expectedText,
                          String originalLocatorValue,
                          String locatorType) {
        this(expectedText, originalLocatorValue, locatorType, null);
    }

    @Override
    public String toString() {
        return String.format("ElementProfile[text='%s', locator=%s:'%s']",
                expectedText, locatorType, originalLocatorValue);
    }
}
