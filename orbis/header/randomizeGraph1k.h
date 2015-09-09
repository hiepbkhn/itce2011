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

#ifndef __DKRANDOMIZE_GRAPH_1K_H
#define __DKRANDOMIZE_GRAPH_1K_H

#include <math.h>
#include <assert.h>
#include <boost/graph/graph_concepts.hpp>
#include <vector>

#include "dkMetrics.h"
#include "dkUtils.h"

////////////////////////////////////////////////////////////
// randomizeGraph1k
////////////////////////////////////////////////////////////
template<class VertexListGraph>
int randomizeGraph1k(VertexListGraph& G, // input graph
		int rewireLimit, // total # of successes
		unsigned int timeLimit_sec, // total possible time
		int seed, // random seed
		bool verbose = false) // verbose to stdout
		{
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;
	// Concept checking methods we use below
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	typedef std::vector<edge_descriptor> EdgeList;

	// For performance ... we know the randomizeGraph2k does NOT
	// create or destroy edges, it just re-wires so we can save on
	// some num_edges function calls
	int numEdges = num_edges(G);

	// basic sanity checks
	assert(numEdges > 0);

	// seed random #
	srand(seed);

	// setup edge list.  Pulling out random edges is much easier and
	// faster than using boost's random_edge function

	EdgeList edgeList(numEdges);
	edge_iterator edgeIter, edgeIterEnd;
	int cnt = 0;
	for (tie(edgeIter, edgeIterEnd) = edges(G), cnt = 0;
			edgeIter != edgeIterEnd; ++edgeIter, ++cnt) {
		edgeList[cnt] = *edgeIter;
	}

	// Calling program should be responsible for settinng rewireLimit
	// and/or time limits ...
	// if (rewireLimit == 0)
	// rewireLimit = 10 * num_edges(G);

	int rewireCount = 0;

	// if they specified no time limit or rewire limit, return -1
	if ((rewireLimit == 0) && (timeLimit_sec == 0))
		return -1;

	struct timeval startTime;
	struct timeval nowTime;
	gettimeofday(&startTime, NULL);

	// If rewirelimit was not specified, we want to loop until the
	// time limit runs out.  Otherwise whichever happens first
	// (rewireLimit is hit or timeout occurs)
	while (!rewireLimit || (rewireCount < rewireLimit)) {
		// only run algorithm for a specified period of time if
		// it is specified
		if (timeLimit_sec) {
			gettimeofday(&nowTime, NULL);
			unsigned int timeDiff = (nowTime.tv_sec - startTime.tv_sec)
					+ (nowTime.tv_usec - startTime.tv_usec) / 1000000;

			if (timeDiff > timeLimit_sec)
				break;
		}

		unsigned int ind1 = rand() % numEdges;
		assert(ind1 < edgeList.size());
		unsigned int ind2 = rand() % numEdges;
		assert(ind2 < edgeList.size());

		if (ind1 == ind2)
			continue;

		vertex_descriptor v1 = source(edgeList[ind1], G);
		vertex_descriptor v2 = target(edgeList[ind1], G);

		vertex_descriptor v3 = source(edgeList[ind2], G);
		vertex_descriptor v4 = target(edgeList[ind2], G);

		// make sure the nodes are distinct......
		if ((v3 == v1) || (v3 == v2))
			continue;

		if ((v4 == v1) || (v4 == v2))
			continue;

		// we will be connecting v1 to v4 ...... make sure this edge
		// doesn't already exist
		if (edge(v1, v4, G).second == true)
			continue;

		// we will be connecting v2 to v3 ...... make sure this edge
		// doesn't already exist
		if (edge(v2, v3, G).second == true)
			continue;

		// Swap the edges
		remove_edge(v1, v2, G);
		remove_edge(v3, v4, G);

		edge_descriptor e1, e2;
		bool addResult;

		// If the previous checks were correct, addResult should
		// always be true, meaning we didnt try to add an edge that
		// alreayd existed
		tie(e1, addResult) = add_edge(v1, v4, G);
		assert(addResult);

		tie(e2, addResult) = add_edge(v2, v3, G);
		assert(addResult);

		edgeList[ind1] = e1;
		edgeList[ind2] = e2;

		rewireCount++;
		if ((rewireCount % 100000 == 0) && verbose) {
			std::cerr << "Completed " << rewireCount << " rewirings"
					<< std::endl;
		}
	} // while

	return rewireCount;
} // randomizeGraph1k

////////////////////////////////////////////////////////////
// randomizeGraph1k
//
// Same thing as above only with a target-r.  I've been meaning to
// change interface to make this process more clean
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int targetRandomizeGraph1k(VertexListGraph& G, // input graph
		int rewireLimit, // total # of successes
		unsigned int timeLimit_sec, // total possible time
		int seed, // random seed
		float r, bool verbose = false) // verbose to stdout
		{
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;
	// Concept checking methods we use below
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	typedef std::vector<edge_descriptor> EdgeList;

	// For performance ... we know the randomizeGraph2k does NOT
	// create or destroy edges, it just re-wires so we can save on
	// some num_edges function calls
	int numEdges = num_edges(G);

	// basic sanity checks
	assert(numEdges > 0);

	// seed random #
	srand(seed);

	// setup edge list.  Pulling out random edges is much easier and
	// faster than using boost's random_edge function

	EdgeList edgeList(numEdges);
	edge_iterator edgeIter, edgeIterEnd;
	int cnt = 0;
	for (tie(edgeIter, edgeIterEnd) = edges(G), cnt = 0;
			edgeIter != edgeIterEnd; ++edgeIter, ++cnt) {
		edgeList[cnt] = *edgeIter;
	}

	// Calling program should be responsible for settinng rewireLimit
	// and/or time limits ...
	// if (rewireLimit == 0)
	// rewireLimit = 10 * num_edges(G);

	int rewireCount = 0;

	// if they specified no time limit or rewire limit, return -1
	if ((rewireLimit == 0) && (timeLimit_sec == 0))
		return -1;

	struct timeval startTime;
	struct timeval nowTime;
	gettimeofday(&startTime, NULL);

	// If rewirelimit was not specified, we want to loop until the
	// time limit runs out.  Otherwise whichever happens first
	// (rewireLimit is hit or timeout occurs)
	while (!rewireLimit || (rewireCount < rewireLimit)) {
		// only run algorithm for a specified period of time if
		// it is specified
		if (timeLimit_sec) {
			gettimeofday(&nowTime, NULL);
			unsigned int timeDiff = (nowTime.tv_sec - startTime.tv_sec)
					+ (nowTime.tv_usec - startTime.tv_usec) / 1000000;

			if (timeDiff > timeLimit_sec)
				break;
		}

		unsigned int ind1 = rand() % numEdges;
		assert(ind1 < edgeList.size());
		unsigned int ind2 = rand() % numEdges;
		assert(ind2 < edgeList.size());

		vertex_descriptor v1 = source(edgeList[ind1], G);
		vertex_descriptor v2 = target(edgeList[ind1], G);

		vertex_descriptor v3 = source(edgeList[ind2], G);
		vertex_descriptor v4 = target(edgeList[ind2], G);

		// make sure the nodes are distinct......
		if ((v3 == v1) || (v3 == v2))
			continue;

		if ((v4 == v1) || (v4 == v2))
			continue;

		// we will be connecting v1 to v4 ...... make sure this edge
		// doesn't already exist
		if (edge(v1, v4, G).second == true)
			continue;

		// we will be connecting v2 to v3 ...... make sure this edge
		// doesn't already exist
		if (edge(v2, v3, G).second == true)
			continue;

		float rDiff_old = fabs(find_r(G) - r);

		// Swap the edges
		remove_edge(v1, v2, G);
		remove_edge(v3, v4, G);

		edge_descriptor e1, e2;
		bool addResult;

		// If the previous checks were correct, addResult should
		// always be true, meaning we didnt try to add an edge that
		// alreayd existed
		tie(e1, addResult) = add_edge(v1, v4, G);
		assert(addResult);

		tie(e2, addResult) = add_edge(v2, v3, G);
		assert(addResult);

		edgeList[ind1] = e1;
		edgeList[ind2] = e2;

		float rDiff_new = fabs(find_r(G) - r);

		// If the new connection was bad, undo it.  We are trying to
		// get closer to r
		if (rDiff_new > rDiff_old) {
			remove_edge(v1, v4, G);
			remove_edge(v2, v3, G);
			e1 = add_edge(v1, v2, G).first;
			e2 = add_edge(v3, v4, G).first;
			edgeList[ind1] = e1;
			edgeList[ind2] = e2;
		}

		rewireCount++;

		if ((rewireCount % 100000 == 0) && verbose) {
			std::cout << "Completed " << rewireCount << " rewirings"
					<< std::endl;
		}
	} // while

	return rewireCount;
} // randomizeGraph1k

#endif
