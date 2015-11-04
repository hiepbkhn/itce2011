/*
 * permutation-test.cpp
 *
 *  Created on: Oct 28, 2012
 *      Author: Nguyen Huu Hiep
 */

// Demonstrate next_permutation() and prev_permutation().
#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

int main() {
	vector<char> v;
	unsigned i;

	// This creates the sorted sequence ABC.
	for (i = 0; i < 3; i++)
		v.push_back('A' + i);

	// Demonstrate next_permutation().
	cout << "All permutations of ABC by use of next_permutation():\n";
	do {
		for (i = 0; i < v.size(); i++)
			cout << v[i];
		cout << "\n";
	} while (next_permutation(v.begin(), v.end()));

	// At this point, v has cycled back to containing ABC.

	cout << endl;

	// Demonstrate prev_permutation().

	// First, backup to the previous permutation.
	prev_permutation(v.begin(), v.end());

	cout << "All permutations of ABC by use of prev_permutation():\n";
	do {
		for (i = 0; i < v.size(); i++)
			cout << v[i];
		cout << "\n";
	} while (prev_permutation(v.begin(), v.end()));

	return 0;
}
