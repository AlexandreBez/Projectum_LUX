// Configuração dos pinos do motor de passo
const int IN1 = 32;
const int IN2 = 33;
const int IN3 = 34;
const int IN4 = 35;

// Configuração dos LEDs
const int UV_LED_PINS[] = { 8, 7, 6, 5, 4, 3, 44, 45 };
const int IR_LED_PINS[] = { 10, 9 };
const int LUX_LED_PINS[] = { 12, 11 };

// Configuração do LED em MCD
const int MAX_MCD_UV = 400;
const int MAX_MCD_IR = 119;
const int MAX_MCD_LUX = 14000;

// Variáveis globais
int intensidadeMCD_UV = 0;
int intensidadeMCD_IR = 0;
int intensidadeMCD_LUX = 0;

// Variáveis do motor
int stepDelay = 10;
int velocidadeMotor = 0;
int stepIndex = 0;
bool sentidoHorario = true;
bool isMotorAtivo = false;

// Sequência de passos do motor para o driver ULN2003
const int stepSequence[8][4] = {
  { 1, 0, 0, 0 },
  { 1, 1, 0, 0 },
  { 0, 1, 0, 0 },
  { 0, 1, 1, 0 },
  { 0, 0, 1, 0 },
  { 0, 0, 1, 1 },
  { 0, 0, 0, 1 },
  { 1, 0, 0, 1 }
};

void setup() {
  Serial.begin(9600);

  // Configuração dos LEDs
  for (int i = 0; i < 12; i++) {
    pinMode(UV_LED_PINS[i], OUTPUT);
    analogWrite(UV_LED_PINS[i], 0);
    pinMode(IR_LED_PINS[i], OUTPUT);
    pinMode(LUX_LED_PINS[i], OUTPUT);
    analogWrite(IR_LED_PINS[i], 0);
    analogWrite(LUX_LED_PINS[i], 0);
  }

  // Configuração dos pinos do motor
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);

  Serial.println("Sistema iniciado. Use os comandos:");
  Serial.println("RPM:<valor>, UV:<valor>, IR:<valor>, LUX:<valor>, STATUS ou RESET.");
  Serial.println("Limite para cada: RPM(-15(Retrogrado) ate 15(Progressivo)), UV(0 ate 400), LUX(0 ate 14000), IR(0 ate 119)");
}

void loop() {
  if (Serial.available()) {
    String comando = Serial.readStringUntil('\n');
    comando.trim();
    processarComando(comando);
  }

  // Atualiza o motor se ativo
  if (isMotorAtivo) {
    motorStep(sentidoHorario);
    delay(stepDelay);
  }
}

void processarComando(String comando) {
  if (comando.startsWith("RPM:")) {
    velocidadeMotor = comando.substring(4).toInt();

    if (velocidadeMotor == 0) {
      isMotorAtivo = false;
      desligarMotor();
      Serial.println("Motor desligado.");
    } else {
      isMotorAtivo = true;
      sentidoHorario = (velocidadeMotor >= 0);
      stepDelay = map(abs(velocidadeMotor), 0, 15, 500, 10);

      Serial.println("Velocidade do motor ajustada para: " + String(velocidadeMotor) + " RPM");
    }
  } else if (comando.startsWith("UV:")) {
    intensidadeMCD_UV = constrain(comando.substring(3).toInt(), 0, MAX_MCD_UV);
    int pwmValue = map(intensidadeMCD_UV, 0, MAX_MCD_UV, 0, 255);
    for (int i = 0; i < 8; i++) {
      analogWrite(UV_LED_PINS[i], pwmValue);
    }
    Serial.println("Intensidade UV ajustada para: " + String(intensidadeMCD_UV) + " MCD");
  } else if (comando.startsWith("IR:")) {
    intensidadeMCD_IR = constrain(comando.substring(3).toInt(), 0, MAX_MCD_IR);
    int pwmValue = map(intensidadeMCD_IR, 0, MAX_MCD_IR, 0, 255);
    for (int i = 0; i < 2; i++) {
      analogWrite(IR_LED_PINS[i], pwmValue);
    }
    Serial.println("Intensidade IR ajustada para: " + String(intensidadeMCD_IR) + " MCD");
  } else if (comando.startsWith("LUX:")) {
    intensidadeMCD_LUX = constrain(comando.substring(4).toInt(), 0, MAX_MCD_LUX);
    int pwmValue = map(intensidadeMCD_LUX, 0, MAX_MCD_LUX, 0, 255);
    for (int i = 0; i < 2; i++) {
      analogWrite(LUX_LED_PINS[i], pwmValue);
    }
    Serial.println("Intensidade LUX ajustada para: " + String(intensidadeMCD_LUX) + " MCD");
  } else if (comando.startsWith("RESET")) {
    for (int i = 0; i < 8; i++) {
      analogWrite(UV_LED_PINS[i], 0);
    }
    for (int i = 0; i < 2; i++) {
      analogWrite(LUX_LED_PINS[i], 0);
    }
    for (int i = 0; i < 2; i++) {
      analogWrite(IR_LED_PINS[i], 0);
    }
    Serial.println("Intensidade do LED e RPM resetado!!!");
  } else if (comando == "STATUS") {
    Serial.println("Status Atual:");
    Serial.println("Intensidade UV total: " + String(intensidadeMCD_UV * 8) + " MCD");
    Serial.println("Intensidade IR total: " + String(intensidadeMCD_IR * 2) + " MCD");
    Serial.println("Intensidade LUX total: " + String(intensidadeMCD_LUX * 2) + " MCD");
    Serial.println("==================================================================");
    Serial.println("Intensidade UV por led: " + String(intensidadeMCD_UV) + " MCD");
    Serial.println("Intensidade IR por led: " + String(intensidadeMCD_IR) + " MCD");
    Serial.println("Intensidade LUX por led: " + String(intensidadeMCD_LUX) + " MCD");
    Serial.println("==================================================================");
    Serial.println("Motor: " + String(isMotorAtivo ? "Ativo" : "Desligado"));
    Serial.println("RPM do Motor: " + String(velocidadeMotor) + " RPM");
    Serial.println("Sentido do Motor: " + String(sentidoHorario ? "Horário" : "Anti-horário"));
  } else {
    Serial.println("Comando inválido! Use RPM:<valor>, UV:<valor>, IR:<valor>, LUX:<valor>, ou STATUS.");
    Serial.println("Limite para cada: RPM(-15(Retrogrado) ate 15(Progressivo)), UV(0 ate 400), LUX(0 ate 14000), IR(0 ate 119)");
  }
}

void motorStep(bool sentidoHorario) {
  if (sentidoHorario) {
    stepIndex++;
    if (stepIndex >= 8) stepIndex = 0;
  } else {
    stepIndex--;
    if (stepIndex < 0) stepIndex = 7;
  }

  digitalWrite(IN1, stepSequence[stepIndex][0]);
  digitalWrite(IN2, stepSequence[stepIndex][1]);
  digitalWrite(IN3, stepSequence[stepIndex][2]);
  digitalWrite(IN4, stepSequence[stepIndex][3]);
}

void desligarMotor() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
}
