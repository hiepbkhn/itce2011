/*
 * binder-test.cpp
 *
 *  Created on: Oct 29, 2012
 *      Author: Nguyen Huu Hiep
 */

// Demonstrate bind2nd().
#include <iostream>
#include <list>
#include <functional>
#include <algorithm>

using namespace std;

template<class InIter>
void show_range(const char *msg, InIter start, InIter end);

int main() {
	list<int> lst;
	list<int>::iterator res_itr;

	for (unsigned i = 1; i < 20; ++i)
		lst.push_back(i);

	show_range("Original sequence:\n", lst.begin(), lst.end());
	cout << endl;

	// Use bind2nd() to create a unary function object
	// that will return true when a value is greater than 10.
	// This is used by remove_if() to remove all elements from
	// lst that are greater than 10.
	res_itr = remove_if(lst.begin(), lst.end(), bind2nd(greater<int>(), 10));

	show_range("Resulting sequence:\n", lst.begin(), res_itr);

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
