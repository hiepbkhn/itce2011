/*
 * tuple.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef TUPLE_H_
#define TUPLE_H_

////
class PairInt{
public:
	int x = 0;
	int y = 0;

	//
	PairInt(int _x, int _y){
		x = _x;
		y = _y;
	}

	// for map<PairInt,..>
	bool operator <(const PairInt& rhs) const{
		return x * 100000 + y < rhs.x * 100000 + rhs.y;
	}
};

////
class PairDouble{
public:
	double x;
	double y;

	//
	PairDouble(double _x, double _y) {
		x = _x;
		y = _y;
	}

	// for map<PairDouble,..>
	bool operator <(const PairInt& rhs) const{
		return x * 1E10 + y < rhs.x * 1E10 + rhs.y;
	}


};


#endif /* TUPLE_H_ */
