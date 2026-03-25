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

                ArrayList<Integer> rutaH1 = new ArrayList<>();
                for (int g : sucesor.rutas[h1]) rutaH1.add(g);
                
                ArrayList<Integer> rutaH2 = (h1 == h2) ? rutaH1 : new ArrayList<>();
                if (h1 != h2) {
                    for (int g : sucesor.rutas[h2]) rutaH2.add(g);
                }
                
                int pieza = rutaH1.remove(p1);
                int indexInsert = p2;
                if (h1 == h2 && p1 < p2) indexInsert--;
                rutaH2.add(indexInsert, pieza);
                
                sucesor.rutas[h1] = new int[rutaH1.size()];
                for (int i=0; i<rutaH1.size(); i++) sucesor.rutas[h1][i] = rutaH1.get(i);
                
                if (h1 != h2) {
                    sucesor.rutas[h2] = new int[rutaH2.size()];
                    for (int i=0; i<rutaH2.size(); i++) sucesor.rutas[h2][i] = rutaH2.get(i);
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
