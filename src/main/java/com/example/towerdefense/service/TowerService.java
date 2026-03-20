package com.example.towerdefense.service;

import com.example.towerdefense.decorator.*;
import com.example.towerdefense.model.BasicTower;
import com.example.towerdefense.model.Tower;

/**
 * Servicio que maneja el estado de la torre actual.
 * Se encarga de crear, resetear y aplicar decorators.
 */
public class TowerService {

    // La torre actual con todos sus decorators apilados
    private Tower currentTower;

    public TowerService() {
        // Arrancamos con una torre base
        this.currentTower = new BasicTower();
    }

    /**
     * Reinicia la torre a su estado base sin mejoras.
     */
    public void resetTower() {
        this.currentTower = new BasicTower();
    }

    /**
     * Aplica un decorator según el tipo recibido.
     * El decorator envuelve la torre actual y la reemplaza.
     *
     * @param type tipo de mejora: speed, freeze, shield, gold
     * @return true si se aplicó correctamente, false si el tipo no existe
     */
    public boolean applyDecorator(String type) {
        switch (type.toLowerCase()) {
            case "speed":
                currentTower = new FasterAttackDecorator(currentTower);
                return true;
            case "freeze":
                currentTower = new FreezeDecorator(currentTower);
                return true;
            case "shield":
                currentTower = new ShieldDecorator(currentTower);
                return true;
            case "gold":
                currentTower = new GoldGeneratorDecorator(currentTower);
                return true;
            default:
                return false;
        }
    }

    public Tower getCurrentTower() {
        return currentTower;
    }
}
