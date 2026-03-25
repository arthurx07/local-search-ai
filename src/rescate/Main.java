package rescate;

import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
// import aima.search.informed.SimulatedAnnealingSearch;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== INICIANDO BÚSQUEDA LOCAL ===");
        
        // 1. INICIALIZAR EL ESCENARIO
        // TODO: Poner estos datos correctamente
        int numGrupos = 100;
        int numCentros = 5;
        int numHelicopteros = 1;
        int semilla = 1234;
        Board.inicializarDatosEstaticos(numGrupos, numCentros, numHelicopteros, semilla);
        
        // 2. CREAR ESTADO INICIAL (0 = Aleatorio, 1 = Greedy)
        Board board = new Board(1);
        
        // 3. PROBAR LA HEURÍSTICA DEL ESTADO INICIAL
        // TODO: Poder decidir entre heurísticas 1 y 2
        HeuristicFunction1 hf = new HeuristicFunction1();
        double costeInicial = hf.getHeuristicValue(board);
        System.out.println("Coste de la solución inicial Greedy: " + costeInicial + " minutos.");

        // 4. EJECUTAR ALGORITMO
        // TODO: Poder decidir entre HC y SA
        DesastresHillClimbingSearch(board, costeInicial);
    }

    private static void DesastresHillClimbingSearch(Board board, double costeInicial) {
        System.out.println("\n--- Ejecutando Hill Climbing ---");
        try {
            // Reiniciamos el rastreador por si hacemos varios experimentos
            HeuristicFunction1.mejorCoste = costeInicial;
            
            Problem problem = new Problem(board, new SuccessorFunctionHC(), new GoalTestFalse(), new HeuristicFunction1());
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);
            
            System.out.println();
            for (Object action : agent.getActions()) {
                System.out.println(action.toString());
            }
            System.out.println(agent.getInstrumentation().toString());
            
            // =======================================================
            // --- NUEVO CÓDIGO PARA IMPRIMIR EL COSTE FINAL ---
            // =======================================================

            System.out.println("--------------------------------------------------");
            System.out.println("¡Búsqueda Finalizada!");
            System.out.println("Coste Inicial: " + costeInicial + " minutos.");
            System.out.println("Coste Final:   " + HeuristicFunction1.mejorCoste + " minutos.");
            double mejora = costeInicial - HeuristicFunction1.mejorCoste;
            System.out.println("Mejora total:  " + mejora + " minutos.");
            System.out.println("--------------------------------------------------");
            // =======================================================

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
