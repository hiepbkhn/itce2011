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

#include <iostream>
#include <fstream>
#include <list>
#include <map>
#include <math.h>

#include <boost/program_options.hpp>
#include "dkUtils.h"
#include "MovingAverage.h"

#define CORR_COEFF .01
#define PRUNE_FACTOR .75

using namespace std;
namespace po = boost::program_options;

// maps newDegree -> list of old degrees.  When scaling up the list
// should always be one.  Also there is a 1-to-1 in the "small degree"
// section and fat tail section, only the fuzzy section will impose a
// non 1-to-1 mapping.
typedef map<int, list<int> > ScaleMapping;

// Scale a given NKMap/NKKMap to have a specified number of nodes.
// The original number of nodes is inferred from the NKmap
pair<int, int> dkRescale1k(const NKMap& originalMap, NKMap& newMap,
		int numNodes_new, ScaleMapping& newToOldMap, int maxDegree);

void dkRescale2k(const NKKMap& originalMap, NKKMap& newMap, int numNodes_new,
		int maxDegree);

// Locate the "noisy knee" - portion inbetween the small degree and
// fat tail degrees. 
int dkGetKnee1(const NKMap& nkmap, const PKMap& pkmap, int numNodes);

// Locates the fat tail
int dkGetKnee2(const NKMap& nkmap);

float calculateCorrelationCoefficient(const list<Point>& points, float xmean,
		float ymean);

// Scale the small degrees - degrees stay the same, # of nodes with
// that degree scales by a factor
void dkScaleSmallDegrees(const PKMap& pkmap, NKMap& rescaledMap,
		list<int>& degreeList, int numNodes, int numNodes_new,
		ScaleMapping& newToOldMap);

// Scale the fat tail.  # of nodes of that degree stay the same, but
// the degree scales up
void dkScaleHighDegrees(const PKMap& pkmap, NKMap& rescaledMap,
		list<int>& degreeList, int numNodes, int numNodes_new, int minDegree,
		int maxDegree, ScaleMapping& newToOldMap);

// Scale the medium degrees.  Returns a pair <X,X'> which is the number
// of unique degrees in the 
pair<int, int> dkScaleMediumDegrees(const PKMap& pkmap, const NKMap& nkmap,
		NKMap& rescaledMap, list<int>& degreeList, int numNodes,
		int numNodes_new, ScaleMapping& newToOldMap);

int main(int argc, char* argv[]) {
	string opt_infile;
	int opt_newNumNodes = 0;
	int opt_k;
	int opt_do_prob = 0;
	int opt_maxDegree = 0;
	int opt_randomSeed = 0;

	// Declare the supported options.
	po::options_description desc_mandatory("Mandatory arguments");
	desc_mandatory.add_options()("dk,k", po::value<int>(&opt_k),
			"{1, 2}-dk distribution")("input,i", po::value<string>(&opt_infile),
			"input distribution file")("nodes,n",
			po::value<int>(&opt_newNumNodes),
			"number of nodes in target graph");

	po::options_description desc_optional("Optional arguments");
	desc_optional.add_options()("help,h", "produce help message")("prob,p",
			"print probability distributions instead of degree distributions")(
			"max_degree,m", po::value<int>(&opt_maxDegree),
			"Maximum degree (330 recommended for router topos")("seed,s",
			po::value<int>(&opt_randomSeed),
			"Random seed (default: system time)");

	// Put any option arguments here
	po::options_description all_options;
	all_options.add(desc_mandatory).add(desc_optional);
	po::variables_map vm;
	po::store(po::parse_command_line(argc, argv, all_options), vm);
	po::notify(vm);

	// Parse options
	if (vm.count("help")) {
		cout << all_options << "\n";
		return 1;
	}
	if (vm.count("prob"))
		opt_do_prob = 1;

	if (vm.count("seed"))
		srand(opt_randomSeed);
	else
		srand(time(NULL));

	if ((!vm.count("dk")) || (!vm.count("input")) || (!vm.count("nodes"))) {
		cerr << "Must specify dk distribution, input graph file and "
				<< "number of nodes" << endl;
		cerr << "Try --help for more information" << endl;
		return 1;
	}

	if ((opt_k != 1) && (opt_k != 2)) {
		cerr << "Invalid dk option: " << opt_k << endl;
		return 1;
	}

	// After command line parameter checks

	// Rescale using 2k
	if (opt_k == 2) {
		NKKMap nkkmap, nkkRescaled;
		if (!read2kDistribution(opt_infile, nkkmap) || (nkkmap.empty())) {
			cerr << "Error reading 2k distribution" << endl;
			exit(1);
		}

		dkRescale2k(nkkmap, nkkRescaled, opt_newNumNodes, opt_maxDegree);
		cout << nkkRescaled;
		return 0;
	} else if (opt_k == 1) {
		NKMap nkMap, nkRescaled;
		if (!read1kDistribution(opt_infile, nkMap) || (nkMap.empty())) {
			cerr << "Error reading 1k distribution" << endl;
			exit(1);
		}

		ScaleMapping notUsed;
		dkRescale1k(nkMap, nkRescaled, opt_newNumNodes, notUsed, opt_maxDegree);

		cerr << "Original avg degree:" << dkCalcAverageDegree(nkMap) << endl;
		cerr << "New avg degree:" << dkCalcAverageDegree(nkRescaled) << endl;

		if (opt_do_prob) {
			PKMap pkmap;
			NKToPK(nkRescaled, pkmap);
			cout << pkmap;
		} else
			cout << nkRescaled << endl;
	}

	return 0;
}

void dkCalcRescaleUpDistribution(const PKMap& rescaledPKMap,
		const ScaleMapping& newToOldMap, const NKKMap& nkkOrig,
		const NKMap& nkOrig, NKKMap& nkkRescaled, int k_knee1, int k_knee2,
		int X, int X_prime, int numNodes, int numNodes_new) {

	PKMap deg_deg_percent;
	NKKMap nkkRescaled_tmp;

	for (PKMap::const_iterator i = rescaledPKMap.begin();
			i != rescaledPKMap.end(); ++i) {
		int degree1 = i->first;

		assert(newToOldMap.find(degree1) != newToOldMap.end());
		assert(!newToOldMap.find(degree1)->second.empty());

		int degree1_orig = newToOldMap.find(degree1)->second.front();

		for (NKMap::const_iterator j = nkOrig.begin(); j != nkOrig.end(); ++j) {
			int k = j->first;
			//int tmp_mk1k2 = nkkOrig[degree1_orig][k];
			int tmp_mk1k2;
			if ((nkkOrig.find(degree1_orig) != nkkOrig.end())
					&& (nkkOrig.find(degree1_orig)->second.find(k)
							!= nkkOrig.find(degree1_orig)->second.end()))

				tmp_mk1k2 = nkkOrig.find(degree1_orig)->second.find(k)->second;
			else
				tmp_mk1k2 = 0;

			//int t2 = nkOrig[degree1_orig];
			int t2 = nkOrig.find(degree1_orig)->second;

			deg_deg_percent[k] = float(tmp_mk1k2) / float(degree1_orig * t2);

		}

		int frac = int((float(X_prime) / float(X)));

		if (frac == 0)
			frac = 1;

		for (PKMap::const_iterator j = rescaledPKMap.begin();
				j != rescaledPKMap.end(); ++j) {
			if (j->second == 0)
				continue;

			int degree2 = j->first;
			int degree2_orig = newToOldMap.find(degree2)->second.front();

			if ((frac > 1.0) && (degree2_orig <= k_knee2)
					&& (degree2_orig >= k_knee1)) {

				nkkRescaled_tmp[degree1][degree2] = int(
						((deg_deg_percent[degree2_orig] * degree1 * numNodes_new
								* rescaledPKMap.find(degree1)->second)) / frac
								+ 0.5);
			} else {
				float totfrac = 0;
				for (list<int>::const_iterator tmpDeg = newToOldMap.find(
						degree2)->second.begin();
						tmpDeg != newToOldMap.find(degree2)->second.end();
						tmpDeg++) {
					totfrac += deg_deg_percent[*tmpDeg];
				}

				nkkRescaled_tmp[degree1][degree2] = int(
						(totfrac * degree1 * numNodes_new
								* rescaledPKMap.find(degree1)->second + .5));
			}
		}
	}

	NKKMap tmp;
	for (NKKMap::iterator i = nkkRescaled_tmp.begin();
			i != nkkRescaled_tmp.end(); ++i) {
		int k1 = i->first;

		for (NKMap::iterator j = i->second.begin(); j != i->second.end(); ++j) {
			int k2 = j->first;
// 	    int edges = j->second;

// 	    if (pkkRescaled[k2][k1] > pkkRescaled[k1][k2])
// 		pkkRescaled[k1][k2] = pkkRescaled[k2][k1];
// 	    else
// 		pkkRescaled[k2][k1] = pkkRescaled[k1][k2];
			int factor = 1;

			if (k1 == k2)
				factor = 2;

			nkkRescaled[k1][k2] = (int) nkkRescaled_tmp[k1][k2] * factor;
			nkkRescaled[k2][k1] = (int) nkkRescaled_tmp[k1][k2] * factor;

		}
	}
}

////////////////////////////////////////////////////////////
// dkRescale2k 
// 
// Rescales a 2k distribution!  I realize this is overkill since it
// stores 2 copies of each distribution (2 copies of the 2k
// distribution, one in pdf form the other in normal form as well as 2
// copies of the corresponding 1k distribution).  Sorry for the
// confusion, I will fix the helper functions so everything only uses
// a single type of distribution.
////////////////////////////////////////////////////////////

void dkRescale2k(const NKKMap& originalMap, NKKMap& rescaledMap,
		int numNodes_new, int maxDegree) {
//    PKKMap pkkRescaledMap;  // Rescaled probability distribution

	NKMap nkmap; // 1k distribution representing our
	PKMap pkmap; // 1k prob. distribution of above

	NKMap nkRescaledMap; // Rescaled 1k distribution

	NKKToNK(originalMap, nkmap);
	NKToPK(nkmap, pkmap);

	int numNodes = dkCalcNumNodes(nkmap);

	int k_knee1 = dkGetKnee1(nkmap, pkmap, numNodes);
	int k_knee2 = dkGetKnee2(nkmap);

	if ((k_knee1 > k_knee2) || (k_knee1 <= 0))
		k_knee1 = k_knee2;

	ScaleMapping newToOldMap;

	// Rescale the 1k distribution
	pair<int, int> supp_M;
	supp_M = dkRescale1k(nkmap, nkRescaledMap, numNodes_new, newToOldMap,
			maxDegree);

	int X = supp_M.first;
	int X_prime = supp_M.second;

	// Convert scaled nkmap to a pkmap
	PKMap rescaledPKMap;
	NKToPK(nkRescaledMap, rescaledPKMap);

	dkCalcRescaleUpDistribution(rescaledPKMap, newToOldMap, originalMap, nkmap,
			rescaledMap, k_knee1, k_knee2, X, X_prime, numNodes, numNodes_new);
}

////////////////////////////////////////////////////////////
// dkRescale1k
//
// Rescales a 1k distribution.  Breaks distribution up into 3 pieces
// and scales each individually
////////////////////////////////////////////////////////////

pair<int, int> dkRescale1k(const NKMap& originalMap, NKMap& rescaledMap,
		int numNodes_new, ScaleMapping& newToOldMap, int maxDegree) {
	PKMap pkmap;
	unsigned int numNodes = dkCalcNumNodes(originalMap);

	if (numNodes_new >= (unsigned int) (numNodes * 10))
		cerr
				<< "Warning - scale factor of greater than 10, weird behaviors may ensue"
				<< endl;

	NKToPK(originalMap, pkmap);

	// Compute the degree where the heavy tail starts in the given
	// degree distribution.
	int k_knee1 = dkGetKnee1(originalMap, pkmap, numNodes);
	int k_knee2 = dkGetKnee2(originalMap);

	if ((k_knee1 > k_knee2) || (k_knee1 <= 0))
		k_knee1 = k_knee2;

	list<int> smallDegreeList;
	list<int> mediumDegreeList;
	list<int> highDegreeList;

	// Break the distribution up into its high, medium and low
	// components
	for (NKMap::const_iterator i = originalMap.begin(); i != originalMap.end();
			++i) {
		int degree = i->first;
		assert(pkmap.find(degree) != pkmap.end());

		if (degree < k_knee1) {
			smallDegreeList.push_back(degree);
		} else if (degree <= k_knee2) {
			mediumDegreeList.push_back(degree);
		} else // if (degree >= k_knee2)
		{
			highDegreeList.push_back(degree);
		}
	}

	if (!highDegreeList.empty())
		dkScaleHighDegrees(pkmap, rescaledMap, highDegreeList, numNodes,
				numNodes_new, k_knee1, maxDegree, newToOldMap);

	if (!smallDegreeList.empty())
		dkScaleSmallDegrees(pkmap, rescaledMap, smallDegreeList, numNodes,
				numNodes_new, newToOldMap);

	// We have to scale the medium components last.  The medium
	// components rely on seeing what the small and high degrees do in
	// order to calculate the scaled distribution

	pair<int, int> supp_M(0, 0);

	if (!mediumDegreeList.empty())
		supp_M = dkScaleMediumDegrees(pkmap, originalMap, rescaledMap,
				mediumDegreeList, numNodes, numNodes_new, newToOldMap);

	return supp_M;
}

////////////////////////////////////////////////////////////
// dkScaleSmallDegrees
//
// Scales the first portion of the distribution.  Degrees are
// preserved, but the probability changes
//
// For i = 1...k_knee1, compute degree distrubtion in the new
// topology using the formula: N_{new}(i) = P(i) * N_{new}
////////////////////////////////////////////////////////////

void dkScaleSmallDegrees(const PKMap& pkmap, NKMap& rescaledMap,
		list<int>& degreeList, int numNodes, int numNodes_new,
		ScaleMapping& newToOldMap) {
	while (!degreeList.empty()) {
		int degree = degreeList.front();
		degreeList.pop_front();

		rescaledMap[degree] = (int) round(
				pkmap.find(degree)->second * (float) numNodes_new);

		assert(newToOldMap.find(degree) == newToOldMap.end());
		newToOldMap[degree].push_back(degree);
	}
}

////////////////////////////////////////////////////////////
// createMediumDegreeMap
//
// Updates newToOldMap with mappings from old distribution to new
// distribution used in the 2k rescaling
////////////////////////////////////////////////////////////

void createMediumDegreeMap(const vector<int>& oldDegrees,
		const vector<int>& newDegrees, ScaleMapping& newToOldMap) {
	if (newDegrees.size() < oldDegrees.size()) {
		// Scaling down
		float step = float(newDegrees.size()) / float(oldDegrees.size());

		for (unsigned int i = 0; i < oldDegrees.size(); ++i) {
			// Get corresponding map new degree
			int newIndex = int(step * float(i));
			int oldDegree = oldDegrees[i];
			int newDegree = newDegrees[newIndex];

			newToOldMap[newDegree].push_back(oldDegree);
		}
	} else {
		// Scaling up
		float step = float(oldDegrees.size() / float(newDegrees.size()));
		for (unsigned int i = 0; i < newDegrees.size(); ++i) {
			int oldIndex = int(step * float(i));
			int newDegree = newDegrees[i];
			int oldDegree = oldDegrees[oldIndex];

			newToOldMap[newDegree].push_back(oldDegree);
		}
	}
}

////////////////////////////////////////////////////////////
// dkScaleMediumDegrees
//
// Scales the medium portion of a degree distribution.  Updates the
// ScaleMapping to keep track of, for each node, which old degrees
// correspond to the newly-created degrees.
//
// The medium degrees are more challenging than the small and high
// degrees.  This method first finds the end points - kl and kh -
// which are the two knees of the distribution.  We then use a linear
// rescaling to calculate the new degrees along the line connecting
// the ends points (linear only in log log scale).  Lastly we add
// randomness and prune the distribution of excess degrees when
// necessary.
//
// Returns the pair (X,X') where X = |supp(P)|_M and X' =
// |supp(P')|_M.  In other words, the number of unique degrees in the
// medium part of the distribution.
//
// This function also updates the scalemapping which maps nodes in the
// new distribution to a list of nodes from the original distribution
////////////////////////////////////////////////////////////

pair<int, int> dkScaleMediumDegrees(const PKMap& pkmap, const NKMap& nkmap,
		NKMap& rescaledMap, list<int>& degreeList, int numNodes,
		int numNodes_new, ScaleMapping& newToOldMap) {
	int X = 0; // size of support in original graph
	int X_prime = 0; // ''                  new
	int kl, kh; // beginning and end degrees
	int kl_nodes, kh_nodes; // corresponding # of nodes for kl and kh
	int u, u_prime;
	float totalScaleFactor = float(numNodes_new) / numNodes;

	Point startPoint; // start point of the degree dist in log scale
	Point endPoint; // end point of the degree dist in log scale

	if (degreeList.empty())
		return (pair<int, int>(0, 0));

	// Calculate and add kl.  kl degree maintained, distribution is
	// scaled.  kh degree is scaled but total number of nodes is kept
	// the same
	kl = degreeList.front();
	kl_nodes = (int) ceil(pkmap.find(kl)->second * float(numNodes_new));

	kh = (int) ceil(totalScaleFactor * degreeList.back());
	kh_nodes = (int) round(
			pkmap.find(degreeList.back())->second * float(numNodes));

	// Check if no new degrees are going to be added (most likely
	// scaling down or scaling up by a very marginal amount)
	if (kl >= kh) {
		rescaledMap[kl] = kl_nodes;
		newToOldMap[kl].push_back(kl);

		//assert(0);
		return (pair<int, int>(0, 0));
	}

	// Start and end points for linear degree rescaling
	startPoint = Point(log(kl), log(kl_nodes));
	endPoint = Point(log(kh), log(kh_nodes));

	// Calculate u - the total number of nodes in the medium portion
	// of the original distribution

	u = 0;
	list<int>::reverse_iterator i = degreeList.rend();
	for (i = degreeList.rbegin(); i != degreeList.rend(); ++i) {
		int degree = *i;
		int numNodes = nkmap.find(degree)->second;
		u += numNodes;

		X++; // # of unique degrees in the original medium part
	}

	// calculate u' - the expected number of nodes in the medium
	// portion of the rescaled distribution

	u_prime = 0;
	int tmp = 0;
	for (NKMap::const_iterator i = rescaledMap.begin(); i != rescaledMap.end();
			++i) {

		tmp += i->second;
	}
	u_prime = numNodes_new - tmp;

	// Corner case - depending on input graph, scaling and the small
	// and large degrees the above calculation could result in a
	// negative numbers.
	if (u_prime <= 1) {
		// Corner case - overlap
		if (rescaledMap.find(kl) != rescaledMap.end())
			return (pair<int, int>(X, 0));

		rescaledMap[kl] = kl_nodes;
		newToOldMap[kl].push_back(kl);

		return (pair<int, int>(X, 1));
	}

	map<int, int> newDegreeMap; // map new degree -> # of edges

	// XXX try somethign new (from calvin)
	// The below is a direct port from Priya's original methods
	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		int deg = i->first;
		int newDegree = (int) round(totalScaleFactor * (deg - 1)) + 1;
		// int newDegree = (int) totalScaleFactor * (deg-1);

		if ((newDegree < kl) || (newDegree > kh)) {
			continue;
		}
		if (newDegreeMap.find(newDegree) != newDegreeMap.end()) {
			continue;
		}
		float slope = (startPoint.y - endPoint.y) / (startPoint.x - endPoint.x);

		float b = startPoint.y - slope * startPoint.x;

		float x_new = log(newDegree);
		float y_new = slope * x_new + b;

		int newNumNodes_projected = (int) round(powf(M_E, y_new));
		if (rand() % 2)
			newNumNodes_projected += (rand()
					% ((int) ceil(float(newNumNodes_projected) / 2) + 1));
		else
			newNumNodes_projected -= (rand()
					% ((int) ceil(float(newNumNodes_projected) / 2) + 1));

		// For graphs with no medium degrees (knee1 == knee2)
		if (u == 1)
			newNumNodes_projected = 1;

		newDegreeMap[newDegree] = newNumNodes_projected;
	}

	// Create a mapping (this is needed by 2k rescaling) that means
	// each degree in the new distribution to 1 or more corresponding
	// degrees in the original distribution
	vector<int> oldDegrees;
	for (list<int>::iterator i = degreeList.begin(); i != degreeList.end();
			++i) {
		oldDegrees.push_back(*i);
	}

	vector<int> newDegrees;
	for (map<int, int>::iterator i = newDegreeMap.begin();
			i != newDegreeMap.end(); ++i) {
		if (i->second > 0) {
			newDegrees.push_back(i->first);
			rescaledMap[i->first] = i->second;
			X_prime++;
		}
	}

	// Create a new degree map
	createMediumDegreeMap(oldDegrees, newDegrees, newToOldMap);

	// Calculate degrees of new rescaled topology
	// list<int> newDegreeList;
	return (pair<int, int>(X, X_prime));
}

////////////////////////////////////////////////////////////
// dkScaleHighDegrees
//
// scales all of the heavy tail degrees (1k)
//
//  pkmap - the original probability map
//  rescaledMap - the rescaled distribution map (nk)
//  numNodes - original # of nodes
//  numNodes_new - total number of target nodes
// 
//  minDegree - Don't scale nodes less than minDegree - typically this
//  is the 90th percentile or simply all of the smaller degrees
// 
//  max_degree - the maximum number of degree (for router level
//  topologies).  0 means no max degree.  ~330 is often used for
//  scaling router level topos
//
//  ScaleMapping - maps [newDegree] -> [list of old nodes].  THis
//  mapping gets updated for every node scaled
////////////////////////////////////////////////////////////

void dkScaleHighDegrees(const PKMap& pkmap, NKMap& rescaledMap,
		list<int>& degreeList, int numNodes, int numNodes_new, int minDegree,
		int maxDegree, ScaleMapping& newToOldMap) {

//     cerr << "maxDegree: " << maxDegree << " and:" 
// 	 << degreeList.back() << " and:" << degreeList.front() << endl;

	while (!degreeList.empty()) {
		int degree = degreeList.front();
		degreeList.pop_front();

		int newDegree = (int) ceil(degree * double(numNodes_new) / numNodes);

		if (newDegree <= minDegree) {
			newToOldMap[minDegree].push_back(degree);
			continue;
		}

		if (maxDegree && (newDegree > maxDegree)) {
			if (!newToOldMap[maxDegree].empty()) {
				newToOldMap[maxDegree].clear();
			}
			newToOldMap[maxDegree].push_front(degree);

			rescaledMap[maxDegree] = (int) round(
					float(numNodes) * (pkmap.find(degree)->second));

// 	    cerr <<  "WAWOOHA: " << rescaledMap[maxDegree] << endl;
			// Get the max degree
			//newToOldMapping
			// assert(0);
			continue;
		}

		// Add degree
		rescaledMap[newDegree] = (int) round(
				float(numNodes) * (pkmap.find(degree)->second));

		// Update the mapping.  This might not be a 1 to 1 mapping if,
		// for example, we scale down and multiple degrees collapse
		// into a single.  Scaling up will always result into 1 to 1
		// mapings
		newToOldMap[newDegree].push_back(degree);
	}

//     assert(0);.
}

////////////////////////////////////////////////////////////
// calculateCorrelationCoefficient
//
// Calculates the correlation coefficient for a set of points given a
// specified mean.  This is used for calculating the first knee.
////////////////////////////////////////////////////////////

float calculateCorrelationCoefficient(const list<Point>& points, float xmean,
		float ymean) {
	// for explanation on these variables, see equation for
	// calculating correlation coefficient

	float numer = 0.0;
	float denom_x = 0.0;
	float denom_y = 0.0;
	int cnt = 0;
	for (list<Point>::const_iterator i = points.begin(); i != points.end();
			++i) {
		const Point& p = *i;
		numer += (p.x - xmean) * (p.y - ymean);
		denom_x += (p.x - xmean) * (p.x - xmean);
		denom_y += (p.y - ymean) * (p.y - ymean);
		cnt++;
	}

	// base case - 0 or 1 points, just return 1
	if (cnt <= 1)
		return 1;

	// if variance = 0 (they all lie on same line), then return a
	// correlation of 1
	if ((denom_x == 0) || (denom_y == 0))
		return 1.0;

	return (float) numer / (sqrt(denom_x * denom_y));
}

////////////////////////////////////////////////////////////
// dkGetKnee1
//
// Finds the first knee (point that seperates between small degees and
// medium degrees).  Uses a linear regression to find the point when
// the input distribution stops "looking like a straight line".  This
// must all be done in log scale
////////////////////////////////////////////////////////////

int dkGetKnee1(const NKMap& nkmap, const PKMap& pkmap, int numNodes) {

	// Start looking after the 85th percentile.  The higher percentile
	// degrees might express some non-linear behavior (in log-log
	// scale), but everything after the 90th % seems to be linear
	// until the first knee ... \cite{Priya}

	float cumm = 0.0;
	int degree = 0;

	PKMap::const_iterator i;
	for (i = pkmap.begin(); i != pkmap.end(); ++i) {
		degree = i->first;
		float percent = i->second;
		cumm += percent;

		if (cumm > .85)
			break;
	}

	// Check if we couldn't find a first knee in the graph.  This
	// really never happen for well formed topologies
	if (i == pkmap.end())
		return 0;

	// Starting at the 90th percentile, find the "knee" of the curve.
	// Approach one: Priya's approach, as soon as there exists a gap
	// of 4 or more between successive degrees, mark that as the knee

	// Below code does a linear regression in log scale... IMO this is
	// the best approach

	MovingAverage movingAverage_x(3); // running average
	MovingAverage movingAverage_y(3); // running average
	list<Point> points;
	for (; i != pkmap.end(); ++i) {
		int degree = i->first;
		float pr = i->second;

		Point p(log(degree), log(pr));
		points.push_back(p);
		while (points.size() > 3)
			points.pop_front();

		// We need to keep track of the means in order to calculate
		// the correlation coefficient
		movingAverage_x.addPoint(p.x);
		movingAverage_y.addPoint(p.y);

		if (!movingAverage_x.isFull())
			continue;

		float r = calculateCorrelationCoefficient(points,
				movingAverage_x.getAverage(), movingAverage_y.getAverage());

		if (fabs(r) < CORR_COEFF)
		{
			return degree;
		}
	}

	return 0;
}

////////////////////////////////////////////////////////////
// dkGetKnee2
//
// Finds the second knee on a distribution.  This is where the heavy
// tail starts.
////////////////////////////////////////////////////////////

int dkGetKnee2(const NKMap& nkmap) {
	int noSpikeCount = 0;
	int possibleKnee = 0;
	int first = 1;

	NKMap::const_reverse_iterator i;

	if (nkmap.begin() == nkmap.end())
		return 0;

	for (i = nkmap.rbegin(); i != nkmap.rend(); ++i) {
		int degree = i->first;
		int numEdges = i->second;

		if (first) {
			possibleKnee = degree;
			first = 0;
			continue;
		}

		if (numEdges > 2)
			return possibleKnee;

		// do we see a spike
		if (numEdges > 1) {
			noSpikeCount = 0;
		} else {
			// no spike ...
			noSpikeCount++;
			if (noSpikeCount >= 3)
				possibleKnee = degree;
		}
	}

	return 0;
}
