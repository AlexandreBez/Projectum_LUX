#include "U8glib.h"
#include <Stepper.h> // Biblioteca para controle do motor de passo

#define LED_MAX_INTENSITY 400
#define LED_QUANTITY 10

U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NO_ACK);

// Configuração dos pinos dos LEDs
const int ledPin[] = {44, 45, 2, 3, 4, 5, 6, 7, 8, 9};
int pwm_led = 0; // Valor PWM para os LEDs

// Configuração dos pinos do motor de passo
const int IN1 = 30;
const int IN2 = 31;
const int IN3 = 32;
const int IN4 = 33;

// Configuração do motor de passo
const int STEPS_PER_REV = 2048; // Passos por rotação (ajuste conforme necessário)
Stepper stepper(STEPS_PER_REV, IN1, IN3, IN2, IN4); // Configuração em sequência

// Variável para controlar a velocidade do motor
int motorSpeed = 10; // Velocidade inicial do motor (RPM)

// Função para calcular a intensidade luminosa
float lux_intensity() {
  float duty_cycle = pwm_led / 255.0;
  return LED_MAX_INTENSITY * duty_cycle;
}

// Função para calcular lúmens
float calc_lumens() {
  float mcd = lux_intensity();
  float candelas = mcd / 1000.0;
  float angle_degrees = 30.0;
  float angle_radians = radians(angle_degrees / 2.0);
  float area_angular = 2 * PI * (1 - cos(angle_radians));
  return candelas * area_angular;
}

// Função para exibir dados no display
void drawDisplay(float intensity, float lumens) {
  u8g.firstPage();
  do {
    u8g.setFont(u8g_font_6x10);
    u8g.setPrintPos(0, 10);
    u8g.print(F("PWM: "));
    u8g.print(pwm_led);

    u8g.setPrintPos(0, 25);
    u8g.print(F("Intensity: "));
    u8g.print(intensity, 1);
    u8g.println(F(" mcd"));

    u8g.setPrintPos(0, 40);
    u8g.print(F("Lumens: "));
    u8g.print(lumens, 6);
    u8g.println(F(" lm"));

    u8g.setPrintPos(0, 55);
    u8g.print(F("Motor RPM: "));
    u8g.print(motorSpeed);
  } while (u8g.nextPage());
}

void setup() {
  Serial.begin(9600); // Inicializa a comunicação Serial

  // Configura os pinos dos LEDs como saída
  for (int i = 0; i < LED_QUANTITY; i++) {
    pinMode(ledPin[i], OUTPUT);
  }

  // Configura os pinos do motor de passo
  stepper.setSpeed(motorSpeed);

  // Mensagem inicial no display
  u8g.setFont(u8g_font_6x10);
  u8g.firstPage();
  do {
    u8g.setPrintPos(0, 10);
    u8g.println(F("System ready..."));
    u8g.setPrintPos(0, 25);
    u8g.println(F("Enter PWM/Motor RPM"));
  } while (u8g.nextPage());

  delay(1000);
}

void loop() {
  // Verifica se há dados disponíveis no Serial
  if (Serial.available() > 0) {
    String input = Serial.readStringUntil('\n'); // Lê a entrada como String

    // Processa a entrada como valor PWM ou velocidade do motor
    if (input.startsWith("PWM:")) {
      int new_pwm = input.substring(4).toInt(); // Extrai o valor após "PWM:"

      // Verifica se o valor está no intervalo válido (0 a 255)
      if (new_pwm >= 0 && new_pwm <= 255) {
        pwm_led = new_pwm;

        // Calcula intensidade e lúmens com base no novo PWM
        float total_lumen = calc_lumens() * LED_QUANTITY;
        float intensity = lux_intensity();

        // Atualiza todos os LEDs
        for (int i = 0; i < LED_QUANTITY; i++) {
          analogWrite(ledPin[i], pwm_led);
        }

        // Atualiza o display com os valores
        drawDisplay(intensity, total_lumen);

        // Feedback no Serial Monitor
        Serial.print(F("PWM atualizado: "));
        Serial.println(pwm_led);
      } else {
        Serial.println(F("Valor invalido! Insira entre 0 e 255."));
      }
    } else if (input.startsWith("RPM:")) {
      int new_speed = input.substring(4).toInt(); // Extrai o valor após "RPM:"

      // Verifica se o valor está no intervalo válido
      if (new_speed > 0 && new_speed <= 15) { // Máx: 15 RPM (ajuste conforme necessário)
        motorSpeed = new_speed;
        stepper.setSpeed(motorSpeed);

        // Atualiza o display com o valor do RPM
        drawDisplay(lux_intensity(), calc_lumens() * LED_QUANTITY);

        // Feedback no Serial Monitor
        Serial.print(F("Velocidade do motor atualizada: "));
        Serial.print(motorSpeed);
        Serial.println(F(" RPM"));
      } else {
        Serial.println(F("Valor invalido! Insira RPM entre 1 e 15."));
      }
    } else {
      Serial.println(F("Comando invalido! Use PWM:<valor> ou RPM:<valor>."));
    }
  }

  // Gira o motor continuamente com a velocidade configurada
  stepper.step(STEPS_PER_REV / 100); // Pequenos passos para movimento contínuo
}
