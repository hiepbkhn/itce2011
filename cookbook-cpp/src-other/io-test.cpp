/*
 * io-test.cpp
 *
 *  Created on: Mar 27, 2017
 *      Author: Administrator
 */

#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>

using namespace std;

vector<string> split(const string &s, char delim) {
	vector<string> elems;

	stringstream ss;
	ss.str(s);
	string item;
	while (getline(ss, item, delim))
		elems.push_back(item);

	return elems;
}

int main(){

	ifstream f("oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt");
	// Verify that the file has been successfully opened.
	if(!f) {
		cout << "Cannot open file.\n";
		return 1;
	}

	int maxNodeId = -1;

	string line;
	while (getline(f, line)){

		vector<string> items = split(line, '\t');

		int obj_id = stoi(items[1]);
		double x = stod(items[5]);
		double y = stod(items[6]);
		int timestamp = stoi(items[4]);
		double speed = stod(items[7]);
		int next_node_x = stoi(items[8]);
		int next_node_y = stoi(items[9]);
		int k_anom = stoi(items[10]);
		double min_length = stod(items[11]);

		maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;


		cout<<obj_id<<" "<< x<<" "<< y<<" "<< timestamp<<" "<< next_node_x<<" "<< next_node_y<<" "<<k_anom<<" "<< min_length<<endl;
		break;
	}
	f.close();

	return 0;
}


