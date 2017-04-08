/*
 * ex_scoped_thread.cpp
 *
 *  Created on: Apr 8, 2017
 *      Author: Administrator
 */

//#define BOOST_THREAD_PROVIDES_FUTURE
//#include <boost/thread.hpp>
//#include <boost/thread/future.hpp>
//#include <functional>
//#include <iostream>
//
//void accumulate(boost::promise<int> &p) {
//	int sum = 0;
//	for (int i = 0; i < 5; ++i)
//		sum += i;
//	p.set_value(sum);
//}
//
//int main() {
//	boost::promise<int> p;
//	boost::future<int> f = p.get_future();
//	boost::thread t { accumulate, std::ref(p) };
//	std::cout << f.get() << '\n';
//}

// Example 44.15. Using boost::packaged_task
//#define BOOST_THREAD_PROVIDES_FUTURE
//#include <boost/thread.hpp>
//#include <boost/thread/future.hpp>
//#include <utility>
//#include <iostream>
//
//int accumulate() {
//	int sum = 0;
//	for (int i = 0; i < 5; ++i)
//		sum += i;
//	return sum;
//}
//
//int main() {
//	boost::packaged_task<int> task { accumulate };
//	boost::future<int> f = task.get_future();
//	boost::thread t { std::move(task) };
//	std::cout << f.get() << '\n';
//}

// Example 44.16. Using boost::async()
#define BOOST_THREAD_PROVIDES_FUTURE
#include <boost/thread.hpp>
#include <boost/thread/future.hpp>
#include <iostream>

int accumulate() {
	int sum = 0;
	for (int i = 0; i < 5; ++i)
		sum += i;
	return sum;
}

int main() {
	boost::future<int> f = boost::async(accumulate);
	std::cout << f.get() << '\n';
}
