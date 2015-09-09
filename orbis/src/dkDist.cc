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

// dkDist - used for generated the dk distributions for some input
// file

#include <iostream>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/program_options.hpp>

#include "dkMetrics.h"
#include "dkUtils.h"

using namespace std;
using namespace boost;
namespace po = boost::program_options;

typedef boost::adjacency_list<setS, vecS, undirectedS,
		property<vertex_name_t, int> ,
		property<edge_name_t, int> > Graph;

int get1kDistribution(const Graph& g, NKMap& nkmap);
int get2kDistribution(const Graph& g, NKKMap& nkmap);

int main(int argc, char* argv[]) {

	string opt_infile;
	string opt_outfile;
	int opt_k;
	int opt_do_prob = 0;

	Graph g;

	// Declare the supported options.
	po::options_description desc_mandatory("Mandatory arguments");
	desc_mandatory.add_options()("dk,k", po::value<int>(&opt_k),
			"{1, 2}-dk distribution")("input,i", po::value<string>(&opt_infile),
			"input graph file");

	po::options_description desc_optional("Optional arguments");
	desc_optional.add_options()("help,h", "produce help message")("prob,p",
			"print probability distributions instead of degree distributions")
//	("output,o",po::value<string>(&opt_outfile),"output file (default: stdout)")
			;

	po::options_description all_options;
	all_options.add(desc_mandatory).add(desc_optional);
	po::variables_map vm;
	po::store(po::parse_command_line(argc, argv, all_options), vm);
	po::notify(vm);

	if (vm.count("help")) {
		cout << all_options << "\n";
		return 1;
	}

	if (vm.count("prob"))
		opt_do_prob = 1;

	if ((!vm.count("dk")) || (!vm.count("input"))) {
		cout << "Must set dk distribution and input graph file " << endl;
		cout << "Try --help for more information" << endl;
		return 1;
	}

	if ((opt_k != 1) && (opt_k != 2)) {
		cout << "Invalid dk option: " << opt_k << endl;
		return 1;
	}

	// After command line param checks

	// Read input graph
	if (!readInputGraph(g, opt_infile)) {
		cerr << "Error reading input graph file " << opt_infile << endl;
		return 1;
	}
	if ((num_vertices(g) == 0) || (num_edges(g) == 0)) {
		cerr << "Trivial (possibly corrupt) input graph" << endl;
		return -1;
	}

	// Calculate either 1k or 2k distribution.  If -p is specified
	// calculate as a pdf.

	if (opt_k == 1) {
		NKMap nkmap;
		if (get1kDistribution(g, nkmap) < 0) {
			cerr << "Error calculating 1k distribution" << endl;
			return 1;
		}
		if (opt_do_prob) {
			PKMap pkmap;
			NKToPK(nkmap, pkmap);
			cout << pkmap;
		} else
			cout << nkmap;
	} else if (opt_k == 2) {
		NKKMap nkkmap;
		if (get2kDistribution(g, nkkmap) < 0) {
			cerr << "Error calculating 2k distribution" << endl;
			return 1;
		}
		if (opt_do_prob) {
			PKKMap pkkmap;
			NKKToPKK(nkkmap, pkkmap);
			cout << pkkmap;
		} else
			cout << nkkmap;
	}

}

////////////////////////////////////////////////////////////
// get1kDistribution
//
// Get the 1k distribution from a graph
////////////////////////////////////////////////////////////

int get1kDistribution(const Graph& g, NKMap& nkmap) {
	// iterate through all the nodes of g, for each degree count the
	// number of nodes of that degree
	typedef graph_traits<Graph>::vertex_iterator vertex_iterator;
	vertex_iterator i, end;

	nkmap.clear();

	for (boost::tie(i, end) = vertices(g); i != end; ++i) {
		int deg = out_degree(*i, g);
		if (nkmap.find(deg) == nkmap.end())
			nkmap[deg] = 1;
		else
			nkmap[deg]++;
	}
	return 0;
}

////////////////////////////////////////////////////////////
// get2kDistribution
//
// Get the 1k distribution from a graph
////////////////////////////////////////////////////////////

int get2kDistribution(const Graph& g, NKKMap& nkkmap) {
	typedef graph_traits<Graph>::edge_iterator edge_iterator;
	typedef graph_traits<Graph>::vertex_descriptor vertex_descriptor;

	edge_iterator i, end;

	nkkmap.clear();

	for (boost::tie(i, end) = edges(g); i != end; ++i) {
		vertex_descriptor v1 = source(*i, g);
		vertex_descriptor v2 = target(*i, g);

		int deg1 = out_degree(v1, g);
		int deg2 = out_degree(v2, g);

		if (deg1 != deg2) {
			nkkmap[deg1][deg2]++;
			nkkmap[deg2][deg1]++;
		} else {
			nkkmap[deg1][deg2]++;
		}
	}

	return 0;
}
