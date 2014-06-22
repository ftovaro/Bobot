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
import javax.imageio.ImageIO;

/**
 *
 * @author molayab
 */
public class ImageStream extends Stream {
    private ByteArrayOutputStream buffer;

    public ImageStream(int port) throws IOException {
        super(port);

        buffer = new ByteArrayOutputStream();
    }

    @Override
    protected void received() {
        synchronized (kernel) {
            try {
                int chunk;
                int lastChunk = 0;
                
                while ((chunk = input.read()) >= 0) {
                    buffer.write(chunk);
                    
                    if (lastChunk == 0xFF && chunk == 0xD9) break;
                    
                    lastChunk = chunk;
                }

                if (delegate != null) {
                    BufferedImage image = 
                            ImageIO.read(new ByteArrayInputStream(buffer.toByteArray()));
                    
                    ImageFrame frame = new ImageFrame(image);
                    frame.setCreated(System.currentTimeMillis());
                    frame.setSize(buffer.size());
                    
                    delegate.didReceiveDataFrom(this, frame);
                }
                
                buffer.reset();
            } catch (IOException ex) {

            }
        }
    }
}
