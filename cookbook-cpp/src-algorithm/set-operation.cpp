/*
 * set-operation.cpp
 *
 *  Created on: Mar 26, 2017
 *      Author: Nguyen Huu Hiep
 */

// set_intersection example
#include <iostream>     // std::cout
#include <algorithm>    // std::set_intersection, std::sort
#include <vector>       // std::vector
#include <set>

using namespace std;
int main() {

	////
//	int first[] = { 5, 10, 15, 20, 25 };
//	int second[] = { 50, 40, 30, 20, 10 };
//	std::vector<int> v(10);                      // 0  0  0  0  0  0  0  0  0  0
//	std::vector<int>::iterator it;
//
//	std::sort(first, first + 5);     //  5 10 15 20 25
//	std::sort(second, second + 5);   // 10 20 30 40 50
//
//	it = std::set_intersection(first, first + 5, second, second + 5, v.begin());
//	// 10 20 0  0  0  0  0  0  0  0
//	v.resize(it - v.begin());                      // 10 20
//
//	std::cout << "The intersection has " << (v.size()) << " elements:\n";
//	for (it = v.begin(); it != v.end(); ++it)
//		std::cout << ' ' << *it;
//	std::cout << '\n';

	/////////// intersection/difference
//	set<int> s1;
//	set<int> s2;
//
//	s1.insert(1);
//	s1.insert(2);
//	s1.insert(3);
//	s1.insert(4);
//
//	s2.insert(1);
//	s2.insert(6);
//	s2.insert(3);
//	s2.insert(0);
//
//	set<int> intersect;
//	set_intersection(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(intersect, intersect.begin()));
//	std::cout << "The intersection has " << (intersect.size()) << " elements:\n";
//	for (set<int>::iterator it = intersect.begin(); it != intersect.end(); ++it)
//		std::cout << ' ' << *it;
//	cout<<endl;
//
//	//
//	set<int> difference;
//	set_difference(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(difference, difference.begin()));
//	std::cout << "The difference has " << (difference.size()) << " elements:\n";
//	for (set<int>::iterator it = difference.begin(); it != difference.end(); ++it)
//		std::cout << ' ' << *it;

	//// sort list of sets by size

	set<int> s1;
	set<int> s2;
	set<int> s3;

	s1.insert(1);
	s1.insert(2);
	s1.insert(3);
	s1.insert(4);

	s2.insert(1);
	s2.insert(6);

	s3.insert(1);
	s3.insert(6);
	s3.insert(5);
	vector<set<int>> set_list;
	set_list.push_back(s1);
	set_list.push_back(s2);
	set_list.push_back(s3);

	sort(set_list.begin(), set_list.end());
	for(set<int> s : set_list)
		cout<<s.size() << " ";

	return 0;
}

