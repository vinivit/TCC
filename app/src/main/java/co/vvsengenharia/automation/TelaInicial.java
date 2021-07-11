package co.vvsengenharia.automation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TelaInicial<getApplicationContext> extends AppCompatActivity {
    MqttHelper mqttHelper;
    String NotificacaoFluxo, NotificacaoFluxo2;
    String solicitarStatus = "N";
    TextView txtStatusAgua, txtStatusAgua2;

    private NotificationManagerCompat notificationManagerCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);
        txtStatusAgua = findViewById(R.id.statusNivelCx1);
        txtStatusAgua2 = findViewById(R.id.statusNivelCx2);
        conectaMqtt();
        this.notificationManagerCompat = NotificationManagerCompat.from(this);
    }
    /*  @Override // add
    protected void onStop () {
        super .onStop() ;
        startService( new Intent( this, NotificationApp. class )) ;
    } */
    public void solicitarStatus(View v) {
        status();
    }
    public void sobre(View v) {
        Intent intent = new Intent(getApplicationContext(), Sobre.class);
        startActivity(intent);
    }
    public void entrarCaixa1(View v) {
        Intent intent = new Intent(getApplicationContext(), Caixa1.class);
        startActivity(intent);
    }
    public void entrarCaixa2(View v) {
        Intent intent = new Intent(getApplicationContext(), Caixa2.class);
        startActivity(intent);
    }
    public void configuracao (View v) {
        Intent intent = new Intent(getApplicationContext(), ConfiguracaoCaixa.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        finishAffinity();
        System.exit(0);
    }
    private void status(){
        mqttHelper.publishToTopic(mqttHelper.enviaStatus,solicitarStatus.getBytes());
        mqttHelper.publishToTopic(mqttHelper.enviaStatus2,solicitarStatus.getBytes());
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
    /* private void sendOnChannel2(String title, String message)  {

        Intent resultIntent = new Intent(this, Caixa2.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, NotificationApp.CHANNEL_2_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build();

        int notificationId = 2;
        this.notificationManagerCompat.notify(notificationId, notification);
	} */
    private void conectaMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(TelaInicial.this,"conectado",Toast.LENGTH_LONG).show();
                status();
            }
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(TelaInicial.this,"não conectado",Toast.LENGTH_LONG).show();
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(mqttHelper.recebeStatusNivel)) {
                    txtStatusAgua.setText(new String(message.getPayload()) + " % ");
                }
                if (topic.equals(mqttHelper.recebeNotificacaoFluxo)) {
                    NotificacaoFluxo = new String(message.getPayload());
                    if(NotificacaoFluxo.matches("BAIXO")){
                        sendOnChannel1("Caixa-d'agua Inteligente - ATENÇÃO!!!", " CAIXA 1 - VAZÃO ABAIXO DO NORMAL");
                    }
                }
                if (topic.equals(mqttHelper.recebeStatusNivel2)) {
                    txtStatusAgua2.setText(new String(message.getPayload()) + " % ");
                }
                if (topic.equals(mqttHelper.recebeNotificacaoFluxo2)) {
                    NotificacaoFluxo2 = new String(message.getPayload());
                    if(NotificacaoFluxo2.matches("BAIXO")){
                        sendOnChannel1("Caixa-d'agua Inteligente - ATENÇÃO!!!", " CAIXA 2 - VAZÃO ABAIXO DO NORMAL");
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }
}

