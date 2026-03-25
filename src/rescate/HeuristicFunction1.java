package rescate;

import aima.search.framework.HeuristicFunction;

public class HeuristicFunction1 implements HeuristicFunction {

    /*
    Esta función heurística se basa en **minimizar la suma de todos los tiempos
    empleados por los helicópteros en rescatar a todos los grupos.**
     */
    
    // Mejor coste evaluado (global, estático)
    public static double mejorCoste = Double.MAX_VALUE;

    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        double tiempoTotal = 0.0;
        
        // Iteramos sobre todos los helicópteros
        for (int h = 0; h < Board.numHelicopterosTotal; h++) {
            int[] ruta = board.rutas[h];
            if (ruta == null || ruta.length == 0) continue; // Si no sale, no suma tiempo
            
            double tiempoHeli = 0.0;
            int centroId = h / Board.numHelicopterosPorCentro; // Saber de qué centro salió
            int nodoActual = centroId; // Empieza geográficamente en el centro
            
            for (int i = 0; i < ruta.length; i++) {
                int grupoId = ruta[i];
                
                if (grupoId == -1) {
                    // Volver al centro: sumamos el viaje y los 10 mins SIEMPRE (sin IFs)
                    tiempoHeli += Board.tiempoViaje[nodoActual][centroId] + 10.0;
                    nodoActual = centroId;

                } else {
                    // Viajar al grupo y recogerlo usando los tiempos precalculados
                    int nodoDestino = Board.numCentros + grupoId;
                    tiempoHeli += Board.tiempoViaje[nodoActual][nodoDestino];
                    tiempoHeli += Board.tiempoRecogidaPorGrupo[grupoId];
                    nodoActual = nodoDestino;
                }
            }

            // Como siempre acaba en -1, le hemos sumado 10 mins extra al final de su jornada. Se los restamos.
            tiempoHeli -= 10.0;

            tiempoTotal += tiempoHeli;
        }

        if (tiempoTotal < mejorCoste) {
            mejorCoste = tiempoTotal;
        }

        return tiempoTotal;
    }
}
