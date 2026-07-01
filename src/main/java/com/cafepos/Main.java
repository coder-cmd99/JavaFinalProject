package com.cafepos;

import com.cafepos.gui.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Run with:
 *   javac -cp .;lib/mysql-connector-j-*.jar -d out src/main/java/com/cafepos/**\/*.java
 *   java  -cp out;lib/mysql-connector-j-*.jar com.cafepos.Main
 *
 * Or via the provided build.bat / build.sh scripts.
 */
public class Main {

    public static void main(String[] args) {
        // Apply system look-and-feel for native dialogs on the host OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Metal L&F
        }

        // All Swing operations must happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
