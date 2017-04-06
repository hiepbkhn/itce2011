/*
 * multi-thread.cpp
 *
 *  Created on: Apr 6, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>
#include <cstdint>
#include <chrono>
#include <thread>    // for std::thread

const int max_sum_item = 1000000000;

using namespace std::chrono;

__int64 get_millisec() {
	__int64 now = duration_cast < milliseconds
			> (system_clock::now().time_since_epoch()).count();
	return now;
}

//void do_sum(std::uint64_t *total) {
//	*total = 0;
//
//	for (int i = 0; i < max_sum_item; i++)
//		*total += i;	// dereference -> costly
//}

void do_sum(std::uint64_t *total) {
	std::uint64_t localTotal = 0;

	for (int i = 0; i < max_sum_item; i++)
		localTotal += i;

	*total = localTotal;
}

int main() {
	uint64_t result;

	__int64 start = get_millisec();

	std::thread worker(do_sum, &result);
	worker.join();

	std::cout << result << std::endl;
	std::cout<<"Elapsed : " << (get_millisec() - start) <<std::endl;
}

