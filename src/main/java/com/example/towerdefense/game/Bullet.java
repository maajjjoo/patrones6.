package com.example.towerdefense.game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Cohete disparado por click del jugador hacia un objetivo.
 */
public class Bullet {

    float x, y;
    final Enemy        target;
    final int          damage;
    final List<String> effects;
    final Color        color;
    final float        speed = 320;
    boolean dead  = false;
    int     kills = 0;
    double  angle = 0;

    private static final Color COL_DEFAULT = new Color(255, 120, 160);
    private static final Color COL_FREEZE  = new Color(150, 200, 255);
    private static final Color COL_GOLD    = new Color(255, 210, 80);
    private static final Color COL_SHIELD  = new Color(200, 150, 255);
    private static final Color COL_SPEED   = new Color(120, 200, 255);

    public Bullet(float x, float y, Enemy target, int damage, List<String> effects) {
        this.x       = x;
        this.y       = y;
        this.target  = target;
        this.damage  = damage;
        this.effects = effects;

        if      (effects.contains("Freeze"))         color = COL_FREEZE;
        else if (effects.contains("Gold Generator")) color = COL_GOLD;
        else if (effects.contains("Shield"))         color = COL_SHIELD;
        else if (effects.contains("Fast Attack"))    color = COL_SPEED;
        else                                         color = COL_DEFAULT;

        this.angle = Math.atan2(target.y - y, target.x - x);
    }

    public void update(float dt, List<Particle> particles, List<FloatText> floatTexts, int currentKills) {
        this.kills = currentKills;
        if (dead || target == null || target.dead) { dead = true; return; }

        float dx   = target.x - x;
        float dy   = target.y - y;
        float dist = (float) Math.hypot(dx, dy);
        angle = Math.atan2(dy, dx);

        if (dist < 12) {
            target.hp -= damage;
            if (effects.contains("Freeze")) target.frozen = 2.5f;

            for (int i = 0; i < 8; i++) {
                double a = Math.random() * Math.PI * 2;
                float spd = 50 + (float)(Math.random() * 90);
                particles.add(new Particle(target.x, target.y,
                    i % 2 == 0 ? color : new Color(255, 220, 240),
                    (float)Math.cos(a)*spd, (float)Math.sin(a)*spd, 0.5f, 4));
            }

            if (target.hp <= 0) {
                target.dead = true;
                kills++;
                Color[] cols = {new Color(255,120,160), new Color(255,180,210),
                                new Color(255,210,80),  new Color(200,150,255)};
                for (int i = 0; i < 14; i++) {
                    double a = Math.random() * Math.PI * 2;
                    float spd = 70 + (float)(Math.random() * 110);
                    particles.add(new Particle(target.x, target.y, cols[i%4],
                        (float)Math.cos(a)*spd, (float)Math.sin(a)*spd - 50, 0.9f, 6));
                }
                floatTexts.add(new FloatText("+" + damage + " ✨", target.x, target.y - 20,
                    new Color(255, 100, 150)));
            }
            dead = true;
            return;
        }

        x += (dx / dist) * speed * dt;
        y += (dy / dist) * speed * dt;
    }

    public void draw(Graphics2D g2) {
        if (dead) return;
        AffineTransform old = g2.getTransform();
        g2.translate((int)x, (int)y);
        g2.rotate(angle - Math.PI / 2);

        // Estela
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 55));
        g2.fillOval(-5, 4, 10, 14);

        // Cuerpo
        g2.setColor(color);
        g2.fillRoundRect(-4, -10, 8, 14, 4, 4);

        // Punta
        g2.setColor(color.darker());
        g2.fillPolygon(new int[]{-4,4,0}, new int[]{-10,-10,-18}, 3);

        // Aletas
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2.fillPolygon(new int[]{-4,-8,-4}, new int[]{2,-3,4}, 3);
        g2.fillPolygon(new int[]{ 4, 8, 4}, new int[]{2,-3,4}, 3);

        // Ventanita
        g2.setColor(new Color(200, 230, 255, 200));
        g2.fillOval(-2, -8, 4, 4);

        // Llama
        long t = System.currentTimeMillis();
        float flicker = (float)(0.7 + 0.3 * Math.sin(t / 60.0));
        g2.setColor(new Color(255, 200, 80, (int)(180 * flicker)));
        g2.fillOval(-3, 4, 6, (int)(8 * flicker));
        g2.setColor(new Color(255, 120, 40, (int)(120 * flicker)));
        g2.fillOval(-2, 5, 4, (int)(5 * flicker));

        g2.setTransform(old);
    }
}
