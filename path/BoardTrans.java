/*
 * Names       : Jouke van der Maas & Wessel Klijnsma
 * Student IDs : 10186883 and 10172432
 * Description : Part of path planning problem for a robot arm.
 * Date        : June 2012
 * Comments    : The structure of this file was provided by Nikos Massios
 *   and Matthijs Spaan.
 */

import java.util.Vector;
import java.util.List;
import java.util.Arrays;

class BoardTrans
{
	/**
	 * BoardTrans takes one optional argument, specifying the position on the
	 * field it should use. It defaults to b7.
	 */
	public static void main(String[] args)
	{
		String position;
		
		try
		{
			position = args[0];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			position = "b7";
		}
		
		StudentBoardTrans boardTrans = new StudentBoardTrans(position);
		
		// set up the board in starting position
		boardTrans.board.setup();
		
		// draw the board state
		boardTrans.board.print();
		
		try
		{
			System.out
					.println("The dimensions of the squares on the board are "
							+ boardTrans.board.delta_x + " by "
							+ boardTrans.board.delta_y + "mm");
			
			System.out.println("The x,y coordinates of the board are "
					+ boardTrans.board.coords.x + ","
					+ boardTrans.board.coords.y);
			
			ChessPiece p = boardTrans.board.hasPiece(boardTrans.pos) ? boardTrans.board
					.getPiece(boardTrans.pos) : null;
			if (p != null)
			{
				System.out.println("The height of the piece at "
						+ boardTrans.pos + " is " + p.height + " mm");
				
				System.out.println("The color of the piece at "
						+ boardTrans.pos + " is " + p.side);
			}
		} catch (Exception e)
		{
			System.out.println(e);
			System.exit(1);
		}
		
		StudentBoardTrans.BoardLocation location = boardTrans.boardLocation;
		BoardLocation realLocation = new BoardLocation(boardTrans.pos);
		
		System.out.println("You think position " + boardTrans.pos + " is at ("
				+ location.column + "," + location.row
				+ "), the correct answer is (" + realLocation.column + ","
				+ realLocation.row + ")");
		
		Point cartesian = new Point();
		cartesian = boardTrans.toCartesian(location.column, location.row);
		
		System.out.println("You think " + boardTrans.pos + " is at "
				+ cartesian + ", the correct answer is "
				+ boardTrans.board.toCartesian(boardTrans.pos));
		
		// Let's turn the board 45 degrees
		boardTrans.board.theta = 45;
		
		// recalculate cartesian
		cartesian = boardTrans.toCartesian(location.column, location.row);
		
		System.out.println("You think " + boardTrans.pos + " is at "
				+ cartesian + ", the correct answer is "
				+ boardTrans.board.toCartesian(boardTrans.pos));
		
		// Let's move the position of the board and turn it again
		boardTrans.board.coords.x = 100;
		boardTrans.board.coords.y = 200;
		boardTrans.board.theta = -60;
		
		// recalculate cartesian
		cartesian = boardTrans.toCartesian(location.column, location.row);
		
		System.out.println("You think " + boardTrans.pos + " is at "
				+ cartesian + ", the correct answer is "
				+ boardTrans.board.toCartesian(boardTrans.pos));
	}
	
}

class StudentBoardTrans
{
	public ChessBoard board; // our board
	public String pos; // the position we're going to examine
	public BoardLocation boardLocation;
	
	public StudentBoardTrans(String position)
	{
		board = new ChessBoard();
		pos = position;
		boardLocation = new BoardLocation();
	}
	
	public Point toCartesian(int column, int row)
	{
		// size of the squares
		double dx = board.delta_x;
		double dy = board.delta_y;
		
		// start of h8 square
		double posx = board.coords.x - board.sur_x;
		double posy = board.coords.y + board.sur_y;
		
		// position of square if the board is not rotated
		double x, y, z;
		x = posx - (8 - column) * dx + dx / 2;
		y = posy + (8 - row) * dy - dy / 2;
		z = board.board_thickness;
		
		// rotation vars
		double xshift = board.coords.x;
		double yshift = board.coords.y;
		double cos = Math.cos(Math.toRadians(board.theta));
		double sin = Math.sin(Math.toRadians(board.theta));
		
		// first do a translation
		double[] vec = { x - xshift, y - yshift };
		
		// then a rotation (note, this is the transpose of the rotation matrix)
		double[][] trans = { { cos, -sin }, { sin, cos } };
		double[] newVec = doTransformation(trans, vec);
		
		// translate back
		return new Point(newVec[0] + xshift, newVec[1] + yshift, z);
	}
	
	/**
	 * Applies a linear transformation in matrix form to a vector
	 */
	private double[] doTransformation(double[][] matrix, double[] vector)
	{
		double[] newVec = new double[matrix[0].length];
		for (int c = 0; c < matrix.length; c++)
		{
			for (int r = 0; r < matrix[c].length; r++)
			{
				newVec[r] += matrix[c][r] * vector[c];
			}
		}
		return newVec;
	}
	
	class BoardLocation
	{
		public int row;
		public int column;
		
		public BoardLocation()
		{
			// look up the index in a list, for smaller code
			List<Character> letters = Arrays.asList('a', 'b', 'c', 'd', 'e',
					'f', 'g', 'h');
			
			column = letters.indexOf(pos.charAt(0));
			row = Character.getNumericValue(pos.charAt(1)) - 1;
		}
	}
}
