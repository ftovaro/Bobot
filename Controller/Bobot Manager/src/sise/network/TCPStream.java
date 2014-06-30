/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author molayab
 */
public abstract class TCPStream extends Network{
    protected Socket client;
    protected DataInputStream input;
    protected DataOutputStream output;
    
    private final ServerSocket socket;
    private boolean isConnected;
    
    public TCPStream(int port) throws IOException {
        super();
        
        socket = new ServerSocket(port);
        isConnected = false;
    }
    
    @Override
    public void perform() {
        try {
            client = socket.accept();
            
            if (delegate != null) {
                delegate.didConnectClient(this, client.getInetAddress());
            }
            
            isConnected = true;
            
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
            
            while (isConnected) {
                if (input.available() > 0) {
                    didReceivedPacket();
                }
            }
        } catch (Exception ex) { }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void close() {
        try {
            isConnected = false;
            
            client.close();
        } catch (Exception ex) { }
    }
    
    public abstract void didReceivedPacket();
    public abstract void send(Object sender);
}
