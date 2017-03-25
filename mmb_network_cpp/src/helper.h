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

};

#endif /* HELPER_H_ */
