// aws에서 시작시간, 끝시간, posture값을 전달받고 시작시간부터 끝시간까지 posture값에 따라 뒤척임 횟수를 계산하고 
// 뒤척임 횟수와 우노보드에서 시리얼 통신으로 전달받은 코골이 횟수를 aws로 보내는 코드
//      베게 센서
//  3번   2번   1번
//  6번   5번   4번
#include <ArduinoBearSSL.h>
#include <ArduinoECCX08.h>
#include <ArduinoMqttClient.h>
#include <WiFiNINA.h> // change to #include <WiFi101.h> for MKR1000
#include "arduino_secrets.h"

#include <ArduinoJson.h>

/////// Enter your sensitive data in arduino_secrets.h
const char ssid[]        = SECRET_SSID;
const char pass[]        = SECRET_PASS;
const char broker[]      = SECRET_BROKER;
const char* certificate  = SECRET_CERTIFICATE;

WiFiClient    wifiClient;            // Used for the TCP socket connection
BearSSLClient sslClient(wifiClient); // Used for SSL/TLS connection, integrates with ECC508
MqttClient    mqttClient(sslClient);

unsigned long lastMillis = 0;
int tcount = 0;
int posture1 = 1;
char snoring[10];
char inByte;
bool O_sent = false;
bool X_sent = true;

//펌프 핀
const int AIA = 0; //1
const int AIB = 1;
const int BIA = 2; //2
const int BIB = 3;

const int AIA1 = 4; //3
const int AIB1 = 5;
const int BIA1 = 6; //4
const int BIB1 = 7;

const int AIA2 = 8; //5
const int AIB2 = 9;
const int BIA2 = 10; //6
const int BIB2 = 11;

//펌프 동작 속도
byte speed = 255;

//압력센서 핀
int Sensor1 = A1;
int Sensor2 = A2;
int Sensor3 = A3;
int Sensor4 = A4;
int Sensor5 = A5;
int Sensor6 = A6;

int Reading1;
int Reading2;
int Reading3;
int Reading4;
int Reading5;
int Reading6;

int prevReading1 = 0;
int prevReading2 = 0;
int prevReading3 = 0;
int prevReading4 = 0;
int prevReading5 = 0;
int prevReading6 = 0;

bool motorRunning = false;
unsigned long motorStartTime = 0;

char startTimeStr[6];
char endTimeStr[6];
bool communicationEnabled = false;

bool startMessageSent = false;

void setup() {
  Serial.begin(115200);
  while (!Serial){
    ; // 시리얼 포트 연결 대기. USB 포트만 필요
  }

  Serial1.begin(9600); // Serial1 초기화 (우노 보드와의 통신)

  if (!ECCX08.begin()) { // ECCX08 시작에 실패하면 실행
    Serial.println();
    Serial.write("Arduino\n");
    Serial.println("No ECCX08 present!");
    while (1);
  }

  ArduinoBearSSL.onGetTime(getTime); // SSL 연결을 위한 현재 시간을 가져
  sslClient.setEccSlot(0, certificate); // ECC 슬롯을 0으로 설정하고 인증서를 sslClient에 로드
  mqttClient.onMessage(onMessageReceived); // 들어오는 MQTT 메시지를 처리하는 onMessageReceived 함수를 등록

  pinMode(AIA, OUTPUT);
  pinMode(AIB, OUTPUT);
  pinMode(BIA, OUTPUT);
  pinMode(BIB, OUTPUT);
  pinMode(AIA1, OUTPUT);
  pinMode(AIB1, OUTPUT);
  pinMode(BIA1, OUTPUT);
  pinMode(BIB1, OUTPUT);
  pinMode(AIA2, OUTPUT);
  pinMode(AIB2, OUTPUT);
  pinMode(BIA2, OUTPUT);
  pinMode(BIB2, OUTPUT);
}

void loop() {
  //입력받은 센서값을 Reading에 저장
  Reading1 = analogRead(Sensor1);
  Reading2 = analogRead(Sensor2);
  Reading3 = analogRead(Sensor3);
  Reading4 = analogRead(Sensor4);
  Reading5 = analogRead(Sensor5);
  Reading6 = analogRead(Sensor6);

  //Reading값의 범위 수정
  int value1 = map(Reading1, 0, 1024, 0, 255);
  int value2 = map(Reading2, 0, 1024, 0, 255);
  int value3 = map(Reading3, 0, 1024, 0, 255);
  int value4 = map(Reading4, 0, 1024, 0, 255);
  int value5 = map(Reading5, 0, 1024, 0, 255);
  int value6 = map(Reading6, 0, 1024, 0, 255);

  //시리얼모니터로 센서값 확인
  Serial.print("Sensor1: ");
  Serial.print(value1);
  Serial.print(", Sensor2: ");
  Serial.print(value2);
  Serial.print(", Sensor3: ");
  Serial.println(value3);
  Serial.print("Sensor4: ");
  Serial.print(value4);
  Serial.print(", Sensor5: ");
  Serial.print(value5);
  Serial.print(", Sensor6: ");
  Serial.println(value6);
  Serial.print("toss and turn: ");
  Serial.print(tcount); //뒤척임 횟수
  Serial.println(" times");
  Serial.println(" ");
  delay(3000);

  if (Serial1.available()) { //우노보드와 시리얼 통신으로 데이터 받기
     int len = Serial1.readBytesUntil('\n', snoring, sizeof(snoring) - 1);  // snoring값을 문자열로 저장
    snoring[len] = '\0';  
    Serial1.write(snoring); // 읽어들인 데이터를 시리얼 포트 1에 다시 씀
    Serial.print("Snoring times: "); 
    Serial.println(snoring); //시리얼모니터로 snoring값 확인
    
    
  }
  
  if (Serial.available()) { //우노보드로 값 전송
    inByte = Serial.read();
    Serial.print(inByte);
    Serial1.write(inByte);
  }

  if (WiFi.status() != WL_CONNECTED) { //WiFi 연결
    connectWiFi();
  }

  if (!mqttClient.connected()) { //MQTT 연결
    connectMQTT();
  }

  mqttClient.poll(); //MQTT 클라이언트 상태 확인

  if (millis() - lastMillis > 5000) { 
    lastMillis = millis();
    char payload[512];
    getDeviceStatus(payload); //디바이스 상태를 저장하는 함수
    sendMessage(payload); //저장된 메세지를 전송하는 함수
  }

  checkCommunicationTime();
}

unsigned long getTime() {
  return WiFi.getTime();
}

void connectWiFi() { //WiFi 연결하는 함수
  Serial.print("Attempting to connect to SSID: ");
  Serial.print(ssid); //와이파이 정보 확인
  Serial.print(" ");
  
  while (WiFi.begin(ssid, pass) != WL_CONNECTED) {
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the network");
  Serial.println();
}

void connectMQTT() { //MQTT 연결하는 함수
  Serial.print("Attempting to MQTT broker: ");
  Serial.print(broker); //aws 계정 정보 확인
  Serial.println(" ");

  while (!mqttClient.connect(broker, 8883)) {
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the MQTT broker");
  Serial.println();

  mqttClient.subscribe("$aws/things/MyMKRWiFi1010/shadow/update/delta"); //주제 구독
}

void getDeviceStatus(char* payload) { //aws에 디바이스 상태 전달 (뒤척임값, 코골이 횟수 전달)
  sprintf(payload, "{\"state\":{\"reported\":{\"moving\":\"%d\",\"snoring\":\"%s\"}}}", tcount, snoring);
}

void sendMessage(char* payload) { //MQTT 메시지 전송
  char TOPIC_NAME[]= "$aws/things/MyMKRWiFi1010/shadow/update"; //주제 이름 설정
  
  Serial.print("Publishing send message:"); 
  Serial.println(payload); //전송할 메시지 출력 (getDeviceStatus에 있는 메시지)
  mqttClient.beginMessage(TOPIC_NAME); //해당 주제로 MQTT 메시지 전송 시작
  mqttClient.print(payload); //payload를 MQTT 메시지로 출력
  mqttClient.endMessage();
}

void onMessageReceived(int messageSize) {
  char prevPos[] = "0";
  Serial.println("messageReceived 호출됨");
  Serial.print("Received a message with topic '");
  Serial.print(mqttClient.messageTopic()); //수신된 메시지의 주제 출력
  Serial.print("', length ");
  Serial.print(messageSize);
  Serial.println(" bytes:");

  char buffer[512];
  int count=0;
  while (mqttClient.available()) { 
     buffer[count++] = (char)mqttClient.read(); //버퍼에 수신된 데이터 저장
  }
  buffer[count]='\0';
  Serial.println(buffer);
  Serial.println();

  //JSON형식의 메시지를 읽음
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, buffer); //버퍼의 내용을 JSON으로 바꿈
  JsonObject root = doc.as<JsonObject>();
  JsonObject state = root["state"]; //state 찾기
  const char* posture = state["posture"]; //posture 찾기
  const char* endTime = state["endTime"]; //endTime 찾기
  Serial.println(posture); //posture값 출
  
  if (endTime) { //endTime을 읽어서 출력
    strncpy(endTimeStr, endTime, sizeof(endTimeStr));
    endTimeStr[sizeof(endTimeStr) - 1] = '\0';
    Serial.print("Updated endTimeStr: ");
    Serial.println(endTimeStr);
  }

    if (strcmp(posture, "1") == 0) { //posture값이 1이면
      Serial.println("tnt1 호출됨"); //시리얼 모니터로 확인
      tnt1(); //1번 계산 함수
    
    } else if (strcmp(posture, "2") == 0) {
      Serial.println("tnt2 호출됨");
      tnt2();

    } else if (strcmp(posture, "3") == 0) {
      Serial.println("tnt3 호출됨");
      tnt3();

    }
    
}

void tnt1() { //1번 계산 함수 - 정자세
// 가운데 센서값이 감소하고 양쪽 사이드의 센서값이 하나라도 증가하면 양 사이드의 펌프가 모두 작동
  if ((Reading2 + 5 < prevReading2 || Reading5 + 5 < prevReading5) && 
  (Reading1 > prevReading1 + 5 || Reading3 > prevReading3 + 5 || 
  Reading4 > prevReading4 + 5 || Reading6 > prevReading6 + 5)) {
    if (!motorRunning) { // 펌프가 작동 중이 아니라면
      motorStartTime = millis(); // 현재 시간 기록
      analogWrite(AIA, 0); //1 on
      analogWrite(AIB, speed);
      analogWrite(AIA1, 0); //3 on
      analogWrite(AIB1, speed);
      analogWrite(BIA1, 0); //4 on
      analogWrite(BIB1, speed);
      analogWrite(BIA2, 0); //6 on
      analogWrite(BIB2, speed);
      motorRunning = true;
      O_sent = false;
    }
    tcount++; //뒤척임값 1증가
  }
  

  if (motorRunning && millis() - motorStartTime >= 50000) { // 모터가 작동 중이고 10초가 지났다면
    stop(); //펌프 멈춤
    motorRunning = false;
  }

  

  prevReading1 = Reading1;
  prevReading2 = Reading2;
  prevReading3 = Reading3;
  prevReading4 = Reading4;
  prevReading5 = Reading5;
  prevReading6 = Reading6;

  //우노보드에 O를 전달해 바람 빼는 코드
  if (!O_sent && millis() - motorStartTime >= 10000) { // 모터가 작동 중이고 10초가 지났다면
    Serial1.write('O'); //우노보드로 알파벳 'O' 전달
    O_sent = true;
  }
}

void tnt2() { //2번 계산 함수 - 옆으로 눕는 자세
  //한 쪽 사이드의 센서값이 줄어들고 가운데 센서값이 커지면 반대쪽 사이드의 펌프 작동
  if ((Reading1 + 5 < prevReading1 || Reading4 + 5 < prevReading4) && 
  (Reading2 > prevReading2 + 5 || Reading5 > prevReading5 + 5)) {
    if (!motorRunning) {
      motorStartTime = millis();
      analogWrite(AIA1, 0); //3 on
      analogWrite(AIB1, speed);
      analogWrite(BIA2, 0); //6 on
      analogWrite(BIB2, speed);
      motorRunning = true;
    }
    tcount++;
  }

  else if ((Reading3 + 5 < prevReading3 || Reading6 + 5 < prevReading6) && 
  (Reading1 > prevReading1 + 5 || Reading4 > prevReading4 + 5)) {
    if (!motorRunning) {
      motorStartTime = millis();
      analogWrite(AIA, 0); //1 on
      analogWrite(AIB, speed);
      analogWrite(BIA1, 0); //4 on
      analogWrite(BIB1, speed);
      motorRunning = true;
    }
    tcount++;
  }
  

  if (motorRunning && millis() - motorStartTime >= 10000) {
    stop();
    motorRunning = false;
  }

  prevReading1 = Reading1;
  prevReading2 = Reading2;
  prevReading3 = Reading3;
  prevReading4 = Reading4;
  prevReading5 = Reading5;
  prevReading6 = Reading6;
  if (!O_sent && millis() - motorStartTime >= 10000) {
    Serial1.write('O');
    O_sent = true;
  }
}

void tnt3() {//3번 함수 - 코골이 줄이기
//가운데 센서값이 커지면 5번 펌프를 제외한 모든 펌프 on
  if (Reading2 + 5 > prevReading2 || Reading5 + 5 > prevReading5){
    motorStartTime = millis();
    analogWrite(AIA, 0); //1 on
    analogWrite(AIB, speed);
    analogWrite(BIA, 0); //2 on
    analogWrite(BIB, speed);
    analogWrite(AIA1, 0); //3 on
    analogWrite(AIB1, speed);
    analogWrite(BIA1, 0); //4 on
    analogWrite(BIB1, speed);
    analogWrite(BIA2, 0); //6 on
    analogWrite(BIB2, speed);
  }
  if (motorRunning && millis() - motorStartTime >= 10000) { 
    stop();
    motorRunning = false;
  }
  if (!O_sent && millis() - motorStartTime >= 10000) { 
    Serial1.write('O');
    O_sent = true;
  }

}

void stop() { //모든 펌프 OFF
  analogWrite(AIA, 0);
  analogWrite(AIB, 0);
  analogWrite(BIA, 0);
  analogWrite(BIB, 0);
  analogWrite(AIA1, 0);
  analogWrite(AIB1, 0);
  analogWrite(BIA1, 0);
  analogWrite(BIB1, 0);
  analogWrite(AIA2, 0);
  analogWrite(AIB2, 0);
  analogWrite(BIA2, 0);
  analogWrite(BIB2, 0);
}

void checkCommunicationTime() { //시작시간과 끝시간 읽기
  unsigned long currentMillis = millis(); //현재 시간 가져오기
  int currentHour = currentMillis / 3600000 % 24; //현재 시간을 시간으로 계산
  int currentMinute = currentMillis / 60000 % 60; //현재 시간을 분으로 계산
  
  int startHour, startMinute;
  int endHour, endMinute;

  sscanf(startTimeStr, "%d:%d", &startHour, &startMinute); //시작 시간을 받아 시간과 분으로 나누기
  sscanf(endTimeStr, "%d:%d", &endHour, &endMinute); //끝시간도

  int currentTime = currentHour * 100 + currentMinute; //0830, 1125 같은 형식으로 만들기
  int startTime = startHour * 100 + startMinute;
  int endTime = endHour * 100 + endMinute;

  if (startTime < endTime) { //현재 시간이 설정된 시간 사이에 있는지 아닌지 판단
    communicationEnabled = (currentTime >= startTime && currentTime < endTime); 
  } else {
    communicationEnabled = (currentTime >= startTime || currentTime < endTime);
  }

  if (!communicationEnabled) { //통신이 안되면
    tcount = 0; //뒤척임값 0으로 초기화
    Serial.println("연결 실패"); //시리얼 모니터에서 확인
    startMessageSent = false; 
  }
  
  if (currentTime == startTime && !startMessageSent) { //현재 시간이 시작시간이 됐는데 시작메시지가 전송되지 않을 때
    sendStartMessage(); //시작 메시지 전송
    startMessageSent = true;
  }
  
}


void sendStartMessage() {
  Serial1.println("ON"); //Serial1에 On 메시지 출력

  int startHour, startMinute;
  int endHour, endMinute;

  sscanf(startTimeStr, "%d:%d", &startHour, &startMinute); //시작 시간을 받아 시간과 분으로 나누기
  sscanf(endTimeStr, "%d:%d", &endHour, &endMinute); //끝시간

  int remainingHours = (endHour - startHour) + (endMinute - startMinute) / 60; // 남은 시간을 시간 단위로 계산

  char remainingTime[4];
  itoa(remainingHours, remainingTime, 10); //남은 시간 문자열로 변환

  Serial1.println(remainingTime); //시리얼에 남은 시간 출력
}



