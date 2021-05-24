package co.vvsengenharia.automation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class Caixa1 extends AppCompatActivity {
    static String MQTTHOST = "tcp://soldier.cloudmqtt.com:10659";
    static String USERNAME = "ymnrzrfj";
    static String PASSWORD = "FUlEq42bto_-";
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client;

    String topicoValvula = "CAIXA/VALVULA";
    String topicoNivel = "CAIXA/NIVEL";
    String StatusTopicoAgua = "CAIXA/STATUS/NIVEL";
    String StatusTopicoFluxo = "CAIXA/STATUS/FLUXO";
    String StatusTopicoValvula = "CAIXA/STATUS/VALVULA";


    String StringStatusTopicoAgua, StringStatus;
    int converteStringInt;
    TextView txtStatusAgua, txtStatusFluxo;
    ProgressBar progress_bar;
    ImageView sinalizacao1, sinalizacao2;
    private TextView miTexto;
    private SeekBar miSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());


        // comunica com a tela

        txtStatusFluxo = findViewById(R.id.statusFluxo);
        txtStatusAgua = findViewById(R.id.statusAgua);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        sinalizacao1 = (ImageView) findViewById(R.id.sinalizacao1);
        sinalizacao2 = (ImageView) findViewById(R.id.sinalizacao2);
        miTexto = (TextView)findViewById(R.id.nivelSeekBar);
        miSeek = (SeekBar) findViewById(R.id.seekBar);




        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Caixa1.this, "conectado", Toast.LENGTH_LONG).show();
                    setSubscrition();
                    // cria: solicitar pro microcontrolador os status
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Caixa1.this, "não conectado", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }



        miTexto.setText("definir nível: "+ miSeek.getProgress() + " %" );

        miSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                miTexto.setText("definir nível: "+ progress + " %"); // miTexto.setText("definir nível: "+ progress + "/"+ miSeek.getMax());


                String stringSeek = String.valueOf(progress);

                try {
                    client.publish(topicoNivel, stringSeek.getBytes(), 0, false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                Toast.makeText(getApplicationContext(), " começou arrastar", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Toast.makeText(getApplicationContext(), " parou de arrastar", Toast.LENGTH_LONG).show();

            }
        });



        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if (topic.equals(StatusTopicoAgua)) {

                    txtStatusAgua.setText("nível: " + (new String(message.getPayload()) + " % "));
                    StringStatusTopicoAgua = new String(message.getPayload());
                    if (StringStatusTopicoAgua.matches(".*\\d.*")) {
                        converteStringInt = Integer.parseInt(StringStatusTopicoAgua);

                        progress_bar.setProgress(converteStringInt);

                    }
                }


                if (topic.toString().equals(StatusTopicoValvula)) {
                    StringStatus = new String(message.getPayload());

                    if (StringStatus.matches("L1")) {
                        sinalizacao1.setImageResource(R.drawable.circulored);
                    } else if (StringStatus.matches("D1")) {
                        sinalizacao1.setImageResource(R.drawable.circuloverde);
                    }

                    if (StringStatus.matches("L2")) {
                        sinalizacao2.setImageResource(R.drawable.circulored);
                    } else if (StringStatus.matches("D2")) {
                        sinalizacao2.setImageResource(R.drawable.circuloverde);
                    }
                }


               if (topic.toString().equals(StatusTopicoFluxo)) {
                    txtStatusFluxo.setText(new String(message.getPayload()));
                }
            }

            

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void setSubscrition() {
        try {
            client.subscribe(StatusTopicoAgua, 0);
            client.subscribe(StatusTopicoFluxo, 0);
            client.subscribe(StatusTopicoValvula, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void ligar(View v) {
        String topic = topicoValvula;
        String message = "L1";
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void desligar(View v) {
        String topic = topicoValvula;
        String message = "D1";
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void ligar2(View v) {
        String topic = topicoValvula;
        String message = "L2";
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void desligar2(View v) {
        String topic = topicoValvula;
        String message = "D2";
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void configuracaoCaixa1(View v) {
        Intent intent = new Intent(Caixa1.this, ConfiguracaoCaixa1.class);
        startActivity(intent);
    }

    public void voltar(View v) {
        Intent intent = new Intent(Caixa1.this, TelaInicial.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(this, TelaInicial.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
        finishAffinity(); //Método para matar a activity e não deixa-lá indexada na pilhagem
        return;
    }








}


