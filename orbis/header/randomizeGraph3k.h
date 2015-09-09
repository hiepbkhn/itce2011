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

#ifndef __DKRANDOMIZE_GRAPH_3K_H
#define __DKRANDOMIZE_GRAPH_3K_H

////////////////////////////////////////////////////////////
// randomizeGraph3k
//
// Creates 3k-random rewirings on a given graph.  Helper functions are
// in namespace randomGraph3k
////////////////////////////////////////////////////////////

// namespace for helper methods
namespace randomGraph3k {
using namespace boost;

template<class VertexListGraph>
inline void revertToPreReWired(
		typename graph_traits<VertexListGraph>::vertex_descriptor v1,
		typename graph_traits<VertexListGraph>::vertex_descriptor v2,
		typename graph_traits<VertexListGraph>::vertex_descriptor v3,
		typename graph_traits<VertexListGraph>::vertex_descriptor v4,
		int ind1,
		int ind2,
		std::vector<typename graph_traits<VertexListGraph>::edge_descriptor> &edgeList,
		VertexListGraph& G) {
	typedef graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;

	remove_edge(v1, v4, G);
	remove_edge(v2, v3, G);

	edge_descriptor e1 = add_edge(v1, v2, G).first;
	edge_descriptor e2 = add_edge(v3, v4, G).first;

	edgeList[ind1] = e1;
	edgeList[ind2] = e2;
}

// helper function!
template<class VertexListGraph>
inline int swapEdge3k(
		unsigned int ind1, // index into edgeList
		unsigned int ind2, // index into edgeList
		std::vector<typename graph_traits<VertexListGraph>::edge_descriptor> &edgeList,
		typename graph_traits<VertexListGraph>::degree_size_type maxDegree,
		std::map<typename graph_traits<VertexListGraph>::degree_size_type, int> &nodeDegreeDistribution
		,
		VertexListGraph& G) {
	typedef graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::adjacency_iterator adjacency_iterator;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	typedef std::map<degree_size_type, int> NodeDegreeDistributionMap;

	// function_requires?!

	vertex_descriptor v1, v2, v3, v4;

	v1 = source(edgeList[ind1], G);
	v2 = target(edgeList[ind1], G);
	v3 = source(edgeList[ind2], G);
	v4 = target(edgeList[ind2], G);

	degree_size_type k1 = out_degree(v1, G);
	degree_size_type k2 = out_degree(v2, G);
	degree_size_type k3 = out_degree(v3, G);
	degree_size_type k4 = out_degree(v4, G);

	//     #make sure the nodes are distinct......
	if ((v3 == v1) || (v3 == v2))
		return 0;

	if ((v4 == v1) || (v4 == v2))
		return 0;

	//   #they have to have atleast 1 degree in common.....
	if ((k3 != k1) && (k3 != k2) && (k4 != k1) && (k4 != k2))
		return 0;
	//    #    "No common degree.... move on" 

	//  # ignore obvious isomorphic cases........
	if ((k3 == 1) || (k4 == 1))
		return 0;

	if ((k1 == 1) || (k2 == 1))
		return 0;

	//  #we want to swap v1 with v4 and v2 with v3... Therefore, k3
	//  #should be == to k1 or k4 == k2....
	//# what if its the other way around?
	// # print "Before swapping, vertices are $v1 $v2 $v3 $v4\n";
	//# print "degrees are $k1 $k2 $k3 $k4 \n";

	if ((k3 == k2) || (k4 == k1)) {
		vertex_descriptor t = v3;
		v3 = v4;
		v4 = t;

		k3 = out_degree(v3, G);
		k4 = out_degree(v4, G);
	}

	//  #now, this if st. has to be true....
	if ((k3 == k1) || (k4 == k2)) {
		if (edge(v2, v3, G).second == true)
			return 0;

		if (edge(v1, v4, G).second == true)
			return 0;
	} else {
		assert(0 == 1); // should never reach this
	}

	//      # can be potentially swapped......
	//  # check if 3k property i.e triangle & wedge distribution will be maintained .

	//   # first compute all X, Y & Z pairs for edge e....   X & Y is for wedges on either side 
	//   # and Z is for all the triangles.....

	typedef std::vector<int> DegreeVector;
	DegreeVector X_degrees(maxDegree + 1, 0);
	DegreeVector Y_degrees(maxDegree + 1, 0);
	DegreeVector Z_degrees(maxDegree + 1, 0);

	DegreeVector fX_degrees(maxDegree + 1, 0);
	DegreeVector fY_degrees(maxDegree + 1, 0);
	DegreeVector fZ_degrees(maxDegree + 1, 0);

	DegreeVector RX_degrees(maxDegree + 1, 0);
	DegreeVector RY_degrees(maxDegree + 1, 0);
	DegreeVector RZ_degrees(maxDegree + 1, 0);

	DegreeVector RfX_degrees(maxDegree + 1, 0);
	DegreeVector RfY_degrees(maxDegree + 1, 0);
	DegreeVector RfZ_degrees(maxDegree + 1, 0);

	adjacency_iterator i1, i2;

	for (tie(i1, i2) = adjacent_vertices(v1, G); i1 != i2; ++i1) {
		if (*i1 == v2)
			continue;

		degree_size_type deg = out_degree(*i1, G);
		// #check if x,v2 is an edge.. If yes, then add it to Z
		// #list.. Else, only add it to X-list
		if (edge(v2, *i1, G).second == true)
			Z_degrees[deg] += 1;
		else
			X_degrees[deg] += 1;
	}

	for (tie(i1, i2) = adjacent_vertices(v2, G); i1 != i2; i1++) {
		if (*i1 == v1)
			continue;

		degree_size_type deg = out_degree(*i1, G);
		//    #check if x,v1 is an edge.. If yes, then its already in
		//    #the Z list.. Else, add it to Y-list
		if (edge(v1, *i1, G).second == false)
			Y_degrees[deg] += 1;
	}

	// #before rewiring, for vertices v3 and v4......

	for (tie(i1, i2) = adjacent_vertices(v3, G); i1 != i2; i1++) {
		if (*i1 == v4)
			continue;

		degree_size_type deg = out_degree(*i1, G);
		// #check if x,v4 is an edge.. If yes, then add it to fZ
		// #list.. Else, only add it to fX-list
		if (edge(v4, *i1, G).second == 1)
			fZ_degrees[deg] += 1;
		else
			fX_degrees[deg] += 1;
	}

	for (tie(i1, i2) = adjacent_vertices(v4, G); i1 != i2; i1++) {
		if (*i1 == v3)
			continue;
		degree_size_type deg = out_degree(*i1, G);

		if (edge(v3, *i1, G).second == false)
			fY_degrees[deg] += 1;
	}

	//#Rewire the edges.. Then check that the corresponding X, Y & Z
	//#distributions are miantained...If they are not maintained, go
	//#back to the prerewired state......

	remove_edge(v1, v2, G);
	remove_edge(v3, v4, G);
	bool notExisting;
	edge_descriptor new_v1_v4, new_v2_v3;

	tie(new_v1_v4, notExisting) = add_edge(v1, v4, G);
	assert(notExisting);
	tie(new_v2_v3, notExisting) = add_edge(v2, v3, G);
	assert(notExisting);

	// #update the edges in the list.... 
	// edgeMap[e1] = VertexPair(v1, v4);
	edgeList[ind1] = new_v1_v4;
	//edgeMap[e2] = VertexPair(v2, v3);
	edgeList[ind2] = new_v2_v3;

	for (tie(i1, i2) = adjacent_vertices(v1, G); i1 != i2; i1++) {
		if (*i1 == v4)
			continue;

		degree_size_type deg = out_degree(*i1, G);

		if (edge(v4, *i1, G).second == true)
			RZ_degrees[deg] += 1;
		else
			RX_degrees[deg] += 1;
	}

	for (tie(i1, i2) = adjacent_vertices(v4, G); i1 != i2; i1++) {
		if (*i1 == v1)
			continue;

		degree_size_type deg = out_degree(*i1, G);

		if (edge(v1, *i1, G).second == false)
			RfY_degrees[deg] += 1;
	}

	for (tie(i1, i2) = adjacent_vertices(v3, G); i1 != i2; i1++) {
		if (*i1 == v2)
			continue;

		degree_size_type deg = out_degree(*i1, G);

		if (edge(v2, *i1, G).second == true)
			RfZ_degrees[deg] += 1;
		else
			RfX_degrees[deg] += 1;
	}

	for (tie(i1, i2) = adjacent_vertices(v2, G); i1 != i2; i1++) {
		if (*i1 == v3)
			continue;

		degree_size_type deg = out_degree(*i1, G);

		if (edge(v3, *i1, G).second == false)
			RY_degrees[deg] += 1;
	}

	// #Now, compare the wedge and triangle distributions
	if (k1 == k3) {
		// #Compre Z with RfZ
		for (typename NodeDegreeDistributionMap::iterator i =
				nodeDegreeDistribution.begin();
				i != nodeDegreeDistribution.end(); ++i) {
			int key = i->first;
			// XXX below kept ugly just so i can compare side-by-side
			// with priya's original code

			if ((Z_degrees[key] != RfZ_degrees[key])
					|| (X_degrees[key] != RfX_degrees[key])
					|| (Y_degrees[key] != RY_degrees[key])
					|| (fY_degrees[key] != RfY_degrees[key])
					|| (fX_degrees[key] != RX_degrees[key])
					|| (fZ_degrees[key] != RZ_degrees[key])) {
				revertToPreReWired(v1, v2, v3, v4, ind1, ind2, edgeList, G);
				return 0;
			}

		} // foreach nodeDgreeDistribution

	} // if (k1 == k3)	
	else if (k2 == k4) {
		for (typename NodeDegreeDistributionMap::iterator i =
				nodeDegreeDistribution.begin();
				i != nodeDegreeDistribution.end(); i++) {
			int key = i->first;

			if ((Z_degrees[key] == RZ_degrees[key])
					|| (X_degrees[key] == RX_degrees[key])
					|| (Y_degrees[key] == RfY_degrees[key])
					|| (fY_degrees[key] == RY_degrees[key])
					|| (fX_degrees[key] == RfX_degrees[key])
					|| (fZ_degrees[key] == RfZ_degrees[key])) {
				revertToPreReWired(v1, v2, v3, v4, ind1, ind2, edgeList, G);
				return 0;
			}

		} // foreach nodedegreedistribution
	} //else if (k2 == k4)

	return 1;
} // swapEdge3k(

} // namespace randomGraph3k

template<class VertexListGraph>
int randomizeGraph3k(VertexListGraph& G, int rewireSuccessLimit,
		unsigned int timeLimit_sec, int seed, bool verbose = false) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::adjacency_iterator adjacency_iterator;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	typedef std::map<degree_size_type, int> NodeDegreeDistributionMap;

	// For performance ... we know the randomizeGraph2k does NOT
	// create or destroy edges, it just re-wires so we can save on
	// some num_edges function calls
	unsigned int numEdges = num_edges(G);

	// basic sanity checks
	assert(numEdges > 0);

	// Priya wants this hardcoded
	// XXX decide on a good value for this
	// XXX for now dont want this to be a factor ... b/c I'm doing
	// some stress tests ... can still limit run time using others
	const int rewireAttemptLimit = numEdges * 1000;

	// seed random #
	srand(seed);

	// Degree distribution map stuff
	degree_size_type maxDegree = 0;

	// edgeList stuff ... see my notes - pull out random edges in
	// ~constant time, also iterating through edges starting at
	// the random edge like Priya does in her code is much easier
	// Used to be a map, but we can just hold it in a vector since
	// we never delete or add edges (only re-wire)

	typedef std::vector<edge_descriptor> EdgeList;
	EdgeList edgeList(numEdges);
	edge_iterator edgeIter, edgeIterEnd;
	int cnt;
	for (tie(edgeIter, edgeIterEnd) = edges(G), cnt = 0;
			edgeIter != edgeIterEnd; ++edgeIter, ++cnt) {
		edgeList[cnt] = *edgeIter;
	}

	// no default limits for wirecount and time limits, let calling
	// program handle this.  If no limits are specified, give an error
	if ((rewireAttemptLimit == 0) && (rewireSuccessLimit == 0)
			&& (timeLimit_sec == 0)) {
		return -1;
	}

	int rewireAttemptCount = 0;
	int rewireSuccessCount = 0;

	// set up node degree distribution map
	NodeDegreeDistributionMap nodeDegreeDistribution;
	vertex_iterator i, end;
	for (boost::tie(i, end) = vertices(G); i != end; ++i) {
		degree_size_type deg = out_degree(*i, G);

		if (nodeDegreeDistribution.find(deg) == nodeDegreeDistribution.end()) {
			nodeDegreeDistribution[deg] = 1;
		} else {
			nodeDegreeDistribution[deg] += 1;
		}
		if (maxDegree < deg)
			maxDegree = deg;
	}

	// Well-formed graph
	assert(maxDegree > 0);

	struct timeval startTime;
	struct timeval nowTime;
	gettimeofday(&startTime, NULL);

	while (1) {

		// Check exist conditions only if they are specified
		// (i.e. non-0)
		if (rewireAttemptLimit && (rewireAttemptCount > rewireAttemptLimit)) {
			break;
		}
		if (rewireSuccessLimit && (rewireSuccessCount > rewireSuccessLimit)) {
			break;
		}

		// Check time limit only if one is specified
		if (timeLimit_sec) {
			gettimeofday(&nowTime, NULL);
			unsigned int timeDiff = (nowTime.tv_sec - startTime.tv_sec)
					+ (nowTime.tv_usec - startTime.tv_usec) / 1000000;

			if (timeDiff > timeLimit_sec)
				break;
		}

		// XXX  implicit cast from degree_size_type to int
		unsigned int ind1 = rand() % numEdges;
		assert(ind1 < edgeList.size());

		edge_descriptor edge = edgeList[ind1];
		// variable names to match up with original perl
		vertex_descriptor v1 = source(edge, G);
		vertex_descriptor v2 = target(edge, G);

		degree_size_type k1 = out_degree(v1, G);
		degree_size_type k2 = out_degree(v2, G);

		if ((k1 == 1) || (k2 == 1))
			continue;

		// # pick another edge... edges in the list are in no
		// particular order... so, go through each of them as most
		// edges cannot be rewired.
		int result = 0;
		for (unsigned int ind2 = ind1; ind2 < numEdges; ind2++) {
			vertex_descriptor v3 = source(edgeList[ind2], G);
			vertex_descriptor v4 = target(edgeList[ind2], G);

			// # get the corresponding degrees
			degree_size_type k3 = out_degree(v3, G);
			degree_size_type k4 = out_degree(v4, G);

			if ((k3 == 1) || (k4 == 1))
				continue;
			result = randomGraph3k::swapEdge3k(ind1, ind2, edgeList, maxDegree,
					nodeDegreeDistribution, G);

			if (result == 1)
				break;
		}

		rewireAttemptCount++;
		if (result == 1)
			rewireSuccessCount++;

		if ((rewireAttemptCount % 100 == 0) && verbose) {
			std::cout << "Attempted_Rewires:" << rewireAttemptCount
					<< " Successful_Rewires:" << rewireSuccessCount
					<< std::endl;
		}
	} // while(rewireCount < rewireLimit)

	return rewireSuccessCount;
} // randomizeGraph3k

#endif
