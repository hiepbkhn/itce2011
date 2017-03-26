/*
 * test-oop.cpp
 *
 *  Created on: Mar 25, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>


using namespace std;

////
class EdgeSegment{
public:
	double start_x;
	double start_y;
	double end_x;
	double end_y;
	int cur_edge_id;

	EdgeSegment(){

	}

	//
	EdgeSegment(double _start_x, double _start_y, double _end_x, double _end_y, int _cur_edge_id) {
		start_x = _start_x;
		start_y = _start_y;
		end_x = _end_x;
		end_y = _end_y;
		cur_edge_id = _cur_edge_id;
	}
};

////
class PairBoolSeg{
public:
	bool result;
	EdgeSegment seg;

	PairBoolSeg(bool _result, EdgeSegment& _seg) {
		result = _result;
		seg = _seg;
	};
};

/////////////////////
int main(){
	EdgeSegment e = EdgeSegment(0,0,1,1,10);

	PairBoolSeg p = PairBoolSeg(true, e);

	cout<< e.end_y<<endl;
	cout<< p.result << " " << p.seg.cur_edge_id <<endl;

}



