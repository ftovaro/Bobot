/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

import java.awt.image.BufferedImage;

/**
 *
 * @author molayab
 */
public class ImageFrame {
    private BufferedImage context;
    private long size;
    private long created;
    private long remoteTimestamp;

    public ImageFrame(BufferedImage context) {
        this.context = context;
        
        this.remoteTimestamp = 0;
    }
    
    public BufferedImage getContext() {
        return context;
    }

    public void setRemoteTimestamp(long timestamp) {
        remoteTimestamp = timestamp;
    }
    
    public long getRemoteTimestamp() {
        return remoteTimestamp;
    }
    
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
    
    
}
