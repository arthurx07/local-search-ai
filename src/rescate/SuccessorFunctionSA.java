package rescate;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

// def. función generadora estados accesibles
public class SuccessorFunctionSA implements SuccessorFunction {

    private Random random = new Random();

    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>();
        Board boardActual = (Board) state;

        // Bucle hasta encontrar UN sucesor aleatorio que sea válido
        while (retval.isEmpty()) {
            Board sucesor = new Board(boardActual);
            
            // 1. Elegimos un helicóptero al azar y un grupo al azar de su ruta
            int h1 = random.nextInt(Board.numHelicopterosTotal);
            if (sucesor.rutas[h1] == null || sucesor.rutas[h1].length == 0) continue;
            
            int p1 = random.nextInt(sucesor.rutas[h1].length);
            int grupoA = sucesor.rutas[h1][p1];
            if (grupoA == -1) continue; // Si hemos pillado un delimitador, repetimos

            // 2. Decidimos al azar qué operador aplicar (50% SWAP, 50% MOVE)
            boolean hacerSwap = random.nextBoolean();

            if (hacerSwap) {
                // --- OPERADOR SWAP ALEATORIO ---
                int h2 = random.nextInt(Board.numHelicopterosTotal);
                if (sucesor.rutas[h2] == null || sucesor.rutas[h2].length == 0) continue;
                
                int p2 = random.nextInt(sucesor.rutas[h2].length);
                int grupoB = sucesor.rutas[h2][p2];
                if (grupoB == -1) continue;

                sucesor.rutas[h1][p1] = grupoB;
                sucesor.rutas[h2][p2] = grupoA;
                
            } else {
                // --- OPERADOR MOVE ALEATORIO ---
                int h2 = random.nextInt(Board.numHelicopterosTotal);
                if (sucesor.rutas[h2] == null) continue;
                
                // p2 puede ir hasta .length para insertarse al final de todo
                int p2 = random.nextInt(sucesor.rutas[h2].length + 1);
                
                if (h1 == h2 && (p1 == p2 || p1 == p2 - 1)) continue; // Evitar mover al mismo sitio

                int piezaExtraida = sucesor.rutas[h1][p1];

                if (h1 != h2) {
                    int[] nuevaRutaH1 = new int[sucesor.rutas[h1].length - 1];
                    System.arraycopy(sucesor.rutas[h1], 0, nuevaRutaH1, 0, p1);
                    System.arraycopy(sucesor.rutas[h1], p1 + 1, nuevaRutaH1, p1, sucesor.rutas[h1].length - p1 - 1);
                    sucesor.rutas[h1] = nuevaRutaH1;

                    int[] nuevaRutaH2 = new int[sucesor.rutas[h2].length + 1];
                    System.arraycopy(sucesor.rutas[h2], 0, nuevaRutaH2, 0, p2);
                    nuevaRutaH2[p2] = piezaExtraida;
                    System.arraycopy(sucesor.rutas[h2], p2, nuevaRutaH2, p2 + 1, sucesor.rutas[h2].length - p2);
                    sucesor.rutas[h2] = nuevaRutaH2;
                } else {
                    int[] nuevaRuta = new int[sucesor.rutas[h1].length];
                    int idx = 0;
                    for (int i = 0; i < sucesor.rutas[h1].length; i++) {
                        if (i != p1) nuevaRuta[idx++] = sucesor.rutas[h1][i];
                    }
                    int insertIdx = (p1 < p2) ? p2 - 1 : p2;
                    System.arraycopy(nuevaRuta, insertIdx, nuevaRuta, insertIdx + 1, nuevaRuta.length - 1 - insertIdx);
                    nuevaRuta[insertIdx] = piezaExtraida;
                    sucesor.rutas[h1] = nuevaRuta;
                }
            }

            // 3. Limpiamos y validamos
            sucesor.limpiarYReestructurar();
            if (sucesor.esValido()) {
                String nombreOp = hacerSwap ? "SWAP Aleatorio" : "MOVE Aleatorio";
                retval.add(new Successor(nombreOp, sucesor));
            }
        }

        return retval;
    }
}
