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
	cout<< "inputFile = " << inputFile <<endl;
	ifstream fin(inputFile);

	// Verify that the file has been successfully opened.
	if(!fin) {
		cout << "Cannot open file.\n";
		return;
	}

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
        cout<< node_id << " " << node_x << " " << node_y << " " << temp <<endl;

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
	cout<< "inputFile = " << inputFile <<endl;
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
		cout<< edge_id << " " << start_node_id << " " << end_node_id <<endl;

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

//		edges[edge_id] = Edge(edge_id, start_node_id, end_node_id, edge_class);
//		edges[edge_id].edge_length = Edge::length(nodes[start_node_id], nodes[end_node_id]);

		total_map_len += GeomUtil::get_edge_length(nodes[start_node_id], nodes[end_node_id]);

		//
//		if (node_to_edges.count(start_node_id) == 0)
//			node_to_edges[start_node_id] = vector<int>;
		node_to_edges[start_node_id].push_back(edge_id);

//		if (!node_to_edges.count(end_node_id) == 0)
//			node_to_edges[end_node_id] = vector<int>;
		node_to_edges[end_node_id].push_back(edge_id);
	}
}



