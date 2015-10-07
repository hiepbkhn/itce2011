package dp.combined;

public class CutNode {

	public double mod = 0.0;		// best modularity value of subtree root at this node
	public double mod_noisy = 0.0;	// to satisfy e-DP
	public boolean self = false;	// best modularity by this node or not?
	
	//
	public CutNode(double mod, boolean self) {
		super();
		this.mod = mod;
		this.self = self;
	}

	public CutNode(double mod, double mod_noisy, boolean self) {
		super();
		this.mod = mod;
		this.mod_noisy = mod_noisy;
		this.self = self;
	}
	
	
	
}
