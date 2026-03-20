package com.example.towerdefense;

import com.example.towerdefense.game.GamePanel;
import com.example.towerdefense.service.TowerService;

import javax.swing.*;

/**
 * Punto de entrada. Lanza la ventana Swing del juego.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TowerService towerService = new TowerService();

            JFrame frame = new JFrame("Torre Defensiva — Patrón Decorator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel(towerService);
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
