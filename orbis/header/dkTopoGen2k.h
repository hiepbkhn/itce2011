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

#ifndef __DK_TOPO_GEN_2K_H
#define __DK_TOPO_GEN_2K_H

#include <boost/graph/graph_concepts.hpp>
#include <map>
#include "dkUtils.h"

////////////////////////////////////////////////////////////
// get2kRandomDegree
//
// Helper function used by dkTopoGen2k.  Given a 2k distribution and a
// degree, randomly picks another degree weighted by the 2k
// distribution.  
////////////////////////////////////////////////////////////

int get2kRandomDegree(int degree1, const NKKMap& nkkmap) {
	int totalEdgeCount = 0;
	assert(nkkmap.find(degree1) != nkkmap.end());
	const NKMap& nkmap = nkkmap.find(degree1)->second;

	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		totalEdgeCount += i->second;

// 	if (i->first == degree)
// 	    totalEdgeCount += i->second;
	}

	if (totalEdgeCount <= 0)
		return -1;

	// Sum represents the sum of all edges that degree connects to.
	// Now just pick an edge from 1 ... totalEdgeCount

	int edgeChoice = rand() % totalEdgeCount;

	// OK find the degree that this choice belongs to

	int runningCount = 0;
	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		int degree = i->first;
		int numEdges = i->second;

		if (numEdges == 0)
			continue;

		runningCount += numEdges;

		if (runningCount > edgeChoice) {

			return degree;
		}

	}

	std::cerr << "RunningCount:" << runningCount << " edgeChoice:" << edgeChoice
			<< " totalEdgeCount:" << totalEdgeCount << std::endl;
	assert(0);
	return -1;
}

// maps degree -> list of stubs IDs
typedef std::map<int, std::list<int> > DegreeStubListMap;

int notAvailFirstCount = 0;
int notAvailCount = 0;
int sameIdCount = 0;
int sameNodeIdCount = 0;
int alreadyConnectedCount = 0;

template<class VertexListGraph>
void dkTopoGen2k_connectStubs(VertexListGraph& g, std::list<int>& randomStubIds,
		DegreeStubListMap &degreeStubListMap, const std::vector<Stub>& stubs,
		AdjacencyMap& adjacencyMap, std::vector<int>& available,
		NKKMap& workingMap) {

	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::AdjacencyMatrixConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	int edgeCounter = 0;
	// alright go through stub Id list and try to connect Ids!
	while (!randomStubIds.empty()) {

		// Select a random stub
		int stubId = randomStubIds.front();
		randomStubIds.pop_front();

		// First check - is this stub available?!  It might have been
		// chosen for an edge in a previous iteration
		if (!available[stubId]) {
			notAvailFirstCount++;
			continue;
		}

		Stub stub = stubs[stubId];

		// Now find the degree of the node to connect to based on
		// the 2k distribution
		int targetDegree = get2kRandomDegree(stub.degree, workingMap);
		if (targetDegree <= 0) {
			continue;
		}

		// Find a random node of degree "targetDegree"
		std::list<int>& targetList = degreeStubListMap[targetDegree];
		std::list<int>::iterator i;

		for (i = targetList.begin(); i != targetList.end(); ++i) {

			// is it available?
			int targetId = *i;

			const Stub& potentialStub = stubs[*i];

			// Stub is not available
			if (!available[targetId]) {
				notAvailCount++;
				continue;
			}

			// don't want to connect 2 stubs of the same node
			if (stub.nodeid == potentialStub.nodeid) {
				sameNodeIdCount++;
				continue;
			}

			// Make sure the nodes aren't already connected
			if ((adjacencyMap.find(
					NodeIdPair(stub.nodeid, potentialStub.nodeid))
					!= adjacencyMap.end())
					|| (adjacencyMap.find(
							NodeIdPair(potentialStub.nodeid, stub.nodeid))
							!= adjacencyMap.end())) {
				alreadyConnectedCount++;
				continue;
			}

			add_edge(stub.nodeid, potentialStub.nodeid, g);
			adjacencyMap[NodeIdPair(stub.nodeid, potentialStub.nodeid)] = 1;
			adjacencyMap[NodeIdPair(potentialStub.nodeid, stub.nodeid)] = 1;
			edgeCounter++;

			assert(available[stubId]);
			assert(available[targetId]);

			// Make stubs unavailable
			available[stubId] = 0;
			available[targetId] = 0;

			// update the working 2k distribution
			if (workingMap[stub.degree][potentialStub.degree])
				workingMap[stub.degree][potentialStub.degree]--;
// 	    else
// 		assert(0);

			if (workingMap[potentialStub.degree][stub.degree])
				workingMap[potentialStub.degree][stub.degree]--;

			break;
		}

	} // while(!randomStubs.empty())

}

////////////////////////////////////////////////////////////
// dkTopoGen2k (take 2)
//
// Second implementation of dkTopoGen2k based on randomly choosing and
// connecting stubs rather than choosing edges.  This represents what
// Priya originally had. I have another approach below that randomly
// chooses edges first.  That implementation seems buggy since it
// cannot match the clustering from before.
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int dkTopoGen2k(VertexListGraph& g, const NKKMap &nkkMap) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	AdjacencyMap adjacencyMap;
	std::vector<Stub> stubs; // list of all the possible
	// stubs, indexed by stubId

	DegreeStubListMap degreeStubListMap; // for every degree maintain
	// a list of stubs for that
	// degree

	NKMap nkMap;
	for (NKKMap::const_iterator i = nkkMap.begin(); i != nkkMap.end(); ++i) {
		int degree1 = i->first;
		int tmpdd = 0;
		for (NKMap::const_iterator j = i->second.begin(); j != i->second.end();
				++j) {
// 	    int degree2 = j->first;
			int numEdges = j->second;

			// XXX calvin trying this
// 	    if (degree1 == degree2)
// 		numEdges += numEdges;

			tmpdd += numEdges;
		}
		nkMap[degree1] = int((float(tmpdd) / degree1) + 0.5);
	}

	int nodeId = 0;
	int stubId = 0;

	// First determine how many nodes of each degree we need.  To do
	// this convert our 2k distribution to a 1k distribution

	for (NKMap::const_iterator i = nkMap.begin(); i != nkMap.end(); ++i) {
		int degree = i->first;
		int numNodes = i->second;

		// Create numNodes nodes
		for (int n = 0; n < numNodes; n++) {
			// Each node has "degree" stubs
			for (int d = 0; d < degree; d++) {
				Stub stub;
				stub.degree = degree;
				stub.nodeid = nodeId;
				stubs.push_back(stub);

				degreeStubListMap[degree].push_back(stubId);
				stubId++;
			}
			nodeId++;
		}

		dkShuffleList(degreeStubListMap[degree]);

	}

	// Next create our working data structures.  The available map is
	// a bitmap that denotes whether a stub is used or not. The
	// workingMap is a workign 2k distribution that we modify in order
	// to create the topology

	std::vector<int> available(stubs.size(), 1);
	NKKMap workingMap;

	for (NKKMap::const_iterator i = nkkMap.begin(); i != nkkMap.end(); ++i) {
		int degree1 = i->first;
		const NKMap& inner = i->second;

		for (NKMap::const_iterator j = inner.begin(); j != inner.end(); ++j) {
			int degree2 = j->first;
			int numEdges = j->second;

			// XXX i think priya does this ....
			if (degree1 == degree2)
				workingMap[degree1][degree2] = 2 * numEdges;
			else
				workingMap[degree1][degree2] = numEdges;
		}
	}

	// Get a random list of stub Ids
	std::list<int> randomStubIds;
	for (unsigned int i = 0; i < stubs.size(); ++i) {
		randomStubIds.push_back(i);
	}

	dkShuffleList(randomStubIds);

	for (int FOO = 0; FOO < 1; FOO++) {
		dkTopoGen2k_connectStubs(g, randomStubIds, degreeStubListMap, stubs,
				adjacencyMap, available, workingMap);

		// Debug - dkTopoGen2k_connectStubs is supposed to consume all
		// of the stubs
		assert(randomStubIds.empty());

		// keep track of all the stubs we couldnt connect
		for (unsigned int i = 0; i < available.size(); ++i) {
			if (available[i])
				randomStubIds.push_back(i);
		}
// 	dkShuffleList(randomStubIds);

	}

	// WIth all the remaining stubs, just connect them with 1k
	StubList freeStubList;
	for (std::list<int>::iterator i = randomStubIds.begin();
			i != randomStubIds.end(); ++i) {
		freeStubList.push_back(stubs[*i]);
	}

// //     Just do a 1k now
//     dkTopoGen1k_stublist(g, freeStubList, adjacencyMap);

	return 0;
}

#endif
