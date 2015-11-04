/*
 * http://stackoverflow.com/questions/1558208/operator-overload-in-linked-list
 */

#include <algorithm>
#include <iostream>
#include <iterator>
#include <list>
#include <string>

using namespace std;

struct Car {
	string maker;
	string year;
	string model;
};

// this is the output function for a car
std::ostream& operator<<(std::ostream& sink, const Car& car) {
	// print out car info
	sink << "Make: " << car.maker << "\nYear: " << car.year << "\nModel: "
			<< car.model << std::endl;

	return sink;
}

int main(void) {
	// a car list
	typedef std::list<Car> CarList;

	// a couple cars
	Car car1 = { "Dodge", "2001", "Stratus" };
	Car car2 = { "GMan's Awesome Car Company", "The future", "The best" };

	CarList cars;
	cars.push_back(car1);
	cars.push_back(car2);

	// now traverse the list (copy to ostream)
	std::copy(cars.begin(), cars.end(),
			std::ostream_iterator<Car>(std::cout, "\n"));

	// delete done automatically in destructor
}
