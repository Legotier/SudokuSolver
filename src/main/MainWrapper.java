package main;

import javax.swing.*;

/**
 * A wrapper for the main class so that Java doesn't complain that the JavaFX binaries aren't in modules
 */
public class MainWrapper {
    public static void main(String[] args) {
        try {
            main.Main.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error while starting", JOptionPane.ERROR_MESSAGE);
        }
    }
}