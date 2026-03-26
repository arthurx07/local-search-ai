package rescate;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {

    private static class ResultadoBusqueda {
        public long tiempoMs;
        public double costeInicial;
        public double costeFinal;

        public ResultadoBusqueda(long tiempoMs, double costeInicial, double costeFinal) {
            this.tiempoMs = tiempoMs;
            this.costeInicial = costeInicial;
            this.costeFinal = costeFinal;
        }
    }

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

        if (!inicial.equals("greedy") && !inicial.equals("aleatorio")) {
            System.err.println("Generador del estado inicial desconocido: " + inicial);
            throw new Exception("Generador del estado inicial desconocido");
        }
        
        // 3. PROBAR LA HEURÍSTICA DEL ESTADO INICIAL
        int tipoHeuristica = Integer.parseInt(params.getOrDefault("heuristica", "1"));
        double costeInicial;

        if (tipoHeuristica == 1) {
            costeInicial = new HeuristicFunction1().getHeuristicValue(board);

        } else {
            costeInicial = new HeuristicFunction2().getHeuristicValue(board);

            if (tipoHeuristica != 2) {
                System.err.println("Función Heurística desconocida: " + tipoHeuristica);
                throw new Exception("Función Heurística desconocida");
            }
        }

        // 5. DETERMINAR OPERADORES A USAR
        String operadoresStr = params.getOrDefault("operadores", "swap+move");
        List<String> operadores = Arrays.asList(operadoresStr.split("\\+"));

        List<String> operadoresPermitidos = Arrays.asList("move", "swap");
        if (!operadoresPermitidos.containsAll(operadores)) {
            throw new IllegalArgumentException("Los argumentos contienen operadores desconocidos: " + operadores + ". Los únicos que se permiten son: " + operadoresPermitidos);
        }

        // 4. EJECUTAR ALGORITMO
        String algoritmo = params.getOrDefault("algoritmo", "hc");

        ResultadoBusqueda r;
        if (algoritmo.equals("hc")) {
            // 1. Ejecutamos Hill Climbing
            r = DesastresHillClimbingSearch(board, costeInicial, operadores);

        } else if (algoritmo.equals("sa")) {
            // Recogemos todos los parámetros de SA
            int steps     = Integer.parseInt(params.getOrDefault("steps", "2000"));
            int stiter    = Integer.parseInt(params.getOrDefault("stiter", "100"));
            int k         = Integer.parseInt(params.getOrDefault("k", "5"));
            double lambda = Double.parseDouble(params.getOrDefault("lambda", "0.001"));

            // 2. Ejecutamos Simulated Annealing
            r = DesastresSimulatedAnnealingSearch(board, costeInicial, operadores, steps, stiter, k, lambda);

        } else {
            System.err.println("Algoritmo desconocido: " + algoritmo);
            throw new IllegalArgumentException("Algoritmo desconocido");
        }

        // 5. IMPRIMIR RESULTADOS
        DecimalFormat df = new DecimalFormat("#.00"); // solo dos decimales
        System.out.println("--------------------------------------------------");
        System.out.println("¡Búsqueda Finalizada!");
        System.out.println("Coste Inicial: " + df.format(r.costeInicial) + " minutos.");
        System.out.println("Coste Final:   " + df.format(r.costeFinal)   + " minutos.");
        double mejora = costeInicial - r.costeFinal;
        System.out.println("Mejora total:  " + df.format(mejora) + " minutos.");
        System.out.println("--------------------------------------------------");

        // Específico para experimentos
        System.out.println("COSTE=" + df.format(r.costeFinal));
        System.out.println("TIEMPO_MS=" + df.format(r.tiempoMs));
    }

    private static ResultadoBusqueda DesastresHillClimbingSearch(Board board, double costeInicial, List<String> operadores) {
        System.out.println("\n>>> Ejecutando HILL CLIMBING <<<");
        try {
            // TODO: Tener en cuenta Función Heurística (1 o 2)
            // Reiniciamos el rastreador de la heurística
            HeuristicFunction1.mejorCoste = costeInicial;
            
            Problem problem = new Problem(board, new SuccessorFunctionHC(operadores), new GoalTestFalse(), new HeuristicFunction1());
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

            return new ResultadoBusqueda(tiempoMs, costeInicial, HeuristicFunction1.mejorCoste);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Búsqueda fallida.");
        }
    }

    private static ResultadoBusqueda DesastresSimulatedAnnealingSearch(Board board, double costeInicial, List<String> operadores, int steps, int stiter, int k, double lambda) {
        System.out.println("\n>>> Ejecutando SIMULATED ANNEALING <<<");
        System.out.println("Steps: " + steps + " | Stiter: " + stiter + " | K: " + k + " | Lambda: " + lambda);
        try {
            // TODO: Tener en cuenta Función Heurística (1 o 2)
            // Reiniciamos el rastreador de la heurística
            HeuristicFunction1.mejorCoste = costeInicial;
            
            Problem problem = new Problem(board, new SuccessorFunctionSA(operadores), new GoalTestFalse(), new HeuristicFunction1());
            
            // Parámetros: pasos máximos, iteraciones por paso de temp, factor K, ratio de caída Lambda
            Search search = new SimulatedAnnealingSearch(steps, stiter, k, lambda);

            long inicio = System.nanoTime();
            SearchAgent agent = new SearchAgent(problem, search);
            long fin = System.nanoTime();
            long tiempoMs = (fin - inicio) / 1_000_000;
            
            System.out.println();
            for (Object action : agent.getActions()) {
                System.out.println(action.toString());
            }
            System.out.println(agent.getInstrumentation().toString());
            
            return new ResultadoBusqueda(tiempoMs, costeInicial, HeuristicFunction1.mejorCoste);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Búsqueda fallida.");
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
                    case 'l': map.put("helicopteros", args[i + 1]); break; // -l se queda para helicópteros
                    case 's': map.put("semilla", args[i + 1]); break;
                    case 'i': map.put("inicial", args[i + 1]); break;
                    case 'u': map.put("heuristica", args[i + 1]); break;
                    case 'a': map.put("algoritmo", args[i + 1]); break;
                    case 'o': map.put("operadores", args[i + 1]); break;
                    
                    // NUEVAS LETRAS PARA EVITAR CHOQUES
                    case 't': map.put("steps", args[i + 1]); break;      // -t (Total steps)
                    case 'e': map.put("stiter", args[i + 1]); break;     // -e (Evaluations/iterations)
                    case 'k': map.put("k", args[i + 1]); break;          // -k
                    case 'd': map.put("lambda", args[i + 1]); break;     // -d (Decay/Lambda)
                    
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
        System.out.println("  -e --stiter <n>                       Pasos por cada bajada de temp. (default 100)");
        System.out.println("  -k --k <n>                            Escala de temperatura (default 5)");
        System.out.println("  -d --lambda <n>                       Factor de enfriamiento (default 0.001)");
        System.out.println();
        System.out.println("  -h --help                             Muestra esta ayuda");
        System.out.println();
    }
}
