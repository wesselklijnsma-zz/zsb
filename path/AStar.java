/* Changes:

- now uses TreeSet (sorted) to store paths. Sorting costs O(log(n)), our
  old method for taking the minimum item was O(n).
- now starts the search at the goal (backwards search).
- now doesn't check each path to see if a node was visited (went from O(n^2) to
  O(1)).

*/


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
    private int[][] costs = new int[8][8];

	public AStar(ChessBoard board, BoardLocation start, BoardLocation goal) {
		this.start = start;
		this.goal = goal;
        this.board = board;

        costs[goal.row][goal.column] = -1;
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
		PriorityQueue<Move> paths = new PriorityQueue<Move>();
        List<Move> newMoves = getMoves();
		paths.addAll(newMoves);
        
        if (paths.size() == 0) // no possible moves
            return null;

		Move winningMove = goalReached(newMoves);
		while (winningMove == null) {

			Move optimal = paths.poll();
                      
            if(optimal == null) // no path possible
                return null;

            newMoves = getMoves(optimal);
            paths.addAll(newMoves);

			winningMove = goalReached(newMoves);
		}

		return winningMove.getPath();
	}

	/**
	 * Returns a move that reaches the goal from a collection; or null if one doesn't exist.
	 * 
	 * @param moves
	 * The collection of moves to check.
	 */
	private Move goalReached(List<Move> moves) {
		for (Move m : moves) {
			if (m.loc.row == start.row && m.loc.column == start.column)
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
		return getMoves(new Move(null, goal, 0));
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
        {
            moves.add(new Move(previous, l, 1));
            costs[l.row][l.column] = previous.cost + 1;
        }
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
			if (inBounds(b) && !isOccupied(b) && costs[b.row][b.column] == 0) // not visited before
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
       if(loc.row == start.row && loc.column == start.column)
           return false;
       else
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
	private int getHeuristic(BoardLocation current) {
		return Math.abs(start.column - current.column) + Math.abs(start.row - current.row);
	}
	
	/** 
	 * Used to store moves and paths. Each move contains a reference to the move that
	 * led up to it.	
	 */
	private class Move implements Comparable<Move> {
		public int cost;
        
		private Move previous;
        private int heuristic;
		
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
		public Move(Move previous, BoardLocation loc, int cost)
		{
			this.previous = previous;
			this.loc = loc;
			
			// previous might be null
			int prevCost = previous != null
					? previous.cost
					: 0;
			this.cost = cost + prevCost;
            
            this.heuristic = getHeuristic(loc);
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
				locations.add(current.loc);
				current = current.previous;
			}
		    
            return locations;
		}
        
        public int compareTo(Move other)
        {
            // negative if our cost is lower, 0 if they're the same and positive
            // if the our cost is higher.
            return ((this.cost + this.heuristic) - (other.cost + other.heuristic));
        }
	}

}




