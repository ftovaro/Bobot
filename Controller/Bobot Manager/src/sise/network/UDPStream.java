/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.network;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author molayab
 */
public abstract class UDPStream extends Network {
    protected DatagramSocket socket;
    protected int mtu;
    
    public UDPStream(int port, int mtu) throws SocketException {
        super();
        
        socket = new DatagramSocket(port);
        
        this.mtu = mtu;
    }
    
    public UDPStream(int port) throws SocketException {
        this(port, 1024);
    }
    
    public int getMTU() {
        return mtu;
    }
}
