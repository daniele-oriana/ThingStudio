/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sending;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.sleep;
import java.math.BigInteger;
import java.util.Random;
import monitoring.CpuData;
import static monitoring.CpuData.getMetric;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;


public class ProvaInvio {
    
    private static Sigar sigar;
    
    public ProvaInvio(Sigar s) throws SigarException {
        sigar = s;
        //System.out.println(cpuInfo());
    }
    
    public static Double[] getMetric() throws SigarException {
        CpuPerc cpu = sigar.getCpuPerc();
        //double system = cpu.getSys();
        //double user = cpu.getUser();
        double idle = cpu.getIdle();
//      System.out.println("idle: " +CpuPerc.format(idle) +", system: "+CpuPerc.format(system)+ ", user: "+CpuPerc.format(user));
        return new Double[] {idle};
    }
    
    public static void main(String[]args) throws SigarException, InterruptedException {
        
        String topic        = "/daniele/temperature";
        String content;
        int qos             = 2;
        String broker       = "tcp://mqtt.thingstud.io:1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();
        new ProvaInvio (new Sigar());
        
        
        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("guest");
            connOpts.setPassword("guest".toCharArray());
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            
            
//-------------------------CONNESSO-----------------------------------------------------
            
            new Thread() {
                public void run() {
                    while (true) {
                        BigInteger.probablePrime(MAX_PRIORITY, new Random());
                    }
                }
            ;
            }.start();
        while (true) {
                
                for (Double d : getMetric()) {
                    //System.out.print("\t" + d);
                    //int z = d.intValue();
                    //content = String.valueOf(z);
                    content=d.toString();
                    System.out.println("Publishing message: " + content);
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(qos);
                    sampleClient.publish(topic, message);
                    System.out.println("Message published");
                    
                }
                System.out.println();
                Thread.sleep(1000);
            }
          /*  
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);*/

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
