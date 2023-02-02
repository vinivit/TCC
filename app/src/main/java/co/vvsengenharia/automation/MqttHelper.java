package co.vvsengenharia.automation;
import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://soldier.cloudmqtt.com:00000";
    final String clientId = "ExampleAndroidClient";
    final String username = "aaaaaaa";
    final String password = "aaaaaaa";

    // Tópicos MQTT
    public final String recebeStatusNivel = "CAIXA/STATUS/NIVEL", recebeStatusFluxo = "CAIXA/STATUS/FLUXO";
    public final String recebeStatusValvula = "CAIXA/STATUS/VALVULA", recebeNotificacaoFluxo = "CAIXA/NOTIFICACAO/FLUXO";
    public final String recebeStatusNivel2 = "CAIXA2/STATUS/NIVEL", recebeStatusFluxo2 = "CAIXA2/STATUS/FLUXO";
    public final String recebeStatusValvula2 = "CAIXA2/STATUS/VALVULA", recebeNotificacaoFluxo2 = "CAIXA2/NOTIFICACAO/FLUXO";
    public final String enviaStatus = "CAIXA/STATUS",enviaValvula = "CAIXA/VALVULA",enviaNivel = "CAIXA/NIVEL";
    public final String enviaConfiguracaoTempoNivel = "CAIXA/CONFIGURAR/TEMPO/NIVEL";
    public final String enviaConfiguracaoTempoFluxo = "CAIXA/CONFIGURAR/TEMPO/FLUXO";
    public final String enviaConfiguracaoNotificacaoFluxo = "CAIXA/CONFIGURAR/NOTIFICACAO/FLUXO";
    public final String enviaStatus2 = "CAIXA2/STATUS",enviaValvula2 = "CAIXA2/VALVULA",enviaNivel2 = "CAIXA2/NIVEL";
    public final String enviaConfiguracaoTempoNivel2 = "CAIXA2/CONFIGURAR/TEMPO/NIVEL";
    public final String enviaConfiguracaoTempoFluxo2 = "CAIXA2/CONFIGURAR/TEMPO/FLUXO";
    public final String enviaConfiguracaoNotificacaoFluxo2 = "CAIXA2/CONFIGURAR/NOTIFICACAO/FLUXO";
    final String publishTopic = "TESTE";

    public MqttHelper(Context context){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
                publishToTopic(enviaStatus,publishTopic.getBytes());
                publishToTopic(enviaStatus2,publishTopic.getBytes());
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }
    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }
    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());



        try {

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
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }
    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(recebeStatusNivel, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeStatusValvula, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeStatusFluxo, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeNotificacaoFluxo, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeStatusNivel2, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeStatusValvula2, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeStatusFluxo2, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

            mqttAndroidClient.subscribe(recebeNotificacaoFluxo2, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });


        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
    public void publishToTopic(String publishTopic, byte[] msg){
        try {
            mqttAndroidClient.publish(publishTopic, msg,0,false    );
        } catch (MqttException e) {
            e.printStackTrace();
            Log.w("Mqtt", "Subscribed fail!");
        }
    }
}
