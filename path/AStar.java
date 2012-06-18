/*
 * Names       : Jouke van der Maas & Wessel Klijnsma
 * Student IDs : 10186883 and 10172432
 * Description : Part of path planning problem for a robot arm.
 * Date        : June 2012
 * Comments    : The structure of this file was provided by Nikos Massios
 *   and Matthijs Spaan.
 */
import java.util.*;
/**
 * Used to find a path between chess pieces on a board using the
 * A* algorithm.
 */
public class AStar {

	private BoardLocation start, goal;
    private ChessBoard board;

	public AStar(ChessBoard board, BoardLocation start, BoardLocation goal) {
		this.start = start;
		this.goal = goal;
        this.board = board;
	}

	/**
	 * Returns the actual path the robot arm
	 * has to take to navigate between chess pieces. Its result
	 * is in board locations, not actual coordinates.
	 * 
	 * Note: every Move object also stores all moves leading up to
	 * the current position. This makes handling of paths much easier.
     */
	public List<BoardLocation> getPath() {
		List<Move> paths = new ArrayList<Move>();
		paths.addAll(getMoves());

        if (paths.size() == 0) // no possible moves
            return null;

		Move winningMove = goalReached(paths);
		while (winningMove == null) {
			// getSearchIteration does the actual work; it decides which
			// path to expand.
			paths = getSearchIteration(paths);

            if(paths == null) // no path possible
                return null;

			winningMove = goalReached(paths);
		}

		return winningMove.getPath();
	}

	/**
	 * Expands the optimal path (g * h is minimal) into the
	 * current list of paths. It removes the move right before the path it's
	 * expanding, because it will be embedded in the follow-up moves (see the.
	 * Move class).
	 * 
	 * @param paths
	 * The end nodes of the paths that are currently expanded. Each node also
	 * contains a reference to the node leading up to it.
	 */
	private List<Move> getSearchIteration(List<Move> paths) {
		Move optimal = getOptimalMove(paths);
		paths.remove(optimal);

        // check for impossible paths
        List<Move> newMoves = getMoves(optimal);
        while (newMoves.size() == 0)
        {
            if(paths.size() == 0) // no more moves
                return null;

            optimal = getOptimalMove(paths);
            paths.remove(optimal);
            
            newMoves = getMoves(optimal);
        }

		paths.addAll(newMoves);
		return paths;
	}

	/**
	 * Decides on the optimal move given a collection of possible ones.
	 * It returns the move that minimizes g * h, where g is the cost up to the
	 * current point, and h is a heuristic function.
	 * 
	 * @param paths
	 * The moves to choose from.
	 */
	private Move getOptimalMove(List<Move> moves) {
		Move optimal = null;
		double minCost = Double.MAX_VALUE; // if any move's cost is higher than this, we have a problem anyway
		double cost = 0;

		for (Move m : moves) {
			cost = getAStarCost(m);
			if (cost < minCost) {
				minCost = cost;
				optimal = m;
			}
		}

		return optimal;
	}

	/**
	 * Returns the value of f = g * h from the A* formula (see comment
	 * at getOptimalMove)
	 *  
	 * @param move
	 * The move to calculate the value of f for.
	 */
	private double getAStarCost(Move move) {
		return move.getTotalCost() + getHeuristic(move.loc);
	}

	/**
	 * Returns a move that reaches the goal from a collection; or null if one doesn't exist.
	 * 
	 * @param moves
	 * The collection of moves to check.
	 */
	private Move goalReached(List<Move> moves) {
		for (Move m : moves) {
			if (m.loc.row == goal.row && m.loc.column == goal.column)
				return m;
		}
		return null;
	}

	/**
	 * Returns all possible moves from the starting position.
	 * 
	 * Note: the starting position is wrapped in a Move with cost 0 to simplify
	 * the implementation of these methods. This is always the first move; so the
	 * preceding move is null.
	 */
	private List<Move> getMoves() {
		return getMoves(new Move(null, start, 0));
	}

	/**
	 * Returns all possible moves given a position. The position is wrapped
	 * in a Move object to preserve the followed path.
	 * 
	 * @param previous
	 * The move leading up to the current location. It will contain the path
	 * up untill the current point.
	 */
	private List<Move> getMoves(Move previous) {
		List<BoardLocation> locs = getNewLocations(previous.loc);
		List<Move> moves = new ArrayList<Move>();

		for (BoardLocation l : locs)
            if(!previous.visitedBefore(l))
    			moves.add(new Move(previous, l, 1));

		return moves;
	}

	/**
	 * Returns reachable locations from the current one. It checks if each move is
	 * possible (square not occupied, within bounds, etc.) before returning.
	 * 
	 * Note: it is not checked if the new location has been visited before. This is
	 * the reason the algorithm doesn't return if there is no valid path; this function
	 * will keep returning the same couple of squares, leading into a loop. This is not
	 * an issue if there is a valid route, because the shortest route will never make a loop
	 * into itself. It could mean more paths are explored before the shortest one is found; 
	 * but this is not a big issue.
	 * 
	 * @param current
	 * The location we're moving away from.
	 */
	private List<BoardLocation> getNewLocations(BoardLocation current) {
		List<BoardLocation> locs = new ArrayList<BoardLocation>();
		BoardLocation[] possible = new BoardLocation[] {
				new BoardLocation(current.column + 1, current.row),
				new BoardLocation(current.column - 1, current.row),
				new BoardLocation(current.column, current.row + 1),
				new BoardLocation(current.column, current.row - 1) };

		for (BoardLocation b : possible) {
			if (inBounds(b) && !isOccupied(b))
				locs.add(b);
		}

		return locs;
	}

	/**
	 * Returns true if the specified location is within the bounds of the chess board,
	 * otherwise false.
	 * 
	 * @param loc
	 * The location to check.
	 */
	private boolean inBounds(BoardLocation loc)
	{
		return loc.column >= 0 && loc.column < 8 && loc.row >= 0 && loc.row < 8;
	}
	
	/**
	 * Returns true if the specified location contains a chess piece, otherwise false.
	 * 
	 * @param loc
	 * The location to check.
	 */
	private boolean isOccupied(BoardLocation loc) {
		return board.hasPiece(posToString(loc));
	}
    private String posToString(BoardLocation loc) {
         
        char[] letters =
            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        return Character.toString(letters[loc.column]) + Integer.toString(loc.row + 1);
    }

	/**
	 * Returns a heuristic for the distance between the current location and the goal.
	 * The value is simply the Manhattan distance between the two locations.
	 * 
	 * @param current
	 * The location to give the heuristic value for.
	 */
	private double getHeuristic(BoardLocation current) {
		return Math.abs(goal.column - current.column) + Math.abs(goal.row - current.row);
	}
	
	/** 
	 * Used to store moves and paths. Each move contains a reference to the move that
	 * led up to it.	
	 */
	private class Move {
		private double cost;
		private Move previous;
		
		public BoardLocation loc;
		
		/**
		 * Creates a new Move object.
		 * 
		 * Note: the value of cost is calculated up front, because it's potentially
		 * accessed often; once every cycle. Calculating the cost recursively is quite
		 * expensive, so it's better to do it only once. The cost of the individual move
		 * is never used, so it's not needed to store it.
		 * 
		 * @param previous
		 * The move that led up to the current one.
		 * 
		 * @param loc
		 * The location this new move leads to.
		 * 
		 * @param cost
		 * The cost of the move.
		 */
		public Move(Move previous, BoardLocation loc, double cost)
		{
			this.previous = previous;
			this.loc = loc;
			
			// previous might be null
			double prevCost = previous != null
					? previous.getTotalCost()
					: 0;
			this.cost = cost + prevCost;
		}
		
		public double getTotalCost()
		{
			return cost;
		}
		
		/**
		 * Returns the path from the first move in the tree until the current
		 * one, respectively.
		 * 
		 * Note: since the loop starts with the current Move, each new element
		 * needs to be added at the start of the list to get the order right.
		 */
		public List<BoardLocation> getPath()
		{
			List<BoardLocation> locations = new ArrayList<BoardLocation>();
			Move current = this;
			
			while(current != null) {
				locations.add(0, current.loc);
				current = current.previous;
			}
		    
            return locations;
		}
		
        public boolean visitedBefore(BoardLocation square)
        {
            return (loc.row == square.row && loc.column == square.column) 
                || (previous != null 
                        ? previous.visitedBefore(square) 
                        : false);
        }
	}

}




