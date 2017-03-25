/*
 * Graph.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#include <iostream>
#include <map>

#include "map_loader.h"
#include "geom_util.h"
#include "tuple.h"
#include "helper.h"

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

	// TEST std::sort()
//	vector<PairInt> vec;
//	vec.push_back(PairInt(0,1));
//	vec.push_back(PairInt(4,1));
//	vec.push_back(PairInt(3,4));
//	vec.push_back(PairInt(2,2));
//
//	sort(vec.begin(), vec.end());
//
//	for(vector<PairInt>::iterator it = vec.begin(); it != vec.end(); it++)
//		cout<<(*it).x << " " << (*it).y <<endl;

	// TEST GeomUtil::union_edge()/intersect_edge()
//	EdgeSegment seg1 = EdgeSegment(0,0,4,0,1);
//	EdgeSegment seg2 = EdgeSegment(3,0,6,0,1);
//
//	PairBoolSeg p = GeomUtil::union_edge(seg1, seg2);
//	EdgeSegment seg = p.seg;
//	cout<< seg.start_x << " "<< seg.start_y << " "<< seg.end_x << " "<< seg.end_y <<endl;
//
//	p = GeomUtil::intersect_edge(seg1, seg2);
//	seg = p.seg;
//	cout<< seg.start_x << " "<< seg.start_y << " "<< seg.end_x << " "<< seg.end_y <<endl;


	// TEST MMBMap
//	MMBMap map_data;
//	map_data.read_map("data/", "oldenburgGen");
//
//	cout<<map_data.adj[3700].size() <<endl;



}


