/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author molayab
 */
abstract public class Stream implements Runnable {
    public static final int UDP_MODE = 1;
    public static final int UDP = 1;
    public static final int TCP_MODE = 0;
    public static final int TCP = 0;
    
    protected final Object kernel = this;
    
    protected StreamDelegate delegate;
    protected InputStream input;
    
    private Object connection;
    private DataOutputStream output;
    private InetAddress lastConnection = null;
    private boolean isConnected;
    private boolean isNotify;
    private int timeout;
    private int mode;
    private int mtu = 1024;
    
    
    public Stream(int port, int timeout, int mode) throws Exception {
        switch(mode) {
            case UDP:
                connection = new DatagramSocket(port);
                break;
            case TCP:
                connection = new ServerSocket(port);
                break;
            default:
                throw new Exception("Unknown protocol.");
        }
        
        this.timeout = timeout;
        this.mode = mode;
        this.isNotify = false;
        
        init();
    }
    
    public Stream(int port, int mode) throws Exception {
        this(port, 0, mode);
    }
    
    public Stream(int port) throws Exception {
        this(port, 0, TCP);
    }
    
    public void disconnect() {
        isConnected = false;
    }
    
    private void init() {
        Thread mainLoop = new Thread(this);
        mainLoop.setPriority(Thread.MIN_PRIORITY);
        mainLoop.start();
    }
    
    public void setMTU(int mtu) {
        this.mtu = mtu;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getConnectionMode() {
        return mode;
    }
    
    public void setDelegate(StreamDelegate delegate) {
        this.delegate = delegate;
    }
    
    public void send(byte[] data) throws IOException {
        if (output != null && connection instanceof ServerSocket) {
            output.write(data);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    @Override
    public void run() {
        try {
            while (connection instanceof ServerSocket) {       
                Socket server = ((ServerSocket)connection).accept();
                server.setTcpNoDelay(true);
                
                if (timeout > 0) {
                    server.setSoTimeout(timeout);
                }
                
                output = new DataOutputStream(server.getOutputStream());
                input = new DataInputStream(server.getInputStream());
                
                isConnected = true;
                
                if (delegate != null) {
                    delegate.didConnectedClient(this, server.getInetAddress());
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
            
            while (connection instanceof DatagramSocket) {
                byte[] buffer = new byte[this.mtu];
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                ((DatagramSocket)connection).receive(packet);
                
                if (lastConnection == null || !isConnected) { 
                    if (packet.getAddress() != null) {
                        lastConnection = packet.getAddress();
                        
                        if (delegate != null) {
                            delegate.didConnectedClient(this, packet.getAddress());
                        }
                        
                        isNotify = false;
                        isConnected = true;
                    } 
                }
                
                if (packet.getAddress() == null) {
                    if (delegate != null && !isNotify) {
                        delegate.didDisconnectedClient(this);
                    }
                    
                    isConnected = false;
                    isNotify = true;
                }
                
                synchronized (this) {
                    if(packet.getLength() > 0 && 
                            packet.getAddress().equals(lastConnection)) {
                        input = new ByteArrayInputStream(packet.getData());
                    
                        received();
                    }
                }
                
                packet = null;
                
            }
        } catch (IOException e) {
            
        }
    }
    
    protected abstract void received();
}
