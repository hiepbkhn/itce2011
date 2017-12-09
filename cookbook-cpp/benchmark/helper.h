/*
 * helper.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Nguyen Huu Hiep
 *  Apr 10:
 *  	- TSetComparator -> TSetCompAscend, TsetCompDescend
 *  	- find_next_cover(): faster computation of W
 *  Apr 26
 *  	- greedyCliques()
 *  Apr 27
 *  	- find_init_cover(): split sets in C_0 to smaller cliques
 */

#ifndef HELPER_H_
#define HELPER_H_

#include <iostream>
#include <string>
#include <sstream>
#include <chrono>
#include <vector>

using namespace std;
using namespace chrono;


////
class Timer{
public:
	static __int64 get_millisec(){
		__int64 now = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
		return now;
	}
};


////
class Formatter{
public:
	static string formatDouble(const char *format, double value){
		char buff[20];
		snprintf(buff, sizeof(buff), format, value);
		string ret = buff;
		return ret;
	}

	//
	static vector<string> split(const string &s, char delim) {
	    vector<string> elems;

	    stringstream ss;
		ss.str(s);
		string item;
		while (getline(ss, item, delim))
			elems.push_back(item);

	    return elems;
	}

};



#endif /* HELPER_H_ */
