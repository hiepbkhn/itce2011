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

class QueryLog{
public:
	MMBMap map_data;

	map<int, map<int, Query>> trajs;     //dict of dicts [node][timestamp]
	map<int, vector<Query>> frames;  //dict of lists

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
		while (!f.eof()){

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
				f >> obj_id;
				maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;

				f >> x;
				f >> y;
				f >> timestamp;
				f >> speed;
				f >> next_node_x;
				f >> next_node_y;
				f >> k_anom;
				f >> min_length;
			}else{
			////// 2 - FOR TraceGenerator
				f >> obj_id;
				maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;

				f >> x;
				f >> y;
				f >> timestamp;
				f >> speed;
				f >> next_node_x;
				f >> next_node_y;
				f >> k_anom;
				f >> min_length;
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
//			trajs.get(obj_id).put(timestamp, new Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id,
//					k_anom, min_length, Option.DISTANCE_CONSTRAINT));
//
//
//			if (!frames.containsKey(timestamp))
//				frames.put(timestamp, new ArrayList<Query>());
//			frames.get(timestamp).add(new Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id,
//					k_anom, min_length, Option.DISTANCE_CONSTRAINT));


			//
			if (max_speed < speed)
				max_speed = speed;
		}

		f.close();

	}
};


#endif /* QUERY_LOADER_H_ */
