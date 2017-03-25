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

#include "tuple.h"

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

};

#endif /* GEOM_UTIL_H_ */
