/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

/**
 *
 * @author molayab
 */
public class Bobot {
    protected static Bobot instance = null;
    
    private Controller controller;
    
    protected Bobot() {
    
    }
    
    public static Bobot instance() {
        if (instance == null) instance = new Bobot();
        
        return instance;
    }
    
    public void registerController(Controller controller) {
        this.controller = controller;
    }
    
    public Controller getController() {
        return controller;
    }
}
