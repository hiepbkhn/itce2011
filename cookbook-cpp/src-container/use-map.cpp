/*
 * use-map.cpp
 *
 *  Created on: Oct 28, 2012
 *      Author: Nguyen Huu Hiep
 */

// Demonstrate map.
//
// This program creates a simple phone list in which
// a person's name is the key and the phone number is
// the value. Thus, you can look up a phone number
// given a name.
#include <iostream>
#include <string>
#include <map>
#include <utility>

using namespace std;

void show(const char *msg, map<string, string> mp);

int main() {
	map<string, string> phonemap;

	// Insert elements by using operator[].
	phonemap["Tom"] = "555-1234";
	phonemap["Jane"] = "314 555-6576";
	phonemap["Ken"] = "660 555-9843";

	show("Here is the original map: ", phonemap);
	cout << endl;

	// Now, change the phone number for Ken.
	phonemap["Ken"] = "415 997-8893";
	cout << "New number for Ken: " << phonemap["Ken"] << "\n\n";

	// Use find() to find a number.
	map<string, string>::iterator itr;
	itr = phonemap.find("Jane");
	if (itr != phonemap.end())
		cout << "Number for Jane is " << itr->second << "\n\n";

	// Cycle through the map in the reverse direction.
	map<string, string>::reverse_iterator ritr;
	cout << "Display phonemap in reverse order:\n";
	for (ritr = phonemap.rbegin(); ritr != phonemap.rend(); ++ritr)
		cout << "  " << ritr->first << ": " << ritr->second << endl;
	cout << endl;

	// Create a pair object that will contain the result
	// of a call to insert().
	pair<map<string, string>::iterator, bool> result;

	// Use insert() to add an entry.
	result = phonemap.insert(pair<string, string>("Jay", "555-9999"));
	if (result.second)
		cout << "Jay added.\n";
	show("phonemap after adding Jay: ", phonemap);

	// Duplicate keys are not allowed, as the following proves.
	result = phonemap.insert(pair<string, string>("Jay", "555-1010"));
	if (result.second)
		cout << "Duplicate Jay added! Error!";
	else
		cout << "Duplicate Jay not allowed.\n";
	show("phonemap after attempt to add duplicate Jay key: ", phonemap);

	return 0;
}

// Display the contents of a map<string, string> by using
// an iterator.
void show(const char *msg, map<string, string> mp) {
	map<string, string>::iterator itr;

	cout << msg << endl;

	for (itr = mp.begin(); itr != mp.end(); ++itr)
		cout << "  " << itr->first << ": " << itr->second << endl;

	cout << endl;
}
