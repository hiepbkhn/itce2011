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
#include <unordered_set>
#include <stdlib.h>     /* system, NULL, EXIT_FAILURE */

#include "map_loader.h"
#include "query_loader.h"
#include "geom_util.h"
#include "tuple.h"
#include "helper.h"
#include "option.h"

using namespace std;

Option option = Option("mmb.conf");		// read config

class Graph{
public:
	int num_user;
	MMBMap map_data;
	QueryLog query_log;

	map<int, int> user_mc_set;  //dict of clique_id, e.g. user_mc_set[1] = 2 (clique_id = 2)
	vector<unordered_set<int>> mc_set;        //maximal clique set, list of sets
	map<PairInt, int> graph_edges;   //dict of pair (u,w)
	map<int, Query>last_query;    //dict of (user, last query)
	map<int, vector<EdgeSegment>> user_mesh;      //last cloaked (published) region
	//
	vector<unordered_set<int>> positive_mc_set;   //list of sets
	map<int, int> old_user_mc_set;
	map<int, vector<EdgeSegment>> old_user_mesh;
	//
	vector<unordered_set<int>> cover_set;     // list of sets
	vector<vector<EdgeSegment>> cover_mesh;   // list of meshes,
							// checked agaist option.S_GLOBAL at the end of solve_new_queries()
//    cover_mesh_mmb;
//    //
	vector<unordered_set<int>> new_cover_set;     // list of sets
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
    vector<int> find_partial_coverage(vector<PairEdgeSegInt>& part_edges, Query query){
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
        while (lo-1 > 0 && (part_edges[lo-1].e.cur_edge_id == query.cur_edge_id))
            lo = lo - 1;
        hi = mid;
        while ((hi+1 < part_edges.size()) && (part_edges[hi+1].e.cur_edge_id == query.cur_edge_id))
            hi = hi + 1;
        // DEBUG
//        cout<<"hi-lo = "<<(hi-lo)<<endl;

        vector<int> result;
        for (int i = lo; i < hi+1; i++){
//        	PairEdgeSegInt pair = part_edges[i];
//            if (EdgeSegment::is_line_cover(Point(query), pair.e) == true)
//                result.push_back(pair.obj_id);

			if (EdgeSegment::is_line_cover(Point(query), part_edges[i].e) == true)
				result.push_back(part_edges[i].obj_id);
        }


        return result;
    }

    //
    bool is_reverse_existed(int u, int v, vector<PairInt>& directed_edges){     //check (v,u) in directed_edges
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
	PairIntList compute_edge_list(map<int, vector<EdgeSegment>>& expanding_list, vector<Query>& query_list){
		int num_edges = 0;
		vector<PairInt> list_edges;

		vector<PairInt> directed_edges; //(u,v) in directed_edges: mesh(v) contains u

		vector<PairEdgeSegInt> part_edges; // list of partial edges (edge segment)
		map<int, vector<int>> full_edge_meshes;   // full_edge_meshes[e] = list of meshes containing e

		//1.
		__int64 start = Timer::get_millisec();
		for (map<int, vector<EdgeSegment>>::iterator it = expanding_list.begin(); it != expanding_list.end(); it++){
			int obj_id = it->first;
//			vector<EdgeSegment> mesh = it->second;
			for (EdgeSegment e : it->second){
				if (map_data.is_full_edge(e) == true){   // FULL edge
//					if (! full_edge_meshes.containsKey(e.cur_edge_id))
//						full_edge_meshes[e.cur_edge_id, new Arrayvector<int>());
					full_edge_meshes[e.cur_edge_id].push_back(obj_id);
				}
				else                                       // PARTIAL edge
					part_edges.push_back(PairEdgeSegInt(e, obj_id));          // "list" of only one point mesh(obj_id) covers e
			}
		}
//		cout<<"step 1 - elapsed : " << (Timer::get_millisec() - start) <<endl;


		//sort part_edges by e.cur_edge_id
		start = Timer::get_millisec();
		sort(part_edges.begin(), part_edges.end());
		cout<<"sort - elapsed : " << (Timer::get_millisec() - start) <<endl;
		cout<<"part_edges.size = " << part_edges.size()<<endl;

		//2.
		start = Timer::get_millisec();
		for (Query query : query_list){
			int u = query.obj_id;
			//
			if (full_edge_meshes.count(query.cur_edge_id) > 0){
//				vector<int> list_full = full_edge_meshes[query.cur_edge_id];
				for (int v : full_edge_meshes[query.cur_edge_id])
					directed_edges.push_back(PairInt(u, v));    //NOTE: maybe v = u
			}
			//
			vector<int> list_part = find_partial_coverage(part_edges, query);		// sorted_part_edges -> part_edges
			for (vector<int>::iterator v = list_part.begin(); v != list_part.end(); v++)
				directed_edges.push_back(PairInt(u, (*v)));    //NOTE: maybe v = u
		}

		cout<<"directed_edges.len = " <<directed_edges.size() <<endl;
//		cout<<"step 2 - elapsed : " << (Timer::get_millisec() - start) <<endl;

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
	void write_list_edges(vector<PairInt>& list_edges){
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

	//
	void find_cloaking_sets(int timestamp, map<int, vector<EdgeSegment>>& expanding_list){

		//1. find positive and negative cliques
		positive_mc_set = vector<unordered_set<int>>();	// reset

		vector<unordered_set<int>> negative_mc_set;
		for (unordered_set<int> clique : mc_set){
			if (clique.size() == 1)
				continue;
			//
			double max_min_length = 0;
			int max_k_anom = 0;
			vector<Query> query_list;
			for (int obj_id : clique){
				Query query = query_log.trajs[obj_id][timestamp];
				query_list.push_back(query);
				if (max_min_length < query.min_length)
					max_min_length = query.min_length;
				if (max_k_anom < query.k_anom)
					max_k_anom = query.k_anom;
			}
			//compute length of mesh
			vector<EdgeSegment> mesh;
			for (int obj_id : clique){
//                mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
				// NEW (trial)
				mesh.insert(mesh.end(), expanding_list[obj_id].begin(), expanding_list[obj_id].end());
			}
			// NEW (trial)
			mesh = GeomUtil::clean_fixed_expanding(mesh);

			double clique_len = EdgeSegmentSet::length(mesh);

			//
			if (clique.size() >= max_k_anom &&
					clique_len >= max_min_length * map_data.total_map_len)
				positive_mc_set.push_back(clique);
			else if (clique.size() > 2)
				negative_mc_set.push_back(clique);
		}

		//2.convert negative cliques (heuristically)
		vector<unordered_set<int>> new_negative_mc_set;

		for (unordered_set<int> clique : negative_mc_set){
			vector<Query >query_list;
			for (int obj_id : clique){
				Query query = query_log.trajs[obj_id][timestamp];
				query_list.push_back(query);
			}
			//sort
			sort(query_list.begin(), query_list.end());	// sort by .k_anom

			while (true){
				query_list.pop_back();    //remove the last
				if (query_list.size() == 0)
					break;
				double max_min_length = 0;
				for (Query query : query_list)
					if (max_min_length < query.min_length)
						max_min_length = query.min_length;
				//compute length of mesh
				vector<EdgeSegment> mesh;
				for (Query query : query_list){
//                    mesh = EdgeSegmentSet.union(mesh, expanding_list[query.obj_id])
					// NEW (trial)
					mesh.insert(mesh.end(), expanding_list[query.obj_id].begin(), expanding_list[query.obj_id].end());
				}
				// NEW (trial)
				mesh = GeomUtil::clean_fixed_expanding(mesh);


				double clique_len = EdgeSegmentSet::length(mesh);

				//
				if (query_list.size() >= query_list[query_list.size()-1].k_anom &&
						clique_len >= max_min_length * map_data.total_map_len)
					break;
			}

			//
			if (query_list.size() > 1){
				unordered_set<int> set;
				for (Query query : query_list)
					set.insert(query.obj_id);
				new_negative_mc_set.push_back(set);
			}
		}
		//3.
//        print "positive_mc_set =", positive_mc_set
//        print "new_negative_mc_set =", new_negative_mc_set

		positive_mc_set.insert(positive_mc_set.end(), new_negative_mc_set.begin(), new_negative_mc_set.end());
	}

	//
	void write_positive_mc_set(string filename){
		ofstream f(filename, ofstream::out);
		for (vector<unordered_set<int>>::iterator s = positive_mc_set.begin(); s != positive_mc_set.end(); s++){
			for(int u : (*s))
				f << u << " ";
			f << "\n";
		}

		f.close();

	}

	//
	void solve_new_queries(int timestamp){

		map<int, vector<EdgeSegment>> expanding_list;       //dict of lists
		vector<Query> query_list = query_log.frames[timestamp];       // timestamp


		//0. reset
		reset();

		//1. compute expanding_list
		__int64 start = Timer::get_millisec();
		for (Query query : query_list){
			vector<EdgeSegment> seg_list = map_data.compute_fixed_expanding(query.x, query.y,
											query.cur_edge_id, query.dist);  //old: Option.DISTANCE_CONSTRAINT
			seg_list = GeomUtil::clean_fixed_expanding(seg_list);

			expanding_list[query.obj_id] = seg_list;
		}
		cout<<"expanding_list - elapsed : " << (Timer::get_millisec() - start) <<endl;

		//2. compute mc_set
		start = Timer::get_millisec();
		PairIntList temp = compute_edge_list(expanding_list, query_list);
		int num_edges = temp.num_edges;
		vector<PairInt> list_edges = temp.list_edges;

		cout<<"num_edges= " << num_edges<<endl;
		cout<<"list_edges NEW - elapsed : " << (Timer::get_millisec() - start) <<endl;

		// write list_edges[] to file
		write_list_edges(list_edges);
		//

		start = Timer::get_millisec();
//        // (OLD)
//        graph.add_to_mc_set(list_edges)

		// (NEW) call mace_go.exe
		int ret = system("mace_go.exe M mesh.grh mesh.out");
		printf("The value returned was: %d.\n", ret);

		ifstream f(option.MAXIMAL_CLIQUE_FILE_OUT);

		string line;
		while (getline(f, line)){
			vector<string> node_list = Formatter::split(line, ' ');
			if (node_list.size() < 2)
				continue;
			unordered_set<int> set;
			for (string node : node_list)
				set.insert(stoi(node));
			mc_set.push_back(set);
		}
		f.close();

		cout<<mc_set.size()<<endl;

		cout<<"add_to_mc_set - elapsed : " << (Timer::get_millisec() - start) <<endl;
//        print "mc_set =", mc_set

		//3.
		start = Timer::get_millisec();
		find_cloaking_sets(timestamp, expanding_list);		// compute positive_mc_set

		cout<<"find_cloaking_sets - elapsed : " << (Timer::get_millisec() - start) <<endl;
		// DEBUG
//		write_positive_mc_set("positive_mc_set.0");


		//4. 'Set Cover Problem' (from weighted_set_cover.py)
		start = Timer::get_millisec();

		int num_element = -1;
		for (Query query: query_list)
			if (num_element < query.obj_id)
				num_element = query.obj_id;
		num_element += 1;  // avoid out of range

		int num_cloaked_users = 0;
		if (timestamp == 0){
			PairSetListInt _temp = WeightedSetCover::find_init_cover(positive_mc_set, num_element);

			cover_set = _temp.set_list;
			num_cloaked_users = _temp.i;

			new_cover_set = cover_set;     // for compute CLOAKING MESH
		}
		else{
			PairSetListInt _temp = WeightedSetCover::find_next_cover(positive_mc_set, num_element, cover_set, option.K_GLOBAL);
			new_cover_set = _temp.set_list;
			num_cloaked_users = _temp.i;
		}

		cout<<"Success rate =" << (double)num_cloaked_users/query_list.size() <<endl;
		cout<<"compute cover_set - elapsed : " << (Timer::get_millisec() - start) <<endl;


		//5. compute CLOAKING MESH
		start = Timer::get_millisec();

		double total_mesh_length = 0;
		int total_query = 0;
//		new_cover_mesh = new Arrayvector<vector<EdgeSegment>>();    // NEW
		for (int clique_id = 0; clique_id <new_cover_set.size(); clique_id++){
			unordered_set<int> clique = new_cover_set[clique_id];
			//compute length of mesh
			vector<EdgeSegment> mesh;
			for (int obj_id : clique)
				mesh = GeomUtil::union_set(mesh, expanding_list[obj_id]);

			new_cover_mesh.push_back(mesh);    //NEW

			total_mesh_length += EdgeSegmentSet::length(mesh);
			total_query += clique.size();
		}

		double average_mesh_query = total_mesh_length/total_query;

		cout<<"total_mesh_length = " << total_mesh_length <<endl;
		cout<<"average_mesh_query = " << average_mesh_query <<endl;
		cout<<"Compute CLOAKING MBR - elapsed : " << (Timer::get_millisec() - start) <<endl;
//        print "user_mesh = ", user_mesh


		//5.2 Check MMB/MAB
//        new_cover_mesh_mmb = compute_cover_mesh_mmb(new_cover_mesh, expanding_list)
//
//        if timestamp > 0:
//            start_time = System.currentTimeMillis();
//
//            check_MMB_MAB(checking_pairs, cover_mesh, cover_mesh_mmb, new_cover_mesh, new_cover_mesh_mmb)
//
//            print "check_MMB_MAB() - elapsed : ", (time.clock() - start_time)

		// UPDATE
		cover_set = new_cover_set;
		cover_mesh = new_cover_mesh;
//        cover_mesh_mmb = new_cover_mesh_mmb


		//6. compute user_mc_set (max clique for each obj_id), replace positive_mc_set by cover_set
		start = Timer::get_millisec();
//		user_mc_set = new HashMap<int, int>();
		for (int clique_id = 0; clique_id < cover_set.size(); clique_id++){
			unordered_set<int> clique = cover_set[clique_id];

			for (int obj_id : clique)
				//
				if (user_mc_set.count(obj_id) == 0)
					user_mc_set[obj_id] = clique_id;            //use id
				else if (cover_set[user_mc_set[obj_id]].size() < clique.size())
					user_mc_set[obj_id] = clique_id;               //store the maximum
			//
			for (int obj_id : clique)
				if (user_mc_set[obj_id] == clique_id)  //clique id comparison
					user_mesh[obj_id] = cover_mesh[clique_id];
		}
		cout<<"Compute user_mc_set - elapsed : " << (Timer::get_millisec() - start) <<endl;
//        print "user_mc_set = ", user_mc_set


		//7. publish MBRs (write to file)
		start = Timer::get_millisec();
		write_results_to_files(timestamp);

		cout<<"write_results_to_files - elapsed : " << (Timer::get_millisec() - start) <<endl;
	}

	//
	void write_results_to_files(int timestamp){

		string config_name = option.QUERY_FILE.substr(0, option.QUERY_FILE.length()-4) + "-" +
				Formatter::formatDouble("%.1f", option.DISTANCE_CONSTRAINT) + "-" + Formatter::formatDouble("%.1f", option.MAX_SPEED);

		//1. cover_set
		string filename = option.RESULT_PATH + config_name + "_" + to_string(option.K_GLOBAL) + "_" +
				  Formatter::formatDouble("%.2f", option.INIT_COVER_KEEP_RATIO) + "_cover_set" + "_" + to_string(timestamp) + ".out";
		ofstream f(filename, ofstream::out);
		for (unordered_set<int> clique : cover_set){
			for (int obj_id : clique)
				f << obj_id << ",";
			f << "---";
			for (int obj_id : clique)
				f << query_log.trajs[obj_id][timestamp].k_anom << ",";
			f << "\n";
		}
		f.close();


		//5. user_mesh (only print edge_id) for attacks (in trace_generator)
		filename = option.RESULT_PATH + "/" + config_name + "_edge_cloaking_" + to_string(timestamp) + ".out";
		f = ofstream(filename);
		for (map<int, vector<EdgeSegment>>::iterator it = user_mesh.begin(); it != user_mesh.end(); it++){
			int obj_id = it->first;
			vector<EdgeSegment> mesh = it->second;

			int cur_edge_id = query_log.trajs[obj_id][timestamp].cur_edge_id;
			f << obj_id << "-" << cur_edge_id << "\n";
			for (EdgeSegment seg : mesh)
				f << seg.cur_edge_id << ",";
			f << "\n";
		}

		f.close();
	}

	//
	void run_timestamps(int start_timestamp, int end_timestamp){
		for (int timestamp = start_timestamp; timestamp < end_timestamp+1; timestamp++){
			__int64 start = Timer::get_millisec();
			cout<<"--------->>"<<endl;
			cout<<"TIMESTAMP : " << timestamp<<endl;
			cout<<"self.num_user = " << query_log.frames[timestamp].size()<<endl;

			solve_new_queries(timestamp);

			cout<<"Total time elapsed :" << (Timer::get_millisec() - start) <<endl;
	//            print "check_published_mcset_mbr", \
	//                this.check_published_mcset_mesh(this.user_mc_set, this.user_mesh, timestamp)

				//print "len(graph.mc_set) = ", len(graph.mc_set)
			//print graph.mc_set
		}
	}

};

int main(int argc, char* args[]){

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


	// TEST option



	// TEST MMBMap
//	MMBMap map_data;
//	map_data.read_map("data/", "oldenburgGen");
//
//	cout<<map_data.adj[3700].size() <<endl;

	///////////////////////////////
	// COMMAND-LINE <query_file> <timestep> <distance_constraint> <k_global><INIT_COVER_KEEP_RATIO><NEXT_COVER_KEEP_RATIO>
	cout<<"mmb_network - C++\n";
	int timestep = 3;
	//    timestep = 40       // for lbs_attack

	if(argc > 3){
		option.QUERY_FILE = args[1];
		timestep = stoi(args[2]);
		option.DISTANCE_CONSTRAINT = stoi(args[3]);
	}

	if (argc > 4)
		option.K_GLOBAL = stoi(args[4]);

	if (argc > 5)
		option.INIT_COVER_KEEP_RATIO = stod(args[5]);

	if (argc > 6)
		option.NEXT_COVER_KEEP_RATIO = stod(args[6]);

	//
	cout<<"MAP_FILE = " << option.MAP_NAME<<endl;
	cout<<"timestep = " << timestep<<endl;
	cout<<"QUERY_FILE = " << option.QUERY_FILE<<endl;
	cout<<"DISTANCE_CONSTRAINT = " << option.DISTANCE_CONSTRAINT<<endl;

	//
//	EdgeSegment e = EdgeSegment(11551,	13789,	11315,	13916, 81100852);
//	cout<<e.cur_edge_id<<"\t"<<e.start_x << "\t" << e.start_y << "\t" << e.end_x << "\t" << e.end_y<<endl;
//
////	vector<EdgeSegment> list = {e};		//copy constructor?
//	cout<<"address :"<<&e<<endl;
//	vector<EdgeSegment*> list;
//	list.push_back(&e);
//	cout<<"address :"<<&(list[0])<<endl;
//	cout<<"list.size = "<<list.size()<<endl;
//
//	for(EdgeSegment* a_e : list){
//		a_e->normalize();
//		cout<<"address :"<<&a_e<<endl;
//	}
//
//	cout<<e.cur_edge_id<<"\t"<<e.start_x << "\t" << e.start_y << "\t" << e.end_x << "\t" << e.end_y<<endl;
//	e.normalize();
//	cout<<e.cur_edge_id<<"\t"<<e.start_x << "\t" << e.start_y << "\t" << e.end_x << "\t" << e.end_y<<endl;



	//
	__int64 start = Timer::get_millisec();

	MMBMap map_data = MMBMap();
	map_data.read_map(option.MAP_PATH, option.MAP_NAME);
	cout<<"Load Map : DONE"<<endl;

	QueryLog query_log(map_data);
	query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, timestep, option.QUERY_TYPE);   // default: max_time_stamp = 10 (40: only for attack)
	cout<<"Load Query : DONE"<<endl;

	cout<<"max_speed = " << query_log.max_speed<<endl;
	cout<<"elapsed : " << (Timer::get_millisec() - start) <<endl;


	Graph graph = Graph(0, map_data, query_log);

	// TEST MMBMap.compute_fixed_expanding()
//	Query query = query_log.frames[0][754];
//	vector<EdgeSegment> seg_list = map_data.compute_fixed_expanding(query.x, query.y, query.cur_edge_id, query.dist);
//	for(EdgeSegment e : seg_list)
//		cout<<e.cur_edge_id<<"\t"<<e.start_x << "\t" << e.start_y << "\t" << e.end_x << "\t" << e.end_y <<endl;
//
//	// clean_fixed_expanding()
//	seg_list = GeomUtil::clean_fixed_expanding(seg_list);
//	cout<<"AFTER clean_fixed_expanding: len " << seg_list.size()<<endl;
//	for(EdgeSegment e : seg_list)
//		cout<<e.cur_edge_id<<"\t"<<e.start_x << "\t" << e.start_y << "\t" << e.end_x << "\t" << e.end_y<<endl;

	//
	graph.run_timestamps(0, timestep);

	cout<<"graph.run_timestamps - DONE"<<endl;



}


