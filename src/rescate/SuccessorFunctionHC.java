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
                        
                        // Pasamos a ArrayList para que sea facil borrar e insertar
                        ArrayList<Integer> rutaH1 = new ArrayList<>();
                        for (int g : sucesor.rutas[h1]) rutaH1.add(g);
                        
                        ArrayList<Integer> rutaH2 = (h1 == h2) ? rutaH1 : new ArrayList<>();
                        if (h1 != h2) {
                            for (int g : sucesor.rutas[h2]) rutaH2.add(g);
                        }
                        
                        // Extraemos la pieza
                        int piezaExtraida = rutaH1.remove(p1);
                        
                        // Insertamos la pieza en la nueva posición
                        // Si era el mismo array (h1==h2), tenemos que ajustar el índice porque ha encogido al hacer el remove
                        int indexInsert = p2;
                        if (h1 == h2 && p1 < p2) {
                            indexInsert--;
                        }
                        rutaH2.add(indexInsert, piezaExtraida);
                        
                        // Convertir a arrays nativos
                        sucesor.rutas[h1] = new int[rutaH1.size()];
                        for (int i=0; i<rutaH1.size(); i++) sucesor.rutas[h1][i] = rutaH1.get(i);
                        
                        if (h1 != h2) {
                            sucesor.rutas[h2] = new int[rutaH2.size()];
                            for (int i=0; i<rutaH2.size(); i++) sucesor.rutas[h2][i] = rutaH2.get(i);
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
