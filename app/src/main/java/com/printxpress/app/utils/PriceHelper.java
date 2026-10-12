package com.printxpress.app.utils;

import java.text.DecimalFormat;

public class PriceHelper {

    public static String formatLKR(double amount) {
        DecimalFormat formatter = new DecimalFormat("LKR #,##0.00");
        return formatter.format(amount);
    }

    public static double calculateTotal(double base, double modifier, int quantity) {
        return (base + modifier) * quantity;
    }
}
