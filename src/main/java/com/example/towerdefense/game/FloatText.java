package com.example.towerdefense.game;

import java.awt.*;

/**
 * Texto flotante kawaii que sube y desaparece.
 */
public class FloatText {

    String  text;
    float   x, y;
    Color   color;
    float   life = 1.0f;
    boolean dead = false;

    public FloatText(String text, float x, float y, Color color) {
        this.text  = text;
        this.x     = x;
        this.y     = y;
        this.color = color;
    }

    public void update(float dt) {
        y    -= 45 * dt;
        life -= dt;
        if (life <= 0) dead = true;
    }

    public void draw(Graphics2D g2) {
        if (dead) return;
        int alpha = (int)(life * 255);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        // Sombra suave
        g2.setColor(new Color(255, 255, 255, alpha / 2));
        g2.drawString(text, (int)x - 9, (int)y + 1);
        // Texto
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2.drawString(text, (int)x - 10, (int)y);
    }
}
