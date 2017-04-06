/*
 * single-core.cpp
 *
 *  Created on: Apr 6, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>
#include <chrono>
#include <cstdint>  // for std::uint64_t

const int max_sum_item = 1000000000;

using namespace std::chrono;

__int64 get_millisec() {
	__int64 now = duration_cast < milliseconds
			> (system_clock::now().time_since_epoch()).count();
	return now;
}


int main() {
	std::uint64_t result = 0;

	__int64 start = get_millisec();

	for (int i = 0; i < max_sum_item; i++)
		result += i;

	std::cout << result <<std::endl;
	std::cout<<"Elapsed : " << (get_millisec() - start) <<std::endl;
}

