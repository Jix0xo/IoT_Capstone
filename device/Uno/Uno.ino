#include <SoftwareSerial.h> //라즈베리파이와의 통신

//펌프핀
const int AIA = 4;
const int AIB = 5;
const int BIA = 6;
const int BIB = 7;

//펌프 동작 속도
byte speed = 15;

bool motorRunning = false;
unsigned long motorStartTime = 0;

int snoring = 0;

//mkrWiFi1010 보드와의 통신 시리얼
const byte rxPin = 2;
const byte txPin = 3;
SoftwareSerial mySerial(rxPin, txPin);

void setup() {

  Serial.begin(115200);
  while (!Serial) {
    ; 
  }
  Serial.println();
  Serial.println("SoftwareSerial test");
  mySerial.begin(9600);
  pinMode(AIA, OUTPUT);
  pinMode(AIB, OUTPUT);
  pinMode(BIA, OUTPUT);
  pinMode(BIB, OUTPUT);
}

void loop() {

  if (mySerial.available()) { //mkrWiFi1010보드와의 시리얼 통신
    char a = mySerial.read();

    Serial.write(a);
    if(a == 'O'){ //mkr 보드로부터 받은 값이 알파벳 'O'이면 펌프 동작  
      spin();
      Serial.println("Run"); //시리얼모니터로 확인
    }
    
  }
  if(Serial.available()){ //라즈베리파이와의 시리얼 통신
    char inByte = Serial.read();
    
    Serial.print(inByte);
    Serial.write(inByte);
    
    if(inByte == '1'){ //라즈베리파이로부터 '1'을 받을 때마다
      snoring++; //snoring값을 1씩 증가시키고
      String sString = String(snoring); //문자열로 변환해
      mySerial.println(sString); //mkr보드로 보내기
      Serial.println(sString); //시리얼 모니터로 확인
    }
  }

}

void spin() {
  if (!motorRunning) { //펌프 on
    motorStartTime = millis();
    analogWrite(AIA, 0);
    analogWrite(AIB, speed);
    motorRunning = true;
    }
    
  if (motorRunning && millis() - motorStartTime >= 10000) { //10초가 지나면 off
    analogWrite(AIA, 0);
    analogWrite(AIB, 0);
    motorRunning = false;
  }

}


