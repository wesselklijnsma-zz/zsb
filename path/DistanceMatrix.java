import java.lang.Integer;

/**
 * This class it implements a distance matrix. It can be used
 * to make a distance transform of the chess board locations.
 *
 *  @author Nikos Massios
 *  @author Matthijs Spaan
 *  @version $Id: DistanceMatrix.java,v 1.5 2002/04/26 14:17:35 mtjspaan Exp $
 */
public class DistanceMatrix {
    private static int OCCUPIED = -1;
    private static int EMPTY = -2;
    private static int UNREACHABLE = -3;
    private static int HAVENT_FOUND_IT = 1000;
    private int distanceMatrix[][];

  /**
   * The row of the closest neighbour. The correct value
   * is contained only after a call to the smallestPositiveNeighbourValue
   * method.
   */
    public int neighbourRow;
  /**
   * The row of the closest neighbour. The correct value
   * is contained only after a call to the smallestPositiveNeighbourValue
   * method.
   */
    public int neighbourCol;




  /**
   * Sole constructor. It just allocates memory. A call
   * to the distnanceTransform method is necessary before the distance
   * matrix can be used.
   */
    public DistanceMatrix(){
	distanceMatrix = new int[8][8];
    }

  /**
   * This method uses the locations of the pieces on the board
   * to initialise the distance matrix. It specifies the
   * pieces that are empty and the ones that are occupied.
   * @param board The chess board to use in order to initialise the
   * distance matrix.
   */
    private void init(ChessBoard board){
	ChessPiece p;

	for(int col = 0; col < 8; col++)
	    for(int row = 0; row < 8; row++)
		distanceMatrix[row][col] = EMPTY;
	
	for(int i = 0; i < board.aliveChessPieces.size(); i++){
	    p = (ChessPiece) board.aliveChessPieces.get(i);
	    distanceMatrix[p.getRow()][p.getCol()] = OCCUPIED;
	}
    }

  /**
   * This method prints the distance matrix. The distance
   * matrix values are printed to System.out.<p>
   * "o" stands for occupied.<p>
   * "e" stands for empty. <p>
   * "u" stands for unrechable. High path is necessary there<p>
   * any integer value is the distance from the target.
   */
    public void print(){
	String s;

	s = "  abcdefgh";
	System.out.println(s);
	for(int row = 7; row >=0; row --){
	  s = new Integer(row+1).toString() + " ";
	    for(int col = 0; col < 8; col++){
		if(distanceMatrix[row][col] == OCCUPIED)
		    s = s+"o";
		else if(distanceMatrix[row][col] == EMPTY)
		    s = s+"e";
		else if(distanceMatrix[row][col] == UNREACHABLE)
		    s = s+"u";
		else{
		    Integer dist = new Integer(distanceMatrix[row][col]);
		    s = s+ dist.toString();
		}
	    }
	    System.out.println(s);
	}
    }

  /**
   * This method finds the smallest neightbour and its distance value.
   * It arguments are a location on the board. It examines the
   * neighbours of that location (assuming 4-connectivity) and 
   * returns the distance value of the closest neighbour. The location
   * of the closest neighbour is set at the neighbourRow and
   * neighbourCol fields. These fields can be inspected if its
   * necessary to know the location of the neighbour.
   * @param row The row of the board location.
   * @param col The column of the board location.
   * @return The distance value of the closest neighbour. If no
   * available neighbour exists 1000 is returned.
   * @see #neighbourRow
   * @see #neighbourCol
   */
    public int smallestPositiveNeighbourValue(int row, int col){
	int[] values = new int[4];
	int smallestIndex, smallestValue;


	/*find all neighbours that exist...
	       0
             3 c 2
	       1
	*/
	if(row + 1 < 8)
	    values[0] = distanceMatrix[row+1][col];
	else
	    values[0] = OCCUPIED;
	if(row - 1 >= 0)
	    values[1] = distanceMatrix[row-1][col];
	else
	    values[1] = OCCUPIED;

	if(col + 1 < 8)
	    values[2] = distanceMatrix[row][col+1];
	else
	    values[2] = OCCUPIED;
	if(col - 1 >= 0)
	    values[3] = distanceMatrix[row][col-1];
	else
	    values[3] = OCCUPIED;

	/* find the smallest positive now*/
	smallestValue = HAVENT_FOUND_IT;
	smallestIndex = 0;
	for(int i = 0; i < 4; i++)
	    if((smallestValue > values[i]) && (values[i]>=0)){
		smallestValue = values[i];
		smallestIndex = i;
	    }


	/*Neighbour was all neighbours that exist...
	       0
             3 c 2
	       1
	*/	

	if(smallestIndex == 0){
	    neighbourRow = row + 1;
	    neighbourCol = col;
	}
	else if(smallestIndex == 1){
	    neighbourRow = row - 1;
	    neighbourCol = col;
	}
	else if(smallestIndex == 2){
	    neighbourRow = row;
	    neighbourCol = col + 1;
	}
	else if(smallestIndex == 3){
	    neighbourRow = row;
	    neighbourCol = col - 1;
	}
	
	return(smallestValue);
    }

  /**
   * This method checks if it is notPossible to plan a low path
   * @param target The board location to plan a path to.
   * @return True If a low path to that location is not possible.
   * False if a low path to that location is possible.
   */
    public boolean notPossible(String target){
	BoardLocation t = new BoardLocation(target);

	if(distanceMatrix[t.row][t.column] == UNREACHABLE)
	    return(true);
	else
	    return(false);
    }


  /**
   * This method generates the distance transform. It sets the 
   * correct values in the distance matrix. A call to this method
   * is necessary before a call to the smallestPositiveNeighbourValue
   * method makes sence. It uses the information stored in the board
   * to determine the empty and occupied locations. Then starting
   * from the target location (distance 0), it assigns distances
   * to the empty neighbours of the target location (distance 1) 
   * and to their empty neighbours (distance 2) and so on. Until
   * the whole board in examined. The empty locations that remain
   * at the end of the iteration are unreachable due to obstacles.
   * In terms of path planning a high path should be planned then.
   * @param board The chess board to use.
   * @param target The target location to use when generating the transform.
   */
    public void distanceTransform(ChessBoard board, 
				  String target){
	int targetRow, targetCol;
	int dummyRow, dummyCol;
	int redo;
	int count = 0;
	BoardLocation t = new BoardLocation(target);
	

	init(board);

	distanceMatrix[t.row][t.column] = 0;

	dummyRow = dummyCol = 0;

	redo = 1;
	while((redo == 1) && (count < 16)){
	    redo = 0;
	    count ++;
	    for(int cellCol = 0; cellCol < 8; cellCol++)
		for(int cellRow = 0; cellRow < 8; cellRow++)
		    if(distanceMatrix[cellRow][cellCol] == EMPTY){
			redo = 1;
			if(smallestPositiveNeighbourValue(cellRow,cellCol) != HAVENT_FOUND_IT){
			distanceMatrix[cellRow][cellCol] = smallestPositiveNeighbourValue(cellRow,cellCol) + 1;
			}
		    }
	}

	for(int cellCol = 0; cellCol < 8; cellCol++)
	    for(int cellRow = 0; cellRow < 8; cellRow++)
		if(distanceMatrix[cellRow][cellCol] == EMPTY)
		    distanceMatrix[cellRow][cellCol] = UNREACHABLE;


    }

}

