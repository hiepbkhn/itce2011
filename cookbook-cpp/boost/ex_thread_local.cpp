/*
 * ex_thread_local.cpp
 *
 *  Created on: Apr 8, 2017
 *      Author: Administrator
 */

// Example 44.12
//#include <boost/thread.hpp>
//#include <iostream>
//
//boost::mutex mutex;
//
//void init() {
//	static bool done = false;
//	boost::lock_guard<boost::mutex> lock { mutex };
//	if (!done) {
//		done = true;
//		std::cout << "done" << '\n';
//	}
//}
//
//void thread() {
//	init();
//	init();
//}
//
//int main() {
//	boost::thread t[3];
//
//	for (int i = 0; i < 3; ++i)
//		t[i] = boost::thread { thread };
//
//	for (int i = 0; i < 3; ++i)
//		t[i].join();
//}

// Example 44.13
#include <boost/thread.hpp>
#include <iostream>

boost::mutex mutex;

void init() {
	static boost::thread_specific_ptr<bool> tls;
	if (!tls.get()) {
		tls.reset(new bool { true });
		boost::lock_guard<boost::mutex> lock { mutex };
		std::cout << "done" << '\n';
	}
}

void thread() {
	init();
	init();
}

int main() {
	boost::thread t[3];

	for (int i = 0; i < 3; ++i)
		t[i] = boost::thread { thread };

	for (int i = 0; i < 3; ++i)
		t[i].join();
}
