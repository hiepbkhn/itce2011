/*
 * multi-worker.cpp
 *
 *  Created on: Apr 6, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>       // for std::cout
#include <cstdint>        // for uint64_t
#include <chrono>     // for std::chrono::high_resolution_clock
#include <thread>     // for std::thread
#include <vector>     // for std::vector
#include <algorithm>  // for std::for_each
#include <cassert>        // for assert

//#define TRACE

#ifdef TRACE
#include <mutex>      // for std::mutex

std::mutex coutmutex;
#endif

std::vector<uint64_t *> part_sums;
const int max_sum_item = 1000000000;
const int threads_to_use = 2;

__int64 get_millisec() {
	__int64 now = std::chrono::duration_cast < std::chrono::milliseconds
			> (std::chrono::system_clock::now().time_since_epoch()).count();
	return now;
}

void do_partial_sum(uint64_t *final, int start_val, int sums_to_do) {
#ifdef TRACE
	coutmutex.lock();
	std::cout << "Start: TID " << std::this_thread::get_id() << " starting at "
			<< start_val << ", workload of " << sums_to_do << " items"
			<< std::endl;
	coutmutex.unlock();

	auto start = std::chrono::high_resolution_clock::now();
#endif

	uint64_t sub_result = 0;

	for (int i = start_val; i < start_val + sums_to_do; i++)
		sub_result += i;

	*final = sub_result;

#ifdef TRACE
	auto end = std::chrono::high_resolution_clock::now();

	coutmutex.lock();
	std::cout << "End  : TID " << std::this_thread::get_id() << " with result "
			<< sub_result << ", time taken "
			<< (end - start).count() << std::endl;
//					* ((double) std::chrono::high_resolution_clock::period::num
//							/ std::chrono::high_resolution_clock::period::den)
//			<< std::endl;
	coutmutex.unlock();
#endif
}

/////////////
int main() {
	part_sums.clear();

	for (int i = 0; i < threads_to_use; i++)
		part_sums.push_back(new uint64_t(0));

	std::vector<std::thread *> t;

	int sums_per_thread = max_sum_item / threads_to_use;

	auto start = get_millisec(); //std::chrono::high_resolution_clock::now();

	for (int start_val = 0, i = 0; start_val < max_sum_item; start_val +=
			sums_per_thread, i++) {
		// Lump extra bits onto last thread if work items is not equally divisible by number of threads
		int sums_to_do = sums_per_thread;

		if (start_val + sums_per_thread < max_sum_item
				&& start_val + sums_per_thread * 2 > max_sum_item)
			sums_to_do = max_sum_item - start_val;

		t.push_back(new std::thread(do_partial_sum, part_sums[i], start_val, sums_to_do));

		if (sums_to_do != sums_per_thread)
			break;
	}

	for (int i = 0; i < threads_to_use; i++)
		t[i]->join();

	uint64_t result = 0;

	std::for_each(part_sums.begin(), part_sums.end(), [&result] (uint64_t *subtotal) {result += *subtotal;});

	auto end = get_millisec(); //std::chrono::high_resolution_clock::now();

	for (int i = 0; i < threads_to_use; i++) {
		delete t[i];
		delete part_sums[i];
	}

	assert(result == uint64_t(499999999500000000));

	std::cout << "Result is correct" << std::endl;

	std::cout << "Time taken: " << (end - start) << std::endl;
//			<< (end - start).count() << std::endl;
////					* ((double) std::chrono::high_resolution_clock::period::num
////							/ std::chrono::high_resolution_clock::period::den)
////			<< std::endl;
}

