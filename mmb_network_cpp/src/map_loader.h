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

#include "geom_util.h"
#include "tuple.h"

using namespace std;

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
	deque<SegItem> stack;
	vector<VisitedEdge> visited;	// list of visited edges
	int max_size = 0;

	//
	SegItem get(){
		return stack.front();
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
		if (stack.size() == 0){
			stack.push_front(seg_item);
			visited.push_back(edge);
			return;
		}

		// insert to the head (queue !)
		stack.push_front(seg_item);
		visited.insert(visited.begin() + insert_visited_loc, edge);
	}

	//
	int get_size(){
		return stack.size();
	}

	//
	void print_all(){
//		cout<<"STACK"<<endl;
//		for (deque<SegItem>::iterator seg = stack.begin(); seg < stack.end(); seg++)
//			cout<<*seg->cur_edge_id << " " <<  *seg->x << " " <<  *seg->y << " " <<  *seg->end_x << " " <<  *seg->end_y << " " <<
//					*seg->is_node << " " << *seg->length << "\n";
//		cout<<"VISITED"<<endl;
//		for (vector<VisitedEdge>::iterator edge = visited.begin(); edge < visited.end(); edge++)
//			cout<< *edge->cur_edge_id << " " <<  *edge->start_x << " " <<  *edge->start_y<<"\n";
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

};

#endif /* MAP_LOADER_H_ */
