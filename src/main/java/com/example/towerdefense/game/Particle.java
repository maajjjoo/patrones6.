package com.example.towerdefense.game;

import java.awt.*;

/**
 * Partícula de efecto visual (explosión, impacto, etc.)
 */
public class Particle {

    float x, y, vx, vy;
    float life, maxLife;
    Color color;
    float size;
    boolean dead = false;

    public Particle(float x, float y, Color color, float vx, float vy, float life, float size) {
        this.x = x; this.y = y;
        this.color = color;
        this.vx = vx; this.vy = vy;
        this.life = life; this.maxLife = life;
        this.size = size;
    }

    public void update(float dt) {
        x  += vx * dt;
        y  += vy * dt;
        vy += 60 * dt; // gravedad leve
        life -= dt;
        if (life <= 0) dead = true;
    }

    public void draw(Graphics2D g2) {
        if (dead) return;
        float alpha = life / maxLife;
        int a = (int)(alpha * 255);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        int s = (int)(size * alpha);
        if (s < 1) return;
        g2.fillOval((int)x - s, (int)y - s, s * 2, s * 2);
    }
}
