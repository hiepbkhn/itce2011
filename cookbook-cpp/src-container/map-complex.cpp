/*
 * map-complex.cpp
 *
 *  Created on: Mar 26, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <map>
#include <vector>
#include <set>
#include <iostream>

using namespace std;

int main(){

	//// map(int, vector<int>)
	map<int, vector<int>> m;
	m[0].push_back(1);
	m[0].push_back(2);
	m[0].push_back(3);

	m[2].push_back(2);
	m[2].push_back(5);
	m[2].push_back(8);

	for(map<int, vector<int>>::iterator it = m.begin(); it != m.end(); it++){
		int key = it->first;
		vector<int> value = it->second;
		cout<<"key:"<<key<<" - ";
		for(int v : value)
			cout<<v <<" ";
		cout<<endl;
	}

	return 0;
}


