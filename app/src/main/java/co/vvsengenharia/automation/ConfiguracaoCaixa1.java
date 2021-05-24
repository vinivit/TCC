package co.vvsengenharia.automation;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class ConfiguracaoCaixa1 extends AppCompatActivity {


    static String MQTTHOST = "tcp://soldier.cloudmqtt.com:10659";
    static String USERNAME = "ymnrzrfj";
    static String PASSWORD = "FUlEq42bto_-";
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client;
    String topicoTempoNivel = "CAIXA/TEMPO/NIVEL";
    String topicoTempoFluxo = "CAIXA/TEMPO/FLUXO";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao_caixa1);

        client = new MqttAndroidClient(this.getApplicationContext(),MQTTHOST,clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(ConfiguracaoCaixa1.this,"conectado",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(ConfiguracaoCaixa1.this,"não conectado",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
        public void enviarDadosConfCaixa1 (View view){

            EditText tempoFluxo = findViewById(R.id.tFluxo);
            EditText tempoNivel = findViewById(R.id.tNivel);
            String sTempoFluxo =  tempoFluxo.getText().toString();
            String sTempoNivel =  tempoNivel.getText().toString();



            //if (sTempoFluxo !=null) > criar algo pra não ser nulo

            try {
                client.publish(topicoTempoFluxo, sTempoFluxo.getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }

            try {
                client.publish(topicoTempoNivel, sTempoNivel.getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }

            Toast.makeText(ConfiguracaoCaixa1.this, "configuração enviada", Toast.LENGTH_LONG).show();



        }
    public void voltar(View v) {
        Intent intent = new Intent(this, Caixa1.class);
        startActivity(intent);

    }

    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(this, Caixa1.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
        finishAffinity(); //Método para matar a activity e não deixa-lá indexada na pilhagem
        return;
    }





}



