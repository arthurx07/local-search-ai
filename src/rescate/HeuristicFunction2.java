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
        // TODO: implementar función
        return 0.0;
    }
}
