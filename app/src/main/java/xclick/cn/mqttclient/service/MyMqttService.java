package xclick.cn.mqttclient.service;

import android.content.Context;

import com.orhanobut.logger.Logger;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

import xclick.cn.mqttclient.model.CartInfo;

/**
 * Created by Huwei on 2017/12/23.
 */

public class MyMqttService {
    private Context context ;
    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://192.168.0.36:61613";
    final String userName = "admin";
    final String password = "password" ;

    String clientId = "MqttClient";

    private OnMessageArrived onMessageArrived ;

    public MyMqttService(Context context, final OnMessageArrived onMessageArrived){
        this.context = context ;
        this.onMessageArrived = onMessageArrived ;

        this.clientId += System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(this.context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Logger.d("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    Logger.d("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.d("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Logger.d("Incoming message: topic=" + topic + ", message payload=" + new String(message.getPayload()));
                if(onMessageArrived!=null) onMessageArrived.messageArrived(topic,new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(this.userName);
        mqttConnectOptions.setPassword(this.password.toCharArray());

        try {
            Logger.d("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Logger.d("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe("factory/cart/#", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Logger.d("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Logger.d("Failed to subscribe");
                }
            });



        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public boolean sendTurnOnRequest(CartInfo cartInfo){
        try{
            MqttMessage message = new MqttMessage();

            message.setPayload(cartInfo.getId().getBytes());
            mqttAndroidClient.publish("factory/cart/turnonlight/request", message);
            Logger.d("Message Published");
            if(!mqttAndroidClient.isConnected()){
                Logger.d(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
            return true ;
        }catch (MqttException e) {
            Logger.e("Error send cart turnon light request:" + e.getMessage());
            e.printStackTrace();
        }
        return false ;
    }

    public boolean sendCartInfoRequest(){
        try{
            MqttMessage message = new MqttMessage();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            String szTimeStamp = sdf.format(now);
            message.setPayload(szTimeStamp.getBytes());
            mqttAndroidClient.publish("factory/cart/info/request", message);
            Logger.d("Message Published");
            if(!mqttAndroidClient.isConnected()){
                Logger.d(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
            return true ;
        }catch (MqttException e) {
            Logger.e("Error send cartinfo request:" + e.getMessage());
            e.printStackTrace();
        }
        return false ;
    }

    public interface OnMessageArrived{
        void messageArrived(String topic, String messagePayload);
    }
}
