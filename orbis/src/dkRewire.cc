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
#include <getopt.h>

#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/graph_traits.hpp>
#include <iostream>
#include <fstream>
#include "randomizeGraph1k.h" 
#include "randomizeGraph2k.h"
#include "randomizeGraph3k.h"
#include "extractLargestConnectedComponent.h"

#include <list>
#include <set>
#include <vector>

#include "dkUtils.h"

using namespace boost;
using namespace std;

typedef adjacency_list<setS, vecS, undirectedS, property<vertex_name_t, int> ,
property<edge_name_t, int> > Graph;

typedef graph_traits<Graph>::edge_descriptor Edge;
typedef graph_traits<Graph>::vertex_descriptor Vertex;

// const char usage_short[] = "
// Usage: %s -i input -o output -k k-value -r random_seed\n
// Try '%s --help' for more information.\n";
const char usage_short[] =
		"\
Usage: dkRewire -i input -o output -k {1,2,3} [OPTIONS]\n\
Try `dkRewire --help' for more information.\n";

const char usage_long[] =
		"\
Usage: orbis -i input -o output -k {1,2,3} [OPTIONS]\n\
Randomizes an input graph using 1k, 2k or 3k rewiring techniques\n\
\n\
Mandatory Arguments:\n\
 -i, --input_file       Input file to Orbis.  Each line of the input file\n\
                        contains a pair of vertices denoting an edge \n\
                        whcih imply an edge between them \n\
 -o, --output_file      Output file from Orbis.  The output file will be \n\
                        the same format as the input file.\n\
 -k  --k_value          Specifies the k value for dK rewirings.  Must \n\
                        be either 1, 2 or 3\n\
\n\
Optional Arguments:\n\
 -w, --rewire_limit     The limit on the number of rewires before\n\
                        Orbis returns.\n\
 -t, --time_limit       Specifies the maximum amount of time Orbis will \n\
                        run before returning (in seconds).\n\
 -r, --random_seed      Random seed to give to program.  Leaving out this \n\
                        parameter will let Orbis choose a random seed \n\
                        based on the current system clock. \n\
 -a, --target-r         For 1k-rewiring it is possible to specify a target \n\
                        assortativity coefficient r \n\
\n\
Miscellaneous:\n\
 -h, --help             print this message\n\
\n\
NOTE: At least ONE limit (rewire_attempt, rewire_success or time_limit)\n\
must be specified\n";

void printUsage_short() {
	cerr << usage_short;
}

void printUsage_long() {
	cerr << usage_long;
}

int main(int argc, char *argv[]) {
	Graph g;

	map<char, string> paramMap;
	struct option options[] = { { "input_file", 1, NULL, 'i' }, { "output_file",
			1, NULL, 'o' }, { "k_value", 1, NULL, 'k' }, { "rewire_limit", 1,
			NULL, 'w' }, { "time_limit", 1, NULL, 't' }, { "random_seed", 1,
			NULL, 'r' }, { "help", 0, NULL, 'h' }, { "target-r", 1, NULL, 'a' },
			{ NULL, 0, NULL, 0 } };

	// getopt testing
	int param;
	while ((param = getopt_long(argc, argv, "i:o:k:w:t:r:a:h", options, NULL))
			!= EOF) {
		switch ((char) param) {
		case '?':
			printUsage_short();
			exit(1);
		case 'h':
			printUsage_long();
			exit(1);
		default:
			paramMap[(char) param] = optarg;
		}

	}

	// Check mandatory params
	if ((paramMap.find('i') == paramMap.end())
			|| (paramMap.find('o') == paramMap.end())
			|| (paramMap.find('k') == paramMap.end())) {
		printUsage_short();
		exit(1);
	}

	// Check k values, must be 1, 2, or 3
	int kvalue = atoi(paramMap['k'].c_str());
	int rewireLimit = 0, timeLimit = 0, randomSeed = 0;
	float r = 0;

	if (paramMap.find('w') != paramMap.end())
		rewireLimit = atoi(paramMap['w'].c_str());

	if (paramMap.find('t') != paramMap.end())
		timeLimit = atoi(paramMap['t'].c_str());

	if (paramMap.find('r') != paramMap.end())
		randomSeed = atoi(paramMap['r'].c_str());
	else
		randomSeed = time(NULL);

	if (!((kvalue >= 1) && (kvalue <= 3))) {
		cerr << "Bad k-value: " << kvalue << endl;
		exit(1);
	}

	if (paramMap.find('a') != paramMap.end())
		r = atof(paramMap['a'].c_str());

	if ((paramMap.find('a') != paramMap.end()) && kvalue != 1) {
		cerr << "Can only do target-r rewire for 1k" << endl;
		exit(1);
	}

	if (!readInputGraph(g, paramMap['i'])) {
		cerr << "Bad input graph:" << paramMap['i'] << endl;
		exit(1);
	}

	if ((num_vertices(g) == 0) || (num_edges(g) == 0)) {
		cerr << "Trivial (possibly corrupt) input graph" << endl;
		return -1;
	}

	int numCC = extractLargestConnectedComponent(g);
	cout << "Original graph had " << numCC << " connected components" << endl;

	int rewiredEdges;
	switch (kvalue) {
	case (1):
		if (paramMap.find('a') != paramMap.end()) {
			cout << "Doing target rewiring" << endl;
			rewiredEdges = targetRandomizeGraph1k(g, rewireLimit, timeLimit,
					randomSeed, r, true);
		} else {
			cout << "Doing non target rewiring" << endl;
			rewiredEdges = randomizeGraph1k(g, rewireLimit, timeLimit,
					randomSeed, true);
		}
		break;
	case (2):
		rewiredEdges = randomizeGraph2k(g, rewireLimit, timeLimit, randomSeed,
				true);
		break;
	case (3):
		rewiredEdges = randomizeGraph3k(g, rewireLimit, timeLimit, randomSeed,
				true);
		break;
	default:
		cerr << "Bad k-value" << kvalue << endl;
		exit(1);
	}
	cout << "Successfully rewired " << rewiredEdges << " edges" << endl;

	printGraph(g, string(paramMap['o']));

}

