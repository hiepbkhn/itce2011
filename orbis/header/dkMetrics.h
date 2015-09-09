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

#ifndef __DK_METRICS_H
#define __DK_METRICS_H

#include <algorithm>
#include <boost/graph/graph_concepts.hpp>

#include <boost/graph/visitors.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/breadth_first_search.hpp>
#include <boost/property_map/property_map.hpp>
#include <boost/graph/graph_utility.hpp>

#include <boost/graph/dijkstra_shortest_paths.hpp>

#include <map>
#include <iostream>

////////////////////////////////////////////////////////////
// clustering
//
// Calculates the average clustering for a given graph
////////////////////////////////////////////////////////////
template<class VertexListGraph>
float clustering(const VertexListGraph& g) {
	// Boost typedefs
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::degree_size_type degree_size_type;
	typedef typename GraphTraits::vertices_size_type vertices_size_type;
	typedef typename GraphTraits::adjacency_iterator adjacency_iterator;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	std::map<int, float> clustering;
	std::map<int, int> degreeDistribution_int;
	std::map<int, float> degreeDistribution;
	std::map<int, int> nodesWithDegree;

	vertex_iterator vi, vend;

	vertices_size_type numNodes = num_vertices(g);

	// Calculate the degree distribution
	for (boost::tie(vi, vend) = vertices(g); vi != vend; ++vi) {
		degree_size_type deg = out_degree(*vi, g);
		degreeDistribution_int[(int) deg]++;
	}

	for (typename std::map<int, int>::iterator i =
			degreeDistribution_int.begin(); i != degreeDistribution_int.end();
			++i) {
		int degree = i->first;
		int count = i->second;

		nodesWithDegree[degree] = count;
		degreeDistribution[degree] = (float(degreeDistribution_int[degree])
				/ (float) numNodes);

	}

	// Calculate clustering
	for (boost::tie(vi, vend) = vertices(g); vi != vend; ++vi) {
		int num = 0;
		degree_size_type sdeg = out_degree(*vi, g);
		adjacency_iterator ai1, aend1, ai2, aend2;

		if (sdeg <= 1)
			continue;

		for (boost::tie(ai1, aend1) = adjacent_vertices(*vi, g); ai1 != aend1;
				ai1++) {
			for (boost::tie(ai2, aend2) = adjacent_vertices(*vi, g);
					ai2 != aend2; ai2++) {
				if (*ai2 <= *ai1)
					continue;

				if (edge(*ai1, *ai2, g).second == true)
					num++;
			}
		}

		float clus = (float) (2 * num) / (float) (sdeg * (sdeg - 1));
		clustering[sdeg] += clus;

	}

	float mean_clus = 0;

	for (typename std::map<int, float>::iterator i = clustering.begin();
			i != clustering.end(); ++i) {
		int degree = i->first;
		if (i->second == 0)
			continue;

		clustering[degree] = float(clustering[degree])
				/ float(nodesWithDegree[degree]);
		mean_clus += (float(clustering[degree])
				* float(degreeDistribution[degree]));
	}

	return mean_clus;
}

////////////////////////////////////////////////////////////
// distanceMetrics
//
// Calculates average distance as well as the distance distribution
// for some input.  Because all edges are unit weight, this uses BFS
// instead of dijkstra to calculate all pairs distances.
//
// Returns the average distance and also fills the
// distanceDistribution map with the distance distribution
////////////////////////////////////////////////////////////

template<class VertexListGraph>
float distanceMetrics(const VertexListGraph& g,
		std::map<int, float>& distanceDistribution) {
	// Boost typedefs
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_descriptor edge_descriptor;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::degree_size_type degree_size_type;
	typedef typename GraphTraits::vertices_size_type vertices_size_type;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	vertex_descriptor numNodes = num_vertices(g);
	unsigned long long diameter = 0;
	unsigned long long totalNumPaths = 0;

	unsigned long long avgDist_int = 0;
	float avgDist_float = 0;
	std::map<int, int> dist_distr_int;

	// Iterate over all nodes, calculate all pairs shortest paths

	for (vertex_descriptor s = 0; s < numNodes; s++) {
		std::vector<vertices_size_type> d(numNodes, 0);

		boost::breadth_first_search(
				g,
				s,
				boost::visitor(
						boost::make_bfs_visitor(
								(boost::record_distances(&d[0],
										boost::on_tree_edge())))));

		for (vertex_descriptor t = s + 1; t < numNodes; ++t) {
			if (t <= s)
				continue;

			vertices_size_type pathLength = d[t];
			totalNumPaths++;

			if (diameter < pathLength)
				diameter = pathLength;

			dist_distr_int[pathLength]++;
			avgDist_int += pathLength;

		}

		// XXX debug code
// 	if (s % 500 == 0) {
//  	    std::cerr << s << " out of " << numNodes << std::endl;
// 	}
	}

	avgDist_float = float(avgDist_int) / float(totalNumPaths);

	for (std::map<int, int>::iterator i = dist_distr_int.begin();
			i != dist_distr_int.end(); ++i) {
		distanceDistribution[i->first] = float(i->second)
				/ float(totalNumPaths);
	}
	return avgDist_float;
}

////////////////////////////////////////////////////////////
// averageDegree
//
// Calculate the average degree of a graph
////////////////////////////////////////////////////////////

template<class VertexListGraph>
float averageDegree(const VertexListGraph& g) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::vertex_iterator vertex_iterator;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	vertex_iterator i, end;
	degree_size_type degreeSum = 0;
	unsigned int total = 0;

	for (boost::tie(i, end) = vertices(g); i != end; ++i) {
		degreeSum += out_degree(*i, g);
		total++;
	}

	return float(degreeSum) / float(total);
}

////////////////////////////////////////////////////////////
// find_r
//
// Calculate the assortativity coefficient
////////////////////////////////////////////////////////////

template<class VertexListGraph>
float find_r(const VertexListGraph& g) {
	typedef boost::graph_traits<VertexListGraph> GraphTraits;
	typedef typename GraphTraits::vertex_descriptor vertex_descriptor;
	typedef typename GraphTraits::edge_iterator edge_iterator;
	typedef typename GraphTraits::degree_size_type degree_size_type;

	boost::function_requires<
			boost::VertexAndEdgeListGraphConcept<VertexListGraph> >();

	unsigned int edge_count = num_edges(g);

	long long ass_sum_int = 0;
	long long ass_sq_int = 0;
	long long ass_prod_int = 0;

	float ass_sum_float = 0;
	float ass_sq_float = 0;
	float ass_prod_float = 0;

	edge_iterator i, end;
	for (boost::tie(i, end) = edges(g); i != end; ++i) {
		vertex_descriptor v1 = source(*i, g);
		vertex_descriptor v2 = target(*i, g);

		degree_size_type k1 = out_degree(v1, g);
		degree_size_type k2 = out_degree(v2, g);

// 	ass_sum +=   (float) (k1 + k2) / 2.0 ;
//         ass_prod +=  (float) (k1 * k2);
//         ass_sq +=  ( (float) ( (k1 * k1) + (k2 * k2) ) ) / 2.0 ;

		ass_sum_int += (k1 + k2);
		ass_prod_int += (k1 * k2);
		ass_sq_int += (k1 * k1) + (k2 * k2);
	}

	ass_sum_float = float(ass_sum_int) / 2.0;
	ass_prod_float = float(ass_prod_int);
	ass_sq_float = float(ass_sq_int) / 2.0;

	float ass_num = (float) (ass_prod_float / edge_count)
			- (float) (((ass_sum_float / edge_count)
					* (ass_sum_float / edge_count)));

	float ass_den = (float) (ass_sq_float / edge_count)
			- (float) (((ass_sum_float / edge_count)
					* (ass_sum_float / edge_count)));

	float r = (float) ass_num / (float) ass_den;

	return r;
}

#endif
