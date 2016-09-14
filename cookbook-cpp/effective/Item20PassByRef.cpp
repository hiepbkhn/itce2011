/*
 * Item20PassByRef.cpp
 *
 *  Created on: Sep 14, 2016
 *      Author: huunguye
 */
#include <iostream>
#include <string>

using namespace std;

////
class Window {
	string name;

public:
	Window(string n);
	string getName() const;           // return name of window

	virtual void display() const {};       // draw window and contents

};

Window::Window(string n) {
	name = n;
}

string Window::getName() const {
	return name;
}

////
class WindowWithScrollBars: public Window {

public:
	WindowWithScrollBars(string n):Window(n){};

	void display() const;
};

void WindowWithScrollBars::display() const {
	cout <<	"WindowWithScrollBars.display" << endl;
}

////
//void printNameAndDisplay(Window w) {  // incorrect! parameter may be sliced!
void printNameAndDisplay(const Window &w) { // correct!

	std::cout << w.getName() << endl;

	w.display();

}

////
int main() {

	Window w(string("abc"));

	cout << w.getName() << endl;

	WindowWithScrollBars wwsb(string("def"));
	printNameAndDisplay(wwsb);

	return 0;
}
