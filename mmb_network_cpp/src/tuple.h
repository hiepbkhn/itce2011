/*
 * tuple.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef TUPLE_H_
#define TUPLE_H_

#include "geom_util.h"

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

////
class TripleDouble {
public:
	double v0;
	double v1;
	double v2;

	//
	TripleDouble(double _v0, double _v1, double _v2) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
	}
};

////
class TripleDoubleInt{
public:
	double v0;
	double v1;
	int v2;		// cur_edge_id --> see MMBMap

	//
	TripleDoubleInt(double _v0, double _v1, int _v2) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
	}

	bool operator < (const TripleDoubleInt& rhs) const
	{
		if (v0 < rhs.v0)
			return -1;
		else if (v0 > rhs.v0)
			return 1;

		if (v1 < rhs.v1)
			return -1;
		else if (v1 > rhs.v1)
			return 1;

		if (v2 < rhs.v2)
			return -1;
		else if (v2 > rhs.v2)
			return 1;

		return 0;
	}
};

////
class PairBoolSeg{
public:
	bool result = false;
	EdgeSegment seg;

	PairBoolSeg(bool _result, EdgeSegment& _seg) {
		result = _result;
		seg = _seg;
	};
};


#endif /* TUPLE_H_ */
