/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net;

import java.net.Socket;

/**
 *
 * @author molayab
 */
public interface StreamDelegate {
    public void didReceiveDataFrom(Stream stream, Object data);
    public void didConnectedClient(Stream stream, Socket client);
    public void didDisconnectedClient(Stream stream);
}
