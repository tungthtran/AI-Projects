package edu.cwru.sepia.agent.planner;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;


import java.util.*;



/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
  * Note that SEPIA saves the townhall as a performingUnit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the performingUnit type from that performingUnit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 * 
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 * 
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 * 
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

    private int xExtent;
    private int yExtent;
    public State.StateView state;
    private int playernum;
    private int requiredGold;
    private int requiredWood;
    private int goldAmount;
    private int woodAmount;
    private boolean buildPeasants;
    private	List<SimUnit> townhalls = new ArrayList<>();
    private List<SimUnit> peasants = new ArrayList<>();
    private List<SimResource> woods = new ArrayList<>();
    private List<SimResource> golds = new ArrayList<>();
    private Map<Position, SimResource> resourcesPositions = new HashMap<>();
    private double cost;
    private Stack<StripsAction> actionsTillState;

    class MoveUnitFromBaseToMine implements StripsAction {
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit = peasants.get(0);
        private SimUnit townhall;
        private double cost;
        private SimResource closestGoldMine;
        
        public MoveUnitFromBaseToMine(int unitID, GameState state){
            this.unitID = unitID;
            for(SimUnit performingUnit : state.getPeasants()){
                if(unitID == performingUnit.getID()){
                    performingUnit = new SimUnit(performingUnit);
                    break;
                }
            }
            townhall = townhalls.get(0);
            double minDistance = Double.POSITIVE_INFINITY;
            for (SimResource goldMine : state.getGolds()){
                if (goldMine.getPosition().chebyshevDistance(townhall.getPosition()) < minDistance){
                    closestGoldMine = goldMine;
                    minDistance = goldMine.getPosition().chebyshevDistance(townhall.getPosition());
                }
            }
            cost = minDistance - 1;
        }

        public boolean preconditionsMet(GameState state){
            return performingUnit.getPosition().isAdjacent(townhall.getPosition());
        }

        public GameState apply(GameState current){
            GameState result = new GameState(current);
            List<Position> adjacentPositions = closestGoldMine.getPosition().getAdjacentPositions();
            for (SimUnit peasant : result.getPeasants()){
                if(peasant.getID() == unitID){
                    peasant.setPosition(closestGoldMine.getPosition());
                    for(Position adjacent : adjacentPositions){
                        if(adjacent.chebyshevDistance(closestGoldMine.getPosition())<peasant.getPosition().chebyshevDistance(closestGoldMine.getPosition())){
                            peasant.setPosition(adjacent);
                        }
                    }
                }
            }
            result.getActionsTillState().push(this);
            result.setCost(this.cost+current.getCost());
            return result;
        }
    
        public double getCost(){
            return cost;
        }

        public int getUnitId() {
            return unitID;
        }

        public String getType() {
            return "MoveToGold";
        }
        public SimUnit getPerformingUnit() {
            return performingUnit;
        }
        public SimUnit getTownhall() {
            return townhall;
        }
        public SimResource getGold() {
            return this.closestGoldMine;
        }
        public SimResource getWood() {
            return null;
        }

    }
    class MoveUnitFromBaseToWood implements StripsAction {
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit = peasants.get(0);
        private SimUnit townhall;
        private double cost;
        private SimResource closestWood;
        
        public MoveUnitFromBaseToWood(int unitID, GameState state){
            this.unitID = unitID;
            for(SimUnit performingUnit : state.getPeasants()){
                if(unitID == performingUnit.getID()){
                    performingUnit = new SimUnit(performingUnit);
                    break;
                }
            }
            townhall = townhalls.get(0);
            double minDistance = Double.POSITIVE_INFINITY;
            for (SimResource wood : state.getWoods()){
                if (wood.getPosition().chebyshevDistance(townhall.getPosition()) < minDistance){
                    closestWood = wood;
                    minDistance = wood.getPosition().chebyshevDistance(townhall.getPosition());
                }
            }
            cost = minDistance - 1;
        }
        public boolean preconditionsMet(GameState state){
            return !(performingUnit == null) && performingUnit.getPosition().isAdjacent(townhall.getPosition());
        }
        public GameState apply(GameState current){
            GameState result = new GameState(current);
            List<Position> adjacentPositions = closestWood.getPosition().getAdjacentPositions();
            for (SimUnit peasant : result.peasants){
                if(peasant.getID() == unitID){
                    peasant.setPosition(closestWood.getPosition());
                    for(Position adjacent : adjacentPositions){
                        if(adjacent.chebyshevDistance(closestWood.getPosition())<peasant.getPosition().chebyshevDistance(closestWood.getPosition())){
                            peasant.setPosition(adjacent);
                        }
                    }
                }
            }
            result.getActionsTillState().push(this);
            result.setCost(this.cost+current.getCost());
            return result;
        }

        public double getCost(){
            return cost;
        }

        public int getUnitId() {
            return unitID;
        }
        public String getType() {
            return "MoveToWood";
        }
        public SimUnit getPerformingUnit() {
            return performingUnit;
        }
        public SimUnit getTownhall() {
            return this.townhall;
        }
        public SimResource getGold() {
            return null;
        }
        public SimResource getWood() {
            return this.closestWood;
        }
    }
    class MoveUnitToBase implements StripsAction {
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit = peasants.get(0);
        private SimUnit townhall;
        private double cost;
        
        public MoveUnitToBase(int unitID, GameState state){
            this.unitID = unitID;
            for(SimUnit performingUnit : state.getPeasants()){
                if(unitID == performingUnit.getID()){
                    performingUnit = new SimUnit(performingUnit);
                    break;
                }
            }
            townhall = townhalls.get(0);
            cost = performingUnit.getPosition().chebyshevDistance(townhall.getPosition());
        }

        public boolean preconditionsMet(GameState state){
            return !performingUnit.getPosition().isAdjacent(townhall.getPosition());
        }

        public GameState apply(GameState current){
            List<Position> adjacentPositions = townhall.getPosition().getAdjacentPositions();
            GameState result = new GameState(current);
            for (SimUnit peasant : result.peasants){
                if(peasant.getID() == unitID){
                    peasant.setPosition(townhall.getPosition());
                    for(Position adjacent : adjacentPositions){
                        if(adjacent.chebyshevDistance(townhall.getPosition())<peasant.getPosition().chebyshevDistance(townhall.getPosition())){
                            peasant.setPosition(adjacent);
                        }
                    }
                }
            }
            result.getActionsTillState().push(this);
            result.setCost(this.cost+current.getCost());
            return result;
        }
        public double getCost(){
            return cost;
        }
        public int getUnitId() {
            return unitID;
        }
        public String getType() {
            return "MoveToBase";
        }
        public SimUnit getPerformingUnit() {
            return performingUnit;
        }
        public SimUnit getTownhall() {
            return this.townhall;
        }
        public SimResource getGold() {
            return null;
        }
        public SimResource getWood() {
            return null;
        }
    }

    class HarvestGold implements StripsAction{
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit;
        private SimResource gold;
        private double cost = 1;
    
        public HarvestGold(SimUnit performingUnit, SimResource gold) {
            this.performingUnit = performingUnit;
            this.gold = gold;
        }
    
        @Override
        public boolean preconditionsMet(GameState state) {
            return gold.getType().equals(Type.GOLD_MINE)
                    && performingUnit.getPosition().isAdjacent(gold.getPosition())
                    && performingUnit.getCargoAmount() == 0
                    && state.getGoldAmount() < state.getRequiredGold();
        }
    
        @Override
        public GameState apply(GameState current) {
            GameState newGameState = new GameState(current);
            List<SimResource> golds = new ArrayList<>(newGameState.getGolds());
            SimResource newGold = new SimResource(gold);
            
            // take 100 golds from the mine
            newGold.getCollectedFrom();
            golds.remove(gold);
            // if the gold mine still has gold left
            if(newGold.getAmountLeft() > 0) {
                golds.add(newGold);
            }

            List<SimUnit> units = new ArrayList<>(newGameState.getPeasants());
            SimUnit newUnit = new SimUnit(performingUnit);
            newUnit.setCargoAmount(100);
            newUnit.setCargoType(ResourceType.GOLD);
            units.remove(performingUnit);
            units.add(newUnit);
            newGameState.setPeasants(units);
            newGameState.setGolds(golds);
            newGameState.setCost(this.getCost() + current.getCost());
            newGameState.getActionsTillState().push(this);
            return newGameState;
        }

        @Override
        public double getCost() {
            return cost;
        }

        public String getType() {
            return "HarvestGold";
        }
        public SimUnit getPerformingUnit() {
            return performingUnit;
        }
        public SimUnit getTownhall() {
            return null;
        }
        public SimResource getGold() {
            return this.gold;
        }
        public SimResource getWood() {
            return null;
        }
        public int getUnitId(){
            return this.unitID;
        }
    }

    public class HarvestWood implements StripsAction{
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit;
        private SimResource tree;
        private double cost = 1;
    
        public HarvestWood(SimUnit performingUnit, SimResource tree) {
            this.performingUnit = performingUnit;
            this.tree = tree;
        }
    
        @Override
        public boolean preconditionsMet(GameState state) {
            return tree.getType().equals(Type.TREE)
                    && performingUnit.getPosition().isAdjacent(tree.getPosition())
                    && performingUnit.getCargoAmount() == 0
                    && state.getWoodAmount() < state.getRequiredWood();
        }
    
        @Override
        public GameState apply(GameState current) {
            GameState newGameState = new GameState(current);
            List<SimResource> woods = new ArrayList<>(newGameState.getWoods());
            SimResource newTree = new SimResource(tree);
            
            // take 100 wood from the tree
            newTree.getCollectedFrom();
            woods.remove(tree);
            // if the gold mine still has gold left
            if(newTree.getAmountLeft() > 0) {
                golds.add(newTree);
            }

            List<SimUnit> units = new ArrayList<>(newGameState.getPeasants());
            SimUnit newUnit = new SimUnit(performingUnit);
            newUnit.setCargoAmount(100);
            newUnit.setCargoType(ResourceType.WOOD);
            units.remove(performingUnit);
            units.add(newUnit);
            newGameState.setPeasants(units);
            newGameState.setWoods(woods);
            newGameState.setCost(this.getCost() + current.getCost());
            newGameState.getActionsTillState().push(this);
            return newGameState;
        }

        @Override
        public double getCost() {
            return cost;
        }
        public int getUnitId() {
            return unitID;
        }
        public String getType() {
            return "HarvestWood";
        }
        public SimUnit getPerformingUnit() {
            return performingUnit;
        }
        public SimUnit getTownhall() {
            return null;
        }
        public SimResource getGold() {
            return null;
        }
        public SimResource getWood() {
            return this.tree;
        }
    }

    class Deposit implements StripsAction {
        private int unitID = peasants.get(0).getID();
        private SimUnit performingUnit;
        private SimUnit townhall;
        private double cost = 1;

        public Deposit(SimUnit performingUnit, SimUnit townhall) {
            this.performingUnit = performingUnit;
            this.townhall = townhall;
        }

        @Override
        public boolean preconditionsMet(GameState state) {
            return performingUnit.getPosition().isAdjacent(townhall.getPosition()) && performingUnit.getCargoAmount() > 0;
        }

        @Override
        public GameState apply(GameState current) {
            GameState newGameState = new GameState(current);
            List<SimUnit> peasantUnits = new ArrayList<>(newGameState.getPeasants());
            // List<SimUnit> townhallUnits = new ArrayList<>(newGameState.getTownhalls());
            SimUnit newPeasant = new SimUnit(performingUnit);
            // SimUnit newTownhall = new SimUnit(townhall);

            //move cargo from peasant to townhall
            newPeasant.setCargoAmount(0);
            if(newPeasant.getCargoType().equals(ResourceType.GOLD)){
                newGameState.setGoldAmount(newGameState.getGoldAmount() + 100);
            }
            else if(newPeasant.getCargoType().equals(ResourceType.WOOD)){
                newGameState.setWoodAmount(newGameState.getWoodAmount() + 100);
            }

            peasantUnits.remove(performingUnit);
            peasantUnits.add(newPeasant);
            // townhallUnits.remove(townhalln);
            // townhallUnits.add(newTownhall);

            newGameState.setPeasants(peasantUnits);
            // newGameState.setTownhalls(townhallUnits);
            newGameState.setCost(this.getCost() + current.getCost());
            newGameState.getActionsTillState().push(this);
            return newGameState;
        }

        @Override
        public double getCost() {
            return cost;
        }

        public int getUnitId() {
            return unitID;
        }
        public String getType() {
            return "Deposit";
        }
        public SimUnit getPerformingUnit() {
            return this.performingUnit;
        }
        public SimUnit getTownhall() {
            return this.townhall;
        }
        public SimResource getGold() {
            return null;
        }
        public SimResource getWood() {
            return null;
        }
    }

    
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        // TODO: Implement me!
        this.state = state;
        this.xExtent = state.getXExtent();
        this.yExtent = state.getYExtent();
        this.playernum = playernum;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.buildPeasants = buildPeasants;

        state.getAllUnits().stream().forEach( performingUnit -> {
            if(performingUnit.getTemplateView().getName().toLowerCase().equals("townhall")) {
                townhalls.add( new SimUnit(performingUnit));
            }
            else if(performingUnit.getTemplateView().getName().toLowerCase().equals("peasant")) {
                peasants.add(new SimUnit(performingUnit));
            }
        });

        state.getAllResourceNodes().stream().forEach(node -> {
            if(node.getType() == Type.TREE) {
                woods.add(new SimResource(node, Type.TREE));
                resourcesPositions.put(new Position(node.getXPosition(), node.getYPosition()), new SimResource(node, Type.TREE));
            }
            else if(node.getType() == Type.GOLD_MINE) {
                golds.add(new SimResource(node, Type.GOLD_MINE));
                resourcesPositions.put(new Position(node.getXPosition(), node.getYPosition()), new SimResource(node, Type.GOLD_MINE));
            }
        });
        this.cost = 0.0;
        this.goldAmount = 0;
        this.woodAmount = 0;
        this.actionsTillState = new Stack<>();
    }

    public GameState(GameState gameState) {
        this.state = gameState.state;
        this.buildPeasants = gameState.buildPeasants;
        this.playernum = gameState.playernum;
        this.requiredGold = gameState.requiredGold;
        this.requiredWood = gameState.requiredWood;
        this.xExtent = gameState.xExtent;
        this.yExtent = gameState.yExtent;
        this.townhalls = gameState.townhalls;
        this.peasants = gameState.peasants;
        this.woods = gameState.woods;
        this.golds = gameState.golds;
        this.cost = gameState.cost;
        this.goldAmount = gameState.goldAmount;
        this.woodAmount = gameState.woodAmount;
        Stack<StripsAction> cloneActionsTillState = (Stack<StripsAction>)gameState.getActionsTillState().clone();
        //(Stack<StripsAction>)gameState.getActionsTillState().clone();
        this.actionsTillState = cloneActionsTillState;
    }
    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
        return this.woodAmount >= this.requiredWood && this.goldAmount >= this.requiredGold;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        List<GameState> children = new ArrayList<>();
        for(SimUnit peasant : getPeasants()) {
            StripsAction moveToWood = new MoveUnitFromBaseToWood(peasant.getID(), this);
            if (moveToWood.preconditionsMet(this)){
                children.add(moveToWood.apply(this));
            }
            StripsAction moveToMine = new MoveUnitFromBaseToMine(peasant.getID(), this);
            if (moveToMine.preconditionsMet(this)){
                children.add(moveToMine.apply(this));
            }
            StripsAction moveToBase = new MoveUnitToBase(peasant.getID(), this);
            if(moveToBase.preconditionsMet(this)){
                children.add(moveToBase.apply(this));
            }
            for(SimResource wood : woods) {
                StripsAction harvestWood = new HarvestWood(peasant, wood);
                if(harvestWood.preconditionsMet(this)) {
                    children.add(harvestWood.apply(this));
                }
            }
            for(SimResource gold : golds) {
                StripsAction harvestGold = new HarvestGold(peasant, gold);
                if(harvestGold.preconditionsMet(this)) {
                    children.add(harvestGold.apply(this));
                }
            }
            for(SimUnit townhall: townhalls) {
                StripsAction deposit = new Deposit(peasant, townhall);
                if (deposit.preconditionsMet(this)) {
                    children.add(deposit.apply(this));
                }
            }
        }
        return children;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        // TODO: Implement me!
        Position townhall = townhalls.get(0).getPosition();
        int distanceToClosestGoldMine = golds.get(0).getPosition().chebyshevDistance(townhall);
        int distanceToClosestWood = woods.get(0).getPosition().chebyshevDistance(townhall);
        for (SimResource mine : golds){
            distanceToClosestGoldMine = Math.max(mine.getPosition().chebyshevDistance(townhall),distanceToClosestGoldMine);
        }
        for (SimResource wood : woods){
            distanceToClosestGoldMine = Math.max(wood.getPosition().chebyshevDistance(townhall),distanceToClosestWood);
        }
        return 2*distanceToClosestGoldMine*(requiredGold - goldAmount) + 2*distanceToClosestWood*(requiredWood - woodAmount);
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
        
        if((this.getCost() + this.heuristic()) < (o.getCost() + o.heuristic())) {
            return -1;
        }
        else if((this.getCost() + this.heuristic()) > (o.getCost() + o.heuristic())) {
            return 1;
        }
        else 
            return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: Implement me!
        if(o instanceof GameState) {
            GameState state = (GameState)o;
            return this.getPeasants().equals(state.getPeasants())
                    && this.getGoldAmount() == state.getGoldAmount()
                    && this.getWoodAmount() == state.getWoodAmount()
                    && this.getRequiredGold() == state.getRequiredGold()
                    && this.getRequiredWood() == state.getRequiredWood()
                    && this.getWoods().size() == state.getWoods().size()
                    && this.getGolds().size() == state.getGolds().size();
        }
        else
            return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return Objects.hash(goldAmount, woodAmount, requiredGold, requiredWood, woods.size(), golds.size());
    }


    // list of getters and setters 

    public int getXExtent() {
        return this.xExtent;
    }

    public void setXExtent(int xExtent) {
        this.xExtent = xExtent;
    }

    public int getYExtent() {
        return yExtent;
    }

    public void setYExtent(int yExtent) {
        this.yExtent = yExtent;
    }

    public State.StateView getState() {
        return this.state;
    }

    public void setState(State.StateView state) {
        this.state = state;
    }

    public int getPlayernum() {
        return this.playernum;
    }

    public void setPlayernum(int playernum) {
        this.playernum = playernum;
    }

    public int getRequiredGold() {
        return this.requiredGold;
    }

    public void setRequiredGold(int requiredGold) {
        this.requiredGold = requiredGold;
    }

    public int getRequiredWood() {
        return this.requiredWood;
    }

    public void setRequiredWood(int requiredWood) {
        this.requiredWood = requiredWood;
    }

    public int getGoldAmount() {
        return this.goldAmount;
    }

    public void setGoldAmount(int goldAmount) {
        this.goldAmount = goldAmount;
    }

    public int getWoodAmount() {
        return this.woodAmount;
    }

    public void setWoodAmount(int woodAmount) {
        this.woodAmount = woodAmount;
    }

    public boolean isBuildPeasants() {
        return this.buildPeasants;
    }

    public void setBuildPeasants(boolean buildPeasants) {
        this.buildPeasants = buildPeasants;
    }

    public List<SimUnit> getTownhalls() {
        return this.townhalls;
    }

    public void setTownhalls(List<SimUnit> townhalls) {
        this.townhalls = townhalls;
    }

    public List<SimUnit> getPeasants() {
        return this.peasants;
    }

    public void setPeasants(List<SimUnit> peasants) {
        this.peasants = peasants;
    }

    public List<SimResource> getWoods() {
        return this.woods;
    }

    public void setWoods(List<SimResource> woods) {
        this.woods = woods;
    }

    public List<SimResource> getGolds() {
        return this.golds;
    }

    public void setGolds(List<SimResource> golds) {
        this.golds = golds;
    }

    public Map<Position, SimResource> getResourcesPositions() {
        return this.resourcesPositions;
    }

    public void setResourcesPositions(Map<Position, SimResource> resourcesPositions) {
        this.resourcesPositions = resourcesPositions;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Stack<StripsAction> getActionsTillState() {
        return actionsTillState;
    }

    public void setActionsTillState(Stack<StripsAction> actionsTillState) {
        this.actionsTillState = actionsTillState;
    }

    
}
