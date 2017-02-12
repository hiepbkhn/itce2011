/*
* map-test.cpp
*
*  Created on: Oct 28, 2012
*      Author: Nguyen Huu Hiep
*/

// Demonstrate the basic associative container operations.
//
// This example uses map, but the same basic techniques can be
// applied to any associative container.
#include <iostream>
#include <string>
#include <map>

using namespace std;

void show(const char *msg, map<string, int> mp);

int main() {
	// Declare an empty map that holds key/value pairs
	// in which the key is a string and the value is an int.
	map<string, int> m;

	// Insert characters into v. An iterator to the inserted
	// object is returned.
	m.insert(pair<string, int>("Alpha", 100));
	m.insert(pair<string, int>("Gamma", 300));
	m.insert(pair<string, int>("Beta", 200));

	// Declare an iterator to a map<string, itr>.
	map<string, int>::iterator itr;

	// Display the first element in m.
	itr = m.begin();
	cout << "Here is the first key/value pair in m: " << itr->first << ", "
		<< itr->second << endl;

	// Display the last element in m.
	itr = m.end();
	--itr;
	cout << "Here is the last key/value pair in m: " << itr->first << ", "
		<< itr->second << "\n\n";

	// Display the entire contents of m.
	show("Entire contents of m: ", m);

	// Show the size of m, which is the number of elements
	// currently held by m.
	cout << "Size of m is " << m.size() << "\n\n";

	// Declare a reverse iterator to a map<string, itr>.
	map<string, int>::reverse_iterator ritr;

	// Now, show the contents of m in reverse order.
	cout << "The contents of m in reverse:\n";

	for (ritr = m.rbegin(); ritr != m.rend(); ++ritr)
		cout << "  " << ritr->first << ", " << ritr->second << endl;
	cout << endl;

	// Find an element given its key.
	itr = m.find("Beta");
	if (itr != m.end())
		cout << itr->first << " has the value " << itr->second << "\n\n";
	else
		cout << "Key not found.\n\n";

	// Create another map that is the same as the first.
	map<string, int> m2(m);
	show("Contents of m2: ", m2);

	// Compare two maps.
	if (m == m2)
		cout << "m and m2 are equivalent.\n\n";

	// Insert more elements into m and m2.
	cout << "Insert more elements into m and m2.\n";
	m.insert(make_pair("Epsilon", 99));
	m2.insert(make_pair("Zeta", 88));
	show("Contents of m are now: ", m);
	show("Contents of m2 are now: ", m2);

	// Determine the relationship between m and m2. This is a
	// lexicographical compare. Therefore the first
	// element in the container determines which
	// container is less than the other.
	if (m < m2)
		cout << "m is less than m2.\n\n";

	// Remove the Beta from m.
	m.erase("Beta");
	show("m after removing Beta: ", m);
	if (m > m2)
		cout << "Now, m is greater than m2.\n\n";

	// Exchange the contents of m and m2.
	cout << "Exchange m and m2.\n";
	m.swap(m2);
	show("Contents of m: ", m);
	show("Contents of m2: ", m2);

	// Clear m.
	m.clear();
	if (m.empty())
		cout << "m is now empty.";

	return 0;
}

// Display the contents of a map<string, int> by using
// an iterator.
void show(const char *msg, map<string, int> mp) {
	map<string, int>::iterator itr;

	cout << msg << endl;
	for (itr = mp.begin(); itr != mp.end(); ++itr)
		cout << "  " << itr->first << ", " << itr->second << endl;
	cout << endl;
}
