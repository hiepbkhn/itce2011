/*
 * vector-test.cpp
 *
 *  Created on: Mar 27, 2017
 *      Author: Administrator
 */

// vector assignment
#include <iostream>
#include <vector>

using namespace std;

class A{
public:
	int x;
	int y;

	A(int _x, int _y){
		x = _x;
		y = _y;
	}

	void swap(){
		int temp = x;
		x = y;
		y = temp;
	}
};

int main() {
	////////
//	vector<int> foo(3, 0);
//	vector<int> bar(5, 0);
//	cout << "Size of foo: " << int(foo.size()) << '\n';
//	cout << "Size of bar: " << int(bar.size()) << '\n';
//
//	bar = foo;	// copy
//	cout << "Size of foo: " << int(foo.size()) << '\n';
//	cout << "Size of bar: " << int(bar.size()) << '\n';
//	bar[0] = 1;
//	cout<<foo[0]<<endl;	// still 0
//	cout<<bar[0]<<endl;
//
////	foo = vector<int>();
//	foo.clear();
//
//	cout << "Size of foo: " << int(foo.size()) << '\n';
//	cout << "Size of bar: " << int(bar.size()) << '\n';

	////////
	A a1(1,2);
	A a2(3,4);
	vector<A> foo = {a1, a2};
	vector<A> bar = foo;
	for(A a:bar)
		cout<<&a<<" "<<a.x<<" "<<a.y<<endl;
	cout<<"swapping..\n";
	for(vector<A>::iterator it = bar.begin(); it != bar.end(); it++)
		(*it).swap();
	for(A a:bar)
		cout<<&a<<" "<<a.x<<" "<<a.y<<endl;

	//
	return 0;
}

