/*
 * Option.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Nguyen Huu Hiep
 */
#ifndef OPTION_H_
#define OPTION_H_


#include <string>
#include <map>
#include <iostream>
#include <fstream>

#include "helper.h"

//#include <boost/format.hpp>

using namespace std;

class Option {
public:
	string QUERY_PATH = "";

	int QUERY_TYPE = 0;	//QUERY_TYPE = 0 (Brinkhoff), 1 (TraceGenerator)

	string QUERY_FILE = "";

	//
	string RESULT_PATH = "";

	string MAP_PATH = "";

	string MAP_NAME = "";

	//
	double MAX_SPEED = 0;
	double INIT_DISTANCE = 0;
	int MAX_USER = 0;
	double DISTANCE_CONSTRAINT = 0;


	double MAP_RATIO = 0;

	int K_ANONYMITY = 0;
	int DELAY_MAX = 0;   //report prob. = 0.2 --> 5 timestamps

	int K_GLOBAL = 0;
	int S_GLOBAL = 0;

	double INIT_COVER_KEEP_RATIO = 0;     //0.8, 0.85, 0.9, 0.95, 1.0 (for K_GLOBAL = 2)
	double NEXT_COVER_KEEP_RATIO = 0;

	double MAX_MESH_LENGTH = 0;
	double MIN_MESH_LENGTH = 0;

	string MACE_EXECUTABLE = "";

	string MAXIMAL_CLIQUE_FILE_IN = "";
	string MAXIMAL_CLIQUE_FILE_OUT = "";


	double MAP_WIDTH = 0;
	double MAP_HEIGHT = 0;
	int NUM_NODE_WIDTH = 0;
	int NUM_NODE_HEIGHT = 0;

	string PROFILE_PATH = "";


	// for lbs_attack.py
	//CLOAKING_PATH = "../../mmb_network/out/deviation/"
	string CLOAKING_PATH = "";              // for ICliqueCloak

	string CLOAKING_FILE_NAME = "";  // converted output of ICliqueCloak

	double EDGE_CLASSES[5];    // CDF

	//SPEED_CLASSES = [1.0, 0.8, 1.2, 1.5, 2.0]
	double SPEED_CLASSES[7];   // oldenburgGen, 7 classes
//	double[] SPEED_CLASSES = new double[]{1.0};   // cal, 1 class

	string EDGE_COLORS[7];

	double USER_NOMINAL_SPEEDS[5];    // cal
	int NUM_NOMINAL_SPEEDS = 0;

	double SPEED_PROFILE = 0; 	// 0.5: slow, 1.0: medium 3.0: fast

	int N_USERS = 0;     // 2000, 5000, 10000, 20000
	//N_TRAIN_TRACES = 100

	int MIN_TRAIN_GROUP = 0;     // 3,2,5
	int MAX_TRAIN_GROUP = 0;    // 7,10,15

	int MIN_N_TRAIN_PATH = 0;
	int MAX_N_TRAIN_PATH = 0;

	int MIN_N_RANDOM_TRACE = 0;
	int MAX_N_RANDOM_TRACE = 0;

	int N_TIMESTEPS = 0;    // for prediction S_ij(k)

	int MAX_TRACE_LEN = 0;

	int MIN_SELECTED_TRACE_LEN = 0;

	//
	int K_MIN = 0;
	int K_MAX = 0;
	double MIN_LENGTH_LOW = 0;
	double MIN_LENGTH_HIGH = 0;

	// for attack
	int MAX_OUTPUT_TIMESTEP = 0;    // 20, 50
	int ATTACK_TIME_STEPS = 0;

	int NUM_USERS_NO_DEV = 0;
	int NUM_USERS_WITH_DEV = 0;

	int N_LOOP_MARKOV = 0;


	//
	static string getString(string value){
		return value.substr(1, value.size()-2);
	}
	static int getInt(string value){
		return stoi(value);
	}
	static double getDouble(string value){
		return stod(value);
	}

	//
	Option(string config_file){

		ifstream f(config_file);

		if(!f) {
			cout << "Cannot open config file.\n";
			return;
		}

		map<string,string> opt;
		for (string line; getline(f, line); ){
			if (line.size() == 0 || line.substr(0, 2) == "//")
				continue;

//			cout<<line <<endl;

			int pos = line.find('=');
			string key = line.substr(0, pos);
			string value = line.substr(pos + 1, line.size());
//			cout<<key<<","<<value<<endl;

			opt[key] = value;
		}

		//
		f.close();

		//
		QUERY_TYPE = getInt(opt["QUERY_TYPE"]);
		QUERY_PATH = getString(opt["QUERY_PATH"]);
		QUERY_FILE = getString(opt["QUERY_FILE"]);

		RESULT_PATH = getString(opt["RESULT_PATH"]);
		MAP_PATH = getString(opt["MAP_PATH"]);
		MAP_NAME = getString(opt["MAP_NAME"]);

		MAX_SPEED = getDouble(opt["MAX_SPEED"]);
		INIT_DISTANCE = getDouble(opt["INIT_DISTANCE"]);
		MAX_USER = getInt(opt["MAX_USER"]);
		DISTANCE_CONSTRAINT = getDouble(opt["DISTANCE_CONSTRAINT"]);

		MAP_RATIO = getDouble(opt["MAP_RATIO"]);


	}

	//
	static string getProfileName(Option option){
		map<int, string> num_user_dict;
		num_user_dict[1000] =  "1k";
		num_user_dict[2000] =  "2k";
		num_user_dict[5000] =  "5k";
		num_user_dict[10000] =  "10k";
		num_user_dict[20000] =  "20k";

		string s_MIN_LENGTH_LOW = Formatter::formatDouble("%.4f", option.MIN_LENGTH_LOW);
		s_MIN_LENGTH_LOW = s_MIN_LENGTH_LOW.substr(2, s_MIN_LENGTH_LOW.size()-2);

		string s_MIN_LENGTH_HIGH = Formatter::formatDouble("%.4f", option.MIN_LENGTH_HIGH);
		s_MIN_LENGTH_HIGH = s_MIN_LENGTH_HIGH.substr(2, s_MIN_LENGTH_HIGH.size()-2);

		string PROFILE_NAME = option.MAP_NAME + "_" + num_user_dict[option.N_USERS] + "_" +
					Formatter::formatDouble("%.1f", option.SPEED_PROFILE) + "_" +
					to_string(option.K_MIN) + "_" + to_string(option.K_MAX) + "_" +
					s_MIN_LENGTH_LOW + "_" + s_MIN_LENGTH_HIGH + "_" +
	                to_string(option.MIN_TRAIN_GROUP) + "_" + to_string(option.MAX_TRAIN_GROUP) + "_" +
					to_string(option.MIN_N_TRAIN_PATH) + "_" + to_string(option.MAX_N_TRAIN_PATH) + "_" +
					to_string(option.MIN_N_RANDOM_TRACE) + "_" + to_string(option.MAX_N_RANDOM_TRACE);
		return PROFILE_NAME;

	}
};


#endif /* OPTION_H_ */




