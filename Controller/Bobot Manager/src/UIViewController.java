import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sise.controller.BobotController;
import sise.controller.Communication;
import sise.network.Network;
import sise.network.NetworkDelegate;
import sise.video.Frame;
import sise.video.VideoStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author molayab
 */
public class UIViewController extends javax.swing.JFrame {
    // private ImageStream videoStream;
    private VideoStream vid;
    private Communication communication;
    private ArrayList<Controller> controllers;
    private BobotController bobController;
    
    /**
     * Creates new form UIViewController
     */
    public UIViewController() {
        initComponents();
        
        bobController = new BobotController();
        bobController.setThrottle(0);
        bobController.setTurn(0);
        
        controllers = new ArrayList<>();
        
        for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            if (controller.getType() == Controller.Type.STICK || 
                controller.getType() == Controller.Type.GAMEPAD || 
                controller.getType() == Controller.Type.WHEEL ||
                controller.getType() == Controller.Type.FINGERSTICK )
            {
               controllers.add(controller);
            }
        }
        
        if (!controllers.isEmpty()) {
            controllerHelper();
        }
        
        try {
            //videoStream = new ImageStream(9998, Stream.UDP);
            //videoStream.setDelegate(this);
            //videoStream.setTimeout(1000);
            //videoStream.setMTU(7981);
            
            //new ImageStream(9999, Stream.TCP);
            vid = new VideoStream(5555, 7981);
            communication = new Communication(4545);
            
            communication.setDelegate(new NetworkDelegate() {

                @Override
                public void didConnectClient(Network net, InetAddress cli) {
                    previewFrame.setRemoteHost(cli);
                }

                @Override
                public void didDisconnectClient(Network net) {
                    
                }

                @Override
                public void didReceiveData(Network net, Object data) {
                    try {
                        JSONParser parser=new JSONParser();
                        
                        JSONObject object = (JSONObject)parser.parse((String)data);
                        
                        String signal = (String)object.get("signal");
                        if (signal != null) {
                            signalLabel.setText(signal);
                            
                            String[] split = signal.split("/");
                            
                            signalBar.setMinimum(0);
                            signalBar.setMaximum(Integer.parseInt(split[1]));
                            signalBar.setValue(Integer.parseInt(split[0]));
                        }
                    } catch (Exception ex) {
                        
                    }
                }
            });
            
            vid.setDelegate(new NetworkDelegate() {

                @Override
                public void didConnectClient(Network net, InetAddress cli) {
                    
                }

                @Override
                public void didDisconnectClient(Network net) {
                    
                }

                @Override
                public void didReceiveData(Network net, Object data) {
                    if (net instanceof VideoStream) {
                        if (data instanceof Frame) {
                            previewFrame.addFrame((Frame) data);
                        }
                    }
                }
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void controllerHelper() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    Controller ctr = controllers.get(0);

                    if (!ctr.poll()) {
                        break;
                    }

                    float xAxisPercentage = 0;
                    float yAxisPercentage = 0;
                    float throttle = 0;

                    for (Component component : ctr.getComponents()) {
                        Component.Identifier componentIdentifier = component.getIdentifier();
                
                        // Axes
                        if (component.isAnalog()) {
                            float axisValue = component.getPollData();
                            

                            // X axis
                            if (componentIdentifier == Component.Identifier.Axis.X) {
                                xAxisPercentage = axisValue;
                                continue; // Go to next component.
                            }
                            // Y axis
                            if (componentIdentifier == Component.Identifier.Axis.Y) {
                                yAxisPercentage = axisValue;
                                continue; // Go to next component.
                            }

                            throttle = axisValue;
                        }
                    }
                    
                    int t2 = getTurnValue(xAxisPercentage * -1.0f);
                    if (bobController.getTurn() != t2) {
                        bobController.setTurn(t2);
                        
                        try {
                            communication.send(bobController);
                        } catch (Exception ex) {
                            
                        }
                    }
                    
                    // int t = getThrotleValue(throttle * -1.0f);
                    int t = getTurnValue(yAxisPercentage * -1.0f);
                    if (bobController.getThrottle() != t) {
                        bobController.setThrottle(t);
                        
                        try {
                            communication.send(bobController);
                        } catch (Exception ex) {
                            
                        }
                    }
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        
                    }
                }
            }
        });

        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    
    /**
     * Given value of axis in percentage.
     * Percentages increases from left/top to right/bottom.
     * If idle (in center) returns 50, if joystick axis is pushed to the left/top 
     * edge returns 0 and if it's pushed to the right/bottom returns 100.
     * 
     * @param axisValue
     * @return value of axis in percentage.
     */
    public int getTurnValue(float axisValue) {
        return (int)(axisValue * 100);
    }
    
    public int getThrotleValue(float val) {
        return (int)(((2 - (1 - val)) * 100) / 2);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        signalBar = new javax.swing.JProgressBar();
        signalLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        previewFrame = new UIImagePreview();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bobot Manager");
        setMinimumSize(new java.awt.Dimension(800, 300));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 10));
        jPanel2.setPreferredSize(new java.awt.Dimension(220, 345));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Ajustes"));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "320p", "480p", "720p", "1080p" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Calidad 10% - Mas Rapido", "Calidad 20%", "Calidad 30%", "Calidad 40%", "Calidad 50%", "Calidad 60%", "Calidad 70%", "Calidad 80%", "Calidad 90%", "Calidad 100% - Mas Lento" }));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel1.setText("A mayor tamaño o calidad mayor");

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel2.setText("delay. La conexion tambien afecta.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox2, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton1.setText("Desconectar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Camara debug");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Señal: ");

        signalLabel.setText("0/100");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(signalBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton1))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(signalLabel)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(signalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(signalBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel1.setLayout(new java.awt.BorderLayout());

        previewFrame.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout previewFrameLayout = new javax.swing.GroupLayout(previewFrame);
        previewFrame.setLayout(previewFrameLayout);
        previewFrameLayout.setHorizontalGroup(
            previewFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        previewFrameLayout.setVerticalGroup(
            previewFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 325, Short.MAX_VALUE)
        );

        jPanel1.add(previewFrame, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        //videoStream.disconnect();
        previewFrame.close();
        
        BobotController ctr = new BobotController();
            ctr.setThrottle(12);
            
            System.out.println(ctr.getJSONString());
            ctr.setThrottle(100);
            
            System.out.println(ctr.getJSONString());
            
        try {
            communication.send(ctr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        previewFrame.setDebug(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UIViewController().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private UIImagePreview previewFrame;
    private javax.swing.JProgressBar signalBar;
    private javax.swing.JLabel signalLabel;
    // End of variables declaration//GEN-END:variables

}
