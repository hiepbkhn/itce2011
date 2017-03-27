/*
 * map_loader.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#include <iostream>
#include <fstream>


#include "map_loader.h"

void MMBMap::read_map(string path, string map_name){

	// 1. NODES
	string inputFile = path + map_name + ".node.txt";
	cout<< "nodeFile = " << inputFile <<endl;
	ifstream fin(inputFile);

	// Verify that the file has been successfully opened.
	if(!fin) {
		cout << "Cannot open file.\n";
		return;
	}

	min_x = 1000000000;
	min_y = 1000000000;
	max_x = -1000000000;
	max_y = -1000000000;

	while (!fin.eof()){
		int node_id = 0;
		int node_x;
		int node_y;
		string temp;

		fin >> node_id;
		fin >> node_x;
		fin >> node_y;
		fin >> temp;

		nodes[node_id] = Node(node_id, node_x, node_y);
		xy_to_node_id[PairInt(node_x, node_y)] = node_id;

		// DEBUG
//        cout<< node_id << " " << node_x << " " << node_y << " " << temp <<endl;

		//
		if (min_x > node_x)
			min_x = node_x;
		if (min_y > node_y)
			min_y = node_y;
		if (max_x < node_x)
			max_x = node_x;
		if (max_y < node_y)
			max_y = node_y;
	}

	fin.close();

	cout<< "min,max (X,Y) " << min_x << " " << min_y << " " << max_x << " " << max_y <<endl;
	double dx = max_x - min_x;
	double dy = max_y - min_y;
	cout<<"dx, dy : " << " " << dx << " " << dy <<endl;

	area = dx * dy;
	cout<< "map.area " << area<<endl;

	// 2. EDGES
	inputFile = path + map_name + ".edge.txt";
	cout<< "edgeFile = " << inputFile <<endl;
	fin = ifstream(inputFile);

	while (!fin.eof()){
		int edge_id;
		int edge_class;
		int start_node_id;
		int end_node_id;
		string temp;

		fin >> edge_id;
		fin >> edge_class;
		fin >> start_node_id;
		fin >> end_node_id;
		fin >> temp;

		// DEBUG
//		cout<< edge_id << " " << start_node_id << " " << end_node_id <<endl;

		// CHECK
		if (start_node_id == end_node_id)
//                print "ERROR (start_node_id == end_node_id) at edge_id =", edge_id
			continue;
		if (node_pair_to_edge.count(PairInt(start_node_id, end_node_id)) > 0 )
//                print "ERROR (duplicate edge) at edge_id =", edge_id
			continue;

		Edge edge = Edge(edge_id, start_node_id, end_node_id, edge_class);
		edge.edge_length = Edge::length(nodes[start_node_id], nodes[end_node_id]);
		edges[edge_id] = edge;

		total_map_len += GeomUtil::get_edge_length(nodes[start_node_id], nodes[end_node_id]);

		//
//		if (node_to_edges.count(start_node_id) == 0)
//			node_to_edges[start_node_id] = vector<int>;
		node_to_edges[start_node_id].push_back(edge_id);

//		if (!node_to_edges.count(end_node_id) == 0)
//			node_to_edges[end_node_id] = vector<int>;
		node_to_edges[end_node_id].push_back(edge_id);

		//
//		if (!adj.containsKey(start_node_id))
//			adj.put(start_node_id, new ArrayList<Integer>());
		adj[start_node_id].push_back(end_node_id);

//		if (!adj.containsKey(end_node_id))
//			adj.put(end_node_id, new ArrayList<Integer>());
		adj[end_node_id].push_back(start_node_id);

		//
		node_pair_to_edge[PairInt(start_node_id, end_node_id)] = edge_id;
		node_pair_to_edge[PairInt(end_node_id, start_node_id)] = edge_id;
	}

}

int MMBMap::get_nearest_edge_id(int next_node_x, int next_node_y, double px, double py){
	double min_distance = 100000000.0;
	int nearest_edge_id = -1;

	int next_node_id = get_next_node_id(next_node_x, next_node_y);

	//print type(node_to_edges[next_node_id])

	for (int edge_id : node_to_edges[next_node_id]){
		int start_node_id = edges[edge_id].start_node_id;
		int end_node_id = edges[edge_id].end_node_id;
		//
		double length = LineOperator::distance_to(nodes[start_node_id].x, nodes[start_node_id].y,
			nodes[end_node_id].x, nodes[end_node_id].y, px, py);
		if (length < min_distance){
			min_distance = length;
			nearest_edge_id = edge_id;
		}
	}

	return nearest_edge_id;
}

vector<EdgeSegment> MMBMap::compute_fixed_expanding(double x, double y, int cur_edge_id, double length){

	vector<EdgeSegment>result;

	MMBStack stack;
	//
	bool is_node = get_next_node_id((int)x, (int)y) > -1;

	stack.push(SegItem(x, y, -1, -1, length, cur_edge_id, is_node));

	while (stack.get_size() > 0){
		// DEBUG
//            stack.print_all()
//            print "END OF LIST"
		//
		SegItem item = stack.get();
//            if item.length == 0:
//                result.append(item)
//                continue

		// case 1, is_node == True
		if (item.is_node == true){
			int node_id = get_next_node_id((int)item.x, (int)item.y);

			for (int end_node_id : adj[node_id]){   //traverse adjacent edges...
				double edge_len = GeomUtil::get_edge_length(nodes[node_id], nodes[end_node_id]);
				if (edge_len < item.length){
					double remaining_len = item.length - edge_len;
					//
					result.push_back(EdgeSegment(nodes[node_id].x, nodes[node_id].y,
										  nodes[end_node_id].x, nodes[end_node_id].y,
										  node_pair_to_edge[PairInt(node_id, end_node_id)] ));
					//
					for (int edge_id : node_to_edges[end_node_id]) //one choice for each adjacent edge
						stack.push(SegItem(nodes[end_node_id].x, nodes[end_node_id].y,
											 -1, -1,
											 remaining_len, edge_id, true));

				}else{
					double end_x = item.x + item.length * (nodes[end_node_id].x - item.x) / edge_len;
					double end_y = item.y + item.length * (nodes[end_node_id].y - item.y) / edge_len;
					result.push_back(EdgeSegment(nodes[node_id].x, nodes[node_id].y,
										  end_x, end_y,
										  node_pair_to_edge[PairInt(node_id, end_node_id)] ));
				}
			}
		}
		// case 2, is_node == False
		else{
			int id_list[2] = {edges[item.cur_edge_id].start_node_id, edges[item.cur_edge_id].end_node_id};
			for (int end_node_id : id_list){
				double segment_len = GeomUtil::get_segment_length(nodes[end_node_id], item.x, item.y);
				if (segment_len < item.length){
					double remaining_len = item.length - segment_len;
					// end_node_id.xy go first to comply with convention: first point always graph node !!
					result.push_back(EdgeSegment(nodes[end_node_id].x, nodes[end_node_id].y,
										  item.x, item.y,
										  item.cur_edge_id));
					//
					for (int edge_id : node_to_edges[end_node_id])
						stack.push(SegItem(nodes[end_node_id].x, nodes[end_node_id].y,
										 -1, -1,
										 remaining_len, edge_id, true));
				}
				else{
					double end_x = item.x + item.length * (nodes[end_node_id].x - item.x) / segment_len;
					double end_y = item.y + item.length * (nodes[end_node_id].y - item.y) / segment_len;
					result.push_back(EdgeSegment(item.x, item.y,
										  end_x, end_y,
										  item.cur_edge_id));
				}
			}
		}
	}


	return result;
}


