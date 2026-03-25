package rescate;

import java.util.HashMap;
import java.util.Map;

import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {

    public static void main(String[] args) throws Exception {
        Map<String, String> params = parseArgs(args);

        if (params.containsKey("help")) {
            printHelp();
            return;
        }

        System.out.println("=== INICIANDO BÚSQUEDA LOCAL ===");
        
        // 1. INICIALIZAR EL ESCENARIO
        int numGrupos       = Integer.parseInt(params.getOrDefault("grupos", "100"));
        int numCentros      = Integer.parseInt(params.getOrDefault("centros", "5"));
        int numHelicopteros = Integer.parseInt(params.getOrDefault("helicopteros", "1"));
        int semilla         = Integer.parseInt(params.getOrDefault("semilla", "1234"));
        // semilla se usará para: distribución de grupos y centros; generación aleatoria del estado inicial
        Board.inicializarDatosEstaticos(numGrupos, numCentros, numHelicopteros, semilla);
        
        // 2. CREAR ESTADO INICIAL (0 = Aleatorio, 1 = Greedy)
        String inicial = params.getOrDefault("inicial", "greedy");
        int tipoEstado = inicial.equals("aleatorio") ? 0 : 1;
        Board board = new Board(tipoEstado);

        if (!inicial.equals("greedy") && !inicial.equals("aleatorio")) System.err.println("Generador de estado inicial desconocido: " + inicial);
        
        // 3. PROBAR LA HEURÍSTICA DEL ESTADO INICIAL
        int tipoHeuristica = Integer.parseInt(params.getOrDefault("heuristica", "1"));
        double costeInicial;

        if (tipoHeuristica == 1) {
            costeInicial = new HeuristicFunction1().getHeuristicValue(board);
        } else {
            costeInicial = new HeuristicFunction2().getHeuristicValue(board);
            if (tipoHeuristica != 2) System.err.println("Función Heurística desconocida: " + tipoHeuristica);
        }

        System.out.println("Coste de la solución inicial (" + tipoHeuristica + "):" + costeInicial + " minutos.");

        // 5. DETERMINAR OPERADORES A USAR
        String operadores = params.getOrDefault("operadores", "swap+move");
        // TODO: Seleccionar operadores (swap / move / swap + move)

        // 4. EJECUTAR ALGORITMO

        String algoritmo = params.getOrDefault("algoritmo", "hc");

        if (algoritmo.equals("hc")) {
        // 1. Ejecutamos Hill Climbing
            DesastresHillClimbingSearch(board, costeInicial);
        } else if (algoritmo.equals("sa")) {
        // 2. Ejecutamos Simulated Annealing
            int steps     = Integer.parseInt(params.getOrDefault("steps", "2000"));
            int k         = Integer.parseInt(params.getOrDefault("k", "5"));
            double lambda = Double.parseDouble(params.getOrDefault("lambda", "0.001"));
            // ¿stiter?
            // SimulatedAnnealingSearch search =  new SimulatedAnnealingSearch(2000,100,5,0.001);
            // SimulatedAnnealingSearch(int steps, int stiter, int k, double lamb)

            DesastresSimulatedAnnealingSearch(board, costeInicial);
        } else {
            System.err.println("Algoritmo desconocido: " + algoritmo);
        }
    }

    private static void DesastresHillClimbingSearch(Board board, double costeInicial) {
        System.out.println("\n\n>>> Ejecutando HILL CLIMBING <<<");
        try {
            // TODO: Tener en cuenta Función Heurística (1 o 2)
            // Reiniciamos el rastreador por si hacemos varios experimentos
            HeuristicFunction1.mejorCoste = costeInicial;
            
            Problem problem = new Problem(board, new SuccessorFunctionHC(), new GoalTestFalse(), new HeuristicFunction1());
            Search search = new HillClimbingSearch();

            long inicio = System.nanoTime();
            SearchAgent agent = new SearchAgent(problem, search);
            long fin = System.nanoTime();
            long tiempoMs = (fin - inicio) / 1_000_000;

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
            
            // PARA EXPERIMENTOS
            System.out.println("COSTE=" + String.format("%.2f", HeuristicFunction1.mejorCoste)); // solo dos decimales
            System.out.println("TIEMPO_MS=" + tiempoMs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void DesastresSimulatedAnnealingSearch(Board board, double costeInicial) {
        System.out.println("\n--- Ejecutando Simulated Annealing ---");
        try {
            // Reiniciamos el rastreador de la heurística
            HeuristicFunction1.mejorCoste = costeInicial;
            
            Problem problem = new Problem(board, new SuccessorFunctionSA(), new GoalTestFalse(), new HeuristicFunction1());
            
            // Parámetros: pasos máximos, iteraciones por paso de temp, factor K, ratio de caída Lambda
            Search search = new SimulatedAnnealingSearch(2000, 100, 5, 0.001);
            SearchAgent agent = new SearchAgent(problem, search);
            
            System.out.println(agent.getInstrumentation().toString());
            System.out.println("Coste Final SA: " + HeuristicFunction1.mejorCoste + " minutos.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================
    // PARSER DE ARGUMENTOS SIMPLE
    // ============================
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String a = args[i];

            if (a.equals("--help") || a.equals("-h")) {
                map.put("help", "true");
                continue;
            }

            // OPCIONES LARGAS: --grupos 100
            if (a.startsWith("--") && i + 1 < args.length) {
                map.put(a.substring(2), args[i + 1]);
                i++;
                continue;
            }

            if (a.startsWith("-") && a.length() == 2 && i + 1 < args.length) {
                char flag = a.charAt(1);
                switch (flag) {
                    case 'g': map.put("grupos", args[i + 1]); break;
                    case 'c': map.put("centros", args[i + 1]); break;
                    case 'l': map.put("helicopteros", args[i + 1]); break;
                    case 's': map.put("semilla", args[i + 1]); break;
                    case 'i': map.put("inicial", args[i + 1]); break;
                    case 'u': map.put("heuristica", args[i + 1]); break;
                    case 'a': map.put("algoritmo", args[i + 1]); break;
                    default:
                        System.err.println("Opción desconocida: -" + flag);
                }
                i++;
            }
        }
        return map;
    }

    // ============================
    // HELP
    // ============================
    private static void printHelp() {
        System.out.println("Uso:");
        System.out.println("  java -jar programa.jar [opciones]");
        System.out.println();
        System.out.println("Opciones:");
        System.out.println("  -g --grupos <n>                       Número de grupos (default 100)");
        System.out.println("  -c --centros <n>                      Número de centros (default 5)");
        System.out.println("  -l --helicopteros <n>                 Número de helicópteros (default 1)");
        System.out.println("  -s --semilla <n>                      Semilla aleatoria (default 1234)");
        System.out.println("  -i --inicial <greedy|aleatorio>       Generación del estado inicial (default greedy)");
        System.out.println("  -u --heuristica <1|2>                 Heurística a usar (default 1)");
        System.out.println("  -a --algoritmo <hc|sa>                Algoritmo de búsqueda (default hc)");
        System.out.println("  -o --operadores <swap|move|swap+move> Operadores (default swap+move)");
        System.out.println();
        System.out.println("Opciones específicas para Simulated Annealing:");
        System.out.println("  -t --steps <n>                        Número de iteraciones totales (default 2000)");
        System.out.println("  -k --k <n>                            Escala de temperatura (default 5)");
        System.out.println("  -l --lambda <n>                       Factor de enfriamiento (default 0.001)");
        System.out.println();
        System.out.println("  -h --help                             Muestra esta ayuda");
        System.out.println();
    }
}
