package org.example;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class EmailApprovalApp {
    public static void main(String[] args) {
        // Set the look and feel to the system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GraphicalUserInterface gui = new GraphicalUserInterface();
            gui.setVisible(true);
        });
    }
}