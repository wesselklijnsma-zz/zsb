public class Entry {
	public static void main(String[] args) {
		try {
			BoardLocation start = new BoardLocation(0, 1);
			BoardLocation goal = new BoardLocation(6, 5);
			
			AStar finder = new AStar(start, goal);
			BoardLocation[] path = finder.getPath();

			for (BoardLocation b : path) {
				System.out.println("(" + b.row + ", " + b.column + ")");
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
}
