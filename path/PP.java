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
 * Used to calculate the positions of the gripper of the robot necessary to move
 * a chesspiece on the board.
 * 
 */
public class PP
{
	private static double SAFE_HEIGHT = 200;
	private static double LOW_HEIGHT = 40;
	private static double LOWPATH_HEIGHT = 20;
	private static double OPEN_GRIP = 30;
	private static double CLOSED_GRIP = 0;

	private static Point location;
	private static Vector<GripperPosition> p = new Vector<GripperPosition>();
	private static double height, grip;
	private static ChessBoard b;
	private static int blackTaken;

	/**
	 * Writes the positions for the specified move to a file called
	 * positions.txt in the current folder.
	 * 
	 * @param args
	 *            The first (0) element should contain a move in the form e3e4.
	 *            If any more elements are provided, they should be element 1,
	 *            2, and 3, containing the x, y and theta that represent the
	 *            position of the board.
	 */
	public static void main( String[] args )
			throws ChessBoard.NoPieceAtPositionException
	{
		String computerFrom, computerTo;

		System.out.println( "**** THIS IS THE STUDENT PP MODULE IN JAVA" );
		System.out.println( "**** The computer move was " + args[0] );

		/* Read possibly changed board position */
		if ( args.length > 1 )
		{
			double x = Double.parseDouble( args[1] ), y = Double
					.parseDouble( args[2] ), theta = Double
					.parseDouble( args[3] );
			Point boardPosition = new Point( x, y, 0 );

			System.out.println( "**** Chessboard is at (x,y,z,theta): (" + x
					+ ", " + y + ", 0, " + theta + ")" );

			b = new ChessBoard( boardPosition, theta );
		} else
			b = new ChessBoard();

		/* Read the board state */
		b.read();
		/* print the board state */
		System.out.println( "**** The board before the move was:" );
		b.print();

		computerFrom = args[0].substring( 0, 2 );
		computerTo = args[0].substring( 2, 4 );

		if ( b.hasPiece( computerTo ) )
			moveToGarbage( computerTo );

		// lowPath automatically calls highPath if no path can be found.
		lowPath( computerFrom, computerTo );

		/* move the computer piece */
		try
		{
			b.movePiece( computerFrom, computerTo );
		} catch ( ChessBoard.NoPieceAtPositionException e )
		{
			System.out.println( e );
			System.exit( 1 );
		}

		System.out.println( "**** The board after the move was:" );
		/* print the board state */
		b.print();

		/* after done write the gripper positions */
		GripperPosition.write( p );
	}

	/**
	 * Calculates positions on the path between two specified locations on the
	 * chessboard. The returned path will be high; above any piece on the board.
	 * No collissions can occur, so a direct route is taken.
	 * 
	 * @param from
	 *            A string containing the start position of the path (in the
	 *            standard chess format).
	 * 
	 * @param to
	 *            A string containing the end position of the path (in the
	 *            standard chess format).
	 */
	private static void highPath( String from, String to )
			throws ChessBoard.NoPieceAtPositionException
	{
		Point startPoint = toPoint( from );
		Point endPoint = toPoint( to );

		double pieceHeight = b.getPiece( from ).height;
		moveGripperHigh( startPoint, endPoint, pieceHeight );
	}

	/**
	 * Calculates positions on the path between two specified locations on the
	 * chessboard. The returned path will be low, in between the chesspieces. A
	 * path without collisions will be chosen.
	 * 
	 * Note: If no safe path can be found, a high path will be used (see
	 * highPath()).
	 * 
	 * @param from
	 *            A string containing the start position of the path (in the
	 *            standard chess format).
	 * 
	 * @param to
	 *            A string containing the end position of the path (in the
	 *            standard chess format).
	 */
	private static void lowPath( String from, String to )
			throws ChessBoard.NoPieceAtPositionException
	{
		BoardLocation fromBoard = new BoardLocation( from );
		BoardLocation toBoard = new BoardLocation( to );

		// use A* to find a path between the pieces
		AStar finder = new AStar( b, fromBoard, toBoard );
		List<BoardLocation> path = finder.getPath();

		if ( path == null ) // there is no safe low path
			highPath( from, to );
		else
			moveGripperLow( from, to, path );
	}

	/**
	 * Finds positions (x, y, z) for the gripper along a specified path of
	 * coordinates on the board (e3).
	 * 
	 * Note: 'height' represents delta height, the height counting from the top
	 * of the board.
	 * 
	 * @param from
	 *            A string containing the start position of the path (in the
	 *            standard chess format).
	 * 
	 * @param to
	 *            A string containing the end position of the path (in the
	 *            standard chess format).
	 * 
	 * @param path
	 *            The path to follow, in board locations. The path should always
	 *            start in the same position as 'from' and end in 'to'.
	 */
	private static void moveGripperLow( String from, String tos,
			List<BoardLocation> path )
			throws ChessBoard.NoPieceAtPositionException
	{
		double pieceHeight = b.getPiece( from ).height;

		// move to above the piece
		location = toPoint( path.get( 0 ) );
		grip = OPEN_GRIP;
		height = SAFE_HEIGHT;
		move();

		// put gripper around the piece
		height = pieceHeight * ( 2 / 3 );
		move();

		// close the gripper and move to the path height
		grip = CLOSED_GRIP;
		move();
		height = LOWPATH_HEIGHT;
		move();

		// follow the path
		for ( BoardLocation loc : path )
		{
			location = toPoint( loc );
			move();
		}

		// put the piece down again
		height = pieceHeight * ( 2 / 3 );
		grip = OPEN_GRIP;
		move();

		// move back up
		height = SAFE_HEIGHT;
		move();
	}

	/**
	 * Finds positions (x,y,z) for the gripper, moving above all chesspieces.
	 * 
	 * @param startPoint
	 *            The current position of the piece.
	 * 
	 * @param endPoint
	 *            The desired position of the piece.
	 * 
	 * @param pieceHeight
	 *            The height of the piece (used when picking it up).
	 */
	private static void moveGripperHigh( Point startPoint, Point endPoint,
			double pieceHeight )
	{

		// getting the piece
		location = startPoint;
		height = SAFE_HEIGHT;
		grip = OPEN_GRIP;
		move();

		height = LOW_HEIGHT;
		move();
		height = pieceHeight * ( 2 / 3 );
		move();
		grip = CLOSED_GRIP;
		move();
		height = SAFE_HEIGHT;
		move();

		// putting it in its new spot
		location = endPoint;
		move();
		height = LOW_HEIGHT + pieceHeight * ( 2 / 3 );
		move();
		height = LOW_HEIGHT / 2 + pieceHeight * ( 2 / 3 );
		move();
		height = pieceHeight * ( 2 / 3 );
		grip = OPEN_GRIP;
		move();
		height = SAFE_HEIGHT;
		move();
	}

	/**
	 * Adds the move specified by the fields location, height and grip to the
	 * vector p, which contains the path in cartesian coordinates at the end of
	 * a program run.
	 * 
	 * Note: the height specified in 'height' is additive; it is added to the
	 * height of the board.
	 */
	private static void move()

	{
		p.add( new GripperPosition( addHeight( location, height ), 0, grip ) );
	}

	/**
	 * Converts a position specified as a string (e.g. a3) to a cartesian
	 * coordinate (x, y, z).
	 * 
	 * @param pos
	 *            The position to convert.
	 */
	private static Point toPoint( String pos )
	{
		StudentBoardTrans trans = new StudentBoardTrans( pos );
		trans.board = b;
		Point point = trans.toCartesian( trans.boardLocation.column,
				trans.boardLocation.row );

		return point;
	}

	/**
	 * Converts a position specified as a row and column to a cartesian
	 * coordinate (x, y, z).
	 * 
	 * Note: the constructor for StudentBoardTrans takes a string, but
	 * this string is not used for the purposes of this method. 'a1' is passed
	 * as a placeholder.
	 * 
	 * @param pos
	 *            The position to convert.
	 */
	private static Point toPoint( BoardLocation pos )
	{
		StudentBoardTrans trans = new StudentBoardTrans( "a1" );
		trans.board = b;
		Point point = trans.toCartesian( pos.column, pos.row );

		return point;
	}

	/**
	 * Adds height to the z-coordinate of the given point.
	 * 
	 * @param p
	 *            The original point.
	 * 
	 * @param offset
	 *            The height to add.
	 */
	private static Point addHeight( Point p, double offset )
	{
		return new Point( p.x, p.y, p.z + offset );
	}

	/**
	 * Finds a path in cartesian coordinates from a piece to a 'garbage'
	 * location; a place for pieces to go when they get taken. The garbage
	 * consists of 16 places next to the board that gets filled gradually.
	 * 
	 * @param to
	 *            The location of the piece that's about to get taken.
	 */
	private static void moveToGarbage( String to )
			throws ChessBoard.NoPieceAtPositionException
	{
		ChessPiece piece = b.getPiece( to );

		Point startPoint = toPoint( to );
		Point endPoint = blackToGarbage( piece );

		// set the board thickness to 0; we're putting things next to it
		double boardThickness = b.board_thickness;
		b.board_thickness = 0;
		moveGripperHigh( startPoint, endPoint, piece.height );
		b.board_thickness = boardThickness;
	}

	/**
	 * Calculates the coordinate of the spot for the next taken black piece.
	 * 
	 * @param piece
	 *            The piece to move.
	 */
	private static Point blackToGarbage( ChessPiece piece )
	{
		int column, row;

		if ( blackTaken > 7 )
		{
			row = 10;
			column = blackTaken - 8;
		} else
		{
			row = 9;
			column = blackTaken;
		}
		blackTaken++;
		return toPoint( new BoardLocation( column, row ) );
	}
}
