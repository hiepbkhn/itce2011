/*
 * query_loader.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef QUERY_LOADER_H_
#define QUERY_LOADER_H_

#include <map>
#include <vector>
#include <string>
#include <iostream>
#include <fstream>

#include "geom_util.h"
#include "helper.h"

class QueryLog{
public:
	MMBMap map_data;

	map<int, map<int, Query>> trajs;     //dict of dicts [node][timestamp]
	map<int, vector<Query>> frames;  //dict of lists [timestamp]

	double max_speed = 0;

	//
	QueryLog(){

	}
	//
	QueryLog(MMBMap _map_data){
		map_data = _map_data;
	}

	// query_type = 0 (Brinkhoff), 1 (TraceGenerator)
	void read_query(string path, string log_file_name, int max_time_stamp, int query_type){	// max_time_stamp = -1

		ifstream f(path + log_file_name);
		// Verify that the file has been successfully opened.
		if(!f) {
			cout << "Cannot open file.\n";
			return;
		}

		int maxNodeId = -1;
		string line;
		while (getline(f, line)){

			vector<string> items = Formatter::split(line, '\t');

			int obj_id;
			double x;
			double y;
			int timestamp;
			double speed;
			int next_node_x;
			int next_node_y;
			int k_anom;
			double min_length;
			////// 1 - FOR Brinkhoff generator
			if (query_type == 0){
				obj_id = stoi(items[1]);
				x = stod(items[5]);
				y = stod(items[6]);
				timestamp = stoi(items[4]);
				speed = stod(items[7]);
				next_node_x = stoi(items[8]);
				next_node_y = stoi(items[9]);
				k_anom = stoi(items[10]);
				min_length = stod(items[11]);

				maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;

			}else{
			////// 2 - FOR TraceGenerator
				obj_id = stoi(items[0]);
				x = stod(items[2]);
				y = stod(items[3]);
				timestamp = stoi(items[1]);
				speed = stod(items[4]);
				next_node_x = stoi(items[5]);
				next_node_y = stoi(items[6]);
				k_anom = stoi(items[7]);
				min_length = stod(items[8]);
				// (End) 2 - FOR TraceGenerator
			}

			//
			if (max_time_stamp != -1 && timestamp > max_time_stamp)
				break;

			// TODO: find next_node_id and cur_edge_id
			int next_node_id = map_data.get_next_node_id(next_node_x, next_node_y);
			int cur_edge_id = map_data.get_nearest_edge_id(next_node_x, next_node_y, x, y);

			// Trajectories
//			if (!trajs.containsKey(obj_id))
//				trajs.put(obj_id, new HashMap<Integer, Query>());
			trajs[obj_id][timestamp] = Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id,
					k_anom, min_length, option.DISTANCE_CONSTRAINT);


//			if (!frames.containsKey(timestamp))
//				frames.put(timestamp, new ArrayList<Query>());
			frames[timestamp].push_back(Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id,
					k_anom, min_length, option.DISTANCE_CONSTRAINT));

			// DEBUG
//			if (obj_id == 0 && timestamp == 0)
//				cout<<obj_id<<" "<< x<<" "<< y<<" "<< timestamp<<" "<< next_node_x<<" "<< next_node_y<<" "<<k_anom<<" "<< min_length<<endl;

			//
			if (max_speed < speed)
				max_speed = speed;
		}

		f.close();

	}
};


#endif /* QUERY_LOADER_H_ */
