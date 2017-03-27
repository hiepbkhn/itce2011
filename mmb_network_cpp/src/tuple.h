/*
 * tuple.h
 *
 *  Created on: Mar 25, 2017
 *      Author: Administrator
 */

#ifndef TUPLE_H_
#define TUPLE_H_

#include <vector>
#include <set>

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
	bool operator <(const PairDouble& rhs) const{
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
			return true;
		else if (v0 == rhs.v0){

			if (v1 < rhs.v1)
				return true;
			else if (v1 == rhs.v1){

				if (v2 < rhs.v2)
					return true;
			}
		}
		return false;
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

////
class PairEdgeSegInt{
public:
	EdgeSegment e;
	int obj_id;

	//
	PairEdgeSegInt(EdgeSegment _e, int _obj_id) {
		e = _e;
		obj_id = _obj_id;
	}

	bool operator < (const PairEdgeSegInt& arg0) const
	{
		return e.cur_edge_id < arg0.e.cur_edge_id;
	}
};

////
class PairIntList {
public:
	int num_edges;
	vector<PairInt> list_edges;
	//
	PairIntList(int _num_edges, vector<PairInt>& _list_edges) {
		num_edges = _num_edges;
		list_edges = _list_edges;
	}
};

////
class PairSetInt {
public:
	set<int> s;
	int i;

	//
	PairSetInt(set<int> _s, int _i) {
		s = _s;
		i = _i;
	}


};

////
class PairSetListInt {
public:
	vector<set<int>> set_list;
	int i;

	//
	PairSetListInt(vector<set<int>>& _set_list, int _i) {
		set_list = _set_list;
		i = _i;
	}
};



#endif /* TUPLE_H_ */
