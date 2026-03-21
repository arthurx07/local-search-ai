package rescate;

import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {

    public static void main(String[] args) throws Exception {
        Board board = new Board(...);
        DesastresHillClimbingSearch(board);
        DesastresSimulatedAnnealingSearch(board);
    }

    private static void DesastresHillClimbingSearch(Board board) {
        System.out.println("\nDesastres Hill Climbing -->");
        try {
            Problem problem =  new Problem(board, new SuccessorFunction(), new GoalTest(), new HeuristicFunction());
            Search search =  new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);
            
            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void DesastresSimulatedAnnealingSearch(Board board) {
        System.out.println("\nDesastres Simulated Annealing  -->");
        try {
            Problem problem =  new Problem(board, new SuccessorFunction(), new GoalTest(), new HeuristicFunction());
            SimulatedAnnealingSearch search =  new SimulatedAnnealingSearch(...); // ? (2000,100,5,0.001);
            // search.traceOn();
            SearchAgent agent = new SearchAgent(problem, search);
            
            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//     private void TSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TSPActionPerformed
// // TODO add your handling code here:
//         ProbTSPJFrame el = new ProbTSPJFrame();
//
//         el.setVisible(true);
//     }//GEN-LAST:event_TSPActionPerformed

}
