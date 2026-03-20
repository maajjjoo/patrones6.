package com.example.towerdefense.model;

import java.util.List;

/**
 * Interface base para todas las torres.
 * Define el contrato que deben cumplir la torre base y todos los decorators.
 */
public interface Tower {
    int getDamage();
    double getAttackSpeed();
    List<String> getEffects();
    String getDescription();
}
