/*
 * basic-seq-technique.cpp
 *
 *  Created on: Oct 28, 2012
 *      Author: Nguyen Huu Hiep
 */

// Demonstrate the basic sequence container operations.
//
// This example uses vector, but the same techniques can be
// applied to any sequence container.
#include <iostream>
#include <vector>

using namespace std;

void show(const char *msg, vector<char> vect);

int main() {
	// Declare an empty vector that can hold char objects.
	vector<char> v;

	// Declare an iterator to a vector<char>.
	vector<char>::iterator itr;

	// Obtain an iterator to the start of v.
	itr = v.begin();

	// Insert characters into v. An iterator to the inserted
	// object is returned.
	itr = v.insert(itr, 'A');
	itr = v.insert(itr, 'B');
	v.insert(itr, 'C');

	// Display the contents of v.
	show("The contents of v: ", v);

	// Declare a reverse iterator.
	vector<char>::reverse_iterator ritr;

	// Use a reverse iterator to show the contents of v in reverse.
	cout << "Here is v in reverse: ";
	for (ritr = v.rbegin(); ritr != v.rend(); ++ritr)
		cout << *ritr << " ";
	cout << "\n\n";

	// Create another vector that is the same as the first.
	vector<char> v2(v);
	show("The contents of v2: ", v2);
	cout << "\n";

	// Show the size of v, which is the number of elements
	// currently held by v.
	cout << "Size of v is " << v.size() << "\n\n";

	// Compare two containers.
	if (v == v2)
		cout << "v and v2 are equivalent.\n\n";

	// Insert more characters into v and v2. This time,
	// insert them at the end.
	cout << "Insert more characters into v and v2.\n";
	v.insert(v.end(), 'D');
	v.insert(v.end(), 'E');
	v2.insert(v2.end(), 'X');
	show("The contents of v: ", v);
	show("The contents of v2: ", v2);
	cout << "\n";

	// Determine if v is less than v2. This is a
	// lexicographical compare. Therefore the first
	// element in the container determines which
	// container is less than another.
	if (v < v2)
		cout << "v is less than v2.\n\n";

	// Now, insert Z at the start of v.
	cout << "Insert Z at the start of v.\n";
	v.insert(v.begin(), 'Z');
	show("The contents of v: ", v);
	cout << "\n";

	// Now, compare v to v2 again.
	if (v > v2)
		cout << "Now, v is greater than v2.\n\n";

	// Remove the first element from v2.
	v2.erase(v2.begin());
	show("v2 after removing the first element: ", v2);
	cout << "\n";

	// Create another vector.
	vector<char> v3;
	v3.insert(v3.end(), 'X');
	v3.insert(v3.end(), 'Y');
	v3.insert(v3.end(), 'Z');
	show("The contents of v3: ", v3);
	cout << "\n";

	// Exchange the contents of v and v3.
	cout << "Exchange v and v3.\n";
	v.swap(v2);
	show("The contents of v: ", v2);
	show("The contents of v3: ", v3);
	cout << "\n";

	// Clear v.
	v.clear();
	if (v.empty())
		cout << "v is now empty.";

	return 0;
}

// Display the contents of a vector<char> by using
// an iterator.
void show(const char *msg, vector<char> vect) {
	vector<char>::iterator itr;

	cout << msg;
	for (itr = vect.begin(); itr != vect.end(); ++itr)
		cout << *itr << " ";
	cout << "\n";
}
