package rescate;

import aima.search.framework.HeuristicFunction;

public class HeuristicFunction2 implements HeuristicFunction {

    /*
    Esta función heurística se basa en **minimizar la suma de todos los tiempos
    empleados por los helicópteros en rescatar a todos los grupos (ídem que heurística 1)
    pero minimizando además el tiempo que se tarda en rescatar a todos los grupos de
    prioridad 1, es decir, minimizar también el tiempo desde el inicio del rescate
    hasta que el ultimo grupo de prioridad 1 llega a un centro de rescate.**
     */
    
    // Mejor coste evaluado (global, estático)
    public static double mejorCoste = Double.MAX_VALUE;

    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        
        double tiempoTotalSumado = 0.0;
        double tiempoMaximoLlegadaP1 = 0.0; 
        
        for (int h = 0; h < Board.numHelicopterosTotal; h++) {
            int[] ruta = board.rutas[h];
            if (ruta == null || ruta.length == 0) continue; 
            
            double tiempoHeliAcumulado = 0.0;
            int centroId = h / Board.numHelicopterosPorCentro; 
            int nodoActual = centroId; 
            
            boolean viajeActualTieneP1 = false;
            
            for (int i = 0; i < ruta.length; i++) {
                int grupoId = ruta[i];
                
                if (grupoId == -1) {
                    // El helicóptero vuelve al centro a descargar
                    tiempoHeliAcumulado += Board.tiempoViaje[nodoActual][centroId] + 10.0;       
                    nodoActual = centroId;    
                    
                    // Si traía un Prioridad 1 en este viaje, registramos a qué hora llegó
                    if (viajeActualTieneP1) {
                        if (tiempoHeliAcumulado > tiempoMaximoLlegadaP1) {
                            tiempoMaximoLlegadaP1 = tiempoHeliAcumulado;
                        }
                        viajeActualTieneP1 = false; // Reseteamos para el siguiente viaje
                    }
                    
                } else {
                    // El helicóptero vuela hacia un grupo
                    int nodoDestino = Board.numCentros + grupoId; 
                    tiempoHeliAcumulado += Board.tiempoViaje[nodoActual][nodoDestino];
                    tiempoHeliAcumulado += Board.tiempoRecogidaPorGrupo[grupoId];
                    nodoActual = nodoDestino; 
                    
                    // Comprobamos si este grupo recién recogido es crítico
                    if (Board.prioridadGrupo[grupoId] == 1) {
                        viajeActualTieneP1 = true;
                    }
                }
            }
            
            // Ajuste de fin de jornada (no contamos los últimos 10 min de descargar)
            tiempoTotalSumado += (tiempoHeliAcumulado - 10.0);
        }
        
        // PONDERACIÓN: Multiplicador para priorizar el rescate de P1
        double pesoPrioridad1 = 5.0; 
        
        double heuristicaFinal = tiempoTotalSumado + (tiempoMaximoLlegadaP1 * pesoPrioridad1);
        
        // Rastreador para los prints del Main
        if (heuristicaFinal < mejorCoste) {
            mejorCoste = heuristicaFinal;
        }
        
        return heuristicaFinal;
    }
}
