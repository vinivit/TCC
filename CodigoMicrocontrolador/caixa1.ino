/* =======================================================================
   TCC - Engenharia de Computação - Univesp.
   
   Sistema Inteligente de Monitoramento de Caixas-D'água.
   
   Integrantes: 
    Ismael Barcelos Negreiros; 
    Sabrina Heloisa da Silva; 
    Vinicius Vitor da Silva.

   Orientador:
    Eliezer Miranda Coelho. 

======================================================================= */


// =======================================================================
// ---  Bibliotecas ---

#include <ESP8266WiFi.h>
#include <PubSubClient.h>


// =======================================================================
// ---  Configuração WIFI/MQTT ---

// WIFI
const char* ssidWifi = "aaaaaaaa"; 
const char* passwordWifi =  "aaaaaaaaaaa";

//MQTT
const char* mqttServer = "soldier.cloudmqtt.com"; 
const int   mqttPort = 00000; 
const char* mqttUser = "aaaaaaaa"; 
const char* mqttPassword = "aaaaaaaaaa"; 
const char* mqttId = "dispositivo1"; 

//Variáveis e objetos globais
WiFiClient espClient; // Cria o objeto espClient
PubSubClient client(espClient); // Instancia o Cliente client passando o objeto espClient


// =======================================================================
// --- Mapeamento de Hardware ---

const byte VALVULAENTRADA = D1; 
const byte VAZAO = D5;
const byte TRIGGER = D6; 
const byte ECHO = D7;  


// =======================================================================
// --- Variáveis Válvula ---

boolean valvEntrada = false;
int valvEntAtual = 0;
int valvEntAnt = 0;
unsigned long tempoValvula = 0;
int intervaloTempoValvula = 30000; // 30 segundos - (3600000 - 60 minutos)


// =======================================================================
// --- Variáveis Nível ---

float altura = 0;
int alturaPorCem = 0;
int nivelAnt = 0; // está sem, qualquer coisa colocar
int nivelMax = 14; 
int nivelMin = 34;
int appNivel = 100; // nível escolhido pelo usuário pelo aplicativo
int nivelAbrirValvEntrada = 80; // nível minimo para valvula atuar, compara com o nívelFlex // aplicativo configurações

unsigned long tempoNivel = 0;
unsigned long tempoAtualizarNivel = 0;
int appTempoNivel = 5000;
int intervaloTempoNivel = 1000;


// =======================================================================
// --- Variáveis Vazão ---

volatile unsigned long contador = 0;
unsigned long somaContador = 0;

const float CALIBRACAO = 6.5;

float fluxo = 0;
float appFluxoNotificacao = 30;
float fluxoAnt = 0; // não utilizado
float volume = 0;

boolean ativarNotificacaoFluxo = false;

unsigned long tempoFluxo = 0;
unsigned long tempoAtualizarFluxo = 0; 
unsigned long tempoNotificacaoFluxo = 0;
unsigned long tempoProximoNotificacaoFluxo = 0;
unsigned long tempoTesteVazao =0;  
unsigned long tempoProximoTesteVazao =0;

int intervaloTempoFluxo = 1000; // 1seg
int intervaloTempoNotificacaoFluxo = 2000;
int intervaloTempoTesteVazao = 4000;
int proximoTesteVazao =  60000; //1min -   (600000 - 10 min)
int proximoNotificacaoFluxo = 60000;
int appTempoFluxo = 5000; // 5seg


// =======================================================================
// ---  Tópicos MQTT ---

char* enviaStatusNivel = "CAIXA/STATUS/NIVEL";
char* enviaStatusFluxo = "CAIXA/STATUS/FLUXO";
char* enviaStatusValvula = "CAIXA/STATUS/VALVULA";
char* enviaNotificacaoFluxo = "CAIXA/NOTIFICACAO/FLUXO";

char* recebeStatus = "CAIXA/STATUS";
char* recebeValvula = "CAIXA/VALVULA";
char* recebeNivel = "CAIXA/NIVEL";
char* recebeConfTempNivel = "CAIXA/CONFIGURAR/TEMPO/NIVEL";
char* recebeConfTempFluxo = "CAIXA/CONFIGURAR/TEMPO/FLUXO";
char* recebeConfNotificaFluxo = "CAIXA/CONFIGURAR/NOTIFICACAO/FLUXO";

char* recebeNivelMax = "CAIXA/NIVEL/MAX";
char* recebeNivelMin = "CAIXA/NIVEL/MIN";


// =======================================================================
// --- Declarar Funções ---

void mqtt_callback(char* topic, byte* dados_tcp, unsigned int length);
void conectaWifi(void);
void MQTT(void);
void valvula(void);
void nivel(void);
ICACHE_RAM_ATTR void contadorPulso(void); 
void vazao(void);
void informaStatus(int);
boolean testeVazao(void); 


// =======================================================================
// --- Configurações Iniciais ---

void setup() { 
  
  pinMode(VALVULAENTRADA, OUTPUT);
  digitalWrite(VALVULAENTRADA, LOW); 
 
  pinMode(TRIGGER, OUTPUT);
  digitalWrite(TRIGGER, LOW); 
  pinMode(ECHO, INPUT);

  pinMode(VAZAO, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(VAZAO), contadorPulso, FALLING);
  
  Serial.begin(115200);
  conectaWifi();
  MQTT();
  
} //end setup


// =======================================================================
// --- Loop Infinito ---

void loop() {
  
  valvula();
  nivel();
  vazao();
  
  if (WiFi.status() == WL_CONNECTED){
    if (client.connected()){
      client.loop();
      }
    else{
      MQTT();
      }
   }
   else{
    conectaWifi();
    }

} //end loop


// =======================================================================
// --- Desenvolvimento das Funções ---

void callback(char* topic, byte* dados, unsigned int length) {
    String msg;
    
    //obtem a string do dados recebido
    for(int i = 0; i < length; i++){
       char c = (char)dados[i];
       msg += c;
    }

    if ((strcmp(topic,recebeStatus)== 0)){
      
       if(msg.equals("V") ){ // Válvula
          informaStatus(1);
          }

       if(msg.equals("F") ){ // Fluxo
          informaStatus(2);
          }

       if(msg.equals("N") ){ // Nível
          informaStatus(3);
          }

       if(msg.equals("STATUS") ){  
        int cont;
        for(cont = 1; cont <= 3; cont++){
          informaStatus(cont);
        }
      }
    } // fim topico recebeStatus

    if ((strcmp(topic,recebeValvula)== 0)){

       if(msg.equals("L1") ){
          valvEntrada = true;
        }
        else if(msg.equals("D1") ){
          valvEntrada = false;
        }
    } // fim topico recebeValvula

    if ((strcmp(topic,recebeNivel)== 0)){
      
      appNivel = msg.toInt();
      }

      if ((strcmp(topic,recebeConfTempNivel)== 0)){
        
        if (msg.toInt()>0) appTempoNivel = msg.toInt()*1000;
        }

      if ((strcmp(topic,recebeConfTempFluxo)== 0)){
        
        if (msg.toInt()>0) appTempoFluxo = msg.toInt()*1000; 
        }

     if ((strcmp(topic,recebeConfNotificaFluxo)== 0)){
        
        if (msg.toInt()>0) appFluxoNotificacao = msg.toInt(); 
        }

     if ((strcmp(topic,recebeNivelMax)== 0)){
      
        nivelMax = msg.toInt();
      }

     if ((strcmp(topic,recebeNivelMin)== 0)){
      
        nivelMin = msg.toInt();
      }
      
} //fim callback


void MQTT(void) {

  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);
  
  while (!client.connected()) {
    Serial.println("Conectando ao servidor MQTT...");
    
    if (client.connect(mqttId, mqttUser, mqttPassword)){
      Serial.println("Conectado ao servidor MQTT!");
      client.publish("CAIXA/PLACA","Em funcionamento!"); // client.publish(topico,msg, qos_level);

      // Tópicos do MQTT para receber msg
        uint16_t packetIdSub1 = client.subscribe(recebeValvula, 0); // client.subscribe(topico, qos_level);
        uint16_t packetIdSub2 = client.subscribe(recebeNivel, 0); 
        uint16_t packetIdSub3 = client.subscribe(recebeConfTempNivel, 0);
        uint16_t packetIdSub4 = client.subscribe(recebeConfTempFluxo, 0);
        uint16_t packetIdSub5 = client.subscribe(recebeConfNotificaFluxo, 0);
        uint16_t packetIdSub6 = client.subscribe(recebeStatus, 0); // recebeNivelMax, recebeNivelMin
        uint16_t packetIdSub7 = client.subscribe(recebeNivelMax, 0);
        uint16_t packetIdSub8 = client.subscribe(recebeNivelMin, 0);
        
     } // if connect 
     else{ 
        Serial.print("Falha ao conectar ");
        Serial.print(client.state());
        delay(2000);
      }
      
  } // while connect

} //fim MQTT


void conectaWifi(void) { 
  
  WiFi.begin(ssidWifi, passwordWifi);

  if (WiFi.status() == WL_CONNECTED){
    Serial.println("Conectado!");
    Serial.println(ssidWifi);
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());
  }
 
  while (WiFi.status() != WL_CONNECTED){   
    delay(100);
    Serial.println("Conectando a WiFi..");
  }
    
} //fim conectaWifi


void valvula(){ 
    
    if ( (testeVazao() && ((alturaPorCem > 99)||(alturaPorCem >= appNivel))) || (!valvEntrada) )  digitalWrite(VALVULAENTRADA, LOW);  
    else if( (valvEntrada) && ( (alturaPorCem < nivelAbrirValvEntrada) || ((millis() - tempoValvula > intervaloTempoValvula) && (alturaPorCem < (4*appNivel/5) ) ) ) )  digitalWrite(VALVULAENTRADA, HIGH); 
    
    valvEntAtual = digitalRead(VALVULAENTRADA);
    tempoValvula = millis();
    
    if(valvEntAnt != valvEntAtual){
      valvEntAnt = valvEntAtual;
      
      Serial.print("Valvula Entrada Nivel: ");
      Serial.println(valvEntAtual);

      if(valvEntAtual) client.publish(enviaStatusValvula,"L1");
      else             client.publish(enviaStatusValvula,"D1");
    }
  
} //fim valvula


void nivel(){
  
  if(millis() - tempoNivel > intervaloTempoNivel){
    noInterrupts();
    digitalWrite(TRIGGER, HIGH); // Manda um sinal de nível alto por 10 microssegundos
    delayMicroseconds(10);
    digitalWrite(TRIGGER, LOW);
  
    altura = pulseIn(ECHO, HIGH)/ 29.4 / 2;
    if (altura > nivelMin) altura = nivelMin;
    
    tempoNivel = millis();
    alturaPorCem = (nivelMin - altura ) * 100 / (nivelMin - nivelMax);

    Serial.print("Nivel : ");
    Serial.print(alturaPorCem);
    Serial.println(" %");
    Serial.print("distancia : ");
    Serial.print(altura);
    Serial.println(" cm");
     
    if((millis() - tempoAtualizarNivel) > appTempoNivel){
      tempoAtualizarNivel = millis();
      
      if (alturaPorCem > 100) alturaPorCem = 100;
      
      String nivelEnviar = String(alturaPorCem);
      char nivelPublica[5];
      nivelEnviar.toCharArray(nivelPublica, 5);
      client.publish(enviaStatusNivel,nivelPublica);
    } 
    
    interrupts();
  } // if intervalo de tempo

} //fim nivel


ICACHE_RAM_ATTR void contadorPulso() {
  
  contador++;
  
} // fim contadorPulso


void vazao() {

  if(digitalRead(VALVULAENTRADA)){
     if(!ativarNotificacaoFluxo){
      tempoNotificacaoFluxo = millis();
      ativarNotificacaoFluxo = true;
      }

  
    if((millis() - tempoFluxo) > intervaloTempoFluxo) {
      detachInterrupt(digitalPinToInterrupt(VAZAO));
      
      fluxo = ((1000.0 / (millis() - tempoFluxo)) * contador) / CALIBRACAO;
      //fluxo = contador / CALIBRACAO;
      Serial.print("Fluxo de: ");
      Serial.print(fluxo);
      Serial.println(" L/min");
      
      volume += fluxo / 60;
      Serial.print("Volume: ");
      Serial.print(volume);
      Serial.println(" L");
  
      somaContador += contador;
      Serial.print("Pulso total: ");
      Serial.print(somaContador);
  
      Serial.print("Pulso : ");
      Serial.print(contador);
      
      contador = 0;
      tempoFluxo = millis();

     attachInterrupt(digitalPinToInterrupt(VAZAO), contadorPulso, FALLING);

     } // if intervalo de tempo
    
    
    if((fluxo <= appFluxoNotificacao) and ((millis() - tempoNotificacaoFluxo) >= intervaloTempoNotificacaoFluxo )){ // espera um tempo pra analisar o fluxo
        if((millis() - tempoProximoNotificacaoFluxo) > proximoNotificacaoFluxo){
          client.publish(enviaNotificacaoFluxo,"BAIXO");
          tempoProximoNotificacaoFluxo = millis();
        }
     } 
     
  } // if válvula
  else{ 
    fluxo = 0;
    tempoNotificacaoFluxo = 0;
    ativarNotificacaoFluxo = false;
  }

  if((millis() - tempoAtualizarFluxo) > appTempoFluxo){
      tempoAtualizarFluxo = millis();
      String fluxoEnviar = String(fluxo);
      char fluxoPublica[5];
      fluxoEnviar.toCharArray(fluxoPublica, 5);
      client.publish(enviaStatusFluxo,fluxoPublica);
      }
    
} // fim vazao


void informaStatus(int num) {

  switch (num)
   {
     case 1:{
          if(digitalRead(VALVULAENTRADA)) client.publish(enviaStatusValvula,"L1");
          else                            client.publish(enviaStatusValvula,"D1");
         
         Serial.println(" STATUS - Valvula");
         Serial.println(digitalRead(VALVULAENTRADA));
         break;
     }
      
     case 2:{
         String fluxoStatus = String(fluxo);
         char fluxoPub[5];
         fluxoStatus.toCharArray(fluxoPub, 5);
         client.publish(enviaStatusFluxo,fluxoPub);
  
         Serial.print("STATUS - Fluxo : ");
         Serial.print(fluxo);
         Serial.println(" L/min");
         break;
     }
  
     case 3:{
        if (alturaPorCem > 100) alturaPorCem = 100;
        String nivelStatus = String(alturaPorCem);
        char nivelPub[5];
        nivelStatus.toCharArray(nivelPub, 5);
        client.publish(enviaStatusNivel,nivelPub);
  
        Serial.print("STATUS - Nível: ");
        Serial.print(alturaPorCem);
        Serial.println(" %");
        break;
     }
   }
  
  } // fim informaStatus

  
  boolean testeVazao(){ 
    if( (alturaPorCem > 99) && (alturaPorCem < 110) && (appNivel == 100) && ((millis() - tempoProximoTesteVazao) > proximoTesteVazao) && ((millis() - tempoTesteVazao) < intervaloTempoTesteVazao) ){ 
      //Serial.println(" teste Vazão - FALSE"); 
      return false;
    }  

    else{
        if((millis() - tempoTesteVazao) > intervaloTempoTesteVazao) tempoProximoTesteVazao = millis(); 
        tempoTesteVazao = millis();
        //Serial.println(" teste Vazão - TRUE");
        return true;
      
   } 

  } // fim testeVazao
  

// =======================================================================
// --- Final do Programa ---
