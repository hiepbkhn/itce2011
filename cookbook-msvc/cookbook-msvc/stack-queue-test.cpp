/*
* stack-test.cpp
*
*  Created on: Oct 28, 2012
*      Author: Nguyen Huu Hiep
*/

// Demonstrate the sequence container adaptors.
#include <iostream>
#include <string>
#include <queue>
#include <stack>

using namespace std;

int main() {
	// Demonstrate queue.
	queue<string> q;

	cout << "Demonstrate a queue for strings.\n";

	cout << "Pushing one two three four\n";
	q.push("one");
	q.push("two");
	q.push("three");
	q.push("four");

	cout << "Now, retrieve those values in FIFO order.\n";
	while (!q.empty()) {
		cout << "Popping ";
		cout << q.front() << "\n";
		q.pop();
	}
	cout << endl;

	// Demonstrate priority_queue.
	priority_queue<int> pq;

	cout << "Demonstrate a priority_queue for integers.\n";

	cout << "Pushing 1, 3, 4, 2.\n";
	pq.push(1);
	pq.push(3);
	pq.push(4);
	pq.push(2);	

	cout << "Now, retrieve those values in priority order.\n";
	while (!pq.empty()) {
		cout << "Popping ";
		cout << pq.top() << "\n";
		pq.pop();
	}
	cout << endl;

	// Finally, demonstrate stack.
	stack<char> stck;

	cout << "Demonstrate a stack for characters.\n";

	cout << "Pushing  A, B, C, and D.\n";
	stck.push('A');
	stck.push('B');
	stck.push('C');
	stck.push('D');

	cout << "Now, retrieve those values in LIFO order.\n";
	while (!stck.empty()) {
		cout << "Popping: ";
		cout << stck.top() << "\n";
		stck.pop();
	}

	return 0;
}
