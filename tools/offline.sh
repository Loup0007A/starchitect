#!/bin/bash
# Vérification hors du jeu de l'analyse et des critiques (aucune bibliothèque de Minecraft nécessaire).
set -e
cd "$(dirname "$0")/.."
OUT=$(mktemp -d)
javac -d "$OUT" -encoding UTF-8 src/main/java/com/starchitect/analysis/*.java src/main/java/com/starchitect/review/*.java \
  src/main/java/com/starchitect/debug/TestBuilds.java tools/OfflineCheck.java
java -cp "$OUT" OfflineCheck "${1:-en}"
