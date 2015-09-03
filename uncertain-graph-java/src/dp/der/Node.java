package dp.der;

public class Node {
	int y1;
	int y2;
	int x1;
	int x2;
	Node[] children = null;	
	int noisy_count;	// number of 1-cells, Geometric
	int level = 0;
	boolean is_leaf = false;
	int stop_cond = 0;		// 1,2,3 stop condition
	
	////
	public Node(int y1, int y2, int x1, int x2, int level) {
		super();
		this.y1 = y1;
		this.y2 = y2;
		this.x1 = x1;
		this.x2 = x2;
		this.level = level;
	}
	
	
}
