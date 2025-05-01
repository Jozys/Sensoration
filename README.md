# Verteilte Sensorauswertung

> [!WARNING]
> Dies ist ein Projekt, welches im Rahmen des Informatikstudiengangs an der DHBW-Karlsruhe im 6.
> Semester erarbeitet wurde. Es genügt nicht zwangsläufig den Ansprüchen der realen Welt und sollte
> dementsprechend ausschließlich für Testzwecke verwendet werden.

## Idee

Die Idee, welche hinter der Sensorauswertung steckt, ist es Sensordaten, die von verschiedenen
Smartphonegeräten stammen mittels des Prinzips von verteilten Systemen zwischen den Geräten zu
teilen und so die Unterschiede in Messdaten verschiedener Smartphones miteinander abgleichen zu
können. Ein Beispiel wäre hier die unterschiedlichen Sounds, welche ein Gerät aufzeichnet. Somit
würden sich die unterschiedlichen Frequenzen in einem Graphen auf den verschiedenen Geräten anzeigen
lassen.

## Umsetzung

Die Umsetzung soll mit einer Android App geschehen, die in Kotlin unter Verwendung von Jetpack
Compose geschrieben wird.

## Setup und Ausführung

### Voraussetzungen

- Android Studio Meerkat (2024.3.1) oder neuer
- JDK 21 oder höher
- Android SDK mit API Level 26 (Android 8) oder höher. Empfohlen wird API Level 35 (Android 15).
- Gradle Version mindestens 8.4 (nach Gradle Compability Matrix), ist aber in der
  gradle-wrapper.properties Datei
  vorgegeben und wird automatisch heruntergeladen.
- Android Gradle Plugin Version 8.8.0 ist verwendet worden. Für andere Versionen ist nicht
  garantiert, dass die App funktioniert.

### Projekt einrichten

1. Klone das Repository:
   ```bash
   git clone https://github.com/Jozys/Sensoration.git
   ```
2. Öffne Android Studio und wähle "Open an existing project"
3. Navigiere zum geklonten Projektverzeichnis und öffne es
4. Warte, bis Gradle die Abhängigkeiten synchronisiert hat

### In einem Emulator ausführen

> [!WARNING]
> Die Ausführung der App in einem Emulator ist möglich, allerdings ist die Funktionalität der Nearby
> Connections API nicht möglich, da Emulatoren keine Bluetooth-Funktionalität haben.
> Daher ist es empfehlenswert, die App auf einem echten Gerät zu testen.

1. Erstelle einen Emulator über den AVD Manager:

    - Klicke auf "Tools" > "Device Manager"
    - Klicke auf "Create Virtual Device"
    - Wähle ein Gerät (z.B. Pixel 7)
    - Wähle ein System Image mit API Level 33 oder höher
    - Konfiguriere den Emulator nach Bedarf und beende die Einrichtung

2. Starte die App:
    - Wähle den erstellten Emulator aus der Geräteliste
    - Klicke auf den Run-Button (▶️) in der Toolbar
    - Die App sollte nun auf dem Emulator starten

### Für Testen mit mehreren Geräten

Um die verteilte Sensorauswertung zu testen, benötigst du:

1. Mehrere echte Geräte
2. Bei Emulatoren: Stelle sicher, dass diese Bluetooth-Unterstützung haben (_Emulatoren haben in der
   Regel
   keine Bluetooth-Funktionalität, daher ist dies nicht empfohlen_)
3. Alternativ: Verwende mehrere physische Geräte, die über Bluetooth oder WLAN verbunden sind

### Hinweise zur Nutzung

Im Normalfall sollten die folgenden Schritte nicht notwendig sein, da die App automatisch die
benötigten Berechtigungen anfordert:

- Für die Bekanntmachung (Advertising) und das Auffinden von Geräten werden entsprechende
  Berechtigungen benötigt
- Bluetooth und Standort müssen auf allen Geräten aktiviert sein
- Bei Android 12+ werden zusätzliche Berechtigungen für "Nearby Devices" benötigt

Sollte es zu Problemen kommen, überprüfe die Berechtigungen in den App-Einstellungen:

- Gehe zu "Einstellungen" > "Apps" > "Sensoration"
- Überprüfe die Berechtigungen unter "Berechtigungen"
- Aktiviere die Berechtigungen für "Standort" und "Bluetooth"
- Aktiviere die Berechtigungen für "Nearby Devices" (Android 12+)

