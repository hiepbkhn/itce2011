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

#ifndef __DK_TOPO_GEN_1K_H
#define __DK_TOPO_GEN_1K_H

#include <boost/graph/graph_concepts.hpp>
#include <list>
#include <vector>
#include "dkUtils.h"
#include "dkTopoGen2k.h"

////////////////////////////////////////////////////////////
// dkTopoGen1k
//
// generate a graph g from the 1k distribution NKMap 
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int dkTopoGen1k(VertexListGraph& g, const NKMap &nkmap) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	boost::function_requires<boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	// AdjacencyMap: Used to do quick lookups of edges existing in
	// constant time.  This can be be a slow operation depending on
	// what the underlying representation of the graph is.  This way
	// checking existing edges is independent of the graph
	// representation.  Typically edge is either O(E/V) or O(log(E/V))
	// depending on what choice of adjacency list is used.  Constant
	// time for an adjacency matrix

	AdjacencyMap adjacencyMap;

	// Update our adjacency map with the existing edges so far.  This
	// in case someone wants to re-wire the graph and continue
	// connecting stubs

	edge_iterator edgeIter, edgeIterEnd;
	for (tie(edgeIter, edgeIterEnd) = edges(g); edgeIter != edgeIterEnd;
			++edgeIter) {
		vertex_descriptor v1, v2;
		v1 = source(*edgeIter, g);
		v2 = target(*edgeIter, g);

		adjacencyMap[NodeIdPair(v1, v2)] = 1;
		adjacencyMap[NodeIdPair(v2, v1)] = 1;
	}

	// Create a random list of all the free stubs

	StubList freeStubList;
	vertex_descriptor nodeId = 0;

	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		int degree = i->first;
		int nodesWithDegree = i->second;
		for (int j = 0; j < nodesWithDegree; ++j) {
			for (int k = 0; k < degree; k++) {
				Stub stub;
				stub.nodeid = nodeId;
				stub.degree = degree;

				freeStubList.push_back(stub);
			}
			nodeId++;
		}
	}
	dkShuffleList(freeStubList);

	return dkTopoGen1k_stublist(g, freeStubList, adjacencyMap);
}

// hiepnh Oct 1, 2015
template<class VertexListGraph>
int dkTopoGen1kSequence(VertexListGraph& g, std::vector<int>& degSeq, int n_nodes) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	boost::function_requires<boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	// AdjacencyMap: Used to do quick lookups of edges existing in
	// constant time.  This can be be a slow operation depending on
	// what the underlying representation of the graph is.  This way
	// checking existing edges is independent of the graph
	// representation.  Typically edge is either O(E/V) or O(log(E/V))
	// depending on what choice of adjacency list is used.  Constant
	// time for an adjacency matrix

	AdjacencyMap adjacencyMap;

	// Update our adjacency map with the existing edges so far.  This
	// in case someone wants to re-wire the graph and continue
	// connecting stubs

	edge_iterator edgeIter, edgeIterEnd;
	for (tie(edgeIter, edgeIterEnd) = edges(g); edgeIter != edgeIterEnd;
			++edgeIter) {
		vertex_descriptor v1, v2;
		v1 = source(*edgeIter, g);
		v2 = target(*edgeIter, g);

		adjacencyMap[NodeIdPair(v1, v2)] = 1;
		adjacencyMap[NodeIdPair(v2, v1)] = 1;
	}

	// Create a random list of all the free stubs

	StubList freeStubList;
	vertex_descriptor nodeId = 0;

	for (int i = 0; i < n_nodes; i++) {
		int degree = degSeq[i];
		for (int k = 0; k < degree; k++) {
			Stub stub;
			stub.nodeid = i;
			stub.degree = degree;

			freeStubList.push_back(stub);
		}
	}
	dkShuffleList(freeStubList);
	std::cerr <<"freeStubList.size = " << freeStubList.size() << std::endl;

	return dkTopoGen1k_stublist(g, freeStubList, adjacencyMap);
}

template<class VertexListGraph>
int dkTopoGen1k_stublist(VertexListGraph& g, StubList& freeStubList,
		AdjacencyMap& adjacencyMap) {
	int needToRewire = 0;

	// Iterate through stubs (random order)

	typename StubList::iterator nextStubIter = freeStubList.begin();
	while (nextStubIter != freeStubList.end()) {
		typename StubList::iterator toDeleteIter = nextStubIter;
		const Stub &stub1 = *nextStubIter;

		nextStubIter++;

		typename StubList::iterator i;

		// Connect stub1 to a random element in the list
		for (i = nextStubIter; i != freeStubList.end(); ++i) {
			// Check that they aren't connected to the same node or
			// aren't already connected
			if (i->nodeid == stub1.nodeid)
				continue;

			if (adjacencyMap.find(NodeIdPair(i->nodeid, stub1.nodeid))
					!= adjacencyMap.end())
				continue;

			break;
		}

		// If we went through the entire free stub list without
		// finding a possible stub to connect to, just move on
		if (i == freeStubList.end()) {
			needToRewire++;
			continue;
		}

		const Stub& stub2 = *i;

		// at this point stub1 and stub2 are both references to stubs
		// which we want to connect.

		add_edge(stub1.nodeid, stub2.nodeid, g);
		adjacencyMap[NodeIdPair(stub1.nodeid, stub2.nodeid)] = 1;
		adjacencyMap[NodeIdPair(stub2.nodeid, stub1.nodeid)] = 1;

		// Safe to delete this iterator because no prevoius iterators
		// are pointing to it (we advanced nexstStubIter)
		freeStubList.erase(toDeleteIter);

		// next StubIter and i could potentially be pointing to the
		// same thing ... increment Stubiter if this is the case b/c
		// we are going to remove i from the free list
		if (nextStubIter == i)
			nextStubIter++;

		freeStubList.erase(i);
	}

	return needToRewire;
}

#endif
