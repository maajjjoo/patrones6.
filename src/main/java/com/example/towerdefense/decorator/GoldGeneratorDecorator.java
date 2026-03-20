package com.example.towerdefense.decorator;

import com.example.towerdefense.model.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator que convierte la torre en un generador de oro.
 * Aumenta el daño (el oro financia mejores municiones)
 * y agrega el efecto de generación de recursos.
 */
public class GoldGeneratorDecorator extends TowerDecorator {

    private static final int GOLD_DAMAGE_BONUS = 3;
    private static final double GOLD_SPEED_BONUS = 0.2;

    public GoldGeneratorDecorator(Tower tower) {
        super(tower);
    }

    @Override
    public int getDamage() {
        return wrappedTower.getDamage() + GOLD_DAMAGE_BONUS;
    }

    @Override
    public double getAttackSpeed() {
        // Genera oro y ataca un poco más rápido
        return wrappedTower.getAttackSpeed() + GOLD_SPEED_BONUS;
    }

    @Override
    public List<String> getEffects() {
        List<String> effects = new ArrayList<>(wrappedTower.getEffects());
        effects.add("Gold Generator");
        return effects;
    }

    @Override
    public String getDescription() {
        return wrappedTower.getDescription() + " + Gold Generator";
    }
}
