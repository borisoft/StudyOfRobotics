#include <Servo.h> 
#define F_SPEED_MAX 5
#define F_SPEED_MIN 35
#define B_SPEED_MAX 250
#define B_SPEED_MIN 220
#define Z_SPEED 128
#define SOFT_IMP_NUM 400

int motorR = DAC0;
int motorL = DAC1;
Servo servo_tern;

volatile int AR = 0;     // right encoder
volatile int AL = 0;     // left encoder
volatile int ButR = 1;   // right bumper
volatile int ButL = 1;

int MR = 20;
int ML = 20;
int tR, tL;
int dk = 73;//385;      // wheel diameter (mm)
float imm = 1000 / (3.14159265 * dk);  //  impulse count on mm
float degmm = 175  * 3.14159265 / 360;  // mm on 1 degree

float x, y;
int angle;
const float Pi = 3.14159;

char prevCommand = 's';
boolean isSended = false;
int countLoop = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  attachInterrupt(24, blinkAR, RISING);
  attachInterrupt(48, blinkAL, RISING);
  servo_tern.attach(4);
  x = y = 0.0;
  angle = 0;
}

void blinkAR() { 
  AR++; 
}

void blinkAL() { 
  AL++; 
}

void speedUpML()   { 
  if (ML <= 0)
    return;
  ML--; 
}

void speedDownML() { 
  if (ML >= 255)
    return;
  ML++; 
}

void speedUpMR() { 
  if (MR <= 0)
    return;
  MR--; 
}

void speedDownMR() { 
  if (MR >= 255)
    return;
  MR++; 
}

bool tooFast(int speed_) { 
  return (speed_ <= F_SPEED_MAX) || (speed_ >= B_SPEED_MAX); 
}

bool tooSlow(int speed_) { 
  return (speed_ >= F_SPEED_MIN) && (speed_ <= B_SPEED_MIN); 
}

void softStart(bool is_frwd) {
  int prevImpNum = AR;
  while (AR < SOFT_IMP_NUM) {
    if (AR - prevImpNum >= 8) {
      if (is_frwd) {
        speedUpML();
        speedUpMR();
      } else {
        speedDownML();
        speedDownMR();
      }
    }
    prevImpNum = AR;
    delay(8);
  }
}

void softStop(bool is_frwd) {
  int prevImpNum = AR;
  while (AR < SOFT_IMP_NUM) {
    if (AR - prevImpNum >= 8) {
      if (is_frwd) {
        speedDownML();
        speedDownMR();
      } else {
        speedUpML();
        speedUpMR();
      }
    }
    prevImpNum = AR;
    delay(8);
  }
}


void r_move(int imp_num, bool is_frwd, bool is_bump) {
  imp_num *= imm;
  AR = 0;
  AL = 0;
  //imp_num -= SOFT_IMP_NUM * 2;
  if (is_frwd) {
    MR = 30;
    ML = 30;
  }
  else {
    MR = 225;
    ML = 225;
  }
  tR = 0;
  tL = 0;
  //softStart(is_frwd); 
  while ((tR < imp_num) || (tL < imp_num)) {
    if (is_bump && (ButR == 0 || ButL == 0))
      break;
    tR = AR;
    tL = AL;
    int diff = tL - tR;
    
    // левый более чем на 100 обгоняет правый ->>
    if (diff > 80) { // Пытаемся ускорить правый, если не получается замедляем левый
      if (tooFast(MR)) {
        speedDownML();
      } else
        speedUpMR();
    }
    
    // правый более чем на 100 обгоняет левый   <<-
    if (diff < -80) {
      if (tooFast(ML)) { // Пытаемся ускорить левый, если не получается замедляем правый
        speedDownMR();
      } else
        speedUpML();
    }
    analogWrite(motorL, ML);
    analogWrite(motorR, MR);
    delay(20);
  }
  //softStop(is_frwd);
  //analogWrite(motorL, Z_SPEED);
  //analogWrite(motorR, Z_SPEED);
  
}

void r_turn(int degrs, boolean is_left) {
  AR = 0;
  AL = 0;
  float impT = 2 * imm * degmm * degrs;  // impulse count for turn on degrs degrees
  int imp = impT;
  if (is_left) {
    while (AR < imp) {
      analogWrite(motorR, 64);    // full forward
      analogWrite(motorL, 192);    // full back
    }
  } else {
    while (AL < imp) {
      analogWrite(motorR, 192);  // full back
      analogWrite(motorL, 64);  // full forward
    }
  }
  //analogWrite(motorR, Z_SPEED);  // full back
  //analogWrite(motorL, Z_SPEED); 
  
}
int str = 0;
char st;


void printCoordinates() {
  Serial.print(" {x=");
  Serial.print(x);
  Serial.print("; y=");
  Serial.print(y);
  Serial.print("; angle=");
  Serial.print(angle);
  Serial.println("}");
}

void loop() {
  if (Serial.available() > 0 ){
    str = Serial.read();
    st = str;
    isSended = false;
    printCoordinates();
  }
  else {
    isSended = true; 
    st = prevCommand;
  }
  switch (st) {
    case 'f':
      prevCommand = 'f';
      r_move(36,  true,  false);
      x += sin(Pi / 180 * angle) * 36; 
      y += cos(Pi / 180 * angle) * 36;
      if (!isSended) {
        Serial.write("k forward 36");
      }
      break;
    case 'b':
      prevCommand = 'b';
      r_move(36,  false,  false);
      x -= sin(Pi / 180 * angle) * 36; 
      y -= cos(Pi / 180 * angle) * 36;
      if (!isSended) {
        Serial.print("k back 36");
      }
      break;
    case 'r':
      prevCommand = 'r';
      r_turn(10, false);
      angle -= 10;
      if (angle > 360)
        angle = angle % 360;
      if (angle < -360)
        angle = angle % 360;
      if (!isSended) {
        Serial.write("k turn 10 right");
      }
      break;
    case 'l':
      prevCommand = 'l';
      r_turn(10, true);
      angle += 10;
      if (angle > 360)
        angle = angle % 360;
      if (angle < -360)
        angle = angle % 360;
      if (!isSended) {
        Serial.write("k turn 10 left");
      }
      break;
    case 's':
      prevCommand = 's';
      analogWrite(motorL, Z_SPEED);
      analogWrite(motorR, Z_SPEED);
      if (!isSended) {
        Serial.write("k");
        Serial.println("stop");
      }
      break;
    default:
      if (!isSended) {
        Serial.write("d"); 
      }
      break;
  }  
  if (st != 's' && countLoop % 10 == 0) {
    printCoordinates();
    countLoop = 0;
  }
  countLoop++;
}

