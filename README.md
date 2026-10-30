# ColorfulBedrock 🌟

**The Java Polish on Bedrock** — Sistema híbrido de Resource Pack para Bedrock (`.mcpack`) y Plugin Java (Paper / Folia) con **PacketEvents v2.14.0** y **Floodgate**.

Inspirado en la funcionalidad de **Unison**, este proyecto logra paridad visual para los jugadores que entran a servidores Java desde Minecraft Bedrock mediante Geyser.

---

## 📁 Estructura del Proyecto

```text
ColorfulBedrock/
├── ColorfulBedrock-BedrockPack.mcpack   <-- Pack empaquetado listo para instalar
├── BedrockPack/                         <-- Código fuente del Resource Pack de Bedrock
│   ├── manifest.json                    <-- Manifiesto con UUIDs únicos
│   ├── pack_icon.png                    <-- Icono del pack
│   ├── materials/
│   │   └── entity.material              <-- Elimina la caja negra de los hologramas
│   ├── textures/
│   │   └── ui/
│   │       ├── nametag_background.png   <-- Textura transparente 1x1
│   │       └── transparent.png
│   └── ui/
│       ├── scoreboard.json              <-- Oculta los números rojos del scoreboard
│       ├── player_list.json             <-- Tab list optimizado
│       └── _global_variables.json
│
└── ColorfulBedrock-Plugin/              <-- Código fuente del Plugin Java (Paper/Folia)
    ├── pom.xml                          <-- Maven con PacketEvents v2.14.0 y Floodgate API
    └── src/main/
        ├── java/com/pumpkings/colorfulbedrock/
        │   ├── ColorfulBedrockPlugin.java        <-- Ciclo de vida y registro en PacketEvents
        │   ├── config/PluginConfig.java          <-- Control de switches de config.yml
        │   ├── converter/
        │   │   ├── BedrockColorConverter.java    <-- Cuantización RGB -> Paleta Extendida (§0-§u)
        │   │   └── ComponentBedrockTranslator.java <-- Traductor de componentes Adventure
        │   ├── listener/BedrockPacketListener.java <-- Interceptor de paquetes de red
        │   └── util/PingGlyphs.java              <-- Conversor de ping a iconos gráficos
        └── resources/
            ├── plugin.yml
            └── config.yml
```

---

## 🚀 Instalación y Configuración

### 1. Resource Pack de Bedrock (`.mcpack`)
1. Toma el archivo `ColorfulBedrock-BedrockPack.mcpack`.
2. Cópialo dentro de la carpeta de Geyser:
   - En Paper/Spigot: `plugins/Geyser-Spigot/packs/`
   - En Velocity/Bungee: `plugins/Geyser-Velocity/packs/`
3. (Opcional) En el `config.yml` de Geyser, puedes activar:
   ```yaml
   force-resource-packs: true
   ```
   Esto asegura que todo jugador de Bedrock cargue automáticamente el pack al conectarse.

### 2. Plugin de Java (`ColorfulBedrock-Plugin`)
1. **Requisitos en el servidor Java:**
   - Servidor **Paper** o **Folia** (1.20.x - 1.21.x) con **Java 21** (o 17+).
   - Plugin **PacketEvents** (v2.14.0 instalado en la carpeta `plugins/`).
   - Plugin **Floodgate** (instalado en el servidor o proxy).
2. **Compilación:**
   Ejecuta en la carpeta `ColorfulBedrock-Plugin`:
   ```bash
   mvn clean package
   ```
3. Coloca el archivo generado `target/colorfulbedrock-1.0.0-SNAPSHOT.jar` en la carpeta `plugins/` de tu servidor.
4. Inicia o reinicia el servidor.

---

## ⚙️ Configuración (`config.yml`)

Cada funcionalidad tiene su propio interruptor independiente:

```yaml
scoreboard:
  enabled: true
  convert-hex: true       # Convierte degradados y HEX de Java a Bedrock (§0-§u)
  clean-numbers: true     # Sincroniza con el pack para ocultar números rojos

tablist:
  enabled: true
  format-ping: true       # Muestra ping legible (ej. 24ms 📶)
  use-custom-glyphs: false # Usa glifos Unicode PUA de fuente custom

holograms:
  enabled: true
  remove-black-box: true  # Alpha = 0 en TextDisplays (sin caja negra)
  force-shadow: true      # Activa sombra de texto idéntica a Java
  convert-hex: true       # Traduce colores RGB en hologramas
```

---

## 🔒 Paridad Garantizada

* **Jugadores de Java Intactos:** El listener verifica cada paquete con `FloodgateApi.getInstance().isFloodgatePlayer(uuid)`. Si el jugador es de Java, el paquete sigue su curso nativo sin ninguna alteración ni pérdida de rendimiento.
