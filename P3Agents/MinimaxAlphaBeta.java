package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;
    private static final Comparator<GameStateChild> COMPARATOR = (o1, o2) -> {
        if(o1.state.getUtility() > o2.state.getUtility()){
            return -1;
        } else if (o1.state.getUtility() < o2.state.getUtility()){
            return 1;
        } else {
            return 0;
        }
    };

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {
        
    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
        if(depth == 0 || node.state.isTerminated() == true) return node;
        double currentAlpha = alpha, currentBeta = beta;
        List<GameStateChild> orderedChildren = orderChildrenWithHeuristics(node.state.getChildren());
        GameStateChild bestMove = orderedChildren.get(0);
        double maxUlt = Double.NEGATIVE_INFINITY;
        for (GameStateChild current: orderedChildren){
            double backedUpValue = alphaBetaSearchMin(current, depth, currentAlpha, currentBeta);
            if(backedUpValue>maxUlt){
                bestMove = current;
                maxUlt = backedUpValue;
            }
        }
        return bestMove;
    }
    /**
     * This function will perform an alphaBetaSearch at the max node and return the backed up utility of the node
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The backed up utility of the node
     */
    public double alphaBetaSearchMax(GameStateChild node, int depth, double alpha, double beta){
        if(depth == 0 || node.state.isTerminated() == true) return node.state.getUtility();
        double currentAlpha = alpha, currentBeta = beta;
        double maxUlt = Double.NEGATIVE_INFINITY;
        List<GameStateChild> orderedChildren = orderChildrenWithHeuristics(node.state.getChildren());
        for (GameStateChild current: orderedChildren){
            double backedUpValue = alphaBetaSearchMin(current, depth, currentAlpha, currentBeta);
            if(backedUpValue>currentBeta)break;
            if(backedUpValue>maxUlt){
                currentAlpha = backedUpValue;
                maxUlt = backedUpValue;
            }
        }
        return maxUlt;
    }
    /**
     * This function will perform an alphaBetaSearch at the min node and return the backed up utility of the node
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The backed up utility of the node
     */
    public double alphaBetaSearchMin(GameStateChild node, int depth, double alpha, double beta){
        if(depth == 0 || node.state.isTerminated() == true) return node.state.getUtility();
        double currentAlpha = alpha, currentBeta = beta;
        double minUlt = Double.POSITIVE_INFINITY;
        List<GameStateChild> orderedChildren = orderChildrenWithHeuristics(node.state.getChildren());
        for (GameStateChild current: orderedChildren){
            double backedUpValue = alphaBetaSearchMax(current, depth-1, currentAlpha, currentBeta);
            if(backedUpValue<currentAlpha)break;
            if(backedUpValue<minUlt){
                currentBeta = backedUpValue;
                minUlt = backedUpValue;
            }
        }
        return minUlt;
    }
    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        return children.sort(COMPARATOR);
    }
}
