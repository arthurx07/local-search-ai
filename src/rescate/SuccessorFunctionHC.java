package rescate;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.List;
import java.util.ArrayList;

// def. función generadora estados accesibles
public class SuccessorFunctionHC implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>(); 
        Board boardActual = (Board) state;

        // ==========================================
        // OPERADOR 1: SWAP (Intercambio)
        // ==========================================
        for (int h1 = 0; h1 < Board.numHelicopterosTotal; h1++) {
            if (boardActual.rutas[h1] == null) continue;
            
            for (int p1 = 0; p1 < boardActual.rutas[h1].length; p1++) {
                int grupoA = boardActual.rutas[h1][p1];
                if (grupoA == -1) continue; // No queremos intercambiar los viajes al centro

                // Recorremos el resto del tablero para coger el Grupo B
                // Empezamos desde h1 para no repetir intercambios (ej: A con B y B con A)
                for (int h2 = h1; h2 < Board.numHelicopterosTotal; h2++) {
                    if (boardActual.rutas[h2] == null) continue;
                    
                    int startP2 = (h1 == h2) ? p1 + 1 : 0;
                    
                    for (int p2 = startP2; p2 < boardActual.rutas[h2].length; p2++) {
                        int grupoB = boardActual.rutas[h2][p2];
                        if (grupoB == -1) continue; 

                        // 1. CLONAR ESTADO
                        Board sucesor = new Board(boardActual);

                        // 2. APLICAR OPERADOR: Intercambiar (Swap)
                        sucesor.rutas[h1][p1] = grupoB;
                        sucesor.rutas[h2][p2] = grupoA;

                        // Limpiamos por si se han movido cosas raras
                        sucesor.limpiarYReestructurar();

                        // 3. VALIDAR RESTRICCIONES
                        if (sucesor.esValido()) {
                            retval.add(new Successor("SWAP: G" + grupoA + " con G" + grupoB, sucesor));
                        }
                    }
                }
            }
        }

        // ==========================================
        // OPERADOR 2: MOVE (Mover a otro helicóptero)
        // ==========================================
        for (int h1 = 0; h1 < Board.numHelicopterosTotal; h1++) {
            if (boardActual.rutas[h1] == null) continue;
            for (int p1 = 0; p1 < boardActual.rutas[h1].length; p1++) {
                int grupoA = boardActual.rutas[h1][p1];
                if (grupoA == -1) continue;

                for (int h2 = 0; h2 < Board.numHelicopterosTotal; h2++) {
                    // Ahora permitimos h1 == h2 para que pueda mover el grupo a un NUEVO viaje dentro del mismo helicóptero
                    if (boardActual.rutas[h2] == null) continue;

                    // p2 puede llegar hasta el .length para insertarlo al final del todo (creando un viaje nuevo)
                    for (int p2 = 0; p2 <= boardActual.rutas[h2].length; p2++) {
                        // Evitar moverlo al mismo sitio donde estaba
                        if (h1 == h2 && (p1 == p2 || p1 == p2 - 1)) continue; 

                        Board sucesor = new Board(boardActual);
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
                        // ¡LA MAGIA! Limpiamos viajes vacíos y creamos los -1 que falten
                        sucesor.limpiarYReestructurar();
                        
                        if (sucesor.esValido()) {
                            retval.add(new Successor("MOVE: G" + grupoA + " a Heli " + h2, sucesor));
                        }
                    }
                }
            }
        }

        return retval;
    }
}
