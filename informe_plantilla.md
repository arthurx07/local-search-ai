# Práctica de Búsqueda Local — IA FIB 2025/2026 Q2
**Herman Daniel Berrio, Marc Pérez, Artur Leivar**

---

# PARTE DESCRIPTIVA

## 1.1 Identificación del problema

### 1.1.1 Descripción y planteamiento del problema

El problema nos sitúa en el escenario de una catástrofe natural donde el tiempo es un factor crítico. Se presenta un espacio físico rectangular de 50×50 km con distintos grupos de personas repartidas aleatoriamente, de diferente tamaño (entre 1 y 12 personas) y nivel de prioridad (grupos con heridos de prioridad 1, sin heridos de prioridad 2), que han quedado aislados y necesitan ser evacuados en helicóptero.

El objetivo es gestionar la logística de una flota de *H* helicópteros distribuidos en *C* centros de rescate situados en los bordes del área. Aunque encontrar *una* solución es inmediato (asignar grupos aleatoriamente), encontrar *la mejor* solución es computacionalmente inabordable por fuerza bruta. La distribución de los viajes debe garantizar la evacuación total respetando por cada viaje: un límite de capacidad (máximo 15 personas) y un límite operativo (máximo 3 grupos por vuelo).

### 1.1.2 Análisis detallado de sus características

- **Entorno observable y estático:** conocemos todos los datos desde el inicio. Las coordenadas, prioridades y tamaños de los grupos no cambian durante la búsqueda, y el resultado de cada movimiento es 100% predecible.
- **Relación con modelos clásicos:** el problema base es una variante del TSP (Travelling Salesman Problem). Con múltiples helicópteros se convierte en un mTSP y, al haber varios centros base, en un MDVRP (*Multi-Depot Vehicle Routing Problem*). Todos son problemas NP-Hard.
- **Micro-TSP interno:** dado que cada viaje puede recoger hasta 3 grupos, dentro de cada salida existe un sub-problema de permutación (TSP de 4 nodos: centro + 3 grupos) que se resuelve por fuerza bruta (6 permutaciones) para ordenar los grupos de forma óptima dentro del viaje.
- **Objetivo temporal, no métrico:** a diferencia del TSP clásico, no optimizamos distancia sino tiempo. Los helicópteros sufren penalizaciones temporales (tiempo de carga variable según prioridad, 10 minutos de repostaje). Por tanto, una solución puede recorrer más kilómetros pero ser temporalmente mejor.

### 1.1.3 Descripción detallada de los elementos del estado

Un **estado** es una asignación completa y válida de todos los grupos a los helicópteros. Sus elementos son:

- **Grupos (estáticos):** conjunto de *G* grupos, cada uno con coordenadas (x,y), número de personas y prioridad. Se comparten entre estados para eficiencia espacial.
- **Centros (estáticos):** conjunto de *C* centros con coordenadas y *H* helicópteros cada uno. También estáticos.
- **Asignación dinámica:** para cada helicóptero, una lista ordenada de viajes. Cada viaje contiene entre 1 y 3 grupos cuya capacidad conjunta no supera 15 personas.
- **Orden dentro del viaje:** los grupos de un viaje se ordenan para minimizar el tiempo de recorrido (mini-TSP resuelto por fuerza bruta).

**Invariante de validez:** todo estado tiene exactamente *G* grupos asignados, ningún viaje supera las restricciones de capacidad (≤15 personas, ≤3 grupos).

### 1.1.4 ¿Por qué es un problema de Búsqueda Local?

Dos factores determinantes justifican el uso de búsqueda local frente a búsqueda exhaustiva o constructiva:

1. **Irrelevancia del camino:** solo nos importa la solución final, no la secuencia de pasos para llegar a ella. La búsqueda local opera manteniendo únicamente el estado actual en memoria.
2. **Explosión combinatoria (NP-Hard):** el espacio de estados tiene tamaño O((H·C)^G). Para G=100 grupos y 5 helicópteros, esto es del orden de 5^100 ≈ 10^69 estados, inabordable por enumeración. Los algoritmos Hill Climbing y Simulated Annealing navegan este espacio eficientemente sin garantía de optimalidad global pero con un coste computacional viable.

---

## 1.2 Estado del problema y representación

### 1.2.1 Estructura de datos

La representación busca un equilibrio entre **eficiencia espacial** (el algoritmo genera miles de estados) y **eficiencia temporal** (calcular la heurística y aplicar operadores debe ser rápido).

**Partes estáticas (atributos `static`):** los grupos, los centros y las distancias entre todos los pares de puntos relevantes se almacenan una sola vez y se comparten entre todos los estados. Esto evita duplicar datos que no cambian y reduce drásticamente el uso de memoria.

**Parte dinámica:** cada estado almacena únicamente la asignación de grupos a viajes, representada como una estructura tridimensional:

```
asignacion[helicóptero][viaje][posición_en_viaje] → id_grupo
```

O equivalentemente una lista de listas de listas de enteros, donde cada entero es un identificador de grupo.

**Justificación:** usar identificadores enteros en lugar de referencias a objetos reduce el tamaño de cada estado y facilita la copia profunda (necesaria al generar sucesores sin modificar el estado padre).

### 1.2.2 Análisis del tamaño del espacio de búsqueda

El espacio de soluciones válidas crece exponencialmente con el número de grupos:

- Cada grupo puede ser asignado a cualquiera de los H×C helicópteros: **(H·C)^G** asignaciones brutas.
- Descontando las restricciones (≤15 personas, ≤3 grupos por viaje), el espacio real es menor pero sigue siendo superpolinomial.
- Para el escenario base (100 grupos, 5 centros, 1 helicóptero/centro): **5^100 ≈ 10^69** estados.

Este tamaño hace inviable cualquier estrategia exhaustiva y justifica plenamente el uso de búsqueda local.

---

## 1.3 Representación y análisis de los operadores

Se han implementado dos operadores sobre el estado:

### Operador SWAP — Intercambio de dos grupos entre viajes

**Descripción:** dados dos grupos G1 y G2 pertenecientes a viajes distintos (o al mismo), intercambia sus posiciones. Si el intercambio viola las restricciones de capacidad, el operador no es aplicable.

**Condiciones de aplicabilidad:**
- Los dos grupos deben existir y pertenecer a viajes distintos (o ser el mismo viaje con posiciones distintas).
- Tras el intercambio, ningún viaje debe superar 15 personas ni 3 grupos.

**Factor de ramificación:** O(G²), donde G es el número de grupos. Para G=100: ~4.950 posibles swaps.

**Efecto:** modifica la asignación sin cambiar el número total de viajes. Permite rebalancear la carga entre helicópteros.

### Operador MOVE — Mover un grupo a un viaje diferente

**Descripción:** mueve un grupo G desde su viaje actual a otro viaje V (de cualquier helicóptero). Si el viaje de origen queda vacío, se elimina. Si no existe ningún viaje destino válido, se puede crear uno nuevo.

**Condiciones de aplicabilidad:**
- El viaje destino tiene menos de 3 grupos y su capacidad + personas del grupo ≤ 15.
- O bien se crea un nuevo viaje vacío para el helicóptero destino.

**Factor de ramificación:** O(G × V), donde V es el número de viajes activos. En el peor caso O(G²).

**Efecto:** permite transferir grupos entre helicópteros y reorganizar las rutas globalmente.

### Justificación de la elección

Usar **swap+move** conjuntamente garantiza conectividad del espacio de búsqueda: desde cualquier solución válida se puede llegar a cualquier otra mediante una secuencia de swaps y moves. Usar solo swap impediría mover grupos entre helicópteros con diferente número de viajes. Usar solo move sería suficiente en teoría pero con mayor ramificación sin beneficio claro. Los resultados del Experimento 1 confirman empíricamente esta elección.

---

## 1.4 Análisis de la función heurística

### Heurística 1 — Suma total del tiempo de rescate

```
H1(estado) = Σ (tiempo de cada viaje de cada helicóptero)
```

Donde el tiempo de un viaje incluye:
- Tiempo de desplazamiento (distancia euclídea / 100 km·h⁻¹)
- Tiempo de recogida (1 min/persona, 2 min/persona para prioridad 1)
- Tiempo de repostaje entre viajes (10 minutos)

**Justificación:** es una medida directa del objetivo del problema. Minimizarla corresponde exactamente al criterio 1 del enunciado.

**Efectos en la búsqueda:** HC converge a un óptimo local que minimiza el tiempo total sin distinguir entre grupos de diferente prioridad.

### Heurística 2 — Suma total + penalización por tiempo de rescate de prioridad 1

```
H2(estado) = H1(estado) + w × max(tiempo_llegada_a_centro de grupos de prioridad 1)
```

Donde `w` es un peso que controla la importancia relativa del segundo criterio.

**Justificación:** introduce un incentivo para que los grupos con heridos sean rescatados antes. A mayor `w`, más se prioriza el tiempo de los grupos urgentes, potencialmente aumentando el tiempo total.

**Diferencia respecto a H1:** H1 trata todos los grupos por igual. H2 introduce un *trade-off*: es posible que una solución con mayor H1 tenga menor H2 (si rescata antes a los heridos). El Experimento 7 explora este trade-off variando `w`.

---

## 1.5 Elección y generación del estado inicial

Se han implementado dos estrategias:

### Estrategia Greedy

**Algoritmo:** se ordenan los grupos por prioridad (primero los de prioridad 1) y, dentro de cada prioridad, por distancia al centro más cercano. Se van asignando en orden al helicóptero con menor tiempo acumulado, formando viajes de hasta 3 grupos respetando la capacidad de 15 personas.

**Bondad:** produce soluciones iniciales de buena calidad, especialmente en el tratamiento de grupos prioritarios. El coste de calcularla es O(G log G) (por la ordenación).

**Ventaja:** proporcionar un punto de partida cercano al óptimo puede reducir el número de pasos hasta converger.

### Estrategia Aleatoria

**Algoritmo:** los grupos se asignan aleatoriamente a los helicópteros, respetando las restricciones de capacidad. Se forman viajes de forma aleatoria hasta que todos los grupos están asignados.

**Bondad:** produce soluciones de calidad generalmente inferior a la greedy. El coste es O(G).

**Ventaja:** explorar desde diferentes puntos del espacio de búsqueda puede evitar que el algoritmo quede atrapado siempre en el mismo óptimo local. Útil para el SA.

---

# PARTE EXPERIMENTAL

> **Metodología común a todos los experimentos:**
> - 10 repeticiones (réplicas) por experimento.
> - Dentro de cada réplica, todos los tratamientos comparten la misma semilla (fairness).
> - Entre réplicas, la semilla es diferente (exploración del espacio de búsqueda).
> - Se reportan media y desviación estándar del coste y del tiempo.
> - Se usan boxplots para visualizar la distribución.

---

## Experimento 1 — Determinar el mejor conjunto de operadores

**Hipótesis:** El conjunto `swap+move` producirá mejores soluciones que cada operador por separado, ya que permite una exploración más completa del espacio de búsqueda. Puede ser más lento por tener mayor factor de ramificación.

**Condiciones:** 100 grupos, 5 centros, 1 helicóptero/centro, inicialización greedy, Hill Climbing, heurística 1. Se prueban: `swap`, `move`, `swap+move`.

**Resultados:**

| Operadores  | Coste medio | Desv. estándar | Tiempo medio (ms) | Desv. estándar |
|-------------|-------------|----------------|-------------------|----------------|
| swap        |             |                |                   |                |
| move        |             |                |                   |                |
| swap+move   |             |                |                   |                |

*(Rellenar con los datos de `resultados/exp1/runs.csv`)*

**Gráfico:** *(Insertar boxplot de coste y tiempo por tipo de operador)*

**Conclusiones:**

Esperábamos que `swap+move` diese el mejor coste. [Confirmar/negar con los datos].

A partir de este experimento, fijamos los operadores a **[RESULTADO]** para el resto de experimentos.

---

## Experimento 2 — Determinar la mejor estrategia de inicialización

**Hipótesis:** La inicialización greedy producirá mejor coste final, ya que parte desde un punto más cercano al óptimo local. Sin embargo, podría explorar menos espacio que la inicialización aleatoria.

**Condiciones:** mismas que exp1, operadores fijados en exp1, se prueban: `greedy`, `aleatorio`.

**Resultados:**

| Inicial   | Coste medio | Desv. estándar | Tiempo medio (ms) | Desv. estándar |
|-----------|-------------|----------------|-------------------|----------------|
| greedy    |             |                |                   |                |
| aleatorio |             |                |                   |                |

**Gráfico:** *(Insertar boxplot)*

**Conclusiones:**

A partir de este experimento, fijamos la inicialización a **[RESULTADO]** para el resto de experimentos.

---

## Experimento 3 — Ajuste de parámetros del Simulated Annealing

**Hipótesis:** Existe una combinación (steps, k, λ) que supera al Hill Climbing. Valores grandes de k y pequeños de λ explorarán más antes de converger.

**Condiciones:** mismas que exp1-2 con SA. Se barren:
- steps ∈ {1000, 5000, 10000}
- k ∈ {1, 5, 10, 25}
- λ ∈ {0.001, 0.01, 0.1, 0.5}

**Resultados:** *(tabla o heatmap de coste medio por (k, λ) para cada steps)*

**Temperatura cero en iteración:** Para la combinación (k, λ), la probabilidad de aceptación P = e^(ΔE / k·e^(−λ·T)) se hace prácticamente 0 en T ≈ −ln(ε/k)/λ. Esto nos ayuda a determinar el número mínimo de steps necesario.

**Mejores parámetros encontrados:** steps=**X**, k=**Y**, λ=**Z**

**Comparación con HC:** *(¿mejora el SA al HC con los mejores parámetros?)*

---

## Experimento 4 — Escalado proporcional (proporción 5:100)

**Hipótesis:** El tiempo de ejecución crece más que linealmente con el tamaño del problema. El SA puede dar mejores soluciones que HC a mayor tamaño, pero a costa de más tiempo.

**Condiciones:** se escala de (5 centros, 100 grupos) hasta (40 centros, 800 grupos) de 5 en 5 / 100 en 100. Se comparan HC y SA con los parámetros fijados en exp1-3.

**Resultados:** *(gráfico de líneas: tiempo vs tamaño, uno para HC y otro para SA)*

**Conclusiones:**

---

## Experimento 5 — Escalado independiente de grupos y centros

**Hipótesis:** Aumentar grupos tendrá mayor impacto en el tiempo que aumentar centros, ya que el factor de ramificación de los operadores crece cuadráticamente con G pero linealmente con C.

**Condiciones:** solo HC. Dos escenarios:
- A: 5 centros fijos, grupos de 100 a 600 (incrementos de 50)
- B: 100 grupos fijos, centros de 5 a 55 (incrementos de 5)

**Resultados:** *(dos gráficas de líneas separadas, misma escala de tiempo)*

**Conclusiones:**

---

## Experimento 6 — Impacto del número de helicópteros

**Hipótesis antes del experimento:**
- Más helicópteros → mejor coste (más paralelización del rescate)
- Más helicópteros → más tiempo de búsqueda (más viajes que reorganizar = mayor espacio de estados)
- Aumentar helicópteros NO es equivalente a aumentar centros: helicópteros de un mismo centro comparten base (mismas distancias), mientras que nuevos centros tienen distancias diferentes

**Condiciones:** 100 grupos, 5 centros, HC, helicópteros de 1 a 10.

**Resultados:** *(gráfica coste vs helicópteros y tiempo vs helicópteros)*

**¿Se confirman las hipótesis?** *(rellenar tras experimento)*

---

## Experimento 7 — Segunda función heurística y ponderaciones

**Diferencia entre los dos criterios:**
- **H1** mide el tiempo total acumulado de todos los helicópteros, tratando todos los grupos por igual.
- **H2** añade una penalización proporcional al tiempo en que el último grupo de prioridad 1 llega a un centro. Esto introduce un sesgo hacia rescatar antes a los heridos, aunque aumente el tiempo total.

La clave conceptual: H2 introduce un *trade-off* entre urgencia y eficiencia global.

**Hipótesis:** al aumentar el peso w de H2, el tiempo hasta rescatar al último herido disminuirá, pero el tiempo total aumentará. Habrá un punto de rendimiento decreciente.

**Condiciones:** 100 grupos, 5 centros, 1 helicóptero, HC y SA. Pesos w ∈ {1, 2, 4, 8, 16}.

**Resultados:**

| Peso w | Algoritmo | Coste total (H1) medio | Tiempo último prio1 medio |
|--------|-----------|------------------------|---------------------------|
| 1      | hc        |                        |                           |
| 1      | sa        |                        |                           |
| 2      | hc        |                        |                           |
| ...    | ...       |                        |                           |

**Gráfico:** *(dos líneas: coste total y tiempo último prio1, en función del peso w)*

**Conclusiones:**

---

## Comparación global HC vs SA

| Criterio               | Hill Climbing | Simulated Annealing |
|------------------------|---------------|---------------------|
| Calidad de la solución |               |                     |
| Tiempo de ejecución    |               |                     |
| Sensibilidad al inicio |               |                     |
| Reproducibilidad       | Alta (determinista) | Baja (estocástico) |
| Escalabilidad          |               |                     |

**Conclusión general:** *(redactar tras tener todos los resultados)*

---

# CONCLUSIONES FINALES

*(Redactar tras completar todos los experimentos)*

---

# APÉNDICE — Instrucciones de ejecución

```bash
# Compilar y crear el JAR
make jar

# Ejecutar un experimento
./experimentos.sh 1

# Ejecutar todos (en orden)
./experimentos.sh 1 && ./experimentos.sh 2 && ./experimentos.sh 3
# (actualizar parámetros en el script según resultados)
./experimentos.sh 4 && ./experimentos.sh 5 && ./experimentos.sh 6 && ./experimentos.sh 7
```
