/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sise.network;

/**
 *
 * @author molayab
 */
public abstract class Network implements Runnable {
    protected NetworkDelegate delegate;
    
    public Network() {
        loop();
    }
    
    public void setDelegate(NetworkDelegate delegate) {
        this.delegate = delegate;
    }
    
    private void loop() {
        Thread thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
 
        thread.start();
    }
    
    public abstract void perform();

    @Override
    public void run() {
        for(;;) {
            perform();
        }
    }
}
