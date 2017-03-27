/*
 * mmb_map.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef MAP_LOADER_H_
#define MAP_LOADER_H_

#include <string>
#include <cmath>
#include <iostream>
#include <deque>
#include <list>
#include <vector>
#include <map>
#include <algorithm>

#include "geom_util.h"
#include "tuple.h"
#include "helper.h"
#include "option.h"

using namespace std;

extern Option option;

////
class SegItem{
public:
	double x = 0.0;
	double y = 0.0;
	double end_x = 0.0;
	double end_y = 0.0;
	double length = 0.0;
	int cur_edge_id = -1;	// = -1 if is_node = true
	bool is_node = false;

	SegItem(const SegItem& other){	// copy constructor
		x = other.x;
		y = other.y;
		end_x = other.end_x;
		end_y = other.end_y;
		length = other.length;
		cur_edge_id = other.cur_edge_id;
		is_node = other.is_node;

	}
	//
	SegItem(double _x, double _y, double _end_x, double _end_y, double _length, int _cur_edge_id, bool _is_node) {
		x = _x;
		y = _y;
		end_x = _end_x;
		end_y = _end_y;
		length = _length;
		cur_edge_id = _cur_edge_id;
		is_node = _is_node;
	}
};

////
class VisitedEdge {
public:
	double start_x;
	double start_y;
	int cur_edge_id;

	//
	VisitedEdge(int _cur_edge_id, double _start_x, double _start_y) {
		start_x = _start_x;
		start_y = _start_y;
		cur_edge_id = _cur_edge_id;
	}
};

////
class MMBStack {
public:
	deque<SegItem> queue;
	vector<VisitedEdge> visited;	// list of visited edges
	int max_size = 0;

	//
	SegItem get(){
		SegItem ret = SegItem(queue.front());	// copy constructor
		queue.pop_front();
		return ret;
	}

	//
	int compare(SegItem seg_item_1, SegItem seg_item_2){
		if (seg_item_1.x == seg_item_2.x && seg_item_1.y == seg_item_2.y)
			return 0;
		if ((seg_item_1.x < seg_item_2.x) || (seg_item_1.x == seg_item_2.x && seg_item_1.y < seg_item_2.y))
			return 1;
		else
			return -1;
	}

	//
	int compare_visited(VisitedEdge edge_1, VisitedEdge edge_2){
		if (edge_1.cur_edge_id < edge_2.cur_edge_id)
			return 1;
		if (edge_1.cur_edge_id > edge_2.cur_edge_id)
			return -1;

		if (edge_1.start_x == edge_2.start_x && edge_1.start_y == edge_2.start_y)
			return 0;
		if ((edge_1.start_x < edge_2.start_x) || (edge_1.start_x == edge_2.start_x && edge_1.start_y < edge_2.start_y))
			return 1;
		else
			return -1;
	}

	//
	void push(SegItem seg_item){
		// check if seg_item is already in visited[]
		int insert_visited_loc = 0;
		VisitedEdge edge = VisitedEdge(seg_item.cur_edge_id, seg_item.x, seg_item.y);
		if (visited.size() > 0){
			bool found_visited = false;
			int lo = 0;
			int hi = visited.size() - 1;
			int mid = (lo + hi) / 2;

			while (true){
				if (compare_visited(visited.at(mid), edge) == 0){
					found_visited = true;
					break;
				}

				if (compare_visited(visited.at(mid), edge) == -1){
					hi = mid - 1;
					if (hi < lo)
						break;
				}
				else{
					lo = mid + 1;
					if (lo > hi)
						break;
				}
				mid = (lo + hi) / 2;
			}

			insert_visited_loc = lo;
			if (found_visited == true)
				return;
		}

		// check if seg_item.xy exist
		if (queue.size() == 0){
			queue.push_front(seg_item);
			visited.push_back(edge);
			return;
		}

		// insert to the head (queue !)
		queue.push_back(seg_item);
		visited.insert(visited.begin() + insert_visited_loc, edge);
	}

	//
	int get_size(){
		return queue.size();
	}

	//
	void print_all(){
		cout<<"STACK"<<endl;
		for (deque<SegItem>::iterator seg = queue.begin(); seg != queue.end(); seg++)
			cout<<(*seg).cur_edge_id << " " <<  (*seg).x << " " <<  (*seg).y << " " <<  (*seg).end_x << " " <<  (*seg).end_y << " " <<
					(*seg).is_node << " " << (*seg).length << "\n";
		cout<<"VISITED"<<endl;
		for (vector<VisitedEdge>::iterator edge = visited.begin(); edge != visited.end(); edge++)
			cout<< (*edge).cur_edge_id << " " <<  (*edge).start_x << " " <<  (*edge).start_y<<"\n";
	}
};


////
class MMBMap{
public:
	map<int, Node> nodes;
	map<int, Edge> edges;

	map<int, vector<int>> adj;          //adjacent lists (list of lists)
	map<int, vector<int>> node_to_edges;

	map<PairInt, int> xy_to_node_id;
	map<PairInt, int> node_pair_to_edge;

	double min_x = 0;
	double min_y = 0;
	double max_x = 0;
	double max_y = 0;
	double dx = 0;
	double dy = 0;
	double area = 0;   // = dx*dy
	double total_map_len = 0.0;

	//
	void read_map(string path, string map_name);

	int get_next_node_id(int next_node_x, int next_node_y){
		if (xy_to_node_id.count(PairInt(next_node_x, next_node_y)) > 0 )
			return xy_to_node_id[PairInt(next_node_x, next_node_y)];
		else
			return -1;
	}

	bool is_full_edge(EdgeSegment seg){
		Edge edge = edges[seg.cur_edge_id];
		Node start_node = nodes[edge.start_node_id];
		Node end_node = nodes[edge.end_node_id];
		if (seg.start_x == start_node.x && seg.start_y == start_node.y &&
			seg.end_x == end_node.y && seg.end_y == end_node.y)
			return true;
		if (seg.start_x == end_node.x && seg.start_y == end_node.y &&
			seg.end_x == start_node.y && seg.end_y == start_node.y)
			return true;
		return false;
	}

	int get_nearest_edge_id(int next_node_x, int next_node_y, double px, double py);
	vector<EdgeSegment> compute_fixed_expanding(double x, double y, int cur_edge_id, double length);

	//
	vector<EdgeSegment> compute_mesh_expanding(vector<EdgeSegment> item_list, double length){
		vector<EdgeSegment> result = item_list;
		//1. call find_boundary_points()
		vector<TripleDoubleInt> boundary_points = MMBMap::find_boundary_points(item_list);

		//2.
		for (TripleDoubleInt point : boundary_points){
			vector<EdgeSegment> new_seg_set = compute_fixed_expanding(point.v0, point.v1, point.v2, option.MAX_SPEED);
			// OLD
//            new_seg_set = EdgeSegmentSet.clean_fixed_expanding(new_seg_set)
//            result = EdgeSegmentSet.union(result, new_seg_set)
			// NEW
			result.insert(result.end(), new_seg_set.begin(), new_seg_set.end());
		}

		GeomUtil::clean_fixed_expanding(result);

		return result;
	}
	//
	bool is_node_in_rec(double min_x, double min_y, double max_x, double max_y, Node node){
		int p1_x = node.x;
		int p1_y = node.y;
		return (min_x <= p1_x) && (p1_x <= max_x) && (min_y <= p1_y) && (p1_y <= max_y);
	}
	//
	bool is_edge_in_rec(double min_x, double min_y, double max_x, double max_y, Edge edge){
		return is_node_in_rec(min_x, min_y, max_x, max_y, nodes[edge.start_node_id]) &&
				is_node_in_rec(min_x, min_y, max_x, max_y, nodes[edge.end_node_id]);
	}
	//
	TripleDouble get_line_equation(double x1, double y1, double x2, double y2){
		return TripleDouble(y2-y1, x1-x2, y1*x2-y2*x1);
	}
	//
	bool is_edge_cut_rec(double min_x, double min_y, double max_x, double max_y, Edge edge){
		int x1 = nodes[edge.start_node_id].x;
		int y1 = nodes[edge.start_node_id].y;
		int x2 = nodes[edge.end_node_id].x;
		int y2 = nodes[edge.end_node_id].y;
		TripleDouble coeff = get_line_equation(x1, y1, x2, y2);
		double a1 = coeff.v0*min_x + coeff.v1*min_y + coeff.v2;
		double a2 = coeff.v0*min_x + coeff.v1*max_y + coeff.v2;
		double a3 = coeff.v0*max_x + coeff.v1*max_y + coeff.v2;
		double a4 = coeff.v0*max_x + coeff.v1*min_y + coeff.v2;

		if  (is_node_in_rec(min_x, min_y, max_x, max_y, nodes[edge.start_node_id]) ||
			is_node_in_rec(min_x, min_y, max_x, max_y, nodes[edge.end_node_id]))
			return true;
		else
			if ((a1*a2 < 0 && (x1-min_x)*(x2-min_x) < 0) || (a2*a3 < 0 && (y1-max_y)*(y2-max_y) < 0)
				|| (a3*a4 < 0 && (x1-max_x)*(x2-max_x) < 0) || (a4*a1 < 0 && (y1-min_y)*(y2-min_y) < 0))
			return true;

		return false;
	}
	//
	vector<EdgeSegment> compute_mesh_mbr(vector<Point> locations){
		vector<EdgeSegment> result;
		//
		double min_x = 100000000;
		double min_y = 100000000;
		double max_x = -100000000;
		double max_y = -100000000;
		for (Point point : locations){
			if (min_x > point.x)
				min_x = point.x;
			if (min_y > point.y)
				min_y = point.y;
			if (max_x < point.x)
				max_x = point.x;
			if (max_y < point.y)
				max_y = point.y;
		}

//        print "compute_mesh_mbr - min,max (X,Y)", min_x, min_y, max_x, max_y

		//Solution 1: linear scan
//        start = time.clock()
		for (map<int, Edge>::iterator it = edges.begin(); it != edges.end(); it++)
			if (is_edge_cut_rec(min_x, min_y, max_x, max_y, it->second)){
				double p1_x = nodes[it->second.start_node_id].x;
				double p1_y = nodes[it->second.start_node_id].y;
				double p2_x = nodes[it->second.end_node_id].x;
				double p2_y = nodes[it->second.end_node_id].y;
				result.push_back(EdgeSegment(p1_x, p1_y, p2_x, p2_y, it->second.edge_id));
			}


		return result;
	}
	static vector<TripleDoubleInt> find_boundary_points(vector<EdgeSegment> item_list){
		// WAY-2
		vector<TripleDoubleInt> nodes;
		vector<TripleDoubleInt> result;
		for (EdgeSegment item : item_list){
			nodes.push_back(TripleDoubleInt(item.start_x, item.start_y, item.cur_edge_id));
			nodes.push_back(TripleDoubleInt(item.end_x, item.end_y, item.cur_edge_id));
		}

		// sort
		sort(nodes.begin(), nodes.end());

		for (int i = 0; i < nodes.size()-1; i++)
			if ((nodes[i+1].v0 != nodes[i].v0) || (nodes[i+1].v1 != nodes[i].v1))
				result.push_back(nodes[i]);

		if ((nodes[nodes.size()-1].v0 != nodes[nodes.size()-2].v0) || (nodes[nodes.size()-1].v1 != nodes[nodes.size()-2].v1))
			result.push_back(nodes[nodes.size()-1]);


		return result;


	}


};

#endif /* MAP_LOADER_H_ */
