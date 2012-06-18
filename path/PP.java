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
 * Used to calculate the positions of the gripper of the robot necessary to 
 * move a chesspiece on the board.
 * 
 */
public class PP {
  private static double SAFE_HEIGHT=200;
  private static double LOW_HEIGHT=40;
  private static double LOWPATH_HEIGHT=20;
  private static double OPEN_GRIP=30;
  private static double CLOSED_GRIP=0;

  private static Point location;
  private static Vector<GripperPosition> p = new Vector<GripperPosition>();
  private static double height, grip;
  private static ChessBoard b;

  /**
   * Writes the positions for the specified move to a file called positions.txt
   * in the current folder.
   *
   * @param args
   * The first (0) element should contain a move in the form e3e4. If any more
   * elements are provided, they should be element 1, 2, and 3, containing the
   * x, y and theta that represent the position of the board.
   */
  public static void main(String[] args) throws ChessBoard.NoPieceAtPositionException {
    String computerFrom, computerTo;

    System.out.println("**** THIS IS THE STUDENT PP MODULE IN JAVA");
    System.out.println("**** The computer move was "+ args[0]);

    /* Read possibly changed board position */
    if(args.length > 1)
    {
      double x=Double.parseDouble(args[1]),
             y=Double.parseDouble(args[2]),
             theta=Double.parseDouble(args[3]);
      Point boardPosition=new Point(x,y,0);

      System.out.println("**** Chessboard is at (x,y,z,theta): ("
                               + x + ", " + y + ", 0, " + theta + ")");

      b = new ChessBoard(boardPosition, theta);
    }
    else
      b = new ChessBoard();

    /* Read the board state*/
    b.read();
    /* print the board state*/
    System.out.println("**** The board before the move was:");
    b.print();
    
    computerFrom = args[0].substring(0,2);
    computerTo = args[0].substring(2,4);
    
    // lowPath automatically calls highPath if no path can be found.
    lowPath(computerFrom, computerTo);

    if(b.hasPiece(computerTo))
        moveToGarbage(computerTo);

    /* move the computer piece */
    try {
        b.movePiece(computerFrom, computerTo);
    } catch (ChessBoard.NoPieceAtPositionException e) {
        System.out.println(e);
        System.exit(1);
    }

    System.out.println("**** The board after the move was:");
    /* print the board state*/
    b.print();
    
    /* after done write the gripper positions */
    GripperPosition.write(p);
  }

  /**
   * Calculates positions on the path between two specified locations on the chessboard. 
   * The returned path will be high; above any piece on the board. No collissions can occur,
   * so a direct route is taken.
   *
   * @param from
   * A string containing the start position of the path (in the standard chess
   * format).
   *
   * @param to
   * A string containing the end position of the path (in the standard chess
   * format).
   */
  private static void highPath(String from, String to) throws ChessBoard.NoPieceAtPositionException
  {
    Point startPoint = toPoint(from);
    Point endPoint = toPoint(to);

    moveGripperHigh(startPoint, endPoint);
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
   * A string containing the start position of the path (in the standard chess
   * format).
   *
   * @param to
   * A string containing the end position of the path (in the standard chess
   * format).
   */
  private static void lowPath(String from, String to) throws ChessBoard.NoPieceAtPositionException {
      BoardLocation fromBoard = new BoardLocation(from);
      BoardLocation toBoard = new BoardLocation(to); 

      // use A* to find a path between the pieces
      AStar finder = new AStar(b, fromBoard, toBoard);
      List<BoardLocation> path = finder.getPath();
      
      if(path == null) // there is no path
          highPath(from, to);
      else
          moveGripperLow(from, to, path);
  }

  private static void moveGripperLow(String from, String to, List<BoardLocation> path) 
      throws ChessBoard.NoPieceAtPositionException
  {
      double pieceHeight = b.getPiece(from).height;
      
      location  = toPoint(path.get(0));
      grip = OPEN_GRIP;  height = SAFE_HEIGHT; move();
      height = pieceHeight/3; move();  
      grip = CLOSED_GRIP; move();
      height = LOWPATH_HEIGHT; move();                          
      
      for(BoardLocation loc : path)
      {
          location = toPoint(loc); move();
      }
            
      height = pieceHeight/3; move();
      grip = OPEN_GRIP; move();
      height = SAFE_HEIGHT; move();
  }  

  private static void moveGripperHigh(Point startPoint, Point endPoint) 
  {
    double pieceHeight = b.getPiece(from).height;

    //getting the piece
    location = startPoint; 
    height = SAFE_HEIGHT;
    grip = OPEN_GRIP; move();
    height = LOW_HEIGHT; move();
    height = pieceHeight / 2; move();
    grip = CLOSED_GRIP; move();
    height = SAFE_HEIGHT; move();
       
    // putting it in its new spot
    location = endPoint; move();
    height = LOW_HEIGHT + pieceHeight / 2; move();
    height = LOW_HEIGHT / 2 + pieceHeight / 2; move();
    height = pieceHeight / 2; move();
    grip = OPEN_GRIP; move();
    height = SAFE_HEIGHT; move();
    }


  private static void move()
  {
      p.add(new GripperPosition(addHeight(location, height), 0, grip));
  }



  private static Point toPoint(String pos) 
  {
      StudentBoardTrans trans = new StudentBoardTrans(pos);    
      Point point = trans.toCartesian(trans.boardLocation.column, trans.boardLocation.row);

      return point;
  }

  private static Point toPoint(BoardLocation pos)
  {
      StudentBoardTrans trans = new StudentBoardTrans("a1");
      Point point = trans.toCartesian(pos.column, pos.row);

      return point;
  }

   private static Point addHeight(Point p, double offset)
  {
    Point pNew = (Point)p.clone();
    pNew.z += offset;
    return pNew;
  }

  private static void moveToGarbage(String to)
      throws ChessBoard.NoPieceAtPositionException
  {

    /* When you're done with highPath(), incorporate this function.
* It should remove a checked piece from the board.
* In main() you have to detect if the computer move checks a white
* piece, and if so call this function to remove the white piece from
* the board first.
*/
    double pieceHeight = b.getPiece(to).height;
    
    StudentBoardTrans fromTrans = new StudentBoardTrans(to);
    int fromColumn = fromTrans.boardLocation.column;
    int fromRow = fromTrans.boardLocation.row;

    Point startPoint = fromTrans.toCartesian(fromColumn, fromRow);
    Point endPoint = fromTrans.toCartesian(-1, fromRow);
     

    moveGripperHigh(startPoint, endPoint, pieceHeight);
     

  }
}
