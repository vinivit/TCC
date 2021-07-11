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
        import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
        import org.eclipse.paho.client.mqttv3.MqttClient;
        import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;


public class Caixa2 extends AppCompatActivity {
    MqttHelper mqttHelper;

    String msgLigaValvula2 = "L1";
    String msgDesligaValvula2 = "D1";

    String stringSeek2;

    String StringStatusTopicoAgua2, StringStatus2;
    String solicitarStatus2 = "STATUS";

    int converteStringInt2;
    TextView txtStatusAgua2, txtStatusFluxo2;
    ProgressBar progress_bar2;
    ImageView sinalizacao2;
    private TextView miTexto2;
    private SeekBar miSeek2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caixa2);


        // comunica com a tela

        txtStatusFluxo2 = findViewById(R.id.statusFluxo2);
        txtStatusAgua2 = findViewById(R.id.statusAgua2);
        progress_bar2 = (ProgressBar) findViewById(R.id.progress_bar2);
        sinalizacao2 = (ImageView) findViewById(R.id.sinalizacao2);
        miTexto2 = (TextView)findViewById(R.id.nivelSeekBar2);
        miSeek2 = (SeekBar) findViewById(R.id.seekBar2);

        conectaMqtt();


        miTexto2.setText("definir nível: "+ miSeek2.getProgress() + " %" );

        miSeek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar2, int progress2, boolean fromUser2) {

                miTexto2.setText("definir nível: "+ progress2 + " %"); // miTexto.setText("definir nível: "+ progress + "/"+ miSeek.getMax());


                stringSeek2 = String.valueOf(progress2);

                mqttHelper.publishToTopic(mqttHelper.enviaNivel2, stringSeek2.getBytes());




            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar2) {

                Toast.makeText(getApplicationContext(), " começou arrastar", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar2) {

                Toast.makeText(getApplicationContext(), " parou de arrastar", Toast.LENGTH_LONG).show();

            }
        });
    }




    public void ligar2(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaValvula2, msgLigaValvula2.getBytes());
    }

    public void desligar2(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaValvula2, msgDesligaValvula2.getBytes());
    }

    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(this, TelaInicial.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
        //finishAffinity();

    }

    public void solicitarStatusCx2(View v) {

        mqttHelper.publishToTopic(mqttHelper.enviaStatus2,solicitarStatus2.getBytes());
    }



    private void conectaMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());

        mqttHelper.setCallback(new MqttCallbackExtended() {


            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(Caixa2.this,"conectado",Toast.LENGTH_LONG).show();

            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(Caixa2.this,"não conectado",Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if (topic.equals(mqttHelper.recebeStatusNivel)) {

                    txtStatusAgua2.setText("nível: " + (new String(message.getPayload()) + " % "));
                    StringStatusTopicoAgua2 = new String(message.getPayload());
                    if (StringStatusTopicoAgua2.matches(".*\\d.*")) {
                        converteStringInt2 = Integer.parseInt(StringStatusTopicoAgua2);

                        progress_bar2.setProgress(converteStringInt2);

                    }
                }


                if (topic.equals(mqttHelper.recebeStatusValvula)) {
                    StringStatus2 = new String(message.getPayload());

                    if (StringStatus2.matches("L1")) {
                        sinalizacao2.setImageResource(R.drawable.circulored);
                    } else if (StringStatus2.matches("D1")) {
                        sinalizacao2.setImageResource(R.drawable.circuloverde);
                    }

                }


                if (topic.equals(mqttHelper.recebeStatusFluxo)) {
                    txtStatusFluxo2.setText(new String(message.getPayload())+" L/s");
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

    }
}