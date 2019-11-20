package utils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher {

    int qos;
    String broker;
    String clientId;

    public MqttPublisher(String mqttBrokerHost){
        this.qos = 2;
        this.broker = mqttBrokerHost;
        this.clientId = "hfp-test";
    }

    public void publish(String topic, String content) throws Exception {

        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient client = new MqttClient(this.broker, this.clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+this.broker);
            client.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(this.qos);
            client.publish(topic, message);
            System.out.println("Message published");
            client.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
            throw new Exception("Failed to publish to test mqtt");
        }
    }
}
