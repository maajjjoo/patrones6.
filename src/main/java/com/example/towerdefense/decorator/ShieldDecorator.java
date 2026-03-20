package com.example.towerdefense.decorator;

import com.example.towerdefense.model.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator que agrega un escudo defensivo a la torre.
 * Aumenta el daño (torres protegidas atacan con más confianza)
 * y agrega el efecto visual de escudo.
 */
public class ShieldDecorator extends TowerDecorator {

    private static final int SHIELD_DAMAGE_BONUS = 8;

    public ShieldDecorator(Tower tower) {
        super(tower);
    }

    @Override
    public int getDamage() {
        return wrappedTower.getDamage() + SHIELD_DAMAGE_BONUS;
    }

    @Override
    public List<String> getEffects() {
        List<String> effects = new ArrayList<>(wrappedTower.getEffects());
        effects.add("Shield");
        return effects;
    }

    @Override
    public String getDescription() {
        return wrappedTower.getDescription() + " + Shield";
    }
}
