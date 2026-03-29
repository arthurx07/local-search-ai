#!/usr/bin/env bash
# =============================================================================
# experimentos.sh — Práctica de Búsqueda Local (IA, FIB 2025/2026 Q2)
# =============================================================================
# USO:
#   ./experimentos.sh {1|2|3|4|5|6|7|all}
#
# ANTES DE LANZAR:
#   1. Lanza los experimentos en orden: 1 → 2 → 3 → luego edita MEJOR_INICIAL,
#      SA_STEPS, SA_K y SA_LAMBDA con los mejores valores hallados → 4 → 5 → 6 → 7
#   2. Comprueba que tu programa acepta --peso (para exp7). Si no, lee el
#      comentario en exp7() y adapta según tu implementación.
# =============================================================================

JAR="programa.jar"
BASE="resultados"
REPS=${REPS:-10}   # Número de repeticiones (se puede sobrescribir: REPS=5 ./experimentos.sh 1)

mkdir -p "$BASE"

# =============================================================================
# ⚙️  PARÁMETROS GLOBALES — ACTUALIZAR TRAS EXPERIMENTOS PREVIOS
# =============================================================================

# Actualizar tras Exp 1:
MEJOR_OPS="swap+move"       # Opciones: "swap" | "move" | "swap+move"

# Actualizar tras Exp 2:
MEJOR_INICIAL="greedy"      # Opciones: "greedy" | "aleatorio"

# Actualizar tras Exp 3 (busca la combinación con menor coste medio en resultados/exp3/runs.csv):
SA_STEPS=10000              # Número total de iteraciones SA
SA_STITER=100               # Iteraciones por cada cambio de temperatura
SA_K=5                      # Escala de temperatura
SA_LAMBDA=0.001             # Factor de enfriamiento

# =============================================================================
# Función auxiliar principal
# =============================================================================
run_program() {
    local grupos="$1"
    local centros="$2"
    local helicopteros="$3"
    local semilla="$4"
    local inicial="$5"
    local heuristica="$6"
    local algoritmo="$7"
    local operadores="$8"
    shift 8

    java -jar "$JAR" \
        --grupos       "$grupos"      \
        --centros      "$centros"     \
        --helicopteros "$helicopteros"\
        --semilla      "$semilla"     \
        --inicial      "$inicial"     \
        --heuristica   "$heuristica"  \
        --algoritmo    "$algoritmo"   \
        --operadores   "$operadores"  \
        "$@"
}

# Extrae el coste y tiempo de la salida del programa
parse_output() {
    local salida="$1"
    COSTE=$(echo  "$salida" | grep "COSTE="    | cut -d= -f2)
    TIEMPO=$(echo "$salida" | grep "TIEMPO_MS=" | cut -d= -f2)
    # Valores por defecto si algo falla (evita CSV roto)
    COSTE=${COSTE:-"ERROR"}
    TIEMPO=${TIEMPO:-"ERROR"}
}

# =============================================================================
# EXPERIMENTO 1 — Mejor conjunto de operadores
# =============================================================================
# Hipótesis: swap+move explorará mejor el espacio que cada operador por separado.
# Variables independientes : operadores (swap | move | swap+move)
# Variables fijas          : 100 grupos, 5 centros, 1 helicóptero, greedy, HC, h=1
# Variable dependiente     : coste final y tiempo de búsqueda
# Criterio de semilla      : misma semilla dentro de cada réplica (fairness entre ops)
# =============================================================================
exp1() {
    local RESDIR="$BASE/exp1"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,operadores,tiempo_ms,coste" > "$CSV"

    local GRUPOS=100  CENTROS=5  HELICOPTEROS=1
    local INICIAL="greedy"  HEURISTICA=1  ALGORITMO="hc"
    local OPERADORES_LIST=("swap" "move" "swap+move")

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM   # Misma semilla para los 3 operadores en este RUN

        for OPS in "${OPERADORES_LIST[@]}"; do
            local SALIDA
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" \
                                 "$SEMILLA" "$INICIAL" "$HEURISTICA"  \
                                 "$ALGORITMO" "$OPS")
            parse_output "$SALIDA"
            echo "1,$RUN,$SEMILLA,$OPS,$TIEMPO,$COSTE" >> "$CSV"
            echo "[EXP1] RUN=$RUN | OPS=$OPS | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
        done
    done
    echo "[X] Exp1 completado → $CSV"
    echo "   - Revisa el CSV y actualiza MEJOR_OPS al inicio del script."
}

# =============================================================================
# EXPERIMENTO 2 — Mejor estrategia de inicialización
# =============================================================================
# Hipótesis: greedy produce mejores soluciones iniciales → converge antes y mejor.
# Variables independientes : inicial (greedy | aleatorio)
# Variables fijas          : 100 grupos, 5 centros, 1 helicóptero, HC, h=1, MEJOR_OPS
# =============================================================================
exp2() {
    local RESDIR="$BASE/exp2"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,inicial,tiempo_ms,coste" > "$CSV"

    local GRUPOS=100  CENTROS=5  HELICOPTEROS=1
    local INICIALES=("greedy" "aleatorio")
    local HEURISTICA=1  ALGORITMO="hc"

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM

        for INI in "${INICIALES[@]}"; do
            local SALIDA
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS" \
                                 "$SEMILLA" "$INI" "$HEURISTICA"       \
                                 "$ALGORITMO" "$MEJOR_OPS")
            parse_output "$SALIDA"
            echo "2,$RUN,$SEMILLA,$INI,$TIEMPO,$COSTE" >> "$CSV"
            echo "[EXP2] RUN=$RUN | INI=$INI | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
        done
    done
    echo "[X] Exp2 completado → $CSV"
    echo "   - Revisa el CSV y actualiza MEJOR_INICIAL al inicio del script."
}

# =============================================================================
# EXPERIMENTO 3 — Ajuste de parámetros del Simulated Annealing
# =============================================================================
# Hipótesis: existe una combinación (steps, k, lambda) que supera al HC.
# Variables independientes : steps (1000|5000|10000), k (1|5|10|25), lambda (0.001|0.01|0.1|0.5)
# Variables fijas          : 100 grupos, 5 centros, 1 helicóptero, MEJOR_INICIAL, h=1, MEJOR_OPS
# Nota: stiter fijo a 100 (divisor de todos los steps usados)
# =============================================================================
exp3() {
    local RESDIR="$BASE/exp3"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,steps,stiter,k,lambda,tiempo_ms,coste" > "$CSV"

    local GRUPOS=100  CENTROS=5  HELICOPTEROS=1
    local HEURISTICA=1  ALGORITMO="sa"
    local STITER=100

    # Barrido de parámetros SA — cubre valores extremos y medios
    local STEPS_LIST=(1000 5000 10000)
    local K_LIST=(1 5 10 25)
    local LAMBDA_LIST=(0.001 0.01 0.1 0.5)

    local TOTAL=$(( ${#STEPS_LIST[@]} * ${#K_LIST[@]} * ${#LAMBDA_LIST[@]} * REPS ))
    local DONE=0
    echo "   (${TOTAL} ejecuciones totales — puede tardar varios minutos)"

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM

        for STEPS in "${STEPS_LIST[@]}"; do
            for K in "${K_LIST[@]}"; do
                for LAMBDA in "${LAMBDA_LIST[@]}"; do
                    local SALIDA
                    SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"          \
                                         "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"     \
                                         "$ALGORITMO" "$MEJOR_OPS"                     \
                                         --steps "$STEPS" --stiter "$STITER"           \
                                         --k "$K" --lambda "$LAMBDA")
                    parse_output "$SALIDA"
                    echo "3,$RUN,$SEMILLA,$STEPS,$STITER,$K,$LAMBDA,$TIEMPO,$COSTE" >> "$CSV"
                    DONE=$(( DONE + 1 ))
                    echo "[EXP3] ($DONE/$TOTAL) RUN=$RUN | STEPS=$STEPS | K=$K | λ=$LAMBDA | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
                done
            done
        done
    done
    echo "[X] Exp3 completado → $CSV"
    echo "   - Busca la fila con menor coste medio y actualiza SA_STEPS, SA_K, SA_LAMBDA."
}

# =============================================================================
# EXPERIMENTO 4 — Escalado proporcional (5:100) con HC y SA
# =============================================================================
# Hipótesis: el tiempo crece polinómicamente; SA puede ser más lento pero mejor.
# Variables independientes : tamaño del problema (centros×5, grupos×100), algoritmo
# Variables fijas          : proporción 5:100, 1 helicóptero, MEJOR_INICIAL, h=1, MEJOR_OPS
# =============================================================================
exp4() {
    local RESDIR="$BASE/exp4"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,algoritmo,grupos,centros,tiempo_ms,coste" > "$CSV"

    local GRUPOS_INI=100  CENTROS_INI=5  HELICOPTEROS=1
    local HEURISTICA=1
    local ALGORITMOS=("hc" "sa")
    local ESCALADOS=4 # 8   # 100→800 grupos, 5→40 centros

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM
        local GRUPOS=$GRUPOS_INI
        local CENTROS=$CENTROS_INI

        for _ in $(seq 1 "$ESCALADOS"); do
            for ALG in "${ALGORITMOS[@]}"; do
                local EXTRA_ARGS=()
                if [[ "$ALG" == "sa" ]]; then
                    EXTRA_ARGS=(--steps "$SA_STEPS" --stiter "$SA_STITER" --k "$SA_K" --lambda "$SA_LAMBDA")
                fi

                local SALIDA
                SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"         \
                                     "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"    \
                                     "$ALG" "$MEJOR_OPS" "${EXTRA_ARGS[@]}")
                parse_output "$SALIDA"
                echo "4,$RUN,$SEMILLA,$ALG,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
                echo "[EXP4] RUN=$RUN | ALG=$ALG | G=$GRUPOS C=$CENTROS | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
            done

            GRUPOS=$(( GRUPOS + GRUPOS_INI ))
            CENTROS=$(( CENTROS + CENTROS_INI ))
        done
    done
    echo "[X] Exp4 completado → $CSV"
}

# =============================================================================
# EXPERIMENTO 5 — Escalado independiente de grupos y centros (solo HC)
# =============================================================================
# Hipótesis: el tiempo crece más rápido al aumentar grupos que al aumentar centros.
# Variables independientes : número de grupos (100→600) o número de centros (5→55)
# Variables fijas          : HC, MEJOR_INICIAL, h=1, MEJOR_OPS, 1 helicóptero
# =============================================================================
exp5() {
    local RESDIR="$BASE/exp5"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,escenario,grupos,centros,tiempo_ms,coste" > "$CSV"

    local GRUPOS_INI=100   GRUPOS_AUGM=50
    local CENTROS_INI=5    CENTROS_AUGM=5
    local HELICOPTEROS=1   HEURISTICA=1   ALGORITMO="hc"
    local ITERACIONES=10

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM

        # Escenario A: grupos crecientes, centros fijos
        local GRUPOS=$GRUPOS_INI
        local CENTROS=$CENTROS_INI
        for _ in $(seq 1 "$ITERACIONES"); do
            local SALIDA
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"         \
                                 "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"    \
                                 "$ALGORITMO" "$MEJOR_OPS")
            parse_output "$SALIDA"
            echo "5,$RUN,$SEMILLA,grupos_crecientes,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "[EXP5-A] RUN=$RUN | G=$GRUPOS C=$CENTROS | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
            GRUPOS=$(( GRUPOS + GRUPOS_AUGM ))
        done

        # Escenario B: centros crecientes, grupos fijos
        GRUPOS=$GRUPOS_INI
        CENTROS=$CENTROS_INI
        for _ in $(seq 1 "$ITERACIONES"); do
            local SALIDA
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"         \
                                 "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"    \
                                 "$ALGORITMO" "$MEJOR_OPS")
            parse_output "$SALIDA"
            echo "5,$RUN,$SEMILLA,centros_crecientes,$GRUPOS,$CENTROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "[EXP5-B] RUN=$RUN | G=$GRUPOS C=$CENTROS | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
            CENTROS=$(( CENTROS + CENTROS_AUGM ))
        done
    done
    echo "[X] Exp5 completado → $CSV"
}

# =============================================================================
# EXPERIMENTO 6 — Impacto del número de helicópteros (HC)
# =============================================================================
# Hipótesis: más helicópteros → mejor coste, pero posiblemente más tiempo de búsqueda.
#            Diferente a aumentar centros: helicópteros comparten base → distancias iguales.
# Variables independientes : número de helicópteros (1→10)
# Variables fijas          : 100 grupos, 5 centros, HC, MEJOR_INICIAL, h=1, MEJOR_OPS
# =============================================================================
exp6() {
    local RESDIR="$BASE/exp6"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,helicopteros,tiempo_ms,coste" > "$CSV"

    local GRUPOS=100  CENTROS=5
    local HEURISTICA=1  ALGORITMO="hc"
    local MAX_HELIS=10

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM

        for HELICOPTEROS in $(seq 1 "$MAX_HELIS"); do
            local SALIDA
            SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"         \
                                 "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"    \
                                 "$ALGORITMO" "$MEJOR_OPS")
            parse_output "$SALIDA"
            echo "6,$RUN,$SEMILLA,$HELICOPTEROS,$TIEMPO,$COSTE" >> "$CSV"
            echo "[EXP6] RUN=$RUN | HELIS=$HELICOPTEROS | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
        done
    done
    echo "[X] Exp6 completado → $CSV"
}

# =============================================================================
# EXPERIMENTO 7 — Segunda función heurística y ponderaciones
# =============================================================================
# Hipótesis: añadir el criterio de prioridad 1 reduce su tiempo de rescate
#            a costa de aumentar el tiempo total; a mayor peso, más sacrificio.
#
# ⚠️  REQUIERE que tu programa acepte --peso <valor> para ponderar h2.
#     Si tu implementación usa --heuristica 2 directamente (sin peso), cambia
#     la variable PESO_LIST a (1) y elimina el argumento --peso de run_program.
#
# Variables independientes : algoritmo (hc|sa), peso de h2 (1|2|4|8|16)
# Variables fijas          : 100 grupos, 5 centros, 1 helicóptero,
#                            MEJOR_INICIAL, MEJOR_OPS, h=2
# =============================================================================
exp7() {
    local RESDIR="$BASE/exp7"
    mkdir -p "$RESDIR"
    local CSV="$RESDIR/runs.csv"
    echo "exp,run,semilla,algoritmo,peso_h2,tiempo_ms,coste" > "$CSV"

    local GRUPOS=100  CENTROS=5  HELICOPTEROS=1
    local HEURISTICA=2
    local ALGORITMOS=("hc" "sa")
    local PESO_LIST=(0 0.5 1 2 4 8 16 32)   # Doblamos el peso cada vez

    for RUN in $(seq 1 "$REPS"); do
        local SEMILLA=$RANDOM

        for ALG in "${ALGORITMOS[@]}"; do
            for PESO in "${PESO_LIST[@]}"; do
                local EXTRA_ARGS=(--weight "$PESO")
                if [[ "$ALG" == "sa" ]]; then
                    EXTRA_ARGS+=(--steps "$SA_STEPS" --stiter "$SA_STITER" --k "$SA_K" --lambda "$SA_LAMBDA")
                fi

                local SALIDA
                SALIDA=$(run_program "$GRUPOS" "$CENTROS" "$HELICOPTEROS"         \
                                     "$SEMILLA" "$MEJOR_INICIAL" "$HEURISTICA"    \
                                     "$ALG" "$MEJOR_OPS" "${EXTRA_ARGS[@]}")
                parse_output "$SALIDA"

                echo "7,$RUN,$SEMILLA,$ALG,$PESO,$TIEMPO,$COSTE" >> "$CSV"
                echo "[EXP7] RUN=$RUN | ALG=$ALG | PESO=$PESO | TIEMPO=$TIEMPO ms | COSTE=$COSTE"
            done
        done
    done
    echo "[X] Exp7 completado → $CSV"
}

# =============================================================================
# Dispatcher
# =============================================================================
make jar || { echo "¡Error al compilar! Comprueba el código Java."; exit 1; }

case "$1" in
    1)   exp1 ;;
    2)   exp2 ;;
    3)   exp3 ;;
    4)   exp4 ;;
    5)   exp5 ;;
    6)   exp6 ;;
    7)   exp7 ;;
    all)
        exp1; exp2; exp3; exp4; exp5; exp6; exp7
        ;;
    *)
        echo "Uso: $0 {1|2|3|4|5|6|7|all}"
        echo ""
        echo "ORDEN RECOMENDADO:"
        echo "  1. ./experimentos.sh 1        -> determina MEJOR_OPS"
        echo "  2. Actualiza MEJOR_OPS en este script"
        echo "  3. ./experimentos.sh 2        -> determina MEJOR_INICIAL"
        echo "  4. Actualiza MEJOR_INICIAL en este script"
        echo "  5. ./experimentos.sh 3        -> determina SA_STEPS, SA_K, SA_LAMBDA"
        echo "  6. Actualiza SA_* en este script"
        echo "  7. ./experimentos.sh {4|5|6|7}  (en cualquier orden)"
        ;;
esac
