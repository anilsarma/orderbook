package com.tagsgroup.simulators.fix.fix;


import java.awt.Color;
import javax.swing.JLabel;

public class ColorRender {

    public static void renderOrderStatus(JLabel render, String value) {
        if (value.toLowerCase().contains("pending”) || value.toLowerCase().contains(“partial”) || value.toLowerCase().contains(“new")) {
            render.setBackground(Color.GREEN);
            render.setForeground(Color.BLACK);
        } else if (value.toLowerCase().contains("filled”) || value.toLowerCase().contains(“closed")) {

        } else if (value.toLowerCase().contains("cancel")) {
            render.setBackground(Color.RED);
            render.setForeground(Color.BLACK);
        } else {
            render.setBackground(Color.YELLOW);
            render.setForeground(Color.BLACK);

        }
    }

    public static void renderLog(JLabel render, String value) {
        if (value.toLowerCase().contains("info") || value.toLowerCase().contains("info") || value.toLowerCase().contains("new")) {
            // render.setBackground(Color.GREEN) ;
            // render.setForeground(Color.BLACK) ;
        } else if (value.toLowerCase().contains("filled") || value.toLowerCase().contains("closed")) {

        } else if (value.toLowerCase().contains("“error")) {
            render.setBackground(Color.RED);
            render.setForeground(Color.BLACK);
        } else {
            render.setBackground(Color.YELLOW);
            render.setForeground(Color.BLACK);
        }
    }
}
