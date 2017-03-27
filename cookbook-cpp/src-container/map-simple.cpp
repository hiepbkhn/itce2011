/*
 * map-simple.cpp
 *
 *  Created on: Mar 27, 2017
 *      Author: Administrator
 */

// accessing mapped values
#include <iostream>
#include <map>
#include <string>

using namespace std;
int main() {
	map<char, string> mymap;

	mymap['a'] = "an element";
	mymap['b'] = "another element";
	mymap['c'] = mymap['b'];

	cout << "mymap['a'] is " << mymap['a'] << '\n';
	cout << "mymap['b'] is " << mymap['b'] << '\n';
	cout << "mymap['c'] is " << mymap['c'] << '\n';
	cout << "mymap['d'] is " << mymap['d'] << '\n';	// empty string


	cout << "mymap now contains " << mymap.size() << " elements.\n";


	map<char, int> int_map;

	int_map['a'] = 3;
	int_map['b'] = 2;
	int_map['c'] = int_map['b'];

	cout << "int_map['a'] is " << int_map['a'] << '\n';
	cout << "int_map['b'] is " << int_map['b'] << '\n';
	cout << "int_map['c'] is " << int_map['c'] << '\n';
	cout << "int_map['d'] is " << int_map['d'] << '\n';	// 0

	return 0;
}

