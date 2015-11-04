/*
 * set-test.cpp
 *
 *  Created on: Oct 28, 2012
 *      Author: Nguyen Huu Hiep
 */

// Demonstrate set.
//
// This example stores objects that contain employee
// information. The employee's ID is used as the key.
#include <iostream>
#include <iterator> //for std::ostream_iterator
#include <algorithm> //for std::copy
#include <set>
#include <string>

using namespace std;

// This class stores employee information.
class employee {
	string name;
	string ID;
	string phone;
	string department;
public:
	// Default constructor.
	employee() {
		ID = name = phone = department = "";
	}

	// Construct temporary object using only the ID, which is the key.
	employee(string id) {
		ID = id;
		name = phone = department = "";
	}

	// Construct a complete employee object.
	employee(string n, string id, string dept, string p) {
		name = n;
		ID = id;
		phone = p;
		department = dept;
	}

	// Accessor functions for employee data.
	string get_name() const {
		return name;
	}
	string get_id() const {
		return ID;
	}
	string get_dept() const {
		return department;
	}
	string get_phone() const {
		return phone;
	}

};

// Compare objects using ID.
bool operator<(employee a, employee b) {
	return a.get_id() < b.get_id();
}

// Check for equality based on ID.
bool operator==(employee a, employee b) {
	return a.get_id() == b.get_id();
}

// Create an inserter for employee.
std::ostream& operator<<(std::ostream& s, const employee& o) {
	s << o.get_name() << endl;
	s << "Emp#:  " << o.get_id() << endl;
	s << "Dept:  " << o.get_dept() << endl;
	s << "Phone: " << o.get_phone() << endl;

	return s;
}

int main() {
	set<employee> emplist;

	// Initialize the employee list.
	emplist.insert(
			employee("Tom Harvy", "9423", "Client Relations", "555-1010"));

	emplist.insert(employee("Susan Thomasy", "8723", "Sales", "555-8899"));

	emplist.insert(employee("Alex Johnson", "5719", "Repair", "555-0174"));

	// Create an iterator to the set.
	set<employee>::const_iterator itr = emplist.begin();

	// Display contents of the set.
	cout << "Current set: \n\n";
	do {
		cout << *itr << endl;
		++itr;
	} while (itr != emplist.end());
	cout << endl;

//	copy(emplist.begin(), emplist.end(), ostream_iterator<employee>(cout, "\n"));

	// Find a specific employee.
	cout << "Searching for employee 8723.\n";
	itr = emplist.find(employee("8723"));
	if (itr != emplist.end()) {
		cout << "Found. Information follows:\n";
		cout << *itr << endl;
	}

	return 0;
}
