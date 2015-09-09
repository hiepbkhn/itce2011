//////////////////////////////////////////////////////////////////////////
// Copyright 2007
// The Regents of the University of California
// All Rights Reserved
//
// Permission to use, copy, modify and distribute any part of
// this software package for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby
// granted, provided that the above copyright notice, this paragraph
// and the following paragraphs appear in all copies.
//
// Those desiring to incorporate this into commercial products or
// use for commercial purposes should contact the Technology Transfer
// Office, University of California, San Diego, 9500 Gilman Drive,
// La Jolla, CA 92093-0910, Ph: (858) 534-5815, FAX: (858) 534-7345.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
// PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
// DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
//
// THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE
// UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE,
// SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS. THE UNIVERSITY
// OF CALIFORNIA MAKES NO REPRESENTATIONS AND EXTENDS NO WARRANTIES
// OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT
// INFRINGE ANY PATENT, TRADEMARK OR OTHER RIGHTS.
//
//
// Written by Priya Mahadevan <pmahadevan@cs.ucsd.edu>  and
//            Calvin Hubble <chubble@cs.ucsd.edu
//
//////////////////////////////////////////////////////////////////////////

#ifndef __MOVING_AVERAGE_H
#define __MOVING_AVERAGE_H

#include <list>

////////////////////////////////////////////////////////////
// MovingAverage
//
// A helper class used for calculating the first knee.  Maintains a
// moving average over a specified window.
////////////////////////////////////////////////////////////

class MovingAverage {
private:
	std::list<float> pointList;
	unsigned int windowSize;

	// Used if we dont have a window
	float sum_nowindow;
	int numPoints_nowindow;
public:
	MovingAverage(unsigned int wsize) :
			windowSize(wsize), sum_nowindow(0.0), numPoints_nowindow(0) {
	}

	void addPoint(float point) {
		if (windowSize) {
			pointList.push_back(point);
			while (pointList.size() > windowSize) {
				pointList.pop_front();
			}
		} else {
			sum_nowindow += point;
			numPoints_nowindow++;
		}
	}

	int getWindowSize() {
		if (windowSize)
			return windowSize;

		return numPoints_nowindow;
	}

	float getAverage() const {
		if (windowSize) {
			float sum = 0;
			int cnt = 0;
			for (std::list<float>::const_iterator i = pointList.begin();
					i != pointList.end(); ++i) {
				sum += *i;
				cnt++;
			}
			assert(cnt > 0);

			return (sum / cnt);
		} else {
			return (sum_nowindow) / (float) numPoints_nowindow;
		}
	}

	// doesn't make sense if we have an infinite window
	bool isFull() const {
		if (pointList.size() < windowSize)
			return false;
		return true;
	}

	// doesn't make sense if we have an infinite window
	float getOldestPoint() const {
		return pointList.front();
	}
};

#endif
