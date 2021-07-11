package co.vvsengenharia.automation;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static android.app.PendingIntent.getActivity;

public class ConfiguracaoCaixa extends AppCompatActivity {
    MqttHelper mqttHelper;
    EditText tempoFluxo, tempoNivel, notificacaoFluxo;
    String sTempoFluxo, sTempoNivel, sNotificacaoFluxo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao_caixa1);
        conectaMqtt();
    }
    /* @Override
    public void onStop() {
        super.onStop();
        this.finish();
    } */
   public void enviarDadosConfCaixa1 (View view){
             tempoFluxo = findViewById(R.id.tFluxo);
             notificacaoFluxo = findViewById(R.id.nFluxo);
             tempoNivel = findViewById(R.id.tNivel);
             sTempoFluxo =  tempoFluxo.getText().toString();
             sNotificacaoFluxo = notificacaoFluxo.getText().toString();
             sTempoNivel =  tempoNivel.getText().toString();

             if (!TextUtils.isEmpty(sTempoFluxo)){
                mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoTempoFluxo,sTempoFluxo.getBytes());
                mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoTempoFluxo2,sTempoFluxo.getBytes());
             }
             if (!TextUtils.isEmpty(sTempoNivel)) {
                 mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoTempoNivel,sTempoNivel.getBytes());
                 mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoTempoNivel2,sTempoNivel.getBytes());
             }

            if (!TextUtils.isEmpty(sNotificacaoFluxo)) {
                mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoNotificacaoFluxo,sNotificacaoFluxo.getBytes());
                mqttHelper.publishToTopic(mqttHelper.enviaConfiguracaoNotificacaoFluxo2,sNotificacaoFluxo.getBytes());
            }
            Toast.makeText(ConfiguracaoCaixa.this, "configuração enviada", Toast.LENGTH_LONG).show();
   }
        /* public void voltar(View v) {
        Intent intent = new Intent(this, Caixa1.class);
        startActivity(intent);



    } */
    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(ConfiguracaoCaixa.this, TelaInicial.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
       finishAffinity(); //Método para matar a activity e não deixa-lá indexada na pilhagem

    }
    private void conectaMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());

        mqttHelper.setCallback(new MqttCallbackExtended() {


            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(ConfiguracaoCaixa.this,"conectado",Toast.LENGTH_LONG).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(ConfiguracaoCaixa.this,"não conectado",Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

    }
}



