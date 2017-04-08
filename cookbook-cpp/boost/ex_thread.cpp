/*
 * ex_thread.cpp
 *
 *  Created on: Apr 8, 2017
 *      Author: Administrator
 */

#include <boost/thread.hpp>
#include <boost/chrono.hpp>
#include <iostream>

void wait(int seconds) {
	boost::this_thread::sleep_for(boost::chrono::seconds { seconds });
}

void thread() {
	std::cout << boost::this_thread::get_id() << '\n';

	for (int i = 0; i < 5; ++i) {
		wait(1);
		std::cout << i << '\n';
	}
}

int main() {
	std::cout << boost::this_thread::get_id() << '\n';
	std::cout << boost::thread::hardware_concurrency() << '\n';

	boost::thread t { thread };
	t.join();
}

