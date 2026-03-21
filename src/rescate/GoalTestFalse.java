package rescate;

import aima.search.framework.GoalTest;

// def. función determina si se ha llegado al estado final
public class GoalTestFalse implements GoalTest {

    public boolean isGoalState(Object state){
        return false;
    }

}
