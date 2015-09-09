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

#include <sys/time.h>
#include <unistd.h>
#include <math.h>
#include <assert.h>

#include <boost/program_options.hpp>
#include <boost/graph/connected_components.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/graph_traits.hpp>

#include <iostream>
#include <fstream>
#include <map>
#include <vector>

#include "extractLargestConnectedComponent.h"
#include "dkUtils.h"
#include "dkTopoGen1k.h"
#include "dkTopoGen2k.h"
#include "randomizeGraph2k.h"

using namespace boost;
using namespace std;
namespace po = boost::program_options;

typedef adjacency_list<setS, vecS, undirectedS> Graph;
Graph g;

int main(int argc, char *argv[]) {

	string opt_infile;
	int opt_randomSeed = 0;
	Graph g;

	srand(0);
	//srand(time(NULL));

	po::options_description desc_mandatory("Mandatory arguments");
	desc_mandatory.add_options()("input,i", po::value<string>(&opt_infile),
			"input 2k dist file");

	po::options_description desc_optional("Optional arguments");
	desc_optional.add_options()("help,h", "produce help message")("seed,s",
			po::value<int>(&opt_randomSeed),
			"Random seed (default: system time)");

	po::options_description all_options;
	all_options.add(desc_mandatory).add(desc_optional);
	po::variables_map vm;
	po::store(po::parse_command_line(argc, argv, all_options), vm);
	po::notify(vm);

	// Parse options
	if (vm.count("help")) {
		cout << all_options << "\n";
		return 1;
	}

	if (vm.count("seed"))
		srand(opt_randomSeed);
	else
		srand(time(NULL));

	if (!vm.count("input")) {
		cerr << "Must specify input distribution file" << endl;
		cerr << "Try --help for more information" << endl;
		return 1;
	}

	NKKMap nkkmap;
	if (!read2kDistribution(opt_infile, nkkmap) || (nkkmap.empty())) {
		cerr << "Could not read 2k distribution:" << opt_infile << endl;
		return 1;
	}

	dkTopoGen2k(g, nkkmap);

	cerr << "Done generating, extracting GCC" << endl;

//     for (int i =0; i < 100; ++i)
// 	rewireToLargestConnectedComponent2k(g);

//     for (int i = 0; i < 300; ++i)
//  	rewireToLargestConnectedComponent1k(g);

	extractLargestConnectedComponent(g);
	printGraph(g, cout);
}
