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
public abstract class Module {
    private final JSONObject bundle;
    
    public Module() {
        bundle = new JSONObject();
    }
    
    public void registerModule(String id, JSONObject module) {
        bundle.put(id, module);
    }
    
    public String getJSONString() {
        return bundle.toJSONString();
    }
}
