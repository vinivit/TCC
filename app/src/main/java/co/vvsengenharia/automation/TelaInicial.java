package co.vvsengenharia.automation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class TelaInicial extends AppCompatActivity {

    static String MQTTHOST = "tcp://soldier.cloudmqtt.com:10659";
    static String USERNAME = "ymnrzrfj";
    static String PASSWORD = "FUlEq42bto_-";
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client;
    String StatusTopicoAgua = "CAIXA/STATUS/NIVEL";
    TextView txtStatusAgua;

    private NotificationManagerCompat notificationManagerCompat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);

        txtStatusAgua = findViewById(R.id.statusNivelCx1);

        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());







        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    setSubscrition();
                    Toast.makeText(TelaInicial.this,"conectado",Toast.LENGTH_LONG).show();
                    // cria: solicitar pro microcontrolador os status
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(TelaInicial.this,"não conectado",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if (topic.equals(StatusTopicoAgua)) {

                    String StringStatusTopicoAgua = new String(message.getPayload());
                    txtStatusAgua.setText(new String(message.getPayload()) + " % ");

                    if (StringStatusTopicoAgua.matches(".*\\d.*")) {
                        int converteStringInt = Integer.parseInt(StringStatusTopicoAgua);

                        if(converteStringInt < 20){
                            sendOnChannel1("Notificação", StringStatusTopicoAgua);

                        }


                }

                }


            }



            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        this.notificationManagerCompat = NotificationManagerCompat.from(this);


    }

    private void setSubscrition() {
        try {
            client.subscribe(StatusTopicoAgua, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void sobre(View v) {
        Intent intent = new Intent(getApplicationContext(), Sobre.class);
        startActivity(intent);
    }

    public void entrarCaixa1(View v) {
        Intent intent = new Intent(getApplicationContext(), Caixa1.class);
        startActivity(intent);
    }

    public void sair (View v) {
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onBackPressed(){ //Botão BACK padrão do android

        finishAffinity();
        System.exit(0);

    }


    private void sendOnChannel1(String title, String message)  {

        Intent resultIntent = new Intent(this, Caixa1.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, NotificationApp.CHANNEL_1_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build();

        int notificationId = 1;
        this.notificationManagerCompat.notify(notificationId, notification);


    }


    }