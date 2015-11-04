/*
 * random-test.cpp
 *
 *  Created on: Nov 4, 2015
 *      Author: huunguye
 */

#include <stdlib.h>

#include <iostream>
#include <chrono>
#include <random>

#define __cplusplus 201103L

int main(){

//	cout<< RAND_MAX;	// 32767

	// obtain a seed from the system clock:
	unsigned seed = std::chrono::system_clock::now().time_since_epoch().count();

	std::minstd_rand0 generator (seed);  // minstd_rand0 is a standard linear_congruential_engine
	std::cout << "Random value: " << generator() << std::endl;

	return 0;
}


