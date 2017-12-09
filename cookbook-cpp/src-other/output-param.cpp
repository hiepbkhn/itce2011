/*
 * output-param.cpp
 *
 *  Created on: Mar 29, 2017
 *      Author: Administrator
 */

// C++ Today - The Beast Is Back

#include <iostream>
#include <vector>
#include <tuple>

using namespace std;

template<typename T>
using iterator_value_type = typename remove_reference<decltype(*declval<T>())>::type;

template<typename ConstInputIterator, typename MinMaxType = iterator_value_type<ConstInputIterator>>
auto lenminmax(ConstInputIterator first, ConstInputIterator last)
-> tuple<size_t, MinMaxType, MinMaxType> {
//	if (first == last) {
//		return {0, 0, 0};
//	}
	size_t count = 1;
	auto minimum(*first);
	auto maximum(minimum); // only evaluate *first once
	while (++first != last) {
		++count;
		auto const value = *first;
		if (value < minimum) {
			minimum = value;
		} else if (maximum < value) {
			maximum = value;
		}
	}
	return {count, minimum, maximum};
}

int main(){

	vector<int const> samples{5, 3, 6, 2, 4, 8, 9, 12, 3};
	int min, max;
	tie(ignore, min, max) = lenminmax(samples.begin(), samples.end());
	cout << "minimum: " << min << "\n"
	<< "maximum: " << max << "\n";

	return 0;
}

