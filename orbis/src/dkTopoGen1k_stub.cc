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

#include <math.h>
#include <assert.h>
#include <boost/graph/connected_components.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/graph_traits.hpp>
#include <boost/program_options.hpp>

#include <sys/time.h>

#include <iostream>
#include <fstream>

#include <map>
#include <vector>

#include "randomizeGraph1k.h"
#include "extractLargestConnectedComponent.h"
#include "dkUtils.h"
#include "dkTopoGen1k.h"
#include "dkTopoGen2k.h"

using namespace boost;
using namespace std;
namespace po = boost::program_options;

typedef adjacency_list<setS, vecS, undirectedS> Graph;

int main(int argc, char *argv[]) {
	Graph g;

	string opt_infile;
	int opt_n_nodes;
	int opt_seed;
	int opt_rewireCount = 0;

	// Declare supported options
	po::options_description desc_mandatory("Mandatory options");
	desc_mandatory.add_options()("input,i", po::value<string>(&opt_infile),
			"input freeStubList file");
	desc_mandatory.add_options()("numnode,n", po::value<int>(&opt_n_nodes),
				"number of nodes");

	po::options_description desc_optional("Optional options");
	desc_optional.add_options()("help,h", "produce help message")("seed,s",
			po::value<int>(&opt_seed), "random seed (default: system time)")(
			"rewire_count,w",
			po::value<int>(&opt_rewireCount),
			"# of attempts to re-wire all disconnected components to GCC (default: 0)")
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
	if (!vm.count("input")) {
		cout << "Must specify input distribution" << endl;
		cout << "Try --help for more information" << endl;
		return 1;
	}
	if (!vm.count("numnode")) {
		cout << "Must specify number of nodes" << endl;
		cout << "Try --help for more information" << endl;
		return 1;
	}

	if (vm.count("seed")) {
		srand(opt_seed);
	} else
		srand(time(NULL));

	cerr<< "opt_infile = " << opt_infile << endl;
	cerr<< "opt_n_nodes = " << opt_n_nodes << endl;

	std::vector<int> degSeq(opt_n_nodes);
//	int degSeq[opt_n_nodes] ;

	int need2Rewire = dkTopoGen1kStubList(g, opt_n_nodes, opt_infile);

	cerr << "done generating" << endl;

	// If specified, try to rewire all of the disconnected components
	// to the GCC using 1k-preserving rewirings
	if (need2Rewire) {
		cerr << "attempting to rewire disconnected components to GCC" << endl;
		for (int c = 0; c < opt_rewireCount; c++)
			rewireToLargestConnectedComponent1k(g);
	}


	cerr << "omitting GCC extraction" << endl;
	/*
	cerr << "extracting GCC" << endl;
	// Remove largest connected components
	int originalComponents = extractLargestConnectedComponent(g);
	if (originalComponents <= 0) {
		std::cerr << "Error removing largest connected component" << std::endl;
		return -1;
	}
	*/

	printGraph(g, cout);

	return 0;
}

