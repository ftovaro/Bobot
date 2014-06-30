/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.controller;

import org.json.simple.JSONObject;

/**
 *
 * @author molayab
 */
public class BobotController extends Module {
    private final JSONObject module;
    private int throttle = 0;
    private int turn = 0;
    
    public BobotController() {
        module = new JSONObject();
        module.put("throttle", 0);
        module.put("turn", 0);
        
        registerModule("controller", module);
    }
    
    public void setThrottle(int power) {
        this.throttle = power;
        module.put("throttle", power);
    }
    
    public void setTurn(int turn) {
        this.turn = turn;
        module.put("turn", turn);
    }

    public int getThrottle() {
        return throttle;
    }

    public int getTurn() {
        return turn;
    }
}
