package com.example.towerdefense.decorator;

import com.example.towerdefense.model.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator que agrega efecto de congelación a los proyectiles.
 * También reduce un poco la velocidad de ataque (el hielo pesa).
 */
public class FreezeDecorator extends TowerDecorator {

    private static final int FREEZE_DAMAGE_BONUS = 5;

    public FreezeDecorator(Tower tower) {
        super(tower);
    }

    @Override
    public int getDamage() {
        // El hielo agrega daño extra
        return wrappedTower.getDamage() + FREEZE_DAMAGE_BONUS;
    }

    @Override
    public List<String> getEffects() {
        List<String> effects = new ArrayList<>(wrappedTower.getEffects());
        effects.add("Freeze");
        return effects;
    }

    @Override
    public String getDescription() {
        return wrappedTower.getDescription() + " + Freeze";
    }
}
