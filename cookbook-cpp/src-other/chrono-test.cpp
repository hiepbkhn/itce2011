/*
 * chrono-test.cpp
 *
 *  Created on: Mar 26, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>
#include <chrono>

using namespace std;
using namespace std::chrono;

int main() {
	__int64 now = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();

	cout<<"milliseconds = "<<now;

	return 0;
}

