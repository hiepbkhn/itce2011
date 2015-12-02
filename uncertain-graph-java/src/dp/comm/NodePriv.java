/*
 * April 1
 * 	- created
 * Dec 2
 * 	- replace Grph by EdgeIntGraph 
 */

package dp.comm;

import java.util.Iterator;
import java.util.Random;

import algs4.EdgeIntGraph;

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.set.BitVectorSet;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class NodePriv extends NodeSet{

	////
	// 
	public NodePriv(EdgeIntGraph G, IntSet A){
		super(G, A);
	}
	
	////
	public NodePriv(EdgeIntGraph G){
		super(G);
	}
	
	//// LOG-LIKELIHOOD partition, using logLK()
	public static void partitionLK(NodePriv R, EdgeIntGraph G, double eps1, int n_steps, int n_samples, int sample_freq){
		System.out.println("NodePriv.partitionLK called");
		// delta U
		int n_nodes = G.V();
		long nMax = 0;
		if (n_nodes % 2 == 0) 
	        nMax = n_nodes*n_nodes/4;
	    else 
	        nMax = (n_nodes*n_nodes-1)/4; 
	    double dU = Math.log(nMax) + (nMax-1)*Math.log(1+1.0/(nMax-1));
	    System.out.println("dU = " + dU);
	    System.out.println("eps1 = " + eps1);
		System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

		int out_freq = (n_steps + n_samples * sample_freq) / 10;
		//
		long start = System.currentTimeMillis();
		boolean is_add = true;			// add or remove
		Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		
		double logLT = R.logLK();
		double logLT2;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
			// decide add or remove
			if (R.S.size() < n_nodes/2 - 1){	// add or remove
				int rand_val = random.nextInt(2);
				if (rand_val == 0)
					is_add = true;
				else
					is_add = false;	
			}else if (R.S.size() == 0){			// only add
				is_add = true;
			}else{								// only remove
				is_add = false;
			}
			
			// perform add or remove
			if (is_add){
				// randomly pick an item from T
				u = R.T.pickRandomElement(random);
				R.add(u, G);
				
			}else{
				// randomly pick an item from S
				u = R.S.pickRandomElement(random);
				R.remove(u, G);
			}
			
			// MCMC
			logLT2 = R.logLK();
			
			if (logLT2 > logLT){
				n_accept += 1;
				n_accept_positive += 1;
				logLT = logLT2;
			}else{
				double prob = Math.exp(eps1/(2*dU)*(logLT2 - logLT));			// prob << 1.0
				double prob_val = random.nextDouble();
				if (prob_val > prob){
					// reverse
					if (is_add)
						R.remove(u, G);
					else
						R.add(u, G);
				}else {
					n_accept += 1;
					logLT = logLT2;
				}
			}
			
			if (i % out_freq == 0)
				System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + R.logLK() + " mincut = " + R.mincut() + " edgeVar = " + R.edgeVar()
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
		}
		
	}
	
	
	//// EDGE-VAR partition, using edgeVar()
	public static void partitionEV(NodePriv R, EdgeIntGraph G, double eps1, int n_steps, int n_samples, int sample_freq){
		System.out.println("NodePriv.partitionEV called");
		// dU
		int n_nodes = G.V();
		
		double dU = 2.0;
	    System.out.println("dU = " + dU);
	    System.out.println("eps1 = " + eps1);
		System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

		int out_freq = (n_steps + n_samples * sample_freq) / 10;
		//
		long start = System.currentTimeMillis();
		boolean is_add = true;			// add or remove
		Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		
		double logLT = R.edgeVar();
		double logLT2;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
			// decide add or remove
			if (R.S.size() < n_nodes/2 - 1){	// add or remove
				int rand_val = random.nextInt(2);
				if (rand_val == 0)
					is_add = true;
				else
					is_add = false;	
			}else if (R.S.size() == 0){			// only add
				is_add = true;
			}else{								// only remove
				is_add = false;
			}
			
			// perform add or remove
			if (is_add){
				// randomly pick an item from T
				u = R.T.pickRandomElement(random);
				R.add(u, G);
				
			}else{
				// randomly pick an item from S
				u = R.S.pickRandomElement(random);
				R.remove(u, G);
			}
			
			// MCMC
			logLT2 = R.edgeVar();
			
			if (logLT2 < logLT){			// smaller
				n_accept += 1;
				n_accept_positive += 1;
				logLT = logLT2;
			}else{
				double prob = Math.exp(eps1/(2*dU)*(logLT - logLT2));			// prob << 1.0
				double prob_val = random.nextDouble();
				if (prob_val > prob){
					// reverse
					if (is_add)
						R.remove(u, G);
					else
						R.add(u, G);
				}else {
					n_accept += 1;
					logLT = logLT2;
				}
			}
			
			if (i % out_freq == 0)
				System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + R.logLK() + " mincut = " + R.mincut() + " edgeVar = " + R.edgeVar()
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
		}
		
	}
}
