/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import javax.imageio.ImageIO;

/**
 *
 * @author molayab
 */
public class ImageStream extends Stream {
    private ByteArrayOutputStream buffer = null;
    private ImageFrame lastFrame;
    
    public ImageStream(int port, int mode) throws Exception {
        super(port, mode);
        
        lastFrame = null;
    }
    
    public ImageStream(int port) throws Exception {
        this(port, TCP);
    }

    public ImageFrame getLastFrame() {
        return lastFrame;
    }
    
    @Override
    protected void received() {
        synchronized (kernel) {
            try {
                int chunk;
                int lastChunk = 0;
                
                Queue<Integer> timeQueue = new ArrayDeque();
                StringBuffer timestamp = new StringBuffer();
                
                while ((chunk = input.read()) >= 0) {
                    if (lastChunk == 0xFF && chunk == 0xD8) {
                        // Inico del frame
                        
                        while (!timeQueue.isEmpty()) {                            
                            int i = timeQueue.poll();

                            timestamp.append((char) i);
                        }
                       
                        
                        buffer = new ByteArrayOutputStream();
                        buffer.write(lastChunk);
                    }
                    
                    if (buffer != null) {
                        buffer.write(chunk);
                        
                        if (lastChunk == 0xFF && chunk == 0xD9) {
                            // Fin del frame
                            BufferedImage image = 
                            ImageIO.read(new ByteArrayInputStream(buffer.toByteArray()));
                    
                            lastFrame= new ImageFrame(image);
                            lastFrame.setCreated(System.currentTimeMillis());
                            lastFrame.setSize(buffer.size());
                            try {
                                lastFrame.setRemoteTimestamp(Long.parseLong(timestamp.toString()));
                            } catch (NumberFormatException e) { }
                            
                        
                            if (delegate != null) {
                                delegate.didReceiveDataFrom(this, lastFrame);
                            }
                            
                            timestamp.delete(0, timestamp.length());
                            
                            buffer.reset();
                            buffer.close();
                            
                            buffer = null;
                            
                            break;
                        }
                    } else {
                        if (chunk >= 48 && chunk < 57) {
                            timeQueue.add(chunk); 
                        }
                    }
                    
                    lastChunk = chunk;
                }
                
            } catch (IOException ex) {

            }
        }
    }
}
