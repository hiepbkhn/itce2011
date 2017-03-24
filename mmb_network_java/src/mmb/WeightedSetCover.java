/*
 * Mar 17, 2017
 * 	- translated from mmb_network/weighted_set_cover.py
 */

package mmb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tuple.PairSetInt;
import tuple.PairSetListInt;

public class WeightedSetCover {
	
	//
	// UNWEIGHTED
	public static PairSetInt find_max_uncovered(List<Set<Integer>> S, Set<Integer> R){
	    int max_inter_len = 0;
	    int max_inter_id = -1;
	    for (int i = 0; i < S.size(); i++){
	    	Set<Integer> s = new HashSet<Integer>(S.get(i));	// copy constructor
	    	s.retainAll(R);	// itersection
	        int inter_len = s.size();
	        if (max_inter_len < inter_len){
	            max_inter_len = inter_len;
	            max_inter_id = i;
	        }
	    }
	    
	    if (max_inter_id == -1)
	        return new PairSetInt(new HashSet<Integer>(), -1);
	    else                
	        return new PairSetInt(S.get(max_inter_id), max_inter_id);   
	}
	
	// (FOR PUBLIC USE)
	// timestamp = 0, S: positive sets, U:universe (compute from S)
	public static PairSetListInt find_init_cover(List<Set<Integer>> S, int num_element){
	    // compute U
	    int[] marked = new int[num_element];
	    for (Set<Integer> a_set : S)
	        for (int item : a_set)
	            marked[item] = 1;
	    
	    Set<Integer> U = new HashSet<Integer>(); 
	    for (int item = 0; item < num_element; item++) 
	    	if (marked[item] == 1)
	    		U.add(item);
	    System.out.println("len(S) =" + S.size());
	    System.out.println("len(U) =" + U.size());
	    
	    Set<Integer> R = new HashSet<Integer>(U);	// deep copy
	    
	    List<Set<Integer>> C_0 = new ArrayList<Set<Integer>>();
	    
	    // NON-BOOTSTRAP
	    // sort S
	    long start = System.currentTimeMillis(); 
	    Collections.sort(S, new Comparator<Set<?>>() {
	        @Override
	        public int compare(Set<?> o1, Set<?> o2) {
	            return Integer.valueOf(o1.size()).compareTo(o2.size());
	        }
	    });
	    
	    System.out.println("Sorting - elapsed :" + (System.currentTimeMillis() - start));
	    
	    start = System.currentTimeMillis(); 
	    while (R.size() != 0){
	        PairSetInt temp = find_max_uncovered(S, R);
	        Set<Integer> S_i = temp.s;
	        int max_inter_id = temp.i;
	        if (max_inter_id == -1)
	            break;
	        
	        C_0.add(S_i);
	        R.removeAll(S_i);		// set difference
	        S.remove(max_inter_id);
	    }
	    
	    System.out.println("find_init_cover - elapsed :" + (System.currentTimeMillis() - start));
	    System.out.println("len(R) =" + R.size());
	    System.out.println("Cover.len: " + C_0.size());
	    int num_cloaked_users = U.size() - R.size();
	    
	    int total_C_0 = 0;
	    for (Set<Integer> a_set : C_0)
	    	total_C_0 += a_set.size();
	    System.out.println("Total elements in C_0 =" + total_C_0);
	    
	    return new PairSetListInt(C_0, num_cloaked_users);
	}

	// (FOR PUBLIC USE)
	// timestamp > 0, S: positive sets, U:universe (compute from S), C_0: cover_set from prev.step
	public static PairSetListInt find_next_cover(List<Set<Integer>> S, int num_element, List<Set<Integer>> C_0, int k_global){
	    // compute U
		int[] marked = new int[num_element];
	    for (Set<Integer> a_set : S)
	        for (int item : a_set)
	            marked[item] = 1;
	    
	    Set<Integer> U = new HashSet<Integer>(); 
	    for (int item = 0; item < num_element; item++) 
	    	if (marked[item] == 1)
	    		U.add(item);
	    System.out.println("len(S) =" + S.size());
	    System.out.println("len(U) =" + U.size());
	    
	    Set<Integer> R = new HashSet<Integer>(U);	// deep copy
	        
	    // result
	    List<Set<Integer>> C_1 = new ArrayList<Set<Integer>>();
	    int total_W = 0;
	    
	    // compute weights
	    long start = System.currentTimeMillis(); 
	    
	    List<List<Integer>> list_pairs = new ArrayList<List<Integer>>();     // list of lists, for MMB/MAB checking
	    
	    List<Integer> W = new ArrayList<Integer>();
	    		
	    for (int i = 0; i < S.size(); i++){
	    	
	        W.add(0);
	        list_pairs.add(new ArrayList<Integer>());   		//have list_pairs[i]
	        for (int j = 0; j < C_0.size(); j++){
	        	Set<Integer> c_set = C_0.get(j);
	        	
	        	Set<Integer> s_set = new HashSet<Integer>(S.get(i));	// copy constructor (moved to here)
	        	s_set.retainAll(c_set);				// intersection
	        	
	            int inter_len = s_set.size();
	            if (inter_len > 0 && inter_len < k_global)
	                W.set(i, W.get(i) + 1);
	            if (inter_len >= k_global)
	                list_pairs.get(i).add(j);
	        }
	    }
	    System.out.println("Computing W - elapsed :" + (System.currentTimeMillis() - start));
	                
	    // sort W
	    start = System.currentTimeMillis(); 
	    
//	    z = sorted(zip(W,S,list_pairs))     //ZIP: simultaneous sorting --> index sort (by W)
	    IndexSorter<Integer> is = new IndexSorter<Integer>(W);
		is.sort();
		Integer[] idx = is.getIndexes();
	    
//		W_temp, S_temp, list_pairs_temp = zip(*z)
		List<Integer> W_temp = new ArrayList<Integer>();
		List<Set<Integer>> S_temp = new ArrayList<Set<Integer>>();
		List<List<Integer>> list_pairs_temp = new ArrayList<List<Integer>>();
		for (int id = 0; id < idx.length; id++){
			W_temp.add(W.get(idx[id]));
			S_temp.add(new HashSet<Integer>(S.get(idx[id])));	// copy constructor
			list_pairs_temp.add(list_pairs.get(idx[id]));
		}
		
		
	    int i = 0;
	    while (W_temp.get(i) == 0 && i < W_temp.size())
	        i = i + 1;
	    System.out.println("i = " + i);
	    
	    W.clear();
	    S.clear();
//	    System.out.println("W_temp.size = " + W_temp.size());
//	    System.out.println("W_temp[last] = " + W_temp.get(W_temp.size()-1));
//	    System.out.println("S_temp.size = " + S_temp.size());
	    
////	    list_pairs.clear();		// WRONG !
//	    for (int id = 0; id < i; id++){
//	    	W.add(W_temp.get(id));
//	    	S.add(S_temp.get(id));
//	    	list_pairs.add(list_pairs.get(id));
//	    }
	    
	    W = W_temp.subList(0, i);
	    S = S_temp.subList(0, i);
	    list_pairs = list_pairs_temp.subList(0, i);
	    
	    
		System.out.println("Sorting - elapsed :" + (System.currentTimeMillis() - start));    
	    
	    // compute C_1
		start = System.currentTimeMillis(); 
	    
//	    checking_pairs = []
	    while (R.size() != 0){
	    	PairSetInt temp = find_max_uncovered(S, R);
	        Set<Integer> S_i = temp.s;
	        int min_element = temp.i;
	        if (min_element == -1)
	            break;
	        
	        C_1.add(S_i);
	        total_W = total_W + W.get(min_element);
	        R.removeAll(S_i);		// set difference
	        
	        S.remove(min_element);
	        W.remove(min_element);
//	        checking_pairs.append(list_pairs[min_element])  // synchronized with C_1 ! 
//	        del list_pairs[min_element]
	    }
	    
	    System.out.println("find_next_cover - elapsed :" + (System.currentTimeMillis() - start));    
	    System.out.println("len(R) =" + R.size());
	    System.out.println("Cover.len: " + C_1.size());
//	    System.out.println("checking_pairs.len: " + checking_pairs.size());
	    int num_cloaked_users = U.size() - R.size();
	    int total_C_1 = 0;
	    for (Set<Integer> a_set : C_1)
	    	total_C_1 += a_set.size();
	    System.out.println("Total elements in C_0 =" + total_C_1);
	    System.out.println("Total weight = " + total_W);
	        
	    return new PairSetListInt(C_1, num_cloaked_users);		//, checking_pairs
	}
	
}
