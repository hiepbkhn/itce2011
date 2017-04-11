/*
 * set-cover.cpp
 *
 *  Created on: Apr 10, 2017
 *      Author: Administrator
 */

#include <string>
#include <iostream>
#include <fstream>
#include <map>
#include <vector>
#include <unordered_set>

#include "../src/tuple.h"
#include "../src/helper.h"


using namespace std;

//
void greedy_set_cover(vector<unordered_set<int>>& S, int num_element){
	vector<unordered_set<int>> tempS;

    // compute U
	__int64 start = Timer::get_millisec();
    int marked[num_element] = {0};
    for (unordered_set<int> a_set : S){
    	tempS.push_back(a_set);
        for (int item : a_set)
            marked[item] = 1;
    }

    unordered_set<int> U;
    for (int item = 0; item < num_element; item++)
    	if (marked[item] == 1)
    		U.insert(item);
    cout<<"len(S) = " << S.size()<<endl;
    cout<<"len(U) = " << U.size()<<endl;
    cout<<"Compute U - elapsed : " << (Timer::get_millisec() - start) <<endl;

    //
    vector<unordered_set<int>> U_S;
	for (int u = 0; u < num_element; u++)
		U_S.push_back(unordered_set<int>());
	for (int i = 0; i < S.size(); i++)
		for (int u : S[i])
			U_S[u].insert(i);

    //
    unordered_set<int> R = U;	// = new Hashunordered_set<int>(U); // deep copy

    vector<unordered_set<int>> C_0;

    //
    start = Timer::get_millisec();

    int S_size = S.size();
    while (R.size() != 0){
        int max_inter_id = -1;
        int max_set_size = 0;
        for (int i = 0; i < S_size; i++){
        	if (max_set_size < tempS[i].size()){
        		max_set_size = tempS[i].size();
        		max_inter_id = i;
        	}
        }

        if (max_set_size == 0)
            break;

        C_0.push_back(S[max_inter_id]);

        unordered_set<int> removed = WeightedSetCover::set_intersect(R, tempS[max_inter_id]);

        R = WeightedSetCover::set_differ(R, removed);		//

        for (int u : removed)
        	for (int i : U_S[u])
//        		if (tempS[i].size() > 0)
        			tempS[i] = WeightedSetCover::set_differ(tempS[i], removed);


    }
    cout<<"S_size = "<<S_size<<endl;

    cout<<"len(R) = " << R.size() <<endl;
    cout<<"Cover.len: " << C_0.size()<<endl;
    int num_cloaked_users = U.size() - R.size();

    int total_C_0 = 0;
    // CHECK
    for (unordered_set<int> a_set : C_0){
    	total_C_0 += a_set.size();
    	for (int item : a_set)
    		marked[item] = 0;
    }
    bool is_cover = true;
	for (int i = 0; i < num_element; i++)
		if (marked[i] == 1){
			is_cover = false;
			break;
		}
	if (!is_cover)
		cout<<"C_0 is NOT a cover"<<endl;
	else
		cout<<"C_0 is a cover"<<endl;
    cout<<"Total elements in C_0 = " << total_C_0<<endl;
}

// similar to greedy_set_cover but use heap for max.item lookup
void fast_set_cover(vector<unordered_set<int>>& S, int num_element){
	int S_size = S.size();
	vector<unordered_set<int>> tempS;

    // compute U
	__int64 start = Timer::get_millisec();
    int marked[num_element] = {0};

    int heap_idx[S_size] = {0};
    int temp_size[S_size] = {0};

    int i = 0;
    for (unordered_set<int> a_set : S){
    	tempS.push_back(a_set);

    	temp_size[i] = a_set.size();
    	heap_idx[i] = i;
    	i++;

        for (int item : a_set)
            marked[item] = 1;
    }

    unordered_set<int> U;
    for (int item = 0; item < num_element; item++)
    	if (marked[item] == 1)
    		U.insert(item);
    cout<<"len(S) = " << S.size()<<endl;
    cout<<"len(U) = " << U.size()<<endl;
    cout<<"Compute U - elapsed : " << (Timer::get_millisec() - start) <<endl;

    // init heap_idx



    // U_S[u] = set of S_i containing u
    vector<unordered_set<int>> U_S;
	for (int u = 0; u < num_element; u++)
		U_S.push_back(unordered_set<int>());
	for (int i = 0; i < S.size(); i++)
		for (int u : S[i])
			U_S[u].insert(i);

    //
    unordered_set<int> R = U;	// = new Hashunordered_set<int>(U); // deep copy

    vector<unordered_set<int>> C_0;

    //
    start = Timer::get_millisec();


    while (R.size() != 0){
        int max_inter_id = -1;
        int max_set_size = 0;
        // use heap


        //
//        for (int i = 0; i < S_size; i++){
//        	if (max_set_size < tempS[i].size()){
//        		max_set_size = tempS[i].size();
//        		max_inter_id = i;
//        	}
//        }

        if (max_set_size == 0)
            break;

        C_0.push_back(S[max_inter_id]);

        unordered_set<int> removed = WeightedSetCover::set_intersect(R, tempS[max_inter_id]);

        R = WeightedSetCover::set_differ(R, removed);		//

        for (int u : removed)
        	for (int i : U_S[u])
//        		if (tempS[i].size() > 0)
        			tempS[i] = WeightedSetCover::set_differ(tempS[i], removed);


    }
    cout<<"S_size = "<<S_size<<endl;

    cout<<"len(R) = " << R.size() <<endl;
    cout<<"Cover.len: " << C_0.size()<<endl;
    int num_cloaked_users = U.size() - R.size();

    int total_C_0 = 0;
    // CHECK
    for (unordered_set<int> a_set : C_0){
    	total_C_0 += a_set.size();
    	for (int item : a_set)
    		marked[item] = 0;
    }
    bool is_cover = true;
	for (int i = 0; i < num_element; i++)
		if (marked[i] == 1){
			is_cover = false;
			break;
		}
	if (!is_cover)
		cout<<"C_0 is NOT a cover"<<endl;
	else
		cout<<"C_0 is a cover"<<endl;
    cout<<"Total elements in C_0 = " << total_C_0<<endl;
}

//// bipartite graph (S, U)
void max_min_set_cover(vector<unordered_set<int>>& S, int num_element){

    // compute U
	__int64 start = Timer::get_millisec();
    int marked[num_element] = {0};
    int count[num_element] = {0};
    for (unordered_set<int> a_set : S){
        for (int item : a_set){
            marked[item] = 1;
            count[item] += 1;
        }
    }

    unordered_set<int> U;
    for (int item = 0; item < num_element; item++)
    	if (marked[item] == 1)
    		U.insert(item);
    cout<<"len(S) = " << S.size()<<endl;
    cout<<"len(U) = " << U.size()<<endl;
    cout<<"Compute U - elapsed : " << (Timer::get_millisec() - start) <<endl;

    //
    vector<unordered_set<int>> C_0;

    // U_S
    vector<unordered_set<int>> U_S;
    vector<int> size_U_S;
    for (int u = 0; u < num_element; u++)
    	U_S.push_back(unordered_set<int>());
	for (int i = 0; i < S.size(); i++)
		for (int u : S[i])
			U_S[u].insert(i);
	for (int u = 0; u < num_element; u++)
		size_U_S.push_back(U_S[u].size());
	//
	unordered_set<int> set_id;		// result: indices of selected sets
	while (true){
		// find u that exists in most of the sets
		int max_u = -1;
		int max_size = 0;
		for (int u = 0; u < num_element; u++)
			if (size_U_S[u] > 0)
				if (max_size < size_U_S[u]){
					max_size = size_U_S[u];
					max_u = u;
				}
		// OR min
//		int max_u = -1;
//		int max_size = 1000000;
//		for (int u = 0; u < num_element; u++)
//			if (size_U_S[u] > 0)
//				if (max_size > size_U_S[u]){
//					max_size = size_U_S[u];
//					max_u = u;
//				}

		if (max_u == -1)
			break;
		//
		set_id.insert(U_S[max_u].begin(), U_S[max_u].end());
		//
		for (int i : U_S[max_u])
			for (int u : S[i])
				size_U_S[u] = 0;
	}

    for (int i : set_id)
    	C_0.push_back(S[i]);
    cout<<"Cover.len: " << C_0.size()<<endl;

    //
    int total_C_0 = 0;
    // CHECK
    for (unordered_set<int> a_set : C_0){
    	total_C_0 += a_set.size();
    	for (int item : a_set)
    		marked[item] = 0;
    }
    bool is_cover = true;
    for (int i = 0; i < num_element; i++)
    	if (marked[i] == 1){
    		is_cover = false;
    		break;
    	}
    if (!is_cover)
    	cout<<"C_0 is NOT a cover"<<endl;
    else
    	cout<<"C_0 is a cover"<<endl;
    cout<<"Total elements in C_0 = " << total_C_0<<endl;


}


/////////////////////////
int main() {
	vector<unordered_set<int>> mc_set;

	// read "mesh.out"
	ifstream f("mesh.out");

	string line;
	int mc_set_len = 0;
	while (getline(f, line)){
		vector<string> node_list = Formatter::split(line, ' ');
		if (node_list.size() < 2)
			continue;
		unordered_set<int> set;
		for (string node : node_list)
			set.insert(stoi(node));
		mc_set.push_back(set);
		mc_set_len += set.size();
	}
	f.close();
	cout<<"mc_set_len = "<<mc_set_len<<endl;

	//
	int num_element = 20000;

	__int64 start = 0;

	// call WeightedSetCover::find_init_cover()
//	start = Timer::get_millisec();
//	PairSetListInt ret = WeightedSetCover::find_init_cover(mc_set, num_element);
//	cout<<"cover set - elapsed : " << (Timer::get_millisec() - start) <<endl;
//	cout<<"--------------"<<endl;
//
	// call greedy_set_cover()
	start = Timer::get_millisec();
	greedy_set_cover(mc_set, num_element);
	cout<<"cover set - elapsed : " << (Timer::get_millisec() - start) <<endl;
	mc_set_len = 0;
	for (unordered_set<int> set : mc_set)
		mc_set_len += set.size();
	cout<<"mc_set_len = "<<mc_set_len<<endl;
	cout<<"--------------"<<endl;

	// call fast_set_cover()
	start = Timer::get_millisec();
	max_min_set_cover(mc_set, num_element);
	cout<<"cover set - elapsed : " << (Timer::get_millisec() - start) <<endl;
	mc_set_len = 0;
	for (unordered_set<int> set : mc_set)
		mc_set_len += set.size();
	cout<<"mc_set_len = "<<mc_set_len<<endl;

	//
	return 0;
}


