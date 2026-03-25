package rescate;

import IA.Desastres.Grupos;
import IA.Desastres.Centros;
import java.util.ArrayList;
import java.util.Random;

public class Board {

    // ==========================================
    // 1. DATOS ESTÁTICOS (El escenario)
    // ==========================================
    public static int numGrupos;
    public static int numCentros;
    public static int numHelicopterosPorCentro;
    public static int numHelicopterosTotal;
    public static int semilla;
    
    // Arrays para las características de los grupos
    public static int[] personasPorGrupo;
    public static int[] prioridadGrupo;
    
    // Matriz de distancias precalculada
    // Índice 0 a numCentros-1 son los Centros.
    // Índice numCentros a numCentros+numGrupos-1 son los Grupos.
    // Así, si quieres la distancia del Centro 0 al Grupo 5: distancias[0][numCentros + 5]
    public static double[][] distancias;

    // NUEVAS ESTRUCTURAS DE ALTO RENDIMIENTO
    public static int[] tiempoRecogidaPorGrupo;
    public static double[][] tiempoViaje; 

    // ==========================================
    // 2. INICIALIZACIÓN ESTÁTICA
    // ==========================================
    public static void inicializarDatosEstaticos(int nGrupos, int nCentros, int nHelicop, int seed) {
        numGrupos = nGrupos;
        numCentros = nCentros;
        numHelicopterosPorCentro = nHelicop;
        numHelicopterosTotal = nCentros * nHelicop;
        semilla = seed;
        
        personasPorGrupo = new int[numGrupos];
        prioridadGrupo = new int[numGrupos];
        tiempoRecogidaPorGrupo = new int[numGrupos];
        int totalNodos = numCentros + numGrupos;
        distancias = new double[totalNodos][totalNodos];
        tiempoViaje = new double[totalNodos][totalNodos];
        
        // Generar los objetos de la librería usando la semilla
        Grupos gruposIA = new Grupos(numGrupos, semilla);
        Centros centrosIA = new Centros(numCentros, numHelicopterosPorCentro, semilla);
        
        // 1. Rellenar el tiempo de recogida precalculado (Ahorra el IF en la heurística)
        for (int i = 0; i < numGrupos; i++) {
            personasPorGrupo[i] = gruposIA.get(i).getNPersonas();
            prioridadGrupo[i] = gruposIA.get(i).getPrioridad();
            
            // Calculamos el tiempo de rescate: 1 min por persona, o 2 mins si es prioridad 1
            if (prioridadGrupo[i] == 1) {
                tiempoRecogidaPorGrupo[i] = personasPorGrupo[i] * 2;
            } else {
                tiempoRecogidaPorGrupo[i] = personasPorGrupo[i] * 1;
            }
        }
        
        // 2. Precalcular la matriz de distancias
        // Primero, extraemos las coordenadas temporalmente
        int[] coordX = new int[totalNodos];
        int[] coordY = new int[totalNodos];
        
        // Coordenadas de los centros
        for (int i = 0; i < numCentros; i++) {
            coordX[i] = centrosIA.get(i).getCoordX();
            coordY[i] = centrosIA.get(i).getCoordY();
        }
        // Coordenadas de los grupos
        for (int i = 0; i < numGrupos; i++) {
            coordX[numCentros + i] = gruposIA.get(i).getCoordX();
            coordY[numCentros + i] = gruposIA.get(i).getCoordY();
        }
        
        // 2. Calcular distancias Y tiempos de viaje precalculados (Ahorra el * 0.6 en la heurística)
        // OPTIMIZACIÓN SIMÉTRICA: [i][j] = [j][i] (Ahorra el 50% de las raíces cuadradas)
        for (int i = 0; i < totalNodos; i++) {
            // La distancia de un nodo a sí mismo es 0
            distancias[i][i] = 0.0;
            tiempoViaje[i][i] = 0.0;
           
            // El bucle 'j' empieza en 'i + 1' para calcular solo la mitad superior de la matriz
            for (int j = i + 1; j < totalNodos; j++) {
                double dx = coordX[i] - coordX[j];
                double dy = coordY[i] - coordY[j];
              
                double dist = Math.sqrt(dx * dx + dy * dy);
                double tiempo = dist * 0.6; 
                
                // Asignamos el valor espejo instantáneamente
                distancias[i][j] = dist;
                distancias[j][i] = dist;
                
                tiempoViaje[i][j] = tiempo;
                tiempoViaje[j][i] = tiempo;
            }
        }
    }

    // ==========================================
    // 3. DATOS DINÁMICOS (El estado de la búsqueda)
    // ==========================================
    // rutas[h] contiene la secuencia de grupos rescatados por el helicóptero h.
    // Usaremos -1 como separador para indicar que el helicóptero vuelve al centro.
    // Ejemplo: rutas[0] = {5, 12, -1, 8, -1} (El helicóptero 0 rescata a 5 y 12, vuelve, luego rescata a 8 y vuelve).
    public int[][] rutas;

    // ==========================================
    // 4. CONSTRUCTORES
    // ==========================================
    
    // Constructor para generar el Estado Inicial
    public Board(int tipoInicializacion) {
        rutas = new int[numHelicopterosTotal][];
        
        if (tipoInicializacion == 0) {
            generarSolucionInicialAleatoria(); 
        } else if (tipoInicializacion == 1){
            generarSolucionInicialGreedy();
        }
    }

    // Constructor de copia para los sucesores
    public Board(Board antiguo) {
        rutas = new int[numHelicopterosTotal][];
        for (int i = 0; i < numHelicopterosTotal; i++) {
            // Clonamos cada array interno para que las modificaciones en un sucesor no afecten al padre
            if (antiguo.rutas[i] != null) {
                rutas[i] = antiguo.rutas[i].clone();
            }
        }
    }

    // ==========================================
    // 5. LÓGICA DE SOLUCIONES INICIALES
    // ==========================================

    // estrategia: por cada grupo:
    // - asignar aleatóriamente un helicóptero (de 1 a numHelicopterosTotal)
    // - si al asignarle, se debe crear un nuevo viaje (por las condiciones), se crea.
    private void generarSolucionInicialAleatoria() {
        ArrayList<ArrayList<Integer>> rutasTemp = new ArrayList<>();
        for (int i = 0; i < numHelicopterosTotal; i++) {
            rutasTemp.add(new ArrayList<>());
        }

        // Variables de control de estado actual por cada helicóptero
        int[] personasActuales = new int[numHelicopterosTotal];
        int[] gruposEnEsteViaje = new int[numHelicopterosTotal];

        Random rnd = new Random(semilla);
        // TODO: decidir qué semilla usar
/*
Objetivo	                                | Semilla recomendada
-------------------------------------------------------------------------------
Reproducibilidad (experimentos, informes)	| Semilla fija o pasada por parámetro
Aleatoriedad real	                        | System.currentTimeMillis()
Aleatoriedad fuerte	                      | SecureRandom
Aleatoriedad dependiente del problema	    | Objects.hash(...)
*/

        // Recorremos todos los grupos a rescatar
        for (int grupoId = 0; grupoId < numGrupos; grupoId++) {
            int personas = personasPorGrupo[grupoId];

            // 1. Elegir helicóptero aleatorio
            int heliId = rnd.nextInt(numHelicopterosTotal);

            // 2. Comprobar si cabe en el viaje actual
            boolean cabeEnViajeActual = (personasActuales[heliId] + personas <= 15) && (gruposEnEsteViaje[heliId] < 3);

            if (!cabeEnViajeActual) {
                // Si no cabe, cerrar viaje si había grupos
                if (gruposEnEsteViaje[heliId] > 0) {
                    rutasTemp.get(heliId).add(-1);
                }
                // Resetear contadores para el nuevo viaje del helicóptero
                personasActuales[heliId] = 0;
                gruposEnEsteViaje[heliId] = 0;
            }

            // 3. Asignar grupo al helicóptero
            rutasTemp.get(heliId).add(grupoId);
            personasActuales[heliId] += personas;
            gruposEnEsteViaje[heliId]++;
        }

        // 4. Cerrar viajes abiertos
        for (int i = 0; i < numHelicopterosTotal; i++) {
            ArrayList<Integer> rutaHeli = rutasTemp.get(i);
            if (!rutaHeli.isEmpty() && rutaHeli.get(rutaHeli.size() - 1) != -1) {
                rutaHeli.add(-1);
            }
        }

        // 5. Convertir a array final rutas[][]
        for (int i = 0; i < numHelicopterosTotal; i++) {
            ArrayList<Integer> rutaHeli = rutasTemp.get(i);
            rutas[i] = new int[rutaHeli.size()];
            for (int j = 0; j < rutaHeli.size(); j++) {
                rutas[i][j] = rutaHeli.get(j);
            }
        }
    }

    // Criterio para la solución greedy inicial:
    // - asignamos un grupo a un helicóptero de su centro más cercano
    // - el helicóptero al que le asignaremos todos los viajes de ese centro es al mismo (el primero del centro)
    private void generarSolucionInicialGreedy() {
        // Estructura temporal para ir construyendo las rutas.
        // ArrayList facilita añadir elementos dinámicamente antes de pasarlo al array final primitivo.
        ArrayList<ArrayList<Integer>> rutasTemp = new ArrayList<>();
        for (int i = 0; i < numHelicopterosTotal; i++) {
            rutasTemp.add(new ArrayList<Integer>());
        }

        // Variables de control de estado actual por cada helicóptero
        int[] personasActuales = new int[numHelicopterosTotal];
        int[] gruposEnEsteViaje = new int[numHelicopterosTotal];

        // Recorremos todos los grupos a rescatar
        for (int grupoId = 0; grupoId < numGrupos; grupoId++) {
            int personas = personasPorGrupo[grupoId];
            
            // 1. Encontrar el centro más cercano a este grupo
            int centroMasCercano = -1;
            double distanciaMinima = Double.MAX_VALUE;
            
            for (int c = 0; c < numCentros; c++) {
                double dist = distancias[c][numCentros + grupoId];
                if (dist < distanciaMinima) {
                    distanciaMinima = dist;
                    centroMasCercano = c;
                }
            }

            // 2. Escoger un helicóptero de ese centro.
            // Para simplificar la estrategia Greedy básica, se lo asignamos al primer helicóptero de ese centro.
            // (El helicóptero 0 del Centro 1 sería el ID global: 1 * numHelicopterosPorCentro + 0)
            int heliId = centroMasCercano * numHelicopterosPorCentro; 
            
            // 3. Comprobar restricciones de capacidad (Max 15 pers) y max salidas (Max 3 grupos)
            boolean cabeEnViajeActual = (personasActuales[heliId] + personas <= 15) && (gruposEnEsteViaje[heliId] < 3);

            if (!cabeEnViajeActual) {
                // Si no cabe o ya ha recogido 3 grupos, el helicóptero debe volver al centro
                if (gruposEnEsteViaje[heliId] > 0) { // Solo si llevaba a alguien
                    rutasTemp.get(heliId).add(-1); // -1 indica vuelta al centro
                }
                // Reseteamos contadores para el nuevo viaje
                personasActuales[heliId] = 0;
                gruposEnEsteViaje[heliId] = 0;
            }

            // 4. Asignar el grupo al helicóptero
            rutasTemp.get(heliId).add(grupoId);
            personasActuales[heliId] += personas;
            gruposEnEsteViaje[heliId]++;
        }

        // Cerramos los viajes (añadimos -1 al final si la ruta no está vacía y no acaba ya en -1)
        for (int i = 0; i < numHelicopterosTotal; i++) {
            ArrayList<Integer> rutaHeli = rutasTemp.get(i);
            if (!rutaHeli.isEmpty() && rutaHeli.get(rutaHeli.size() - 1) != -1) {
                rutaHeli.add(-1);
            }
        }

        // 5. Convertir la estructura temporal ArrayList a nuestro array int[][] final y eficiente
        for (int i = 0; i < numHelicopterosTotal; i++) {
            ArrayList<Integer> rutaHeli = rutasTemp.get(i);
            rutas[i] = new int[rutaHeli.size()];
            for (int j = 0; j < rutaHeli.size(); j++) {
                rutas[i][j] = rutaHeli.get(j);
            }
        }
    }

    // ==========================================
    // 6. LIMPIEZA Y REESTRUCTURACIÓN DE RUTAS
    // ==========================================
    public void limpiarYReestructurar() {
        for (int h = 0; h < numHelicopterosTotal; h++) {
            if (rutas[h] == null || rutas[h].length == 0) continue;

            ArrayList<ArrayList<Integer>> viajes = new ArrayList<>();
            ArrayList<Integer> viajeActual = new ArrayList<>();

            // 1. Extraemos los grupos limpios, agrupados por viaje
            for (int i = 0; i < rutas[h].length; i++) {
                int grupoId = rutas[h][i];
                if (grupoId == -1) {
                    if (!viajeActual.isEmpty()) {
                        viajes.add(viajeActual);
                        viajeActual = new ArrayList<>(); // Preparamos el siguiente viaje
                    }
                } else {
                    viajeActual.add(grupoId);
                }
            }
            // Por si el array terminaba con un grupo y le faltaba el -1
            if (!viajeActual.isEmpty()) {
                viajes.add(viajeActual);
            }

            // 2. Reconstruimos el array de primitivos perfecto
            int totalElementos = 0;
            for (ArrayList<Integer> v : viajes) {
                totalElementos += v.size() + 1; // +1 por el delimitador '-1'
            }

            int[] rutaLimpia = new int[totalElementos];
            int idx = 0;
            for (ArrayList<Integer> v : viajes) {
                for (Integer g : v) {
                    rutaLimpia[idx++] = g;
                }
                rutaLimpia[idx++] = -1; // Cierre de viaje garantizado
            }
            rutas[h] = rutaLimpia;
        }
    }


    // ==========================================
    // 7. VALIDADOR DE ESTADO
    // ==========================================
    // Comprobar que el estado actual es válido. Es decir, que el máximo de personas
    // recogidas por un helicóptero en un viaje es 15 y el máximo de grupos
    // recogidos por un helicóptero en un viaje es 3.
    public boolean esValido() {
        for (int h = 0; h < numHelicopterosTotal; h++) {
            if (rutas[h] == null) continue;
            
            int personasViaje = 0;
            int gruposViaje = 0;
            
            for (int i = 0; i < rutas[h].length; i++) {
                int grupoId = rutas[h][i];
                
                if (grupoId == -1) {
                    // Fin del viaje, reseteamos contadores
                    personasViaje = 0;
                    gruposViaje = 0;

                } else {
                    // Añadimos carga al viaje
                    personasViaje += personasPorGrupo[grupoId];
                    gruposViaje++;
                    
                    // Si nos pasamos de las reglas de la práctica, este estado es inválido
                    if (personasViaje > 15 || gruposViaje > 3) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


}
