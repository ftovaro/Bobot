/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import javax.imageio.ImageIO;
import sise.network.UDPStream;

/**
 *
 * @author molayab
 */
public class VideoStream extends UDPStream {
    private ByteArrayOutputStream stream;
    private Frame lastFrame;
    
    public VideoStream(int port, int mtu) throws SocketException {
        super(port, mtu);
    }
    
    public VideoStream(int port) throws SocketException {
        super(port);
    }

    @Override
    public void perform() {
        synchronized(this) {
            try {
                byte[] buffer = new byte[this.mtu];
            
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
                socket.receive(packet);
                
                if (packet.getLength() > 0) {
                    byte[] data = packet.getData();
                    
                    int sync = data[0] << 8 | (data[1] & 0xFF);
                    int sequence = data[2] << 8 | (data[3] & 0xFF);
                    
                    if (sync > 0 && lastFrame != null) {
                        lastFrame.setSequence(0);
                    }
                    
                    sequence &= 0xFFFF;
                    
                    int timestamp = data[4] << 24 | 
                            (data[5] & 0xFF) << 16 | 
                            (data[6] & 0xFF) << 8 | 
                            (data[7] & 0xFF);

                    int lastchunk = 0;
                    
                    for (int i = 8; i < data.length; ++i) {
                        int chunk = (data[i] & 0xFF);
                        
                        if (lastchunk == 0xFF && chunk == 0xD8) {
                            // Inicio del frame;
                            stream = new ByteArrayOutputStream();
                            stream.write(lastchunk);
                        }
                        
                        if (stream != null) {
                            stream.write(data[i]);

                            if (lastchunk == 0xFF && chunk == 0xD9) {
                                // Fin del frame
                                
                                if (lastFrame != null && 
                                        (timestamp < lastFrame.getTimestamp() ||
                                        sequence < lastFrame.getSequence())) {
                                    
                                    // Se rechaza el frame, por estar desactualizado.
                                    stream.reset();
                                    stream.close();
                                
                                    stream = null;
                                    break;
                                }
                                
                                BufferedImage context
                                        = ImageIO.read(new ByteArrayInputStream(stream.toByteArray()));

                                Frame frame = new Frame(context);
                                frame.setTimestamp(timestamp);
                                frame.setSequence(sequence);
                                frame.setSync(sync > 0);

                                if (delegate != null) {
                                    delegate.didReceiveData(this, frame);
                                }
                                
                                lastFrame = frame;

                                stream.reset();
                                stream.close();
                                
                                stream = null;
                                
                                break;
                            }
                        }
                        
                        lastchunk = chunk;
                    }
                }
            } catch (IOException e) {
                
            } catch (Exception e) {
                
            }          
        }
    }
}
