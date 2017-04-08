/*
 * ex_manged_shared_mem.cpp
 *
 *  Created on: Apr 7, 2017
 *      Author: Administrator
 */

#include <boost/interprocess/managed_shared_memory.hpp>
#include <iostream>

using namespace boost::interprocess;

int main() {

	// Example 33.6
//	shared_memory_object::remove("Boost");
//
//	managed_shared_memory managed_shm { open_or_create, "Boost", 1024 };
//
//	int *i = managed_shm.construct<int>("Integer")(99);	// looks like a call to a constructor
//	std::cout << *i << '\n';
//	std::pair<int*, std::size_t> p = managed_shm.find<int>("Integer");
//	if (p.first)
//		std::cout << *p.first << '\n';

	// Example 33.7
//	shared_memory_object::remove("Boost");
//
//	managed_shared_memory managed_shm { open_or_create, "Boost", 1024 };
//	int *i = managed_shm.construct<int>("Integer")[10](99);
//	std::cout << *i << '\n';
//	std::pair<int*, std::size_t> p = managed_shm.find<int>("Integer");
//	if (p.first) {
//		std::cout << *p.first << '\n';
//		std::cout << p.second << '\n';	//  you can tell whether objects returned by find() are single objects or arrays
//	}

	// Example 33.8
//	try {
//		shared_memory_object::remove("Boost");
//		managed_shared_memory managed_shm { open_or_create, "Boost", 1024 };
//		int *i = managed_shm.construct<int>("Integer")[64](99);	// 512 integers = 2048 bytes : overflow
//	} catch (boost::interprocess::bad_alloc &ex) {
//		std::cerr << ex.what() << '\n';
//	}

	// Example 33.9
	shared_memory_object::remove("Boost");
	managed_shared_memory managed_shm{open_or_create, "Boost", 1024};
	int *i = managed_shm.find_or_construct<int>("Integer")(99);
	std::cout << *i << '\n';
	managed_shm.destroy<int>("Integer");
	std::pair<int*, std::size_t> p = managed_shm.find<int>("Integer");
	std::cout << p.first << '\n';
}

