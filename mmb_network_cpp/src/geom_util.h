/*
 * geom_util.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef GEOM_UTIL_H_
#define GEOM_UTIL_H_

#include <string>
#include <cmath>

using namespace std;




////
class Node{
public:
	int node_id;
	int x;
	int y;

	Node(): node_id(-1), x(0), y(0){}

	Node(int _node_id, int _x, int _y){
		node_id = _node_id;
		x = _x;
		y = _y;
	}

};

////
class Edge{
public:
	int edge_id = -1;
	int start_node_id = -1;
	int end_node_id = -1;
	int edge_class = -1;
	double edge_length = 0.0;

	Edge(){

	}

	Edge(int _edge_id, int _start_node_id, int _end_node_id, int _edge_class) {
		edge_id = _edge_id;
		start_node_id = _start_node_id;
		end_node_id = _end_node_id;
		edge_class = _edge_class;
	}

	static double length(Node node_1, Node node_2){
		return sqrt((node_1.x - node_2.x)*(node_1.x - node_2.x) + (node_1.y - node_2.y)*(node_1.y - node_2.y));
	}
};

////
class Query{
public:
	int obj_id = -1;
	double x = 0.0;
	double y = 0.0;
	int timestamp = -1;
	int next_node_x = 0;
	int next_node_y = 0;
	int next_node_id = -1;
	int cur_edge_id = -1;
	//
	int k_anom = 0;
	double min_length = 0.0;
	double dist = 0.0;

	//


};

////
class Point{
public:
	double x;
	double y;
	int cur_edge_id;

	Point(): x(0.0), y(0.0), cur_edge_id(0){}
	Point(Query query){
		x = query.x;
		y = query.y;
		cur_edge_id = query.cur_edge_id;
	}


};



////
class LineOperator{
public:
	static double distance_to(double x1, double y1, double x2, double y2, double px, double py){
		double normalLength = sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		return abs((px-x1)*(y2-y1)-(py-y1)*(x2-x1))/normalLength;
	}
};


////
class EdgeSegment{
public:
	double start_x;
	double start_y;
	double end_x;
	double end_y;
	int cur_edge_id;

	EdgeSegment(){
		start_x = 0;
		start_y = 0;
		end_x = 0;
		end_y = 0;
		cur_edge_id = -1;
	}

	//
	EdgeSegment(double _start_x, double _start_y, double _end_x, double _end_y, int _cur_edge_id) {
		start_x = _start_x;
		start_y = _start_y;
		end_x = _end_x;
		end_y = _end_y;
		cur_edge_id = _cur_edge_id;
	}

	//
	void normalize(){
		if (start_x > end_x || (start_x == end_x && start_y > end_y)){
			double temp = start_x;
			start_x = end_x;
			end_x = temp;
			temp = start_y;
			start_y = end_y;
			end_y = temp;
		}
	}

	//
	static double square_length(EdgeSegment seg){
		return (seg.start_x - seg.end_x)*(seg.start_x - seg.end_x) + (seg.start_y - seg.end_y)*(seg.start_y - seg.end_y);
	}

	static double length(EdgeSegment seg){
		return sqrt((seg.start_x - seg.end_x)*(seg.start_x - seg.end_x) + (seg.start_y - seg.end_y)*(seg.start_y - seg.end_y));
	}

	static bool is_line_cover(Point point, EdgeSegment line){
		if ((point.x == line.start_x && point.y == line.start_y) ||
			(point.x == line.end_x && point.y == line.end_y))
			return true;
		if (line.start_x == line.end_x)
			return (point.y - line.start_y)*(point.y - line.end_y) < 0;
		if (line.start_y == line.end_y)
			return (point.x - line.start_x)*(point.x - line.end_x) < 0;

		return (point.y - line.start_y)*(point.y - line.end_y) < 0;
	}



//	int compareTo(EdgeSegment arg0) {
//		if (cur_edge_id < arg0.cur_edge_id)
//			return -1;
//		else if (cur_edge_id > arg0.cur_edge_id)
//			return 1;
//
//		if (start_x < arg0.start_x)
//			return -1;
//		else if (start_x > arg0.start_x)
//			return 1;
//
//		if (start_y < arg0.start_y)
//			return -1;
//		else if (start_y > arg0.start_y)
//			return 1;
//
//		if (end_x < arg0.end_x)
//			return -1;
//		else if (end_x > arg0.end_x)
//			return 1;
//
//		if (end_y < arg0.end_y)
//			return -1;
//		else if (end_y > arg0.end_y)
//			return 1;
//
//		return 0;
//	}

};





#endif /* GEOM_UTIL_H_ */
