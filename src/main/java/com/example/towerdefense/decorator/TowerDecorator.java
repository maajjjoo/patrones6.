package com.example.towerdefense.decorator;

import com.example.towerdefense.model.Tower;

import java.util.List;

/**
 * Clase abstracta base para todos los decorators.
 * Envuelve una torre y delega el comportamiento por defecto.
 * Cada decorator concreto sobreescribe solo lo que necesita cambiar.
 */
public abstract class TowerDecorator implements Tower {

    // La torre que estamos decorando (puede ser BasicTower u otro decorator)
    protected final Tower wrappedTower;

    public TowerDecorator(Tower tower) {
        this.wrappedTower = tower;
    }

    @Override
    public int getDamage() {
        return wrappedTower.getDamage();
    }

    @Override
    public double getAttackSpeed() {
        return wrappedTower.getAttackSpeed();
    }

    @Override
    public List<String> getEffects() {
        return wrappedTower.getEffects();
    }

    @Override
    public String getDescription() {
        return wrappedTower.getDescription();
    }
}
