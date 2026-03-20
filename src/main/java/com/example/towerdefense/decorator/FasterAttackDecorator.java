package com.example.towerdefense.decorator;

import com.example.towerdefense.model.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator que aumenta la velocidad de ataque de la torre.
 * Multiplica la velocidad actual por 1.5x.
 */
public class FasterAttackDecorator extends TowerDecorator {

    private static final double SPEED_MULTIPLIER = 1.5;

    public FasterAttackDecorator(Tower tower) {
        super(tower);
    }

    @Override
    public double getAttackSpeed() {
        // Aumenta la velocidad de la torre envuelta
        return wrappedTower.getAttackSpeed() * SPEED_MULTIPLIER;
    }

    @Override
    public List<String> getEffects() {
        List<String> effects = new ArrayList<>(wrappedTower.getEffects());
        effects.add("Fast Attack");
        return effects;
    }

    @Override
    public String getDescription() {
        return wrappedTower.getDescription() + " + Faster Attack";
    }
}
