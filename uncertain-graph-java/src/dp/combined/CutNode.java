package dp.combined;

public class CutNode {

	public double mod = 0.0;		// best modularity value of subtree root at this node
	public boolean self = false;	// best modularity by this node or not?
	
	//
	public CutNode(double mod, boolean self) {
		super();
		this.mod = mod;
		this.self = self;
	}
	
}
