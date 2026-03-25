package rescate;

import aima.search.framework.HeuristicFunction;

public class HeuristicFunction1 implements HeuristicFunction {
    
    // Nuestro hack para rastrear el mejor coste evaluado
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
                    // El helicóptero vuelve al centro
                    double dist = Board.distancias[nodoActual][centroId];
                    tiempoHeli += dist * 0.6; // A 100km/h, 1km = 0.6 minutos
                    tiempoHeli += 10.0;       // Tarda 10 mins en descargar/repostar
                    nodoActual = centroId;    // Actualizamos posición
                } else {
                    // El helicóptero va a rescatar a un grupo
                    int nodoDestino = Board.numCentros + grupoId; // Offset en la matriz
                    double dist = Board.distancias[nodoActual][nodoDestino];
                    tiempoHeli += dist * 0.6;
                    
                    // Tiempo de cargar a las personas
                    int personas = Board.personasPorGrupo[grupoId];
                    int prioridad = Board.prioridadGrupo[grupoId];
                    if (prioridad == 1) {
                        tiempoHeli += personas * 2.0; // Doble de tiempo por heridos
                    } else {
                        tiempoHeli += personas * 1.0; 
                    }
                    nodoActual = nodoDestino; // Actualizamos posición
                }
            }
            tiempoTotal += tiempoHeli;
        }
        
        // ¡Magia! Si este estado es el mejor que hemos visto, lo guardamos
        if (tiempoTotal < mejorCoste) {
            mejorCoste = tiempoTotal;
        }
        
        return tiempoTotal;
    }
}
