/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.network;

import java.net.InetAddress;

/**
 *
 * @author molayab
 */
public interface NetworkDelegate {
    public void didConnectClient(Network net, InetAddress cli);
    public void didDisconnectClient(Network net);
    public void didReceiveData(Network net, Object data);
}
