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
    shift 8 # Quitamos los 8 primeros argumentos

    java -jar "$JAR" \
        --grupos "$grupos" \
        --centros "$centros" \
        --helicopteros "$helicopteros" \
        --semilla "$semilla" \
        --inicial "$inicial" \
        --heuristica "$heuristica" \
        --algoritmo "$algoritmo" \
        --operadores "$operadores" \
        "$@" # permitir recibir parámetros adicionales (para el caso de SA)
}

# ============================
# EXPERIMENTO 1
# ============================
exp1() {
    RESDIR="$BASE/exp1"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,operadores,tiempo_ms,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    HELICOPTEROS=1
    INICIAL="greedy"
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES_LIST=("swap" "move" "swap+move")

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO

        for OPS in "${OPERADORES_LIST[@]}"; do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPS")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
            TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

            echo "1,$RUN,$SEMILLA,$OPS,$TIEMPO,$COSTE" >> "$CSV"
            echo "RUN $RUN | OPS $OPS | TIEMPO $TIEMPO | COSTE $COSTE"
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

    echo "exp,run,semilla,inicial,tiempo_ms,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    HELICOPTEROS=1
    INICIALES=("greedy" "aleatorio")
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES="swap+move" # TODO: Quizás

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO

        for INI in "${INICIALES[@]}"; do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INI" "$HEURISTICA" "$ALGORITMO" "$OPERADORES")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
            TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

            echo "2,$RUN,$SEMILLA,$INI,$TIEMPO,$COSTE" >> "$CSV"
            echo "RUN $RUN | INI $INI | TIEMPO $TIEMPO | COSTE $COSTE"
        done
    done
}

# ============================
# EXPERIMENTO 3
# ============================
exp3() {
    RESDIR="$BASE/exp3"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,steps,stiter,k,lambda,tiempo,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    HELICOPTEROS=1
    INICIAL="aleatorio" # TODO: QUIZÁS ES MEJOR EL GREEDY. NO LO SABEMOS AÚN
    HEURISTICA=1
    ALGORITMO="sa"
    OPERADORES="swap+move" # TODO: Quizás

    STEPS_LIST=(1000 5000 10000) # Número de iteraciones totales TODO: DETERMINARLA
    STITER=100 # TODO: Determinar
    K_LIST=(1 5 10) # Escala de temperatura TODO: DETERMINARLOS
      # Afecta a qué tan alta es la temperatura al principio y cómo de “suave” es el enfriamiento.
    LAMBDA_LIST=(0.01 0.1 0.5 1.0) # Factor multiplicativo de enfriamiento TODO: DETERMINARLOS
      # Ajusta globalmente el nivel de temperatura (más bien un “afinador” de la curva).

    # stiter: desplazamiento del tiempo (normalmente 1) [Iteración inicial para el scheduler]

    # En el ejemplo AIMA de TSP dado, por defecto: steps = 1000, stiter = 1000, k = 5, lambda = 0.01
    # Y se hace, además, antes de llamar a SA: steps -= (steps % 1000);
        # Se fuerza que steps sea múltiplo de 1000: Porque el scheduler de AIMA usa un enfriamiento logarítmico, y la temperatura baja muy lentamente
        # AIMA está pensado para que el usuario trabaje con miles de iteraciones, no con números raros como 1234 o 1573

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO
        for STEPS in "${STEPS_LIST[@]}"; do
            # STEPS=$(( STEPS - (STEPS % 1000) ))
            for K in "${K_LIST[@]}"; do
                for LAMBDA in "${LAMBDA_LIST[@]}"; do
                    SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPERADORES" \
                                          --steps "$STEPS" --stiter "$STITER" --k "$K" --lambda "$LAMBDA")

                    COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
                    TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

                    echo "3,$RUN,$SEMILLA,$STEPS,$STITER,$K,$LAMBDA,$TIEMPO,$COSTE" >> "$CSV"
                    echo "RUN $RUN | STEPS $STEPS | STITER $STITER | K $K | LAMBDA $LAMBDA | TIEMPO $TIEMPO | COSTE $COSTE"
                done
            done
        done
    done
}

# ============================
# EXPERIMENTO 4
# ============================
exp4() {
    RESDIR="$BASE/exp4"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,algoritmo,grupos,centros,tiempo,coste" > "$CSV"

    GRUPOS_INI=100
    CENTROS_INI=5
    HELICOPTEROS=1
    INICIAL="aleatorio" # TODO: QUIZÁS ES MEJOR EL GREEDY. NO LO SABEMOS AÚN
    HEURISTICA=1
    ALGORITMOS=("hc" "sa")
    OPERADORES="swap+move" # TODO: Quizás modificar

    STEPS=2000 # TODO: aún por ver en experimento 3
    STITER=100 # TODO: aún por ver en experimento 3
    K=5 # TODO: aún por ver en experimento 3
    LAMBDA=0.01 # TODO: aún por ver en experimento 3

    # Número de escalados del problema (10 -> 1000 grupos, 50 centros)
    ESCALADOS=10 # TODO: Pensar cuántas iteraciones de esto hacer

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO

        GRUPOS=$GRUPOS_INI
        CENTROS=$CENTROS_INI

        for _ in $(seq 1 "$ESCALADOS"); do
            # SEMILLA=$RANDOM # TODO: Pensar si cambiar la semilla cuando se augmenta grupos y centros
            for ALG in "${ALGORITMOS[@]}"; do
                SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALG" "$OPERADORES" \
                                      --steps "$STEPS" --stiter "$STITER" --k "$K" --lambda "$LAMBDA")

                COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
                TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

                echo "4,$RUN,$SEMILLA,$ALG,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
                echo "RUN $RUN | GRUPOS $GRUPOS | CENTROS $CENTROS | TIEMPO $TIEMPO | COSTE $COSTE"
            done

            GRUPOS=$(( GRUPOS + GRUPOS_INI ))
            CENTROS=$(( CENTROS + CENTROS_INI ))
        done
    done
}



# ============================
# EXPERIMENTO 5
# ============================
exp5() {
    RESDIR="$BASE/exp5"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,grupos,centros,tiempo,coste" > "$CSV"

    GRUPOS_INI=100
    GRUPOS_AUGM=50
    CENTROS_INI=5
    CENTROS_AUGM=5
    HELICOPTEROS=1
    INICIAL="aleatorio" # TODO: QUIZÁS ES MEJOR EL GREEDY. NO LO SABEMOS AÚN
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES="swap+move" # TODO: Quizás modificar

    ITERACIONES=10 # TODO: Pensar cuántas iteraciones de esto hacer

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO

        GRUPOS=$GRUPOS_INI
        CENTROS=$CENTROS_INI

        echo "INCREMENTANDO GRUPOS DE 50 EN 50"
        for _ in $(seq 1 "$ITERACIONES"); do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPERADORES")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
            TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

            echo "5,$RUN,$SEMILLA,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "RUN $RUN | GRUPOS $GRUPOS | CENTROS $CENTROS | TIEMPO $TIEMPO | COSTE $COSTE"

            GRUPOS=$(( GRUPOS + GRUPOS_AUGM ))
        done

        GRUPOS=$GRUPOS_INI

        echo "INCREMENTANDO CENTROS DE 5 EN 5"
        for _ in $(seq 1 "$ITERACIONES"); do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPERADORES")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
            TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

            echo "5,$RUN,$SEMILLA,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "RUN $RUN | GRUPOS $GRUPOS | CENTROS $CENTROS | TIEMPO $TIEMPO | COSTE $COSTE"

            CENTROS=$(( CENTROS + CENTROS_AUGM ))
        done
    done
}

# ============================
# EXPERIMENTO 6
# ============================
exp6() {
    RESDIR="$BASE/exp6"
    mkdir -p "$RESDIR"
    CSV="$RESDIR/runs.csv"

    echo "exp,run,semilla,helicopteros,tiempo,coste" > "$CSV"

    GRUPOS=100
    CENTROS=5
    INICIAL="aleatorio" # TODO: QUIZÁS ES MEJOR EL GREEDY. NO LO SABEMOS AÚN
    HEURISTICA=1
    ALGORITMO="hc"
    OPERADORES="swap+move" # TODO: Quizás modificar

    ITERACIONES=10 # TODO: Pensar cuántas iteraciones de esto hacer

    for RUN in $(seq 1 "$REPS"); do
        SEMILLA=$RANDOM # TODO: PENSAR SI PONER LA SEMILLA QUE CAMBIE EN CADA EJECUCIÓN DEL EXPERIMENTO ES CORRECTO

        for HELICOPTEROS in $(seq 1 "$ITERACIONES"); do
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" "$SEMILLA" "$INICIAL" "$HEURISTICA" "$ALGORITMO" "$OPERADORES")

            COSTE=$(echo "$SALIDA" | grep "COSTE=" | cut -d= -f2)
            TIEMPO=$(echo "$SALIDA" | grep "TIEMPO_MS=" | cut -d= -f2)

            echo "6,$RUN,$SEMILLA,$HELICOPTEROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "RUN $RUN | HELICOPTEROS $HELICOPTEROS | TIEMPO $TIEMPO | COSTE $COSTE"
        done
    done
}

# ============================
# EXPERIMENTO 7
# ============================
# exp7() {
#   TODO
# }

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
