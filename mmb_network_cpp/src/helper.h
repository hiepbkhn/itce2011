/*
 * helper.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Nguyen Huu Hiep
 */

#ifndef HELPER_H_
#define HELPER_H_

#include <iostream>
#include <set>
#include <vector>
#include <algorithm>
#include <chrono>

#include "geom_util.h"
#include "tuple.h"

using namespace std;
using namespace std::chrono;

////
class GeomUtil{
public:
	static double get_edge_length(Node node1, Node node2){
	    return sqrt((node1.x - node2.x)*(node1.x - node2.x) + (node1.y - node2.y)*(node1.y - node2.y));
	}

	static double get_segment_length(Node node, double x, double y){
	    return sqrt((node.x - x)*(node.x - x) + (node.y - y)*(node.y - y));
	}

	static double get_distance(double x1, double y1, double x2, double y2){
	    return sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}

	static PairDouble get_point_on_line(double x1, double y1, double x2, double y2, double length){
	    double vector_len = sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	    double x = x1 + (x2-x1)*length/vector_len;
	    double y = y1 + (y2-y1)*length/vector_len;
	    return PairDouble(x, y);
	}

	static PairDouble get_point_between(double x1, double y1, double x2, double y2, double ratio){
	    double x = x1 + (x2-x1)*ratio;
		double y = y1 + (y2-y1)*ratio;
	    return PairDouble(x, y);
	}

	//
	static PairBoolSeg union_edge(EdgeSegment& seg_1, EdgeSegment& seg_2){
		if (seg_1.cur_edge_id != seg_2.cur_edge_id){
			EdgeSegment _seg;
			return PairBoolSeg(false, _seg);
		}

		// swap (not needed, already sorted)

		// case 0: conincide <start> OR <end> --> return the longer segment
		if ((seg_1.start_x == seg_2.start_x && seg_1.start_y == seg_2.start_y) ||
			(seg_1.end_x == seg_2.end_x && seg_1.end_y == seg_2.end_y)){
			if (EdgeSegment::square_length(seg_1) > EdgeSegment::square_length(seg_2))
				return PairBoolSeg(true, seg_1);
			else
				return PairBoolSeg(true, seg_2);
		}

		//
		if (seg_2.start_x < seg_1.end_x){
			if (seg_2.end_x >= seg_1.end_x){    //overlapped
				EdgeSegment _seg = EdgeSegment(seg_1.start_x, seg_1.start_y, seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id );
				return PairBoolSeg(true, _seg);
			}else                               //covered
				return PairBoolSeg(true, seg_1);
		}

		if (seg_1.start_x == seg_1.end_x)      //vertical !
			if (seg_1.end_y >= seg_2.start_y){  //not disjoint
				if (seg_1.end_y < seg_2.end_y){ //overlapped
					EdgeSegment _seg = EdgeSegment(seg_1.start_x, seg_1.start_y, seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id );
					return PairBoolSeg(true, _seg);
				}else
					return PairBoolSeg(true, seg_1);        //covered
			}

		EdgeSegment _seg;
		return PairBoolSeg(false, _seg);
	}

	//
	static PairBoolSeg intersect_edge(EdgeSegment& seg_1, EdgeSegment& seg_2){     //Note: seg_1, seg_2 are already normalized
		// case 0: conincide <start> OR <end> --> return the shorter segment
		if ((seg_1.start_x == seg_2.start_x && seg_1.start_y == seg_2.start_y) ||
			(seg_1.end_x == seg_2.end_x && seg_1.end_y == seg_2.end_y)){
			if (EdgeSegment::square_length(seg_1) < EdgeSegment::square_length(seg_2))
				return PairBoolSeg(true, seg_1);
			else
				return PairBoolSeg(true, seg_2);
		}

		//
		if (seg_2.start_x < seg_1.end_x){
			if (seg_2.end_x >= seg_1.end_x){    //overlapped
				EdgeSegment _seg = EdgeSegment(seg_2.start_x, seg_2.start_y, seg_1.end_x, seg_1.end_y, seg_1.cur_edge_id );
				return PairBoolSeg(true, _seg);
			}
			else                               //covered
				return PairBoolSeg(true, seg_2);
		}

		if (seg_1.start_x == seg_1.end_x)      //vertical !
			if (seg_1.end_y >= seg_2.start_y){  //not disjoint
				if (seg_1.end_y < seg_2.end_y){ //overlapped
					EdgeSegment _seg = EdgeSegment(seg_1.start_x, seg_1.start_y, seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id );
					return PairBoolSeg(true, _seg);
				}
				else
					return PairBoolSeg(true, seg_2);        //covered
			}

		EdgeSegment _seg;
		return PairBoolSeg(false, _seg);
	}

	//
	static bool is_mmb_cover(MBR mbr, Point point, double radius){

		if ((mbr.min_x <= point.x && point.x <= mbr.max_x) && (mbr.min_y - radius <= point.y && point.y <= mbr.max_y + radius))
			return true;
		if ((mbr.min_x - radius <= point.x && point.x <= mbr.max_x + radius) && (mbr.min_y <= point.y && point.y <= mbr.max_y))
			return true;
		if (GeomUtil::get_distance(point.x, point.y, mbr.min_x, mbr.min_y) <= radius)
			return true;
		if (GeomUtil::get_distance(point.x, point.y, mbr.min_x, mbr.max_y) <= radius)
			return true;
		if (GeomUtil::get_distance(point.x, point.y, mbr.max_x, mbr.min_y) <= radius)
			return true;
		if (GeomUtil::get_distance(point.x, point.y, mbr.max_x, mbr.max_y) <= radius)
			return true;

		return false;
	}

    //
	static vector<EdgeSegment> clean_fixed_expanding(vector<EdgeSegment> result){
		vector<EdgeSegment> new_result = result;

		// 1. NORMALIZE each edge (left_low_x first)
		for (EdgeSegment item : new_result)
			item.normalize();

		// 2. SORT by cur_edge_id
//        System.out.println("new_result.size = " + new_result.size());
		sort(new_result.begin(), new_result.end());

		// DEBUG
//        System.out.println("AFTER sorting");
//        System.out.println("length(new_result) = " + new_result.size());
//        for (EdgeSegment item : new_result)
//        	System.out.println(String.format("%15d %8.2f %10.2f %10.2f %10.2f %10.2f", item.cur_edge_id, EdgeSegment.length(item),
//                item.start_x, item.start_y, item.end_x, item.end_y));

		// 3. REMOVE duplicates
		int cur = 0;
		while (cur < new_result.size()-1){
			if (new_result[cur+1].cur_edge_id == new_result[cur].cur_edge_id &&
				new_result[cur+1].start_x == new_result[cur].start_x &&
				new_result[cur+1].start_y == new_result[cur].start_y &&
				new_result[cur+1].end_x == new_result[cur].end_x &&
				new_result[cur+1].end_y == new_result[cur].end_y){
				new_result.erase(new_result.begin() + (cur+1) );
				continue;
			}
			else
				cur += 1;
		}

		// DEBUG
//        System.out.println("AFTER removing duplicates");
//        System.out.println("length(new_result) = " + new_result.size());
//        for (EdgeSegment item : new_result)
//        	System.out.println(String.format("%15d %8.2f %10.2f %10.2f %10.2f %10.2f", item.cur_edge_id, EdgeSegment.length(item),
//                item.start_x, item.start_y, item.end_x, item.end_y));

		// 4. UNION
		cur = 0;
		while (cur < new_result.size()-1){
			PairBoolSeg test_union = GeomUtil::union_edge(new_result[cur], new_result[cur+1]);
			if (test_union.result == true){
				new_result.erase(new_result.begin() + cur);
				new_result.erase(new_result.begin() + cur);
				new_result.insert(new_result.begin() + cur, test_union.seg);
				continue;
			}
			else
				cur += 1;
		}

		// DEBUG
//        print "length(new_result) = ", length(new_result)
//        for item in new_result:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)



		//
		return new_result;

	}


	//
	static vector<EdgeSegment> union_set(vector<EdgeSegment> set_1, vector<EdgeSegment> set_2){
		vector<EdgeSegment> result = set_1;
		result.insert(result.end(), set_2.begin(), set_2.end());

		return clean_fixed_expanding(result);
	}

	static vector<EdgeSegment> intersect_set(vector<EdgeSegment> set_1, vector<EdgeSegment> set_2){        //Note: set_1, set_2 are already sorted by (cur_edge_id, start_x,...)
		vector<EdgeSegment> result;
		for (EdgeSegment item_1 : set_1)
			for (EdgeSegment item_2 : set_2)
				if (item_1.cur_edge_id == item_2.cur_edge_id){
					PairBoolSeg test_intersect = GeomUtil::intersect_edge(item_1, item_2);
					if (test_intersect.result == true)
						result.push_back(test_intersect.seg);
				}
//        result = sorted(result, key=lambda edge_segment: (edge_segment.cur_edge_id,
//            edge_segment.start_x, edge_segment.start_y, edge_segment.end_x, edge_segment.end_y))

		return result;
	}


};

class Timer{
public:
	static __int64 get_millisec(){
		__int64 now = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
		return now;
	}
};

////
class WeightedSetCover {
public:

	//
	static set<int> set_intersect(set<int> s1, set<int> s2){
		set<int> ret;
		set_intersection(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(ret, ret.begin()));
		return ret;
	}

	//
	static set<int> set_differ(set<int> s1, set<int> s2){
		set<int> ret;
		set_difference(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(ret, ret.begin()));
		return ret;
	}

	// UNWEIGHTED
	static PairSetInt find_max_uncovered(vector<set<int>> S, set<int> R){
	    int max_inter_len = 0;
	    int max_inter_id = -1;
	    for (int i = 0; i < S.size(); i++){
	    	// don't need copy constructor
	    	set<int> s = set_intersect (S[i], R);	// itersection
	        int inter_len = s.size();
	        if (max_inter_len < inter_len){
	            max_inter_len = inter_len;
	            max_inter_id = i;
	        }
	    }

	    if (max_inter_id == -1)
	        return PairSetInt(set<int>(), -1);
	    else
	        return PairSetInt(S[max_inter_id], max_inter_id);
	}

	// (FOR PUBLIC USE)
	// timestamp = 0, S: positive sets, U:universe (compute from S)
	static PairSetListInt find_init_cover(vector<set<int>> S, int num_element){
	    // compute U
	    int marked[num_element] = {0};
	    for (set<int> a_set : S)
	        for (int item : a_set)
	            marked[item] = 1;

	    set<int> U;
	    for (int item = 0; item < num_element; item++)
	    	if (marked[item] == 1)
	    		U.insert(item);
	    cout<<"len(S) = " << S.size()<<endl;
	    cout<<"len(U) = " << U.size()<<endl;

	    set<int> R;	// = new Hashset<int>(U); // deep copy

	    vector<set<int>> C_0;

	    // NON-BOOTSTRAP
	    // sort S
	    __int64 start = Timer::get_millisec();

	    sort(S.begin(), S.end());	// sort decending by .size()

	    cout<<"Sorting - elapsed : " << (Timer::get_millisec() - start) <<endl;

	    start = Timer::get_millisec();
	    while (R.size() != 0){
	        PairSetInt temp = find_max_uncovered(S, R);
	        set<int> S_i = temp.s;
	        int max_inter_id = temp.i;
	        if (max_inter_id == -1)
	            break;

	        C_0.push_back(S_i);
//	        R.removeAll(S_i);			// set difference
	        R = set_differ(U, S_i);		//

	        S.erase(S.begin() + max_inter_id);
	    }

	    cout<<"find_init_cover - elapsed : " << (Timer::get_millisec() - start) <<endl;
	    cout<<"len(R) = " << R.size() <<endl;
	    cout<<"Cover.len: " << C_0.size()<<endl;
	    int num_cloaked_users = U.size() - R.size();

	    int total_C_0 = 0;
	    for (set<int> a_set : C_0)
	    	total_C_0 += a_set.size();
	    cout<<"Total elements in C_0 = " << total_C_0<<endl;

	    return new PairSetListInt(C_0, num_cloaked_users);
	}

	// (FOR PUBLIC USE)
	// timestamp > 0, S: positive sets, U:universe (compute from S), C_0: cover_set from prev.step
	static PairSetListInt find_next_cover(vector<set<int>> S, int num_element, vector<set<int>> C_0, int k_global){
	    // compute U
		int marked[num_element] = {0};
	    for (set<int> a_set : S)
	        for (int item : a_set)
	            marked[item] = 1;

	    set<int> U;
	    for (int item = 0; item < num_element; item++)
	    	if (marked[item] == 1)
	    		U.insert(item);
	    cout<<"len(S) = " << S.size()<<endl;
	    cout<<"len(U) = " << U.size()<<endl;

	    set<int> R; // = new Hashset<int>(U);	// deep copy

	    // result
	    vector<set<int>> C_1;
	    int total_W = 0;

	    // compute weights
	    __int64 start = Timer::get_millisec();

	    vector<vector<int>> list_pairs;     // list of lists, for MMB/MAB checking

	    vector<int> W;

	    for (int i = 0; i < S.size(); i++){

	        W.push_back(0);
//	        list_pairs.add(new Arrayvector<int>());   		//have list_pairs[i]
	        for (int j = 0; j < C_0.size(); j++){
	        	set<int> c_set;

	        	set<int> s_set = S[i];	// copy constructor (moved to here)
	        	c_set = set_intersect(s_set, C_0[j]);				// intersection

	            int inter_len = s_set.size();
	            if (inter_len > 0 && inter_len < k_global)
	                W[i] = W[i] + 1;
	            if (inter_len >= k_global)
	                list_pairs[i].push_back(j);
	        }
	    }
	    cout<<"Computing W - elapsed : " << (Timer::get_millisec() - start) <<endl;

	    // sort W
	    start = Timer::get_millisec();

//	    z = sorted(zip(W,S,list_pairs))     //ZIP: simultaneous sorting --> index sort (by W)
	    IndexSorter<int> is = new IndexSorter<int>(W);
		is.sort();
		int[] idx = is.getIndexes();

//		W_temp, S_temp, list_pairs_temp = zip(*z)
		vector<int> W_temp = new Arrayvector<int>();
		vector<set<int>> S_temp = new Arrayvector<set<int>>();
		vector<vector<int>> list_pairs_temp = new Arrayvector<vector<int>>();
		for (int id = 0; id < idx.length; id++){
			W_temp.add(W[idx[id]));
			S_temp.add(new Hashset<int>(S[idx[id])));	// copy constructor
			list_pairs_temp.add(list_pairs[idx[id]));
		}


	    int i = 0;
	    while (W_temp[i) == 0 && i < W_temp.size())
	        i = i + 1;
	    System.out.println("i = " + i);

	    W.clear();
	    S.clear();
//	    System.out.println("W_temp.size = " + W_temp.size());
//	    System.out.println("W_temp[last] = " + W_temp[W_temp.size()-1));
//	    System.out.println("S_temp.size = " + S_temp.size());

////	    list_pairs.clear();		// WRONG !
//	    for (int id = 0; id < i; id++){
//	    	W.add(W_temp[id));
//	    	S.add(S_temp[id));
//	    	list_pairs.add(list_pairs[id));
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
	        set<int> S_i = temp.s;
	        int min_element = temp.i;
	        if (min_element == -1)
	            break;

	        C_1.add(S_i);
	        total_W = total_W + W[min_element);
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
	    for (set<int> a_set : C_1)
	    	total_C_1 += a_set.size();
	    System.out.println("Total elements in C_0 =" + total_C_1);
	    System.out.println("Total weight = " + total_W);

	    return new PairSetListInt(C_1, num_cloaked_users);		//, checking_pairs
	}
};

#endif /* HELPER_H_ */
