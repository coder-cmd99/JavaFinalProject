#!/usr/bin/env bash
# ============================================================
# Brew & Co. POS — macOS / Linux Build & Run Script
# ============================================================
# Requirements:
#   - JDK 17+  (java & javac must be on PATH)
#   - MySQL JDBC driver in ./lib/  (mysql-connector-j-*.jar)
#   - MySQL running with the schema loaded (run schema.sql first)
# ============================================================

set -e

SRC_ROOT="src/main/java"
OUT_DIR="out"
LIB_DIR="lib"
MAIN_CLASS="com.cafepos.Main"

# ── Find JDBC jar ─────────────────────────────────────────
JDBC_JAR=$(ls "$LIB_DIR"/mysql-connector-j-*.jar 2>/dev/null | head -n1)

if [[ -z "$JDBC_JAR" ]]; then
  echo ""
  echo " ERROR: No mysql-connector-j-*.jar found in ./lib/"
  echo " Download from: https://dev.mysql.com/downloads/connector/j/"
  echo " Place the JAR in the ./lib/ folder and re-run."
  echo ""
  exit 1
fi

echo "Using JDBC driver: $JDBC_JAR"

# ── Compile ───────────────────────────────────────────────
mkdir -p "$OUT_DIR"

echo "Compiling…"
find "$SRC_ROOT" -name "*.java" > sources.txt
javac -cp "$JDBC_JAR" -d "$OUT_DIR" @sources.txt
rm -f sources.txt

echo "Build successful!"
echo ""

# ── Run ───────────────────────────────────────────────────
echo "Starting Brew & Co. POS…"
java -cp "$OUT_DIR:$JDBC_JAR" "$MAIN_CLASS"
