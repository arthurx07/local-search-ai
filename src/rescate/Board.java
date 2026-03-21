package rescate;

import java.util.ArrayList;
import java.util.Random;

// clase que representa al Estado
// It has to implement the state of the problem and its operators
public class Board {

    /// Número de grupos
    private int ngroups;

    /* Constructor */
    public Board(...) {

    }

    
    // ========================================================================
    // Generation of initial state
    // ========================================================================
    
    // Idea general, depende nombres y variables del Estado
    
    private int[][][] trivialInit() {
        int[][][] matriz = new int[ncentros][maxhelicopteros][ngrupos];

        for (int i = 0; i < ngrupos; i++) {
            int c = ngrupos % ncentros; // asignamos grupo a centro
            int h = ngrupos % nhelicopteros; // asignamos grupo a helicoptero
            
            matriz[c][h].put(i); // añadimos grupo i a centro c, helicóptero h
        }

        return matriz;
    }

    private int[][][] randomInit() {
        int[][][] matriz = new int[ncentros][maxhelicopteros][ngrupos];

        ArrayList<Integer> groupsToAssign = new ArrayList<Integer>();
        Random myRandom = new Random();

        for (int i = 0; i < ngrupos; i++) {
            groupsToAssign.add(i);
        }

        for (int i = 0; i < ncentros; i++) {
            for (int j = 0; i < numHelicopterosCentro[i]; j++) {
                int p = myRandom.nextInt(groupsToAssign.size());
                matriz[i][j].put(groupsToAssign.get(p)); // añadimos viaje grupo i
                groupsToAssign.remove(p);
            }
        }

        // ¿¿TODO: Juntar viajes de un mismo helicóptero hasta que ocupen 15??

        return matriz;
    }

    private int[][][] greedyInit() {
        int[][][] matriz = new int[ncentros][maxhelicopteros][ngrupos];

        int[] helicopteroParaGrupo = new int[ngrupos];

        // IDEA:
        // - para cada grupo, asignar el helicóptero más próximo

        for (int i = 0; i < ngrupos; i++) {
            helicopteroParaGrupo = nearestHelicopter(i);
        }

        // - cuando todos los grupos tienen el helicóptero asignado, ir llenando viajes

        // por cada helicóptero, de entre los grupos que tiene asignados, crear
        // llenar viaje hasta personas >15 o grupos >3.
    }

    // private int[] greedyPath(int nc) {
    //     int[] ipath = new int[nc];
    //     ArrayList<Integer> c = new ArrayList<Integer>();
    //
    //     for (int i = 0; i < nc; i++) {
    //         c.add(i);
    //     }
    //
    //     ipath[0] = c.get(0);
    //     c.remove(ipath[0]);
    //     for (int i = 1; i < nc - 1; i++) {
    //         Integer ci = nearestCity(ipath[i - 1], c);
    //         ipath[i] = ci;
    //         c.remove(ci);
    //
    //     }
    //     ipath[nc - 1] = c.get(0);
    //     return ipath;
    // }
    //
    // private Integer nearestCity(int c, ArrayList<Integer> lc) {
    //     int n = 0;
    //
    //     int min = 1000;
    //     for (int i = 0; i < lc.size(); i++) {
    //         if (lc.get(i) != c) {
    //
    //             if (min > dist[c][lc.get(i)]) {
    //                 min = dist[c][lc.get(i)];
    //                 n = lc.get(i);
    //             }
    //         }
    //     }
    //
    //     return (n);
    //
    // }

}
