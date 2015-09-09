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

#ifndef __DKRANDOMIZE_GRAPH_2K_H
#define __DKRANDOMIZE_GRAPH_2K_H

#include <assert.h>
#include <boost/graph/graph_concepts.hpp>
#include <vector>

////////////////////////////////////////////////////////////
// randomizeGraph2k and various helpers.  
//
// Given an input graph randomly requires maintaining the 2k
// distributions
////////////////////////////////////////////////////////////

// helper functions for randomizeGraph2k
// Swaps the edge between (v1,v2) and (v2,v3) assuming there IS one
// returns 0 if no edge was swapped
// Returns 1 if it created an edge between (v1, v4) and (v2, v3)
// returns 2 if it created an edge between (v1, v3) and (v2, v4)
namespace randomGraph2k {
using namespace boost;
template<class VertexListGraph>
inline int swapEdge2k(
		typename graph_traits<VertexListGraph>::vertex_descriptor v1,
		typename graph_traits<VertexListGraph>::vertex_descriptor v2,
		typename graph_traits<VertexListGraph>::vertex_descriptor v3,
		typename graph_traits<VertexListGraph>::vertex_descriptor v4,
		VertexListGraph& G) {
	typedef graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;

	// Double check edges exist between the (v1,v2) and (v3,v4)
	// assert(edge(v1, v2, G).second == true);
// 	assert(edge(v3, v4, G).second == true);
	if (edge(v1, v2, G).second != true)
		return 0;
	if (edge(v3, v4, G).second != true)
		return 0;

	degree_size_type k1 = out_degree(v1, G);
	degree_size_type k2 = out_degree(v2, G);
	degree_size_type k3 = out_degree(v3, G);
	degree_size_type k4 = out_degree(v4, G);

	// #make sure the nodes are distinct......
	if ((v3 == v1) || (v3 == v2))
		return 0;

	if ((v4 == v1) || (v4 == v2))
		return 0;

	if ((k3 != k1) && (k3 != k2) && (k4 != k1) && (k4 != k2))
		return 0;

	if ((k3 == k1) || (k4 == k2)) {

		// # we will be connecting v2 to v3...... make sure it
		// # doesn't already exist
		if (edge(v2, v3, G).second == true)
			return 0;

		// # we will be connecting v1 to v4...... make sure it
		// # doesn't already exist
		if (edge(v1, v4, G).second == true)
			return 0;

		remove_edge(v1, v2, G);
		remove_edge(v3, v4, G);

		edge_descriptor e1, e2;
		bool addResult;

		// If our above checks were correct, this should never
		// assert.  Asserting here means we are trying to add an
		// edge that already exists
		tie(e1, addResult) = add_edge(v1, v4, G);
		assert(addResult);

		tie(e2, addResult) = add_edge(v2, v3, G);
		assert(addResult);

		return 1;

	} // if ((k3 == k1) || (k4 == k2)) 
	else if ((k3 == k2) || (k4 == k1)) {

		// # we will be connecting v1 to v3...... make sure it
		// # doesn't already exist
		if (edge(v1, v3, G).second == true)
			return 0;
		//     # we will be connecting v2 to v4...... make sure it
		//     # doesn't already exist
		if (edge(v2, v4, G).second == true)
			return 0;

		remove_edge(v1, v2, G);
		remove_edge(v3, v4, G);

		edge_descriptor e1, e2;
		bool addResult;

		// If our above checks were correct, this should never
		// assert.  Asserting here means we are trying to add an
		// edge that already exists
		tie(e1, addResult) = add_edge(v1, v3, G);
		assert(addResult);

		tie(e2, addResult) = add_edge(v2, v4, G);
		assert(addResult);

// 	    edgeList[ind1] = e1;
// 	    edgeList[ind2] = e2;

		return 2;

	} //else if ((k3 == k2) || (k4 == k1)) 
	else {
		return 0;
	}

	return 0;
}
} // namespace randomGraph2k

////////////////////////////////////////////////////////////
// randomRewireVertex2k
//
// Given a graph and a single vertex try to randomly rewire that
// vertex.  Used in dk-topo generation if we want to re-wire a
// specific vertex
//
// This is experimental and currently not used by Orbis  
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int randomRewireVertex2k(
		VertexListGraph& G,
		typename boost::graph_traits<VertexListGraph>::vertex_descriptor v,
		std::list<typename boost::graph_traits<VertexListGraph>::edge_descriptor>& candidateEdgeList) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::out_edge_iterator out_edge_iterator;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	// Try all of vs outgoing edges to rewire him
	std::list<edge_descriptor> edgeList;
	out_edge_iterator edgeIter, edgeIterEnd;
	for (tie(edgeIter, edgeIterEnd) = out_edges(v, G); edgeIter != edgeIterEnd;
			++edgeIter) {
		edgeList.push_back(*edgeIter);
	}

	dkShuffleList(edgeList);

//     std::cerr  << "HERE I IS " << std::endl;

	while (!edgeList.empty()) {
		edge_descriptor e = edgeList.front();
		edgeList.pop_front();

		int result = randomRewireEdge2k(G, e, candidateEdgeList);
		if (result)
			return result;
	}

	return 0;
}

////////////////////////////////////////////////////////////
// randomRewireEdge2k
//
// Given a graph and a single edge try to randomly rewire that edge
// maintaining the 2k distribution.  Used in dk-topo generation if we
// want to rewire a specific edge
//
// This is experimental and currently not used by Orbis  
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int randomRewireEdge2k(
		VertexListGraph& G,
		typename boost::graph_traits<VertexListGraph>::edge_descriptor e,
		std::list<typename boost::graph_traits<VertexListGraph>::edge_descriptor>& edgeList) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	//std::list<edge_descriptor> edgeList;

	// Create a list of all edges excluding the one specified

	if (edgeList.empty()) {
		edge_iterator edgeIter, edgeIterEnd;
		for (tie(edgeIter, edgeIterEnd) = edges(G); edgeIter != edgeIterEnd;
				++edgeIter) {
			if (e != *edgeIter)
				edgeList.push_back(*edgeIter);
		}
	}

	dkShuffleList(edgeList);

	vertex_descriptor v1, v2;
	v1 = source(e, G);
	v2 = target(e, G);
	// Go through edgelist until we re-wire successfully
	for (typename std::list<edge_descriptor>::iterator i = edgeList.begin();
			i != edgeList.end(); ++i) {
		vertex_descriptor v3, v4;
		v3 = source(*i, G);
		v4 = target(*i, G);

//	std::cerr << "Trying: " << v1 << " " << v2 << " " << v3
//		  << " " << v4 << std::endl;
		int result = randomGraph2k::swapEdge2k(v1, v2, v3, v4, G);
		if (result) {
//	    std::cerr << "WE DID A FREAKIN SWAP!!" << std::endl;
			return result;
		}
	}

	return 0;
}

////////////////////////////////////////////////////////////
// randomizeGraph2k
//
// Randomly shuffles a graph for some period of time or specified
// steps
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int randomizeGraph2k(VertexListGraph& G, int rewireSuccessLimit,
		unsigned int timeLimit_sec, int seed, bool verbose = false) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	// Concept checking
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	// For performance ... we know the randomizeGraph2k does NOT
	// create or destroy edges, it just re-wires so we can save on
	// some num_edges function calls
	int numEdges = num_edges(G);

	// basic sanity checks
	// XXX decide on a good value for this
	assert(numEdges > 0);

	// Priya wants this as a hardcoded value
	// XXX for now dont want this to be a factor ... b/c I'm doing
	// some stress tests ... can still limit run time using others
	const int rewireAttemptLimit = numEdges * 10000;

	// seed random #
	srand(seed);

	// NO default limits, let calling program handle this ... if
	// calling program specifies no limits, then give an error

	if ((rewireAttemptLimit == 0) && (rewireSuccessLimit == 0)
			&& (timeLimit_sec == 0)) {
		return -1;
	}

	// Every random_edge call runs in O(num_edges), make a helpful
	// data structure.  Also, utilize the fact that the # of edges
	// in our graph stays constant ( all we do is rewire ).

	// Because random_edge is too slow, just keep an edge list as a
	// pair of vectors.  This will work no matter what the underlying
	// representation of the edge list is

	typedef std::pair<vertex_descriptor, vertex_descriptor> VertexPair;
	typedef std::vector<VertexPair> EdgeList;
	EdgeList edgeList(numEdges);
	edge_iterator edgeIter, edgeIterEnd;
	int cnt = 0;
	for (tie(edgeIter, edgeIterEnd) = edges(G), cnt = 0;
			edgeIter != edgeIterEnd; ++edgeIter, ++cnt) {
		vertex_descriptor v1, v2;
		v1 = source(*edgeIter, G);
		v2 = target(*edgeIter, G);
		edgeList[cnt] = VertexPair(v1, v2);
	}

	int rewireAttemptCount = 0, rewireSuccessCount = 0;

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

			if (timeDiff > timeLimit_sec) {
				break;
			}
		}

		unsigned int ind1 = rand() % numEdges;
		assert(ind1 < edgeList.size());
		// should never happen
		int result = 0;
		int tmp_cnt = 1;

		while ((result == 0) && (tmp_cnt < 20)) {
			unsigned int ind2 = rand() % numEdges;
			assert(ind2 < edgeList.size());
			// should never happen

			if (ind1 == ind2)
				continue;

			// Swap the edges we stored at ind1 and ind2.  swapEdge2k
			// will figure out if this is a reasonable swap.

			result = randomGraph2k::swapEdge2k(edgeList[ind1].first,
					edgeList[ind1].second, edgeList[ind2].first,
					edgeList[ind2].second, G);

			// Update our local edgelist with results from the swap
			if (result == 1) {
				vertex_descriptor v1, v2, v3, v4;
				v1 = edgeList[ind1].first;
				v2 = edgeList[ind1].second;

				v3 = edgeList[ind2].first;
				v4 = edgeList[ind2].second;

				edgeList[ind1] = VertexPair(v1, v4);
				edgeList[ind2] = VertexPair(v2, v3);
			} else if (result == 2) {
				vertex_descriptor v1, v2, v3, v4;
				v1 = edgeList[ind1].first;
				v2 = edgeList[ind1].second;

				v3 = edgeList[ind2].first;
				v4 = edgeList[ind2].second;

				edgeList[ind1] = VertexPair(v1, v3);
				edgeList[ind2] = VertexPair(v2, v4);
			}

			tmp_cnt++;
		}

		rewireAttemptCount++;
		if (result)
			rewireSuccessCount++;

		if ((rewireAttemptCount % 100000 == 0) && verbose) {
			std::cout << "Attempted_Rewires:" << rewireAttemptCount
					<< " Successful_Rewires:" << rewireSuccessCount
					<< std::endl;
		}

	} // while(rewireCount < rewireLimit)
	return rewireSuccessCount;
}

#endif
