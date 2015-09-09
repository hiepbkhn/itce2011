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
#include <set>
#include <math.h>

#include <boost/program_options.hpp>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>

#include <boost/graph/betweenness_centrality.hpp>
#include <boost/property_map/property_map.hpp>

#include "dkMetrics.h"
#include "dkUtils.h"
#include "extractLargestConnectedComponent.h" 

using namespace std;
using namespace boost;
using namespace boost::detail::graph;	//HiepNH
namespace po = boost::program_options;

typedef boost::adjacency_list<setS, vecS, undirectedS,
		property<vertex_name_t, int> ,
		property<edge_name_t, int> > Graph;

// Did not implement this as a generic algorithm b/c I need to
// property map to be statically typed.  I'm pretty sure you can run
// the betweenness algorithm using dynamic types  also...
void betweenness(const Graph& g, map<int, float>& degreeBetweenness);

int main(int argc, char* argv[]) {
	string opt_infile;
	Graph g;

	po::options_description desc_mandatory("Mandatory arguments");
	desc_mandatory.add_options()("input,i", po::value<string>(&opt_infile),
			"input graph file");

	po::options_description desc_scalar("Scalar arguments");
	desc_scalar.add_options()("scalar,s", "calculate scalar metrics")(
			"nodistance", "skips calculating avg distance scalar metric");

	po::options_description desc_dist("Distribution arguments");
	desc_dist.add_options()("distance,d", "calculate distance distribution")(
			"betweenness,b", "calculate betweenness distribution");

	po::options_description desc_optional("Optional arguments");
	desc_optional.add_options()("help,h", "produce help message");

	po::options_description all_options;
	all_options.add(desc_mandatory).add(desc_scalar).add(desc_dist);
	all_options.add(desc_optional);

	po::variables_map vm;
	po::store(po::parse_command_line(argc, argv, all_options), vm);
	po::notify(vm);

	// Parse options
	if (vm.count("help")) {
		cout << "Calculates graph metrics for an input graph in INET format"
				<< endl;
		cout << "Distance, betweenness and clustering metrics can be " << endl;
		cout << "computationally expensive depending on input graph" << endl;
		cout << all_options << "\n";
		return 1;
	}

	if (!vm.count("input")) {
		cerr << "Must specify  input graph file" << endl;
		cerr << "Try --help for more information" << endl;
		return 1;
	}

	if (!readInputGraph(g, opt_infile)) {
		cerr << "Error reading input graph:" << opt_infile << endl;
		return -1;
	}

	if ((num_vertices(g) == 0) || (num_edges(g) == 0)) {
		cerr << "Trivial (possibly corrupt) input graph" << endl;
		return -1;
	}

	// put in a flag so that one can skip calculating the distance
	// distributions (neccesary for avg distance scalar metric)
	map<int, float> distanceDistribution;
	float averageDistance = -1;

	if (vm.count("distance")
			|| (vm.count("scalar") && (!vm.count("nodistance")))) {
		averageDistance = distanceMetrics(g, distanceDistribution);

	}

	if (vm.count("scalar")) {
		cout << "num_nodes:" << num_vertices(g) << endl;
		cout << "num_edges:" << num_edges(g) << endl;
		cout << "average_degree:" << averageDegree(g) << endl;
		cout << "assortativity:" << find_r(g) << endl;
		cout << "clustering:" << clustering(g) << endl;

		if (!vm.count("nodistance"))
			cout << "avg_distance" << averageDistance << endl;
	}

	if (vm.count("distance")) {
		for (map<int, float>::iterator i = distanceDistribution.begin();
				i != distanceDistribution.end(); ++i) {
			cout << i->first << "\t" << i->second << endl;
		}
	}

	if (vm.count("betweenness")) {
		map<int, float> degreeeBetweenness;
		betweenness(g, degreeeBetweenness);

		for (map<int, float>::iterator i = degreeeBetweenness.begin();
				i != degreeeBetweenness.end(); ++i) {
			// Priya's graph don't show the degree=1 case (betweenness
			// is 0)
			if (i->first > 1)
				cout << i->first << "\t" << i->second << endl;
		}
	}

	return 0;
}

////////////////////////////////////////////////////////////
// betweenness
//
// Calculates vertex betweenness.  Returns a normalized betweenness
// distribution by degree
////////////////////////////////////////////////////////////

void betweenness(const Graph& g, map<int, float>& degreeBetweenness) {
	// Betweeness - already implemented by boost
	typedef map<int, float> VertexBetweenMap;

	int numVertices = num_vertices(g);
	VertexBetweenMap vertexBetweenMap;
	associative_property_map<VertexBetweenMap> vertexBetweenPropertyMap(
			vertexBetweenMap);

	brandes_betweenness_centrality(g, vertexBetweenPropertyMap);

	map<int, float> degreeBetweenMap;
	map<int, int> degreeBetweenCountMap;

	float vertexPairs = ((numVertices - 1) * (numVertices - 2));

	for (VertexBetweenMap::iterator i = vertexBetweenMap.begin();
			i != vertexBetweenMap.end(); ++i) {
		int nodeId = i->first;
		float btwn = i->second;
		int degree = out_degree(nodeId, g);

		degreeBetweenMap[degree] += (btwn / vertexPairs);
		degreeBetweenCountMap[degree]++;
	}

	for (map<int, float>::iterator i = degreeBetweenMap.begin();
			i != degreeBetweenMap.end(); ++i) {
		int degree = i->first;
		float btwn = i->second;

		degreeBetweenness[degree] = btwn / degreeBetweenCountMap[degree];
	}
}
