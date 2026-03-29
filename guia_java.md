# Guía de implementación Java — Qué verificar y qué puede faltar

## 1. CHECKLIST RÁPIDO — Ejecuta esto primero

```bash
make jar
java -jar programa.jar --help
```

Comprueba que aparecen TODOS estos flags en la ayuda:

| Flag          | ¿Existe? | Notas                                      |
|---------------|----------|--------------------------------------------|
| --grupos      | ✅/❌    |                                            |
| --centros     | ✅/❌    |                                            |
| --helicopteros| ✅/❌    |                                            |
| --semilla     | ✅/❌    |                                            |
| --inicial     | ✅/❌    | acepta "greedy" y "aleatorio"              |
| --heuristica  | ✅/❌    | acepta 1 y 2                               |
| --algoritmo   | ✅/❌    | acepta "hc" y "sa"                         |
| --operadores  | ✅/❌    | acepta "swap", "move", "swap+move"         |
| --steps       | ✅/❌    | Solo SA                                    |
| --stiter      | ✅/❌    | Solo SA — iteraciones por cambio de temp   |
| --k           | ✅/❌    | Solo SA                                    |
| --lambda      | ✅/❌    | Solo SA                                    |
| --peso        | ✅/❌    | Para exp7 — puede no existir todavía       |

---

## 2. FORMATO DE SALIDA REQUERIDO POR EL SCRIPT

El script lee la salida del programa buscando estas líneas exactas:

```
COSTE=12345.67
TIEMPO_MS=3421
```

Asegúrate de que tu `Main.java` imprime exactamente esto al final. Ejemplo:

```java
long startTime = System.currentTimeMillis();

// ... ejecutar algoritmo ...

long endTime = System.currentTimeMillis();
System.out.println("COSTE=" + problem.getValue(solution));
System.out.println("TIEMPO_MS=" + (endTime - startTime));
```

Si el exp7 mide también h1 por separado:
```java
System.out.println("COSTE_H1=" + heuristica1.getValue(solution));
```

---

## 3. HEURÍSTICA 2 — Cómo implementarla si no está

La heurística 2 añade al tiempo total la penalización por el tiempo hasta que
el último grupo de prioridad 1 es rescatado.

```java
// En tu clase HeuristicaRescate2.java (o similar):
@Override
public double getValue(Node node) {
    EstadoRescate estado = (EstadoRescate) node.getState();

    double tiempoTotal = calcularTiempoTotal(estado);       // igual que h1
    double tiempoUltimoPrio1 = calcularTiempoUltimoPrio1(estado);

    double peso = estado.getPesoH2();  // o recuperarlo de otra forma
    return tiempoTotal + peso * tiempoUltimoPrio1;
}

private double calcularTiempoUltimoPrio1(EstadoRescate estado) {
    double maxTiempo = 0;
    // Recorre todos los viajes de todos los helicópteros
    // Para cada grupo de prioridad 1, calcula el momento en que llega al centro
    // Devuelve el máximo de esos momentos
    for (Helicoptero h : estado.getHelicopteros()) {
        double tiempoAcumulado = 0;
        for (Viaje v : h.getViajes()) {
            tiempoAcumulado += v.getTiempoTotal();
            tiempoAcumulado += 10; // minutos de espera entre viajes
            for (Grupo g : v.getGrupos()) {
                if (g.getPrioridad() == 1) {
                    maxTiempo = Math.max(maxTiempo, tiempoAcumulado);
                }
            }
        }
    }
    return maxTiempo;
}
```

---

## 4. PARÁMETRO --peso — Cómo añadirlo si no existe

En tu `Main.java`, donde parseas los argumentos:

```java
// Añadir junto al resto de parámetros
case "--peso":
case "-p":
    peso = Double.parseDouble(args[++i]);
    break;
```

Luego pasar `peso` al constructor de tu heurística 2:
```java
HeuristicaRescate h = (heuristicaNum == 1)
    ? new HeuristicaRescate1()
    : new HeuristicaRescate2(peso);
```

---

## 5. GENERADOR DE SUCESORES — HC vs SA (importante para nota)

El enunciado exige implementar la generación de sucesores de forma diferente
para HC y SA. Si no lo tienes, aquí está el patrón:

### Para Hill Climbing → genera TODOS los sucesores
```java
// En tu clase SucesorHC.java
@Override
public List<Action> actions(Object state) {
    EstadoRescate e = (EstadoRescate) state;
    List<Action> acciones = new ArrayList<>();

    // Operador SWAP: intercambiar grupo i con grupo j entre viajes
    for (int i = 0; i < e.numGrupos(); i++) {
        for (int j = i + 1; j < e.numGrupos(); j++) {
            acciones.add(new SwapAction(i, j));
        }
    }

    // Operador MOVE: mover grupo i al viaje v
    for (int i = 0; i < e.numGrupos(); i++) {
        for (int v = 0; v < e.numViajes(); v++) {
            if (e.esMoveValido(i, v)) {
                acciones.add(new MoveAction(i, v));
            }
        }
    }

    return acciones;
}
```

### Para Simulated Annealing → genera UN sucesor aleatorio
```java
// En tu clase SucesorSA.java
@Override
public List<Action> actions(Object state) {
    EstadoRescate e = (EstadoRescate) state;
    Random rand = new Random();

    // Escoge operador al azar
    boolean usarSwap = rand.nextBoolean(); // o según el flag --operadores

    if (usarSwap) {
        int i = rand.nextInt(e.numGrupos());
        int j = rand.nextInt(e.numGrupos());
        return List.of(new SwapAction(i, j));
    } else {
        int i = rand.nextInt(e.numGrupos());
        int v = rand.nextInt(e.numViajes());
        return List.of(new MoveAction(i, v));
    }
}
```

---

## 6. SEMILLA ALEATORIA — Reproducibilidad

Para que los experimentos sean reproducibles, asegúrate de que pasas
la semilla a AMBOS generadores: el de grupos/centros Y el de operaciones
aleatorias del SA.

```java
Random rand = new Random(semilla);
Grupos grupos = new Grupos(numGrupos, semilla);
Centros centros = new Centros(numCentros, numHelicopteros, semilla);
// Y al crear el estado inicial aleatorio:
new EstadoRescate(grupos, centros, rand);
```

---

## 7. ORDEN DE IMPLEMENTACIÓN si algo falta

1. Verificar salida `COSTE=` y `TIEMPO_MS=` → **crítico para el script**
2. Verificar `--stiter` → si no existe, elimínalo del script (es opcional en AIMA)
3. Implementar heurística 2 → necesaria para exp7
4. Añadir `--peso` → necesario para exp7
5. Verificar HC vs SA en generación de sucesores → importante para la nota
