/*
 * vector-set-op.cpp
 *	- extracted from mmb_network_cpp
 *
 *  Created on: Mar 28, 2017
 *      Author: Administrator
 */

#include <vector>
#include <set>
#include <unordered_set>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <algorithm>
#include <chrono>

using namespace std;
using namespace chrono;

////
class PairSetInt {
public:
	set<int> s;
	int i;

	//
	PairSetInt(set<int> _s, int _i) {
		s = _s;
		i = _i;
	}
};

////
struct TSetComparator {
	bool operator()(set<int> s1, set<int> s2) {
		return (s1.size() < s2.size());
	}
};

class Timer{
public:
	static __int64 get_millisec(){
		__int64 now = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
		return now;
	}
};

//
vector<string> split(const string &s, char delim) {
	vector<string> elems;

	stringstream ss;
	ss.str(s);
	string item;
	while (getline(ss, item, delim))
		elems.push_back(item);

	return elems;
}

//
vector<set<int>> read_positive_mc_set(string filename){
	vector<set<int>> ret;

	ifstream fin(filename);
	string line;
	while (getline(fin, line)){
		vector<string> items = split(line, ' ');
		set<int> s;
		for(vector<string>::iterator item = items.begin(); item != items.end(); item++)
			s.insert(stoi(*item));

		ret.insert(ret.end(), s);
	}

	//
	return ret;
}

//
set<int> set_intersect(set<int>& s1, set<int>& s2){
	set<int> ret;
	set_intersection(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(ret, ret.begin()));
	return ret;
}

//
set<int> set_differ(set<int>& s1, set<int>& s2){
	set<int> ret;
	set_difference(s1.begin(),s1.end(),s2.begin(),s2.end(), inserter(ret, ret.begin()));
	return ret;
}

//
PairSetInt find_max_uncovered(vector<set<int>>& S, int S_size, set<int>& R) {
	int max_inter_len = 0;
	int max_inter_id = -1;
	for (int i = 0; i < S_size; i++) {	// OLD: S.size()
		// branch and bound
		if (S[i].size() <= max_inter_len)
			continue;

		int inter_len = set_intersect(S[i], R).size();

		if (max_inter_len < inter_len) {
			max_inter_len = inter_len;
			max_inter_id = i;
		}
	}

	if (max_inter_id == -1)
		return PairSetInt(set<int>(), -1);
	else
		return PairSetInt(S[max_inter_id], max_inter_id);
}

void find_init_cover(vector<set<int>>& S, int num_element){
	// compute U
	__int64 start = Timer::get_millisec();
	int marked[num_element] = {0};
	for (set<int> a_set : S)
		for (int item : a_set)
			marked[item] = 1;

	set<int> U;
	for (int item = 0; item < num_element; item++)
		if (marked[item] == 1)
			U.insert(item);
	cout<<"len(S) = " << S.size()<<endl;
	cout<<"len(U) = " << U.size()<<endl;
	cout<<"Compute U - elapsed : " << (Timer::get_millisec() - start) <<endl;

	//
	set<int> R = U;	// = new Hashset<int>(U); // deep copy

	vector<set<int>> C_0;

	// NON-BOOTSTRAP
	// sort S
	start = Timer::get_millisec();

	//
	TSetComparator SetComparator;
	sort(S.begin(), S.end(), SetComparator);	// sort ascending by .size()

	cout<<"Sorting - elapsed : " << (Timer::get_millisec() - start) <<endl;

	//
	start = Timer::get_millisec();

	int S_size = S.size();				// we consider only sets with id from 0->S_size-1
	while (R.size() != 0){
		PairSetInt temp = find_max_uncovered(S, S_size, R);		// 100: S_size
		set<int> S_i = temp.s;
		int max_inter_id = temp.i;
		if (max_inter_id == -1)
			break;

		C_0.push_back(S_i);
//	        R.removeAll(S_i);			// set difference
		R = set_differ(R, S_i);		//

//	        S.erase(S.begin() + max_inter_id);		// COSTLY ?

		set<int> tmp_set = S[max_inter_id];
		S[max_inter_id] = S[S_size-1];
		S[S_size-1] = tmp_set;
		S_size = S_size - 1;
	}

	cout<<"find_init_cover - elapsed : " << (Timer::get_millisec() - start) <<endl;
	cout<<"len(R) = " << R.size() <<endl;
	cout<<"Cover.len: " << C_0.size()<<endl;
	int num_cloaked_users = U.size() - R.size();

	int total_C_0 = 0;
	for (set<int> a_set : C_0)
		total_C_0 += a_set.size();
	cout<<"Total elements in C_0 = " << total_C_0<<endl;

}

//////////////////////////
int main(){

	__int64 start = Timer::get_millisec();
	vector<set<int>> positive_mc_set = read_positive_mc_set("D:/github/itce2011/mmb_network_cpp/positive_mc_set.0");
	cout<<"read_positive_mc_set - elapsed : " << (Timer::get_millisec() - start) <<endl;

	start = Timer::get_millisec();
	int num_elements = 5000;
	find_init_cover(positive_mc_set, num_elements);
	cout<<"find_init_cover - elapsed : " << (Timer::get_millisec() - start) <<endl;

	return 0;
}


