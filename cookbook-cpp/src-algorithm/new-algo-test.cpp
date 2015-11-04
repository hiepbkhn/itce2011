/*
 * new-algo-test.cpp
 *
 *  Created on: Oct 29, 2012
 *      Author: Nguyen Huu Hiep
 */

// This program demonstrates the disjoint() algorithm.
#include <iostream>
#include <list>
#include <algorithm>

using namespace std;

template<class InIter>
void show_range(const char *msg, InIter start, InIter end);

template<class InIter>
bool disjoint(InIter start, InIter end, InIter start2, InIter end2);

int main() {
	list<char> lst1, lst2, lst3;

	for (int i = 0; i < 5; i++)
		lst1.push_back('A' + i);
	for (int i = 6; i < 10; i++)
		lst2.push_back('A' + i);
	for (int i = 8; i < 12; i++)
		lst3.push_back('A' + i);

	show_range("Contents of lst1: ", lst1.begin(), lst1.end());
	show_range("Contents of lst2: ", lst2.begin(), lst2.end());
	show_range("Contents of lst3: ", lst3.begin(), lst3.end());

	cout << endl;

	// Test lst1 and lst2.
	if (disjoint(lst1.begin(), lst1.end(), lst2.begin(), lst2.end()))
		cout << "lst1 and lst2 are disjoint\n";
	else
		cout << "lst1 and lst2 are not disjoint.\n";

	// Test lst2 and lst3.
	if (disjoint(lst2.begin(), lst2.end(), lst3.begin(), lst3.end()))
		cout << "lst2 and lst3 are disjoint\n";
	else
		cout << "lst2 and lst3 are not disjoint.\n";

	return 0;
}

// Show a range of elements.
template<class InIter>
void show_range(const char *msg, InIter start, InIter end) {

	InIter itr;

	cout << msg;

	for (itr = start; itr != end; ++itr)
		cout << *itr << " ";
	cout << endl;
}

// This function is an algorithm that deterimes if the contents of
// two ranges are disjoint. That is, if they contain no elements
// in common.
template<class InIter>
bool disjoint(InIter start, InIter end, InIter start2, InIter end2) {

	InIter itr;

	for (; start != end; ++start)
		for (itr = start2; itr != end2; ++itr)
			if (*start == *itr)
				return false;

	return true;
}
