/*
 * ex_shared_mem.cpp
 *
 *  Created on: Apr 7, 2017
 *      Author: Administrator
 */

#include <boost/interprocess/shared_memory_object.hpp>
#include <boost/interprocess/mapped_region.hpp>
#include <iostream>

using namespace boost::interprocess;

int main() {
	shared_memory_object shdmem { open_or_create, "Boost", read_write };
	shdmem.truncate(1024);
	std::cout << shdmem.get_name() << '\n';
	offset_t size;
	if (shdmem.get_size(size))
		std::cout << size << '\n';

	// Example 33.2
//	mapped_region region{shdmem, read_write};
//	std::cout << std::hex << region.get_address() << '\n';
//	std::cout << std::dec << region.get_size() << '\n';
//	mapped_region region2 { shdmem, read_only };
//	std::cout << std::hex << region2.get_address() << '\n';
//	std::cout << std::dec << region2.get_size() << '\n';

	// Example 33.3
//	mapped_region region{shdmem, read_write};
//	int *i1 = static_cast<int*>(region.get_address());
//	*i1 = 99;
//	mapped_region region2 { shdmem, read_only };
//	int *i2 = static_cast<int*>(region2.get_address());
//	std::cout << *i2 << '\n';

	// Example 33.3
	bool removed = shared_memory_object::remove("Boost");
	std::cout << std::boolalpha << removed << '\n';


}

