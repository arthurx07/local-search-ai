#!/usr/bin/env bash

JAR="programa.jar"
BASE="resultados"
REPS=${REPS:-10} # por defecto 10 repeticiones

mkdir -p "$BASE"

# ============================
# Función auxiliar para ejecutar el programa
# ============================
run_program() {
    local grupos="$1"
    local centros="$2"
    local helicopteros="$3"
    local semilla="$4"
    local inicial="$5"
    local heuristica="$6"
    local algoritmo="$7"
    local operadores="$8"

    java -jar "$JAR" \
        --grupos "$grupos" \
        --centros "$centros" \
        --helicopteros "$helicopteros" \
        --semilla "$semilla" \
        --inicial "$inicial" \
        --heuristica "$heuristica" \
        --algoritmo "$algoritmo" \
        --operadores "$operadores"
}

# ============================
# EXPERIMENTO 1
# ============================
exp1() {
    RESDIR="$BASE/exp1"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,operadores,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    HELICOPTEROS=1
    SEMILLA=$RANDOM
    INICIAL="greedy"
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES_LIST=("swap" "move" "swap+move")

    for RUN in $(seq 1 "$REPS"); do
        for OPS in "${OPERADORES_LIST[@]}"; do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPS")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)

            echo "1,$RUN,$SEMILLA,$OPS,$COSTE" >> "$CSV"
            echo "RUN: $RUN, OPS: $OPS"
        done
    done
}

# ============================
# EXPERIMENTO 2
# ============================
exp2() {
    RESDIR="$BASE/exp2"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,inicial,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    HELICOPTEROS=1
    SEMILLA=$RANDOM
    INICIALES=("greedy" "aleatorio")
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES="swap+move"

    for RUN in $(seq 1 "$REPS"); do
        for EST in "${INICIALES[@]}"; do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$EST" "$HEURISTICA" "$ALGORITMO" "$OPERADORES")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)

            echo "2,$RUN,$SEMILLA,$EST,$COSTE" >> "$CSV"
        done
    done
}

# ============================
# EXPERIMENTO 3
# ============================
exp3() {
    OUT="$BASE/exp3"
    mkdir -p "$OUT"
    CSV="$OUT/runs.csv"

    echo "exp,run,semilla,T0,K,L,tiempo_ms,coste" > "$CSV"

    T0_LIST=(100 250 500)
    K_LIST=(10 20)
    L_LIST=(50 100)

    for T0 in "${T0_LIST[@]}"; do
        for K in "${K_LIST[@]}"; do
            for L in "${L_LIST[@]}"; do
                for RUN in $(seq 1 10); do
                    SEMILLA=$RANDOM

                    SALIDA=$(java -jar "$JAR" \
                        --estado greedy \
                        --algoritmo sa \
                        --grupos 100 \
                        --centros 5 \
                        --helicopteros 5 \
                        --heuristica 1 \
                        --semilla "$SEMILLA" \
                        --operadores swap+move \
                        --T0 "$T0" --K "$K" --L "$L")

                    COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
                    TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

                    echo "3,$RUN,$SEMILLA,$T0,$K,$L,$TIEMPO,$COSTE" >> "$CSV"
                done
            done
        done
    done
}

# resultados/
#   exp1/
#     config.txt
#     runs.csv
#   exp2/
#     config.txt
#     runs.csv
#   exp3/
#     config.txt
#     runs.csv
#   exp4/
#     hc.csv
#     sa.csv
#   exp5/
#     grupos.csv
#     centros.csv
#   exp6/
#     runs.csv
#   exp7/
#     pesos.csv

# ============================
# Dispatcher
# ============================

make jar

case "$1" in
    1) exp1 ;;
    2) exp2 ;;
    3) exp3 ;;
    4) exp4 ;;
    5) exp5 ;;
    6) exp6 ;;
    7) exp7 ;;
    all)
        exp1
        exp2
        exp3
        exp4
        exp5
        exp6
        exp7
        ;;
    *)
        echo "Uso: $0 {1|2|3|4|5|6|7|all}"
        ;;
esac
