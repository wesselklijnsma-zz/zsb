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
 * Used to find a path between chess pieces on a board using the A* algorithm.
 */
public class AStar
{

	private BoardLocation start, goal;
	private ChessBoard board;
	private int[][] costs = new int[8][8];

	/**
	 * Initializes the class.
	 * 
	 * @param board
	 *            The board setup to find a path on.
	 * 
	 * @param start
	 *            The start of the path. The algorithm uses backwards search; so
	 *            this is the goal position of the algorithm.
	 * 
	 * @param goal
	 *            The end of the path. Since the algorithm uses backwards search
	 *            this is where the search starts.
	 */
	public AStar( ChessBoard board, BoardLocation start, BoardLocation goal )
	{
		this.start = start;
		this.goal = goal;
		this.board = board;

		// should be 0; but then we'd have to change
		// everything else to -1. The algorithm checks if a value
		// is 0 to see if it has been there.
		costs[goal.row][goal.column] = -1;
	}

	/**
	 * Returns the actual path the robot arm has to take to navigate between
	 * chess pieces. Its result is in board locations, not actual coordinates.
	 * 
	 * Note: every Move object also stores all moves leading up to the current
	 * position. This makes handling of paths much easier. A PriorityQueue is
	 * used to sort the items based on the total cost of the path and a
	 * heuristic function. The sorting algorithm is optimized for a mostly
	 * sorted list; which is what we want. We'll only be adding a few items at a
	 * time and we want it to be fast.
	 */
	public List<BoardLocation> getPath()
	{
		PriorityQueue<Move> paths = new PriorityQueue<Move>();
		List<Move> newMoves = getMoves();
		paths.addAll( newMoves );

		if ( paths.size() == 0 ) // no possible moves
			return null;

		Move winningMove = goalReached( newMoves );
		while ( winningMove == null )
		{

			Move optimal = paths.poll();

			if ( optimal == null ) // no moves left
				return null;

			newMoves = getMoves( optimal );
			paths.addAll( newMoves );

			// we only need to check the new moves
			winningMove = goalReached( newMoves );
		}

		return winningMove.getPath();
	}

	/**
	 * Returns a move that reaches the goal from a collection; or null if one
	 * doesn't exist.
	 * 
	 * @param moves
	 *            The collection of moves to check.
	 */
	private Move goalReached( List<Move> moves )
	{
		for ( Move m : moves )
		{
			if ( m.loc.row == start.row && m.loc.column == start.column )
				return m;
		}
		return null;
	}

	/**
	 * Returns all possible moves from the starting position.
	 * 
	 * Note: the starting position is wrapped in a Move with cost 0 to simplify
	 * the implementation of these methods. This is always the first move; so
	 * the preceding move is null.
	 */
	private List<Move> getMoves()
	{
		return getMoves( new Move( null, goal, 0 ) );
	}

	/**
	 * Returns all possible moves given a position. The position is wrapped in a
	 * Move object to preserve the followed path.
	 * 
	 * Note: this function also updates the costs array for the newly found
	 * moves. If a cost was already specified, it will not be changed and the
	 * move will not be returned in the result.
	 * 
	 * @param previous
	 *            The move leading up to the current location. It will contain
	 *            the path up untill the current point.
	 */
	private List<Move> getMoves( Move previous )
	{
		List<BoardLocation> locs = getNewLocations( previous.loc );
		List<Move> moves = new ArrayList<Move>();

		for ( BoardLocation l : locs )
		{
			moves.add( new Move( previous, l, 1 ) );
			costs[l.row][l.column] = previous.cost + 1;
		}
		return moves;
	}

	/**
	 * Returns reachable locations from the current one. It checks if each move
	 * is possible. A move is possible if the new position is within bounds of
	 * the board, not occupied by any piece and if it has not been visited
	 * before.
	 * 
	 * Note: To see if a square has been visited before, its value in the costs
	 * array is checked. If the value is 0, it is assumed the square hasn't been
	 * visited. The square where the search is started is (incorrectly) set to
	 * cost -1 to prevent visiting it twice.
	 * 
	 * @param current
	 *            The location we're moving away from.
	 */
	private List<BoardLocation> getNewLocations( BoardLocation current )
	{
		List<BoardLocation> locs = new ArrayList<BoardLocation>();
		BoardLocation[] possible = new BoardLocation[] {
				new BoardLocation( current.column + 1, current.row ),
				new BoardLocation( current.column - 1, current.row ),
				new BoardLocation( current.column, current.row + 1 ),
				new BoardLocation( current.column, current.row - 1 ) };

		for ( BoardLocation b : possible )
		{
			if ( inBounds( b ) && !isOccupied( b )
					&& costs[b.row][b.column] == 0 ) // not
														// visited
														// before
				locs.add( b );
		}

		return locs;
	}

	/**
	 * Returns true if the specified location is within the bounds of the chess
	 * board, otherwise false.
	 * 
	 * @param loc
	 *            The location to check.
	 */
	private boolean inBounds( BoardLocation loc )
	{
		return loc.column >= 0 && loc.column < 8 && loc.row >= 0 && loc.row < 8;
	}

	/**
	 * Returns true if the specified location contains a chess piece, otherwise
	 * false.
	 * 
	 * Note: if the position is the goal position of the search (the start
	 * position of the returned path), there is always piece on it (since it's
	 * where we're coming from). This piece is ignored, because the goal would
	 * never be reached otherwise.
	 * 
	 * @param loc
	 *            The location to check.
	 */
	private boolean isOccupied( BoardLocation loc )
	{
		if ( loc.row == start.row && loc.column == start.column )
			return false;
		else
			return board.hasPiece( posToString( loc ) );

	}

	/**
	 * Needed to check if a square is occupied. The used api requires a string
	 * instead of a BoardPosition to its functions.
	 */
	private String posToString( BoardLocation loc )
	{

		char[] letters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
		return Character.toString( letters[loc.column] )
				+ Integer.toString( loc.row + 1 );
	}

	/**
	 * Returns a heuristic for the distance between the current location and the
	 * goal. The value is simply the Manhattan distance between the two
	 * locations.
	 * 
	 * Note: 'goal' here means goal for the algorithm, not the end position of
	 * the path.
	 * 
	 * @param current
	 *            The location to give the heuristic value for.
	 */
	private int getHeuristic( BoardLocation current )
	{
		return Math.abs( start.column - current.column )
				+ Math.abs( start.row - current.row );
	}

	/**
	 * Used to store moves and paths. Each move contains a reference to the move
	 * that led up to it.
	 * 
	 * Note: cost and heuristic are calculated once, when the move is created,
	 * so it doesn't have to be done a lot of times when they are used (and
	 * potentially cause performance issues).
	 * 
	 * Another Note: Move implements Comparable to allow PriorityQueue to sort
	 * based on the cost + heuristic value.
	 */
	private class Move implements Comparable<Move>
	{
		public int cost;

		private Move previous;
		private int heuristic;

		public BoardLocation loc;

		/**
		 * Creates a new Move object.
		 * 
		 * Note: the value of cost is calculated up front, because it's
		 * potentially accessed often; once every cycle. Calculating the cost
		 * recursively is quite expensive, so it's better to do it only once.
		 * The cost of the individual move is never used, so it's not needed to
		 * store it.
		 * 
		 * @param previous
		 *            The move that led up to the current one.
		 * 
		 * @param loc
		 *            The location this new move leads to.
		 * 
		 * @param cost
		 *            The cost of the move.
		 */
		public Move( Move previous, BoardLocation loc, int cost )
		{
			this.previous = previous;
			this.loc = loc;

			// previous might be null
			int prevCost = previous != null ? previous.cost : 0;
			this.cost = cost + prevCost;

			this.heuristic = getHeuristic( loc );
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

			while ( current != null )
			{
				locations.add( current.loc );
				current = current.previous;
			}

			return locations;
		}

		/**
		 * Returns a negative integer if the other cost + heuristic is higher, 0
		 * if they are equal and a positive integer if this cost + heuristic is
		 * higher according to the Comparable specification (see java
		 * documentation).
		 */
		public int compareTo( Move other )
		{
			return ( ( this.cost + this.heuristic ) - ( other.cost + other.heuristic ) );
		}
	}

}
