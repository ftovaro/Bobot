
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import net.ImageFrame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author molayab
 */
public class UIImagePreview extends JPanel {
    private ImageFrame image;
    private Font font = new Font("Monospaced", Font.PLAIN, 18); 
    private InetAddress remoteHost = null;
    private String frameRate = "0 fps";
    private boolean isDebug = false;
    private boolean isTCP = true;
    private long frameCount;
    
    public UIImagePreview() {
        super(null);
        
        frameCount = 0;
        
        setMinimumSize(new Dimension(320, 240));
        setMaximumSize(new Dimension(1920, 1080));
        
        Timer task = new Timer();
        task.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                frameRate = (frameCount) + " fps";
                
                frameCount = 0;
                repaint();
            }
        }, 0, 1000);
        
        
    }
    
    public void addFrame(ImageFrame image) {
       this.image = image;
       
       frameCount++;
       
       repaint();
    }
    
    public void setRemoteHost(InetAddress addr) {
        remoteHost = addr;
        
        repaint();
    }
    
    public void setDebug(boolean debug) {
        this.isDebug = debug;
    }
    
    public void isTCP(boolean tcp) {
        isTCP = tcp;
    }
    
    public void close() {
        image = null;
        
        setBackground(Color.DARK_GRAY);
    }
    
    @Override
    public void setFont(Font font) {
        this.font = font;
    }
    
    @Override
    protected void paintComponent(Graphics g) {    
        super.paintComponent(g);
 
        g.setColor(Color.WHITE);
        g.setFont(font);
                 
        if (image != null) {
            g.drawImage(image.getContext(), 
                    (getWidth() / 2) - (image.getContext().getWidth() / 2), 
                    (getHeight() / 2) - (image.getContext().getHeight() / 2), 
                    null); 
        }   
        
        if (remoteHost != null && !remoteHost.equals("")) {
            String header = (isTCP) ? "Conectado: " : "UDP Datagrama; De: ";
            
            g.drawString((header + remoteHost.getHostAddress()), 10, 20);
        } else {
            g.drawString("Sin conexión", 10, 20);
        }
        
        try {
           g.drawString(("Frame: " + frameRate), 10, 40); 
           
           if (isDebug) {
                g.drawString(" > @size: " + image.getSize() + " bytes", 10, 60);
           
                long currentTime = System.currentTimeMillis();
                
                g.drawString(" > @delay_local: " 
                   + (currentTime - image.getCreated())
                   + "ms", 10, 80);
                
                if (image.getRemoteTimestamp() > 0) {
                    long delay = ((currentTime / 1000) - image.getRemoteTimestamp());
                    
                    if (delay < 10000) {
                        g.drawString(" > @delay_remote: " 
                            + delay
                            + " sec(s)", 10, 100);
                    } else {
                        g.drawString(" > @delay_remote: fuera de rango" 
                            , 10, 100);
                    }
                    
                } else {
                    g.drawString(" > @delay_remote: no disponible"
                        , 10, 100);
                }
                
           }
        } catch (Exception e) { }
    }
}
