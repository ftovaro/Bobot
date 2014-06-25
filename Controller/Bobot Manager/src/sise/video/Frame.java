/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.video;

import java.awt.image.BufferedImage;

/**
 *
 * @author molayab
 */
public class Frame {
    private final BufferedImage context;
    private int timestamp;
    private int sequence;
    private boolean sync;
    
    public Frame(BufferedImage context) {
        this.context = context;
        this.timestamp = 0;
        this.sequence = 0;
        this.sync = false;
    }
    
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    public void setSync(boolean sync) {
        this.sync = sync;
    }
    
    public int getTimestamp() {
        return timestamp;
    }
    
    public int getSequence() {
        return sequence;
    }
    
    public boolean isSync() {
        return sync;
    }
    
    public BufferedImage getContext() {
        return context;
    }
    
    public boolean isNextFrame(int sequence) {
        return this.sequence < sequence;
    }
}
