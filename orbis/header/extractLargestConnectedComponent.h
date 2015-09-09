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

#ifndef __EXTRACT_LARGEST_CC_H
#define __EXTRACT_LARGEST_CC_H

#include <boost/graph/graph_concepts.hpp>
#include <boost/graph/connected_components.hpp>
#include <boost/graph/copy.hpp>
#include <boost/property_map/property_map.hpp>
#include <boost/graph/betweenness_centrality.hpp>

#include <vector>

#include "dkUtils.h"

////////////////////////////////////////////////////////////
// rewireToLargestConnectedComponent1k
//
// Performs 1k-preserving rewiring to try and connect disconnected
// components to the GCC.  Maybe this code should be moved somewhere
// else?
//
// This is somewhat experimental and I don't believe in their current
// state any tools use this
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int rewireToLargestConnectedComponent1k(VertexListGraph& G) {
	// Boost typedefs so this implementation is independent
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::adjacency_iterator adjacency_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::vertices_size_type vertices_size_type;

	// Concept checking methods we use below
	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	// Calculate connected components of input graph.  For each vertex
	// this will tell us the CC it belong to
	// vertices_size_type numVertices = num_vertices(G);

	// Maps vertex -> component
	typedef std::map<vertex_descriptor, vertices_size_type> VertexComponentMap;
	VertexComponentMap vertexComponentMap;
	boost::associative_property_map<VertexComponentMap> vertexComponentPropertyMap(
			vertexComponentMap);

	// Colormap needed by GCC boost algorithm
	typedef std::map<vertex_descriptor, boost::default_color_type> ColorMap;
	ColorMap colorMap;
	boost::associative_property_map<ColorMap> colorPropertyMap(colorMap);

	unsigned int totalComponents = connected_components(G,
			vertexComponentPropertyMap, boost::color_map(colorPropertyMap));

	vertices_size_type largestComponent = 0;
	vertices_size_type maxSize = 0;
	std::map<vertices_size_type, vertices_size_type> componentSize;

	// Bin vertices by their component and calculate which component
	// is the biggest:
	std::map<vertices_size_type, std::vector<vertex_descriptor> > verticesByComponent;

	for (typename VertexComponentMap::iterator i = vertexComponentMap.begin();
			i != vertexComponentMap.end(); i++) {
		vertex_descriptor vertex = i->first;
		vertices_size_type componentNum = i->second;
		verticesByComponent[componentNum].push_back(vertex);
		if (componentSize.find(componentNum) == componentSize.end())
			componentSize[componentNum] = 1;
		else
			componentSize[componentNum]++;

		if (componentSize[componentNum] > maxSize) {
			largestComponent = componentNum;
			maxSize = componentSize[componentNum];
		}
	}

	// Figure out which vertices belong to the gcc.  Also store only
	// the vertices that have a degree of > 1.
	std::vector<vertex_descriptor>& gccNodes =
			verticesByComponent[largestComponent];
	std::list<vertex_descriptor> gccNodes_nodeg1;

	for (typename std::vector<vertex_descriptor>::iterator i = gccNodes.begin();
			i != gccNodes.end(); ++i) {
		if (out_degree(*i, G) > 1)
			gccNodes_nodeg1.push_back(*i);
	}

	// randomize our list of vertices from the gcc (only ones with deg
	// > 1)
	dkShuffleList(gccNodes_nodeg1);

	// Iterate through the components in a random order
	std::list<vertices_size_type> componentList;
	for (vertices_size_type i = 0; i < totalComponents; i++)
		componentList.push_back(i);

	dkShuffleList(componentList);
	// Iterate through all disconnected components and try to rewire
	// them to the GCC using a 1k-preserving rewire
	for (typename std::list<vertices_size_type>::iterator i =
			componentList.begin(); i != componentList.end(); i++) {
		vertices_size_type comp = *i;
		std::cerr << "on component: " << comp << std::endl;
		if (comp == largestComponent)
			continue;

		if (verticesByComponent[comp].size() < 3)
			continue;

		std::vector<vertex_descriptor>& vertexList = verticesByComponent[comp];

		// Pick a random node out of the disconnected component.  Make
		// sure it has at least 1 other connection
		vertex_descriptor disconnectedNode = vertexList[rand()
				% vertexList.size()];

		std::cerr << "Selected disconnected node:" << disconnectedNode
				<< " degree:" << out_degree(disconnectedNode, G) << std::endl;

		if (out_degree(disconnectedNode, G) == 0)
			continue;

		// Try and connect to a node in the GCC (only look at vertices
		// that have a degree > 1)
		vertex_descriptor gccNode = gccNodes_nodeg1.front();

		// Get a node v2 adjacent to our "disconnectedNode
		vertex_descriptor v2 = *(adjacent_vertices(disconnectedNode, G).first);

		// FIND a GCC NODE, v4, THAT DOESNT HAVE DEGREE 1
		// scratch this - find a random neighbor

		unsigned int randNum = rand() % out_degree(gccNode, G);
		unsigned int cnt = 0;
		adjacency_iterator vi, vend;
		for (boost::tie(vi, vend) = adjacent_vertices(gccNode, G); vi != vend;
				++vi, ++cnt) {
			if (cnt >= randNum)
				break;
// 	    if (out_degree(*vi, G) != 1)
// 		break;
		}

		if (vi == vend) {
			std::cerr << "Could not find someone to connect to sorry"
					<< std::endl;
			continue;
		}

		vertex_descriptor v4 = *vi;
		vertex_descriptor v1 = disconnectedNode;
		vertex_descriptor v3 = gccNode;

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

		assert(edge(v1, v2, G).second == true);
		assert(edge(v3, v4, G).second == true);

		// Swap the edges
		remove_edge(v1, v2, G);
		remove_edge(v3, v4, G);
		assert(add_edge(v1, v4, G).second == true);
		assert(add_edge(v2, v3, G).second == true);

		return 0;
	}

	return 0;
}

////////////////////////////////////////////////////////////
// extractLargestConnectedComponent
//
// algorithm for removing the largest connected component of a graph.
// Uses the connected_components library that comes with boost.  
////////////////////////////////////////////////////////////

template<class VertexListGraph>
int extractLargestConnectedComponent(VertexListGraph& G) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::vertices_size_type vertices_size_type;
	typedef typename GraphTraits::edge_iterator edge_iterator;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();
	boost::function_requires<boost::MutableGraphConcept<VertexListGraph> >();

	std::cerr << "Graph size: " << num_vertices(G) << std::endl;
	;

	// Maps vertex -> component
	typedef std::map<vertex_descriptor, vertices_size_type> VertexComponentMap;
	VertexComponentMap vertexComponentMap;
	boost::associative_property_map<VertexComponentMap> vertexComponentPropertyMap(
			vertexComponentMap);

	// Colormap needed by GCC boost algorithm
	typedef std::map<vertex_descriptor, boost::default_color_type> ColorMap;
	ColorMap colorMap;
	boost::associative_property_map<ColorMap> colorPropertyMap(colorMap);

	int totalComponents = 0;
	totalComponents = connected_components(G, vertexComponentPropertyMap,
			boost::color_map(colorPropertyMap));
	std::cerr << "total components: " << totalComponents << std::endl;

	if (totalComponents <= 0)
		return -1;

	// componentSize[i] - size of component i, initialize this data
	// structure.  Compute the size of all total components as well as
	// the largest connected component
	vertices_size_type largestComponent = 0;
	vertices_size_type maxSize = 0;
	std::map<vertices_size_type, vertices_size_type> componentSize;

	for (typename VertexComponentMap::iterator i = vertexComponentMap.begin();
			i != vertexComponentMap.end(); i++) {
		// vertex_descriptor vertex = i->first;
		vertices_size_type componentNum = i->second;

		if (componentSize.find(componentNum) == componentSize.end())
			componentSize[componentNum] = 1;
		else
			componentSize[componentNum]++;

		if (componentSize[componentNum] > maxSize) {
			largestComponent = componentNum;
			maxSize = componentSize[componentNum];
		}
	}

	// now remove all vertices not in component 'largestComponent'
	// need to be careful b/c removing vertices messes up iterators.
	// First we clear vertexes not in our largest connected component
	// ... we cannot remove vertices now because doing so will
	// invalidate our vertex descriptors stored in "components".
	// After we remove all the edges from vertices we want to remove,
	// we delete all vertices with 0 edges
	for (typename VertexComponentMap::iterator i = vertexComponentMap.begin();
			i != vertexComponentMap.end(); i++) {
		vertex_descriptor vertex = i->first;
		vertices_size_type componentNum = i->second;

		if (componentNum != largestComponent)
			clear_vertex(vertex, G);
	}

	// The following should be safe for both vectors and lists we
	// could make this faster if we knew that a list was beig used as
	// the underyling data structure
	int done = 0;
	VertexListGraph newGraph;
	// Maps vertex descriptors in old graph to those in new GCC graph
	std::map<vertex_descriptor, vertex_descriptor> oldToNewVertexMap;
	while (!done) {
		vertex_iterator vi, vend;
		for (boost::tie(vi, vend) = vertices(G); vi != vend; ++vi) {
			// Old way - remove vertices from old graph.  This is slow
			// if using vecS to store edges.  Will come back to this

// 	    if (out_degree(*vi, G) == 0)
// 	    {
// 		remove_vertex(*vi, G);
// 		break; // iterators might be messed
// 	    }

			// new way - create a new graph
			if (out_degree(*vi, G) > 0) {
				if (oldToNewVertexMap.find(*vi) == oldToNewVertexMap.end()) {
					vertex_descriptor newVertex = add_vertex(newGraph);
					oldToNewVertexMap[*vi] = newVertex;
				}
			}
		}
		if (vi == vend)
			done = 1;
	}

	edge_iterator edgeIter, edgeIterEnd;
	for (boost::tie(edgeIter, edgeIterEnd) = edges(G); edgeIter != edgeIterEnd;
			edgeIter++) {
		vertex_descriptor newV1 = oldToNewVertexMap[source(*edgeIter, G)];
		vertex_descriptor newV2 = oldToNewVertexMap[target(*edgeIter, G)];
		add_edge(newV1, newV2, newGraph);
	}

	// boost graph clear had weird results (only cleared vertices, not
	// edges) for vertex/edge stored as vecS.  This seemed to fix the
	// problem
	vertex_iterator vi, vend;
	for (boost::tie(vi, vend) = vertices(G); vi != vend; ++vi)
		clear_vertex(*vi, G);
	G.clear();

	// XXX currently all implementations are using vecS.  Removing
	// vertices is something like O(V+E) complexity per each
	// remove. Faster to construct a new graph then copy it over.
	// Future versions will just use listS as an underlying data
	// structure and this performance enhancement won't be necessary

	copy_graph(newGraph, G);
	return totalComponents;
}

////////////////////////////////////////////////////////////
// rewireToLargestConnectedComponent2k
////////////////////////////////////////////////////////////

// template <class VertexListGraph>
// int rewireToLargestConnectedComponent2k(VertexListGraph& G)
// {

//     typedef boost::graph_traits<VertexListGraph> GraphTraits;
//     typedef typename GraphTraits::edge_iterator edge_iterator;
//     typedef typename GraphTraits::edge_descriptor edge_descriptor;
//     typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
//     typedef typename GraphTraits::vertex_iterator vertex_iterator;
//     typedef typename GraphTraits::adjacency_iterator adjacency_iterator;
//     typedef typename GraphTraits::out_edge_iterator out_edge_iterator;

//     // Concept checking methods we use below
//     boost::function_requires< boost::VertexAndEdgeListGraphConcept
// 	<VertexListGraph> >();
//     boost::function_requires< boost::MutableGraphConcept
// 	<VertexListGraph> >();

//     unsigned int numVertices = num_vertices(G);
//     std::vector<int> components(numVertices);
//     std::vector<int>::size_type totalComponents = 
// 	connected_components(G, &components[0]);

//      std::cerr << "totalComponents: " << totalComponents << std::endl;

//      if (totalComponents <= 1)
// 	 return totalComponents;

//     int largestComponent = 0;
//     int maxSize = 0;

//     // For each component, give the vertices in that component
//     std::vector< std::vector<int> > verticesByComponent(totalComponents);

//     for (std::vector<int>::size_type i = 0;
// 	 i != numVertices;
// 	 i++)
//     {
// 	int componentNum = components[i];
// 	assert(i < numVertices);
// 	verticesByComponent[componentNum].push_back(i);

// 	if (verticesByComponent[componentNum].size() > maxSize)
// 	{
// 	    largestComponent = componentNum;
// 	    maxSize = verticesByComponent[componentNum].size();
// 	}
//     }

//     if (totalComponents <= 0)
// 	return -1;

//     // Get a list of all edges in the GCC so we know candidates to
//     // swap with
//     std::vector<int>& gccNodes = verticesByComponent[largestComponent];
// //     std::map<edge_descriptor, int> uniqueEdges;
//     std::list<edge_descriptor> gccEdgeList;
//     for (typename std::vector<int>::iterator i = gccNodes.begin();
//  	 i != gccNodes.end();
//  	 ++i)
//     {
// 	if (out_degree(*i, G) <= 1)
// 	    continue;

// 	// Get all out edges for the node
// 	out_edge_iterator edgeIter, edgeIterEnd;
// 	for (boost::tie(edgeIter, edgeIterEnd) = out_edges(*i,G);
// 	     edgeIter != edgeIterEnd;
// 	     ++edgeIter)
// 	{
// 	    edge_descriptor e = *edgeIter;
// 	    // No duplicates
// //  	    if (uniqueEdges.find(e) == uniqueEdges.end())
//    //  	    {

// 	    // XXX calvin
// 	    vertex_descriptor s1 = target(e, G);
// 	    vertex_descriptor s2 = target(e, G);

// 	    if (out_degree(s1, G) <= 1)
// 		continue;

// 	    if (out_degree(s2, G) <= 1)
// 		continue;

// 	    gccEdgeList.push_back(e);
// // 		uniqueEdges[e] = 1;
// //  	    }
// 	}
//     }

//     // Edges in gcc
//     dkShuffleList(gccEdgeList);

//     for (int comp = 0; comp < totalComponents; comp++)
//     {
// 	if (comp == largestComponent)
// 	    continue;

// 	if (verticesByComponent[comp].size() < 10)
// 	    continue;

// 	int rw;
// 	for (std::vector<int>::iterator i = verticesByComponent[comp].begin();
// 	     i != verticesByComponent[comp].end();
// 	     ++i)
// 	{
// // 	    std::cerr << "YO:" << *i << " outdeg:" << out_degree(*i, G)
// // 		      << std::endl;
// 	    if (out_degree(*i, G) <= 1)
// 		continue;
// 	    rw = randomRewireVertex2k(G, *i, gccEdgeList);
// 	    break;
// 	}

// 	if (rw)
// 	    return 1;
//     }
//     return 0;
// }

#endif
