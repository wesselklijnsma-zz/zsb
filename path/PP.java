/*
* PP.java
* Assignment for the Path planning part of the ZSB lab course.
*
* This you will work on writing a function called highPath() to move a
* chesspiece across the board at a safe height. By raising the gripper 20 cm
* above the board before moving it over the board you don't risk hitting any
* other pieces on the board. This means you don't have to do any pathplanning
* yet.
*
* Input of this program is a commandline argument, specifying the computer
* (white) move. Your job is to find the correct sequence of GripperPositions
* (stored in Vector p) to pick up the correct white piece and deposit it at
* its desired new location. Read file
* /opt/stud/robotics/hints/HIGHPATH_POSITIONS to see what intermediate
* positions you should calculate.
*
* To run your program, fire up playchess or one of its derviates endgame* and
* the umirtxsimulator. In the simulator you can see the effect of your path
* planning although the board itself is not simulated. When you think you've
* solved this assignment ask one of the lab assistents to verify it and let
* it run on the real robot arm.
*
* You can also compare your solution with the standard PP solution outside
* playchess by running in a shell:
* java PPstandard e2e4
* cat positions.txt
* java PP e2e4
* cat positions.txt
*
*
*
* Nikos Massios, Matthijs Spaan <mtjspaan@science.uva.nl>
* $Id: Week2.java,v a4f44ea5d321 2008/06/16 09:18:44 obooij $
*/

import java.io.*;
import java.lang.*;
import java.util.*;

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
    
    /* plan a path for the move */
    //highPath(computerFrom, computerTo, b, p);
    
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

  private static void highPath(String from, String to) throws ChessBoard.NoPieceAtPositionException
  {

    System.out.println("**** In high path");

    double pieceHeight = b.getPiece(from).height;

    // Use the boardLocation and toCartesian methods you wrote:
    Point startPoint = toPoint(from);
    Point endPoint = toPoint(to);

    moveGripperHigh(startPoint, endPoint, pieceHeight);
    System.out.println(p);
    /* Example of adding a gripperposition to Vector p.
* Point tempPoint;
* GripperPosition temp;
* tempPoint = new Point(x-coordinate, y-coordinate, z-coordinate);
* temp = new GripperPosition(tempPoint, angle, CLOSED_GRIP/OPEN_GRIP);
* Now you only have to add it at the end of Vector p.
*/
  }

  private static void lowPath(String from, String to) throws ChessBoard.NoPieceAtPositionException {
      System.out.println("**** In low path");
      BoardLocation fromBoard = new BoardLocation(from);
      BoardLocation toBoard = new BoardLocation(to); 

      AStar finder = new AStar(b, fromBoard, toBoard);
      List<BoardLocation> path = finder.getPath();
      
      if(path == null)
          highPath(from, to);
      else
          printPath(path);
          moveGripperLow(from, to, path);
  }

  private static void printPath(List<BoardLocation> path)
  {
      for(BoardLocation loc : path)
          System.out.printf("%d,%d ", loc.column, loc.row);
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

  private static void moveGripperHigh(Point startPoint, Point endPoint, double pieceHeight) 
  {
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
    System.out.println("**** In moveToGarbage");
    
    double pieceHeight = b.getPiece(to).height;
    
    StudentBoardTrans fromTrans = new StudentBoardTrans(to);
    int fromColumn = fromTrans.boardLocation.column;
    int fromRow = fromTrans.boardLocation.row;

    Point startPoint = fromTrans.toCartesian(fromColumn, fromRow);
    Point endPoint = fromTrans.toCartesian(-1, fromRow);
     

    moveGripperHigh(startPoint, endPoint, pieceHeight);
     

  }
}
