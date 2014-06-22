/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author molayab
 */
abstract public class Stream implements Runnable {
    protected final Object kernel = this;
    
    protected StreamDelegate delegate;
    protected DataInputStream input;
    
    private ServerSocket connection;
    private DataOutputStream output;
    private boolean isConnected;
    private int timeout;
    
    
    public Stream(int port, int timeout) throws IOException {
        connection = new ServerSocket(port);
        
        this.timeout = timeout;
        
        init();
    }
    
    public Stream(int port) throws IOException {
        this(port, 0);
    }
    
    public void disconnect() {
        isConnected = false;
    }
    
    private void init() {
        Thread mainLoop = new Thread(this);
        mainLoop.setPriority(Thread.MIN_PRIORITY);
        mainLoop.start();
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setDelegate(StreamDelegate delegate) {
        this.delegate = delegate;
    }
    
    public void send(byte[] data) throws IOException {
        if (output != null) {
            output.write(data);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    @Override
    public void run() {
        try {
            for (;;) {
                Socket server = connection.accept();
                server.setTcpNoDelay(true);
                
                if (timeout > 0) {
                    server.setSoTimeout(timeout);
                }
                
                output = new DataOutputStream(server.getOutputStream());
                input = new DataInputStream(server.getInputStream());
                
                isConnected = true;
                
                if (delegate != null) {
                    delegate.didConnectedClient(this, server);
                }
                
                long startTime = System.currentTimeMillis();
                
                synchronized (this) {
                    while (server.isConnected() && isConnected) {
                        if (input.available() > 0) {
                            received();
                        }
                    }
                }
                
                server.close();
                isConnected = false;
                
                if (delegate != null) {
                    delegate.didDisconnectedClient(this);
                }
            }
        } catch (IOException e) {
            
        }
    }
    
    protected abstract void received();
}
