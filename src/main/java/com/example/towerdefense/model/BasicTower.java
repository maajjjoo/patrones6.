package com.example.towerdefense.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Torre base sin mejoras.
 * Dispara proyectiles simples con stats iniciales.
 */
public class BasicTower implements Tower {

    private static final int    BASE_DAMAGE       = 8;   // menos daño base
    private static final double BASE_ATTACK_SPEED = 0.8; // más lenta sin mejoras

    @Override
    public int getDamage() {
        return BASE_DAMAGE;
    }

    @Override
    public double getAttackSpeed() {
        return BASE_ATTACK_SPEED;
    }

    @Override
    public List<String> getEffects() {
        // La torre base no tiene efectos especiales
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Basic Tower";
    }
}
