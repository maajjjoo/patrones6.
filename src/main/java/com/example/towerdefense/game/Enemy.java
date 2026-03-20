package com.example.towerdefense.game;

import java.awt.*;
import java.util.Random;

/**
 * Fantasma que avanza de izquierda a derecha hacia la torre.
 * Hay 3 tipos: NORMAL, FAST, BOSS.
 */
public class Enemy {

    public enum Type { NORMAL, FAST, BOSS }

    float   x, y;
    int     maxHp, hp;
    float   speed;
    float   frozen      = 0;
    boolean dead        = false;
    final int contactDamage;
    final Type type;
    private final float floatOffset;
    private final int   size;

    public Enemy(int wave, Random rng, float startY, Type type) {
        this.type        = type;
        this.floatOffset = rng.nextFloat() * (float)(Math.PI * 2);

        switch (type) {
            case FAST -> {
                size          = 11;
                maxHp         = 40 + wave * 15;
                speed         = 110 + wave * 12;
                contactDamage = 10 + wave * 2;
            }
            case BOSS -> {
                size          = 22;
                maxHp         = 200 + wave * 60;
                speed         = 35 + wave * 5;
                contactDamage = 40 + wave * 8;
            }
            default -> {           // NORMAL
                size          = 15;
                maxHp         = 70 + wave * 30;
                speed         = 60 + wave * 8;
                contactDamage = 20 + wave * 4;
            }
        }
        this.hp = maxHp;
        this.x  = -size - 10;
        this.y  = startY;
    }

    public void update(float dt) {
        if (dead) return;
        float spd = frozen > 0 ? speed * 0.3f : speed;
        frozen = Math.max(0, frozen - dt);
        x += spd * dt;
    }

    /** true si llegó a la torre (borde derecho del campo) */
    public boolean reachedTower(int towerX) {
        return x >= towerX - 30;
    }

    public void draw(Graphics2D g2) {
        if (dead) return;

        float bob = (float)(Math.sin(System.currentTimeMillis() / 350.0 + floatOffset) * 3);
        int cx = (int) x;
        int cy = (int)(y + bob);

        // Color según tipo
        Color body;
        if (frozen > 0)        body = new Color(180, 210, 255);
        else if (type == Type.BOSS)   body = new Color(50, 50, 50);
        else if (type == Type.FAST)   body = new Color(200, 200, 200);
        else                          body = new Color(240, 240, 240);

        // Sombra
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillOval(cx - size + 2, cy + size, size * 2 - 4, 6);

        // Cuerpo — semicírculo superior
        g2.setColor(body);
        g2.fillArc(cx - size, cy - size, size * 2, size * 2, 0, 180);

        // Falda ondulada
        int[] px = { cx - size, cx - size/2, cx, cx + size/2, cx + size };
        int[] py = { cy, cy + size - 4, cy + size, cy + size - 4, cy };
        g2.fillPolygon(px, py, 5);

        // Borde
        Color border = (type == Type.BOSS) ? new Color(20,20,20) : new Color(160,160,160);
        g2.setColor(frozen > 0 ? new Color(100,160,230) : border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(cx - size, cy - size, size * 2, size * 2, 0, 180);
        g2.drawPolyline(px, py, 5);

        // Brillo
        g2.setColor(new Color(255, 255, 255, 130));
        g2.fillOval(cx - size/2 - 1, cy - size + 3, 6, 5);

        // Cara
        if (frozen > 0) {
            g2.setColor(new Color(80, 120, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, size < 14 ? 8 : 10));
            g2.drawString("x", cx - size/2 - 1, cy - 1);
            g2.drawString("x", cx + 1,           cy - 1);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(cx - 4, cy + 4, cx + 4, cy + 4);
        } else {
            Color eyeColor = (type == Type.BOSS) ? new Color(255, 50, 50) : Color.BLACK;
            int es = Math.max(3, size / 4);
            g2.setColor(eyeColor);
            g2.fillOval(cx - es*2, cy - es - 1, es + 2, es + 2);
            g2.fillOval(cx + es/2, cy - es - 1, es + 2, es + 2);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - es*2 + 1, cy - es, 2, 2);
            g2.fillOval(cx + es/2 + 1, cy - es, 2, 2);
            g2.setColor(type == Type.BOSS ? new Color(200,0,0) : Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            if (type == Type.BOSS) {
                // Boca malvada
                g2.drawArc(cx - 6, cy + 2, 12, 7, 0, 180);
                g2.fillRect(cx - 4, cy + 6, 2, 3);
                g2.fillRect(cx + 2, cy + 6, 2, 3);
            } else {
                g2.drawArc(cx - 4, cy + 1, 8, 5, 0, -180);
                g2.fillRect(cx - 2, cy + 4, 2, 2);
                g2.fillRect(cx + 1, cy + 4, 2, 2);
            }
        }

        // Corona en el jefe
        if (type == Type.BOSS && frozen == 0) {
            g2.setColor(new Color(255, 200, 0));
            int[] kx = {cx-8, cx-5, cx, cx+5, cx+8, cx+6, cx+3, cx, cx-3, cx-6};
            int[] ky = {cy-size-2, cy-size+4, cy-size-2, cy-size+4, cy-size-2,
                        cy-size+8, cy-size+8, cy-size+8, cy-size+8, cy-size+8};
            g2.fillPolygon(kx, ky, 10);
        }

        // Anillo de hielo
        if (frozen > 0) {
            g2.setColor(new Color(150, 200, 255, 90));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - size - 4, cy - size - 4, (size + 4) * 2, (size + 4) * 2);
        }

        // Barra de vida
        int bw = size * 2 + 8;
        int bx = cx - bw / 2;
        int by = (int)y - size - 12;
        g2.setColor(new Color(200, 200, 200));
        g2.fillRoundRect(bx, by, bw, 5, 3, 3);
        float pct = (float) hp / maxHp;
        g2.setColor(pct > 0.5f ? new Color(150,230,180) : pct > 0.25f ? new Color(255,220,100) : new Color(255,130,150));
        g2.fillRoundRect(bx, by, (int)(bw * pct), 5, 3, 3);
    }
}
