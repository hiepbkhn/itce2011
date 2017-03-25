/*
 * Graph.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#include <iostream>
#include <map>

#include "geom_util.h"
#include "map_loader.h"
#include "tuple.h"

using namespace std;

int main(){

//	Node a = Node(1,2,3);
//	Edge b;
//
//	cout << a.node_id << " " << a.x << " " << a.y << endl;
//	cout << b.edge_id << " " << b.start_node_id << " " << b.edge_length << endl;

	// TEST map with PairInt key
//	map<PairInt, int> map;
//	map[PairInt(1,2)] = 3;
//
//	cout<< map[PairInt(1,2)] <<endl;

	// TEST MMBMap
	MMBMap map_data;
	map_data.read_map("data/", "oldenburgGen");

}


