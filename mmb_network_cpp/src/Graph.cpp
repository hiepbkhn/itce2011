/*
 * Graph.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#include <iostream>
#include <fstream>
#include <map>
#include <vector>
#include <set>

#include "map_loader.h"
#include "query_loader.h"
#include "geom_util.h"
#include "tuple.h"
#include "helper.h"
#include "option.h"

using namespace std;

Option option = Option("mmb.conf");

class Graph{
public:
	int num_user;
	MMBMap map_data;
	QueryLog query_log;

	map<int, int> user_mc_set;  //dict of clique_id, e.g. user_mc_set[1] = 2 (clique_id = 2)
	vector<set<int>> mc_set;        //maximal clique set, list of sets
	map<PairInt, int> graph_edges;   //dict of pair (u,w)
	map<int, Query>last_query;    //dict of (user, last query)
	map<int, vector<EdgeSegment>> user_mesh;      //last cloaked (published) region
	//
	vector<set<int>> positive_mc_set;   //list of sets
	map<int, int> old_user_mc_set;
	map<int, vector<EdgeSegment>> old_user_mesh;
	//
	vector<set<int>> cover_set;     // list of sets
	vector<vector<EdgeSegment>> cover_mesh;   // list of meshes,
							// checked agaist Option.S_GLOBAL at the end of solve_new_queries()
//    cover_mesh_mmb;
//    //
	vector<set<int>> new_cover_set;     // list of sets
	vector<vector<EdgeSegment>> new_cover_mesh;   // list of meshes,

	///////////
	Graph(int _num_user, MMBMap _map_data, QueryLog _query_log){
		num_user = _num_user;
		map_data = _map_data;
		query_log = _query_log;
	}

	//
    void reset(){
        num_user = 0;
        user_mc_set.clear();   //dict of sets
        mc_set.clear();        //maximal clique set, list of sets
        //
        old_user_mc_set.clear();
        old_user_mesh.clear();
        //
        user_mesh.clear();
    }

    //
    // copied from EdgeSegmentSet.is_set_cover()
    vector<int> find_partial_coverage(vector<PairEdgeSegInt> part_edges, Query query){
        if (part_edges.size() == 0)
            return vector<int>();

        //binary search
        int lo = 0;
        int hi = part_edges.size() - 1;
        int mid = (lo + hi) / 2;
        bool found = false;
        while (true){
            if (part_edges[mid].e.cur_edge_id == query.cur_edge_id){
                found = true;
                break;
            }
            if (part_edges[mid].e.cur_edge_id > query.cur_edge_id){
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

        if (found == false)
            return vector<int>();

        //
        lo = mid;
        while (lo-1 > 0 && part_edges[lo-1].e.cur_edge_id == query.cur_edge_id)
            lo = lo - 1;
        hi = mid;
        while (hi+1 < part_edges.size() && part_edges[hi+1].e.cur_edge_id == query.cur_edge_id)
            hi = hi + 1;

        vector<int> result;
        for (int i = lo; i < hi+1; i++){
        	PairEdgeSegInt pair = part_edges[i];
            if (EdgeSegment::is_line_cover(Point(query), pair.e) == true)
                result.push_back(pair.obj_id);
        }


        return result;
    }

    //
    bool is_reverse_existed(int u, int v, vector<PairInt> directed_edges){     //check (v,u) in directed_edges
        //binary search
        int lo = 0;
        int hi = directed_edges.size() - 1;
        int mid = (lo + hi) / 2;
        bool found = false;
        while (true){
            if (directed_edges[mid].x == v){
                found = true;
                break;
            }
            if (directed_edges[mid].x > v){
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

        if (found == false)
            return false;

        //
        lo = mid;
        while (lo-1 > 0 && directed_edges[lo-1].x == v)
            lo = lo - 1;
        hi = mid;
        while (hi+1 < directed_edges.size() && directed_edges[hi+1].x == v)
            hi = hi + 1;

    	for (int i = lo; i < hi+1; i++){
        	PairInt pair = directed_edges[i];
            if (pair.y == u)
				return true;
    	}
        return false;
    }

    // build the Constraint Graph
	PairIntList compute_edge_list(map<int, vector<EdgeSegment>> expanding_list, vector<Query> query_list){
		int num_edges = 0;
		vector<PairInt> list_edges;

		vector<PairInt> directed_edges; //(u,v) in directed_edges: mesh(v) contains u

		vector<PairEdgeSegInt> part_edges; // list of partial edges (edge segment)
		map<int, vector<int>> full_edge_meshes;   // full_edge_meshes[e] = list of meshes containing e

		//1.
		for (map<int, vector<EdgeSegment>>::iterator it = expanding_list.begin(); it != expanding_list.end(); it++){
			int obj_id = it->first;
			vector<EdgeSegment> mesh = it->second;
			for (EdgeSegment e : mesh){
				if (map_data.is_full_edge(e) == true){   // FULL edge
//					if (! full_edge_meshes.containsKey(e.cur_edge_id))
//						full_edge_meshes.put(e.cur_edge_id, new Arrayvector<Integer>());
					full_edge_meshes[e.cur_edge_id].push_back(obj_id);
				}
				else                                       // PARTIAL edge
					part_edges.push_back(PairEdgeSegInt(e, obj_id));          // "list" of only one point mesh(obj_id) covers e
			}
		}

		//sort part_edges by e.cur_edge_id
		sort(part_edges.begin(), part_edges.end());

		//2.
		for (Query query : query_list){
			int u = query.obj_id;
			//
			if (full_edge_meshes.count(query.cur_edge_id) > 0){
				vector<int> list_full = full_edge_meshes[query.cur_edge_id];
				for (int v : list_full)
					directed_edges.push_back(PairInt(u, v));    //NOTE: maybe v = u
			}
			//
			vector<int> list_part = find_partial_coverage(part_edges, query);		// sorted_part_edges -> part_edges
			for (int v : list_part)
				directed_edges.push_back(PairInt(u, v));    //NOTE: maybe v = u
		}

		cout<<"directed_edges.len = " <<directed_edges.size() <<endl;
		//sort
		sort(directed_edges.begin(), directed_edges.end());

		//3. find undirected edge: both (u,v) and (v,u) from directed_edges
		for (PairInt pair : directed_edges)
			if (pair.x < pair.y){
				int u = pair.x;
				int v = pair.y;
				if (is_reverse_existed(u, v, directed_edges)){
					num_edges += 1;
					list_edges.push_back(PairInt(u,v));
				}
			}

		return PairIntList(num_edges, list_edges);
	}

	// for mace_go.exe
	void write_list_edges(vector<PairInt> list_edges){
		//
		int max_edge_id = -1;
		for (PairInt e : list_edges){
			if (max_edge_id < e.x)
				max_edge_id = e.x;
			if (max_edge_id < e.y)
				max_edge_id = e.y;
		}

		vector<vector<int>> list_adj;
		for (int i = 0; i < max_edge_id+1; i++)   //list of lists
			list_adj.push_back(vector<int>());

		for (PairInt e : list_edges)
			list_adj[e.x].push_back(e.y);

		//
		ofstream f(option.MAXIMAL_CLIQUE_FILE_IN, ofstream::out);
		for (int u = 0; u < max_edge_id+1; u++){
			if (list_adj[u].size() > 0){
				for (int v : list_adj[u])
					f << v << ",";
			}
			f << "\n";
		}

		f.close();
	}

};

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


	// TEST Option



	// TEST MMBMap
//	MMBMap map_data;
//	map_data.read_map("data/", "oldenburgGen");
//
//	cout<<map_data.adj[3700].size() <<endl;



}


