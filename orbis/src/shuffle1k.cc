/*
 * shuffle1k.cc
 *
 *  Created on: Nov 3, 2015
 *      Author: huunguye
 */

#include <math.h>
#include <assert.h>
#include <sys/time.h>
#include <iostream>
#include <fstream>
#include <map>
#include <vector>

#include <boost/program_options.hpp>
#include "dkUtils.h"

using namespace boost;
using namespace std;
namespace po = boost::program_options;

int main(int argc, char *argv[]) {

	string opt_infile;
	int opt_n_nodes;
	int opt_seed;
	int opt_rewireCount = 0;

	// Declare supported options
	po::options_description desc_mandatory("Mandatory options");
	desc_mandatory.add_options()("input,i", po::value<string>(&opt_infile),
			"input degree sequence file");
	desc_mandatory.add_options()("numnode,n", po::value<int>(&opt_n_nodes),
				"number of nodes");

	po::options_description desc_optional("Optional options");
	desc_optional.add_options()("help,h", "produce help message");

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

	// read degree sequence
	std::vector<int> degSeq(opt_n_nodes);
	if (!read1kSequence(opt_infile, degSeq)) {
		cerr << "Could not read 1k sequence:" << opt_infile << endl;
		return 1;
	}

	// compute freeStubList and shuffle it
	StubList freeStubList;
	for (int i = 0; i < opt_n_nodes; i++) {
		int degree = degSeq[i];
		for (int k = 0; k < degree; k++) {
			Stub stub;
			stub.nodeid = i;
			stub.degree = degree;

			freeStubList.push_back(stub);
		}
	}
	dkShuffleList(freeStubList);
	std::cerr <<"freeStubList.size = " << freeStubList.size() << std::endl;

	// write freeStubList to .stub file (for Java Orbis)
	for (StubList::iterator i = freeStubList.begin();	i != freeStubList.end(); ++i) {
		std::cout << i->nodeid << " " << i->degree << std::endl;
	}

	return 0;
}



