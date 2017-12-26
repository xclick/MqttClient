package xclick.cn.mqttclient.service;

import org.eclipse.paho.android.service.MqttAndroidClient;

/**
 * Created by Huwei on 2017/12/23.
 */

public class MyMqttService {
    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://192.168.0.36:61613";
    String clientId = "ExampleAndroidClient";

    final String subscriptionTopic = "exampleAndroidTopic";
    final String publishTopic = "exampleAndroidPublishTopic";
    final String publishMessage = "Hello World!";

    public MyMqttService(){
        this.clientId += System.currentTimeMillis();

    }
}
