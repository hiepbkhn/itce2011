/*
 * helper.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Nguyen Huu Hiep
 */

#ifndef HELPER_H_
#define HELPER_H_

#include "geom_util.h"
#include "tuple.h"

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

#endif /* HELPER_H_ */
