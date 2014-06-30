/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import sise.network.TCPStream;


/**
 *
 * @author molayab
 */
public class Communication extends TCPStream {

    public Communication(int port) throws IOException {
        super(port);
    }

    @Override
    public void didReceivedPacket() { 
        synchronized (this) {
            try {
                String line = input.readLine();
                
                if (delegate != null) {
                    delegate.didReceiveData(this, line);
                }
            } catch (Exception ex) {

            }
        }

    }

    @Override
    public void send(Object sender) {
        try {
            writeOutput(sender.toString());
        } catch (IOException ex) {
            // ERROR
        }
    }
    
    public void send(Module module) throws IOException {
        writeOutput(module.getJSONString() + "\r\n");
    }
    
    private void writeOutput(String obj) throws IOException {
        if (isConnected()) {
            byte[] utf8 = obj.getBytes("UTF-8");
            
            output.write(utf8);
        }
    }
}
