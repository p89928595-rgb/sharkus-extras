# Sharkus Extras (Fabric, Minecraft 1.21.4)

Menü öffnen: **Rechte Shift-Taste** (in den Steuerungs-Einstellungen änderbar).

Features (alle einzeln an/aus): AppleSkin, Health Indicator, Armor Status,
Team Tracker, Wortfilter, Hit Color, Effekt-Timer.

## Bauen
Voraussetzung: Java 21 + Internet.

    gradle build          (oder in IntelliJ: Projekt öffnen -> Gradle -> build)

Die fertige Mod liegt danach in `build/libs/sharkus-extras-1.0.0.jar`.
In den `mods`-Ordner legen, zusammen mit Fabric Loader + Fabric API für 1.21.4.

## Ohne Installation bauen (GitHub)
1. Kostenloses Konto auf github.com, neues Repository erstellen.
2. Den entpackten Inhalt dieses Ordners hochladen (inkl. Ordner `.github`).
3. Reiter "Actions" -> "Build Mod" -> nach ca. 3 Minuten unter "Artifacts" die `sharkus-extras-jar` herunterladen.
4. Die entpackte `.jar` in den `mods`-Ordner legen (Fabric Loader + Fabric API für 1.21.4 nötig).
