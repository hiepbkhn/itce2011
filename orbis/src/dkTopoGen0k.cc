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

using namespace boost;
using namespace std;
namespace po = boost::program_options;

typedef adjacency_list<setS, vecS, undirectedS> Graph;
Graph g;

int main(int argc, char *argv[]) {

	double opt_averageDegree = 0;
	int opt_randomSeed = 0;
	int opt_numNodes = 0;
	Graph g;

	po::options_description desc_mandatory("Mandatory arguments");
	desc_mandatory.add_options()("average_degree,k",
			po::value<double>(&opt_averageDegree),
			"Average node degree (k_bar)")("nodes,n",
			po::value<int>(&opt_numNodes), "number of nodes in target graph");

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

	if (opt_averageDegree <= 0) {
		cerr << "Must specify k, average degree (for 0K distribution)" << endl;
		cerr << "Try --help for more information" << endl;
		return 1;
	}
	if (opt_numNodes <= 0) {
		cerr << "Must specify n, target number of nodes" << endl;
		cerr << "Try --help for more information" << endl;
		return 1;
	}

	double connectProb = opt_averageDegree / double(opt_numNodes);

	// Connect all pairs of nodes with pr (k / n)
	for (int i = 0; i < opt_numNodes; i++) {
		for (int j = i + 1; j < opt_numNodes; j++) {
			// connect node i to node j withi pr (k / n)
			double randomNum = double(rand()) / double(RAND_MAX);
			if (randomNum < connectProb) {
				// using a set for edgeList so duplicate edges won't be added
				add_edge(i, j, g);
			}
		}
	}
	extractLargestConnectedComponent(g);
	printGraph(g, cout);
}
