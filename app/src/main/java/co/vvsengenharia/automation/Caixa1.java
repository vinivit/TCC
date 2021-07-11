package co.vvsengenharia.automation;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
public class Caixa1 extends AppCompatActivity {
    MqttHelper mqttHelper;
    String msgLigaValvula = "L1",msgDesligaValvula = "D1", solicitarStatus = "STATUS";
    String StringStatusTopicoAgua, StringStatus,stringSeek;
    int converteStringInt;

    TextView txtStatusAgua, txtStatusFluxo;
    private TextView miTexto;
    ProgressBar progress_bar;
    ImageView sinalizacao1;
    private SeekBar miSeek;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caixa1);
        // comunica com a tela
        txtStatusFluxo = findViewById(R.id.statusFluxo);
        txtStatusAgua = findViewById(R.id.statusAgua);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        sinalizacao1 = (ImageView) findViewById(R.id.sinalizacao1);
        miTexto = (TextView)findViewById(R.id.nivelSeekBar);
        miSeek = (SeekBar) findViewById(R.id.seekBar);
        conectaMqtt();
        miTexto.setText("definir nível: "+ miSeek.getProgress() + " %" );
        miSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                miTexto.setText("definir nível: "+ progress + " %");



                stringSeek = String.valueOf(progress);

                mqttHelper.publishToTopic(mqttHelper.enviaNivel, stringSeek.getBytes());
                //client.publish(topicoNivel, stringSeek.getBytes(), 0, false);


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
    }
    public void ligar(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaValvula, msgLigaValvula.getBytes());
    }
    public void desligar(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaValvula, msgDesligaValvula.getBytes());
    }
    public void solicitarStatusCx1(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaStatus,solicitarStatus.getBytes());
    }
    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(this, TelaInicial.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
        //finishAffinity();

    }
    private void conectaMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());

        mqttHelper.setCallback(new MqttCallbackExtended() {


            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(Caixa1.this,"conectado",Toast.LENGTH_LONG).show();

            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(Caixa1.this,"não conectado",Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if (topic.equals(mqttHelper.recebeStatusNivel)) {

                    txtStatusAgua.setText("nível: " + (new String(message.getPayload()) + " % "));
                    StringStatusTopicoAgua = new String(message.getPayload());
                    if (StringStatusTopicoAgua.matches(".*\\d.*")) {
                        converteStringInt = Integer.parseInt(StringStatusTopicoAgua);

                        progress_bar.setProgress(converteStringInt);

                    }
                }


                if (topic.equals(mqttHelper.recebeStatusValvula)) {
                    StringStatus = new String(message.getPayload());

                    if (StringStatus.matches("L1")) {
                        sinalizacao1.setImageResource(R.drawable.circulored);
                    } else if (StringStatus.matches("D1")) {
                        sinalizacao1.setImageResource(R.drawable.circuloverde);
                    }

                }


                if (topic.equals(mqttHelper.recebeStatusFluxo)) {
                    txtStatusFluxo.setText(new String(message.getPayload()) +" L/s");
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

    }
}


