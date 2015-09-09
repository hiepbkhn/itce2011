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

#ifndef __DK_UTILS_H
#define __DK_UTILS_H

#include <boost/graph/graph_concepts.hpp>
#include <boost/array.hpp>
#include <boost/property_map/property_map.hpp>
#include <iostream>
#include <fstream>
#include <list>
#include <map>
#include <vector>
#include <cstdlib>

// Various utilities used by all of the dk code

////////////////////////////////////////////////////////////
// Stubs 
////////////////////////////////////////////////////////////

struct Stub {
	int nodeid; // id of node to which it is attached...
	int degree; // degree of the node stub is connected to
};

typedef std::list<Stub> StubList;

struct EdgeStub2k {
	int degree1;
	int degree2;
};

////////////////////////////////////////////////////////////
// Various other types used
////////////////////////////////////////////////////////////

class Point {
public:
	float x, y;
	Point() :
			x(-1), y(-1) {
	}
	Point(float x1, float y1) :
			x(x1), y(y1) {
	}
};

typedef std::pair<int, int> NodeIdPair;
typedef std::map<NodeIdPair, int> AdjacencyMap;

////////////////////////////////////////////////////////////
// NK/PK maps
//  NK map refers to a 1k distribution of node counts
//      nkmap[i] = n  ==>  there are n nodes of degree i
//  PK map refers to a 1k distribution of probabilities
//      pkmap[i] = p  ==>  p% of nodes have degree i
//  NKK map refers to a 2k distribution of edge counts
//      nkkmap[i][j] = k ==> k edges between nodes of
//      degree i and degree j        
//  PKK map refers to a 1k distribution of probabilities
//      pkkmap[i][j] = p  ==> pr. of an edge existing between
//      node of degree i and degree j
//
//  Definitions and various methods for converting between.
////////////////////////////////////////////////////////////

typedef std::map<int, int> NKMap;
std::ostream& operator<<(std::ostream& out, const NKMap& m);

typedef std::map<int, NKMap> NKKMap;
std::ostream& operator<<(std::ostream& out, const NKKMap& m);

typedef std::map<int, float> PKMap;
std::ostream& operator<<(std::ostream& out, const PKMap& m);

typedef std::map<int, PKMap> PKKMap;
std::ostream& operator<<(std::ostream& out, const PKKMap& m);

void NKToPK(const NKMap& nkmap, PKMap& pkmap, int numNodes = 0);
void NKKToPKK(const NKKMap& nkmap, PKKMap& pkkmap);
void NKKToNK(const NKKMap& nkkmap, NKMap& nkmap);
void PKKToPK(const PKKMap& pkkmap, PKMap& pkmap, float averageDegree);
void PKKToNKK(const PKKMap& pkkmap, NKKMap& nkkmap, int numEdges);

unsigned int dkCalcNumNodes(const NKMap& nkmap);
unsigned int dkkCalcNumEdges(const NKKMap& nkkmap);
float dkCalcAverageDegree(const NKMap& nkmap);

////////////////////////////////////////////////////////////
// dkShuffleList
//
// Takes a list and shuffles it randomly.  Runs in O(n) (although wiht
// a decent constant) since it first copies the list into an array,
// shuffles the array in O(n) and copies the list back.  Requires O(n)
// copies of object of type T
//
// must be defined here b/c of the template
////////////////////////////////////////////////////////////

template<class T>
void dkShuffleList(std::list<T> &theList) {
	// O(n)
	const int listSize = theList.size();
	std::vector<T> theVector(listSize);

	// copy data into a vector O(n)
	int cnt = 0;
	for (typename std::list<T>::iterator i = theList.begin();
			i != theList.end(); ++i) {
		theVector[cnt] = *i;
		cnt++;
	}

	// shuffle the vector - O(n)
	for (int i = 0; i < listSize; ++i) {
		int randIdx = rand() % listSize;
		T tmp = theVector[randIdx];
		theVector[randIdx] = theVector[i];
		theVector[i] = tmp;
	}

	// copy back into list - O(n)
	theList.clear();
	for (int i = 0; i < listSize; ++i) {
		theList.push_back(theVector[i]);
	}

}

////////////////////////////////////////////////////////////
// read2kDistribution,read1kDistribution
//
// Various I/O routines for reading distributions
// readInputGraph defined here (not in .cc) b/c of template
// printGraph ditto
////////////////////////////////////////////////////////////

bool read2kDistribution(const std::string& filename, NKKMap& nkkmap);
bool read1kDistribution(const std::string& fileName, NKMap& degNumNodesMap);

template<class VertexListGraph>
bool readInputGraph(VertexListGraph& g, const std::string& filename) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	std::ifstream myfile;
	char inLine[1024];
	unsigned int lineNumber = 0;

	myfile.open(filename.c_str(), std::ios::in);
	if (!myfile.is_open()) {
		std::cerr << "Could not open " << filename << std::endl;
		return false;
	}

	// XXX this depends on inputs being an int
	std::map<int, vertex_descriptor> idToDescriptor;

	typename boost::property_map<VertexListGraph, boost::vertex_name_t>::type nameMap =
			get(boost::vertex_name, g);

	while (!myfile.eof()) {
		lineNumber++;
		myfile.getline(inLine, 1024);
		if (myfile.fail() && !myfile.eof()) {
			std::cerr << "Error - Long line, #" << lineNumber << std::endl;
			return false;
		}

		int id1, id2;
		if (sscanf(inLine, "%d %d", &id1, &id2) != EOF)
		{
			if (id1 == id2) {
				std::cerr << "Self edge detected ... line #" << lineNumber
						<< std::endl;
				continue;
			}

			if (idToDescriptor.find(id1) == idToDescriptor.end()) {
				vertex_descriptor v1 = add_vertex(g);
				idToDescriptor[id1] = v1;
				nameMap[v1] = id1;
			}
			if (idToDescriptor.find(id2) == idToDescriptor.end()) {
				vertex_descriptor v2 = add_vertex(g);
				idToDescriptor[id2] = v2;
				nameMap[v2] = id2;
			}

			// make sure we dont add double edges!
			if (edge(idToDescriptor[id1], idToDescriptor[id2], g).second
					== false)
				add_edge(idToDescriptor[id1], idToDescriptor[id2], g);
		}
	}
	myfile.close();

	return true;
}

template<class VertexListGraph>
int printGraph(VertexListGraph& g, std::ostream& outFile) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	edge_iterator i, end;
	for (boost::tie(i, end) = edges(g); i != end; ++i) {
		vertex_descriptor v1 = source(*i, g);
		vertex_descriptor v2 = target(*i, g);
		outFile << v1 << " " << v2 << std::endl;
	}

	return 0;
}

template<class VertexListGraph>
int printGraph(VertexListGraph& g, const std::string& filename) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	std::ofstream outFile;
	outFile.open(filename.c_str(), std::ios::out);
	if (!outFile.is_open()) {
		std::cerr << "Could not open " << filename << std::endl;
		return -1;
	}

	if (printGraph(g, outFile) < 0) {
		outFile.close();
		return -1;
	}

	outFile.close();

	return 0;
}

#endif
