/*
 * ex_scoped_thread.cpp
 *
 *  Created on: Apr 8, 2017
 *      Author: Administrator
 */

#include <boost/thread.hpp>
#include <boost/thread/scoped_thread.hpp>
#include <boost/chrono.hpp>
#include <iostream>

void wait(int seconds) {
	boost::this_thread::sleep_for(boost::chrono::seconds { seconds });
}

void thread() {
	for (int i = 0; i < 5; ++i) {
		wait(1);
		std::cout << i << '\n';
	}
}

int main() {
	boost::scoped_thread<> t { boost::thread { thread } };
}

