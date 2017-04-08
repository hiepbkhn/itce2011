/*
 * ex_managed_shared_containers.cpp
 *
 *  Created on: Apr 7, 2017
 *      Author: Administrator
 */

#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/allocators/allocator.hpp>
#include <boost/interprocess/containers/string.hpp>
#include <functional>		// std::bind
#include <iostream>

using namespace boost::interprocess;

void construct_objects(managed_shared_memory &managed_shm) {
	managed_shm.construct<int>("Integer")(99);
	managed_shm.construct<float>("Float")(3.14);
}

int main() {

	// Example 33.10
//	shared_memory_object::remove("Boost");
//	managed_shared_memory managed_shm { open_or_create, "Boost", 1024 };
//
//	typedef allocator<char, managed_shared_memory::segment_manager> CharAllocator;
//	typedef basic_string<char, std::char_traits<char>, CharAllocator> string;
//
//	string *s = managed_shm.find_or_construct<string>("String")("Hello!", managed_shm.get_segment_manager());
//	s->insert(5, ", world");
//	std::cout << *s << '\n';

	// Example 33.11
	shared_memory_object::remove("Boost");
	managed_shared_memory managed_shm { open_or_create, "Boost", 1024 };
	auto atomic_construct = std::bind(construct_objects, std::ref(managed_shm));

	managed_shm.atomic_func(atomic_construct);
	std::cout << *managed_shm.find<int>("Integer").first << '\n';
	std::cout << *managed_shm.find<float>("Float").first << '\n';
}

