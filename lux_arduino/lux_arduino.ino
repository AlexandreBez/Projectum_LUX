#include "U8glib.h"

#define LED_MAX_INTENSITY 400
#define LED_QUANTITY 14

U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NO_ACK);

// Configuração dos pinos
const int ledPin[] = {44, 45, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
int pwm_led = 0; // Valor PWM para os LEDs

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

  } while (u8g.nextPage());
}

void setup() {
  Serial.begin(9600); // Inicializa a comunicação Serial

  // Configura os pinos dos LEDs como saída
  for (int i = 0; i < LED_QUANTITY; i++) {
    pinMode(ledPin[i], OUTPUT);
  }

  // Mensagem inicial no display
  u8g.setFont(u8g_font_6x10);
  u8g.firstPage();
  do {
    u8g.setPrintPos(0, 10);
    u8g.println(F("System ready..."));
    u8g.setPrintPos(0, 25);
    u8g.println(F("Enter PWM (0-255)"));
  } while (u8g.nextPage());

  delay(1000);
}

void loop() {
  // Verifica se há dados disponíveis no Serial
  if (Serial.available() > 0) {
    String input = Serial.readStringUntil('\n'); // Lê a entrada como String
    int new_pwm = input.toInt(); // Converte a entrada para inteiro

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
  }
}
