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
#include "dkUtils.h"

// Please see dkUtils.h for explanation of what these all do

std::ostream& operator<<(std::ostream& out, const NKMap& m) {
	for (NKMap::const_iterator i = m.begin(); i != m.end(); ++i) {
		out << i->first << " " << i->second << std::endl;
	}
	return out;
}

std::ostream& operator<<(std::ostream& out, const NKKMap& m) {
	for (NKKMap::const_iterator i = m.begin(); i != m.end(); ++i) {
		for (NKMap::const_iterator j = i->second.begin(); j != i->second.end();
				++j) {
			out << i->first << " " << j->first << " " << j->second << std::endl;
		}
	}
	return out;
}

std::ostream& operator<<(std::ostream& out, const PKMap& m) {
	for (PKMap::const_iterator i = m.begin(); i != m.end(); ++i) {
		out << i->first << " " << i->second << std::endl;
	}
	return out;
}

std::ostream& operator<<(std::ostream& out, const PKKMap& m) {
	for (PKKMap::const_iterator i = m.begin(); i != m.end(); ++i) {
		for (PKMap::const_iterator j = i->second.begin(); j != i->second.end();
				++j) {
			out << i->first << " " << j->first << " " << j->second << std::endl;
		}
	}
	return out;
}

unsigned int dkCalcNumNodes(const NKMap& nkmap) {
	unsigned int cnt = 0;

	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		cnt += (unsigned int) i->second;
	}

	return cnt;
}

// returns true on success, false
bool read1kDistribution(const std::string& fileName, NKMap& degNumNodesMap) {
	std::ifstream inFile;
	inFile.open(fileName.c_str(), std::ios::in);
	if (!inFile.is_open()) {
		std::cerr << "Could not open " << fileName << std::endl;
		return false;
	}

	int totalNodeCount_check = 0;
	char inLine[1024];
	int degree;
	int numNodesWithDegree;
	int numStubs = 0;
	unsigned int lineNumber = 0;

	degNumNodesMap.clear();

	// First line contains the number of nodes
	while (!inFile.eof()) {
		lineNumber++;

		inFile.getline(inLine, 1024);
		if (inFile.fail() && !inFile.eof()) {
			std::cerr << "Error - long line, # " << lineNumber << std::endl;
			return false;
		}
		int scanfResult = sscanf(inLine, "%d %d", &degree, &numNodesWithDegree);
		if (scanfResult == EOF
			)
			break;
		if (scanfResult != 2) {
			std::cerr << "Error(2) on line: " << inLine << ", line #"
					<< lineNumber << std::endl;
			continue;
		}

		if (degNumNodesMap.find(degree) != degNumNodesMap.end()) {
			std::cerr << "Repeated degree: " << degree << " line # "
					<< lineNumber << std::endl;
			return false;
		}

		degNumNodesMap[degree] = numNodesWithDegree;
		totalNodeCount_check += numNodesWithDegree;
		numStubs += (numNodesWithDegree * degree);
	}

	return true;
}

// hiepnh Oct 1, 2015
// returns true on success, false
//bool read1kSequence(const std::string& fileName, int degSeq[]) {
bool read1kSequence(const std::string& fileName, std::vector<int>& degSeq) {
	std::ifstream inFile;
	inFile.open(fileName.c_str(), std::ios::in);
	if (!inFile.is_open()) {
		std::cerr << "Could not open " << fileName << std::endl;
		return false;
	}

	char inLine[10];
	int degree;
	unsigned int lineNumber = 0;

	while (!inFile.eof()) {

		inFile.getline(inLine, 10);
		if (inFile.fail() && !inFile.eof()) {
			std::cerr << "Error - long line, # " << lineNumber+1 << std::endl;
			return false;
		}

		int scanfResult = sscanf(inLine, "%d", &degree);
//		std::cerr << "lineNumber " << lineNumber << " deg " << degree << std::endl;

		if (scanfResult == EOF)
			break;
//		if (scanfResult != 2) {
//			std::cerr << "Error(2) on line: " << inLine << ", line #"
//					<< lineNumber << std::endl;
//			continue;
//		}
		degSeq[lineNumber] = degree;

		lineNumber++;
	}

	return true;
}

// hiepnh Nov 3, 2015
bool readFreeStubList(const std::string& fileName, StubList& freeStubList) {
	std::ifstream inFile;
	inFile.open(fileName.c_str(), std::ios::in);
	if (!inFile.is_open()) {
		std::cerr << "Could not open " << fileName << std::endl;
		return false;
	}

	char inLine[50];
	int nodeid;
	int degree;
	unsigned int lineNumber = 0;

	while (!inFile.eof()) {

		inFile.getline(inLine, 50);
		if (inFile.fail() && !inFile.eof()) {
			std::cerr << "Error - long line, # " << lineNumber+1 << std::endl;
			return false;
		}

		int scanfResult = sscanf(inLine, "%d %d", &nodeid, &degree);
//		std::cerr << "lineNumber " << lineNumber << " deg " << degree << std::endl;

		if (scanfResult == EOF)
			break;
//		if (scanfResult != 2) {
//			std::cerr << "Error(2) on line: " << inLine << ", line #"
//					<< lineNumber << std::endl;
//			continue;
//		}
		Stub stub;
		stub.nodeid = nodeid;
		stub.degree = degree;

		freeStubList.push_back(stub);

		lineNumber++;
	}

	return true;
}


// Read a 2k distribution off disk, store in the nkk map
bool read2kDistribution(const std::string& filename, NKKMap& nkkmap) {
	std::ifstream inFile;
	char inLine[1024];
	unsigned int lineNumber = 0;

	inFile.open(filename.c_str(), std::ios::in);
	if (!inFile.is_open()) {
		std::cerr << "Could not open " << filename << std::endl;
		return false;
	}

	nkkmap.clear();
	while (!inFile.eof()) {
		lineNumber++;

		int deg1, deg2, numEdges;
		inFile.getline(inLine, 1024);
		if (inFile.fail() && !inFile.eof()) {
			std::cerr << "Error - long line, #" << lineNumber << std::endl;
			return false;
		}
		int scanfResult = sscanf(inLine, "%d %d %d", &deg1, &deg2, &numEdges);

		if (scanfResult != EOF)
		{
			if (scanfResult != 3) {
				std::cerr << "Bad line: " << inLine << ", line #" << lineNumber
						<< std::endl;
				return false;
			}

			nkkmap[deg1][deg2] = numEdges;
			nkkmap[deg2][deg1] = numEdges; // fix non-symmetric graphs dists
		}
	}
	return true;
}

// Various utilities for converting between nk and pk.  Useful in
// rescaling.  nkmap maps degrees -> # of nodes with that degree
void NKToPK(const NKMap& nkmap, PKMap& pkmap, int numNodes) {
	pkmap.clear();

	// Calculate total number of nodes if not specified by usera
	if (numNodes == 0) {
		for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
			// degree = i->first
			// # nodes with degree = i->second
			numNodes += i->second;
		}
	}
	if (numNodes == 0)
		return;

	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		int degree = i->first;
		int numNodesWithDegree = i->second;

		pkmap[degree] = float(numNodesWithDegree) / float(numNodes);
	}
}

void NKKToPKK(const NKKMap& nkkmap, PKKMap& pkkmap) {
	int numEdges = dkkCalcNumEdges(nkkmap);

	// Count the number of total edges in the graph
	for (NKKMap::const_iterator nkkIter = nkkmap.begin();
			nkkIter != nkkmap.end(); ++nkkIter) {
		int degree1 = nkkIter->first;
		const NKMap& nkmap = nkkIter->second;

		for (NKMap::const_iterator nkIter = nkmap.begin();
				nkIter != nkmap.end(); ++nkIter) {
			int degree2 = nkIter->first;
			int edges = nkIter->second;

			if (degree1 == degree2)
				pkkmap[degree1][degree2] = float(edges) / (float) numEdges;
			else
				pkkmap[degree1][degree2] = float(edges)
						/ ((float) numEdges * 2);
		}
	}
}

void NKKToNK(const NKKMap& nkkmap, NKMap& nkmap) {
	nkmap.clear();

	for (NKKMap::const_iterator i = nkkmap.begin(); i != nkkmap.end(); ++i) {
		int degree1 = i->first;
		const NKMap& tmpMap = i->second;
		int totalEdgeCount = 0;

		for (NKMap::const_iterator j = tmpMap.begin(); j != tmpMap.end(); ++j) {
			int degree2 = j->first;
			int numEdges = j->second;
			totalEdgeCount += numEdges;

			// XXX put htis back in
			if (degree1 == degree2)
				totalEdgeCount += numEdges; // count double
		}

		int degEdges = (int) round(float(totalEdgeCount) / float(degree1));
		nkmap[degree1] = degEdges;
	}
}

void PKKToPK(const PKKMap& pkkmap, PKMap& pkmap, float averageDegree) {
	pkmap.clear();
	for (PKKMap::const_iterator i = pkkmap.begin(); i != pkkmap.end(); ++i) {
		int degree1 = i->first;
		const PKMap& tmp = i->second;
		float sum = 0;

		for (PKMap::const_iterator j = tmp.begin(); j != tmp.end(); ++j) {
			float pr = j->second;
			sum += pr;
		}
		sum = sum * float(averageDegree) / float(degree1);
		pkmap[degree1] = sum;
	}
}

// Count the number of total edges in the graph
unsigned int dkkCalcNumEdges(const NKKMap& nkkmap) {
	unsigned int numEdges = 0;
	for (NKKMap::const_iterator nkkIter = nkkmap.begin();
			nkkIter != nkkmap.end(); ++nkkIter) {
		int degree1 = nkkIter->first;
		const NKMap& nkmap = nkkIter->second;

		for (NKMap::const_iterator nkIter = nkmap.begin();
				nkIter != nkmap.end(); ++nkIter) {
			int degree2 = nkIter->first;
			if (degree1 == degree2)
				numEdges += 2 * (nkIter->second);
			else
				numEdges += (nkIter->second);
		}
	}
	return ((int) ceil(numEdges / 2));
}

void PKKToNKK(const PKKMap& pkkmap, NKKMap& nkkmap, int numEdges) {
	nkkmap.clear();

	for (PKKMap::const_iterator i = pkkmap.begin(); i != pkkmap.end(); ++i) {
		int degree1 = i->first;
		const PKMap& tmpMap = i->second;

		for (PKMap::const_iterator j = tmpMap.begin(); j != tmpMap.end(); ++j) {
			int degree2 = j->first;
			float pr = j->second;
			int nk;

			if (degree1 == degree2)
				nk = int(round(pr * numEdges));
			else

				nk = int(round(pr * 2.0 * numEdges));

			if (nk > 0)
				nkkmap[degree1][degree2] = nk;
		}
	}
}

float dkCalcAverageDegree(const NKMap& nkmap) {
	int totalNodes = 0;
	int count = 0;

	for (NKMap::const_iterator i = nkmap.begin(); i != nkmap.end(); ++i) {
		int degree = i->first;
		int numNodes = i->second;

		totalNodes += numNodes;
		count += degree * numNodes;
	}
	return float(count) / float(totalNodes);
}
