'''
Created on Mar 6, 2013

@author: Nguyen Huu Hiep
'''

import time
import math
import pp
import option

#SET_LIST_PATH = "D:/workspace-python/mmb_network/out/"
SET_LIST_PATH = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/runtime_new/_temp/"

#
# list_type = "cover_set", "positive_mc_set"
def read_set_list(path, config, list_type, timestamp):
    
    f = open(path + config + "_" + list_type + "_" + str(timestamp) + ".out", "r")
    fstr = f.read()
    f.close()
    
    set_list = []
    for line in fstr.split("\n"):
        if line == "": # EOF
            break
        items = line.split(",")
        items.pop()
        set_list.append(set([int(item) for item in items]))
            

    return set_list    

#
def get_max_id(set_list):
    max_id = 0
    for a_set in set_list:
        for item in a_set:
            if max_id < item:
                max_id = item
    return max_id + 1
            
### PARALLEL
def find_max_uncovered(S, R):
    max_inter_len = 0
    max_inter_id = -1
    for i, s in enumerate(S):
        inter_len = len(s.intersection(R))
        if max_inter_len < inter_len:
            max_inter_len = inter_len
            max_inter_id = i
            
    if max_inter_id == -1:
        return [], -1
    else:                
        return S[max_inter_id], max_inter_id   

#
def truncate_list(S, keep_ratio):
    size = int(math.ceil(len(S) * keep_ratio))
    return list(S[0:size])

# (FOR PUBLIC USE)
# timestamp = 0, S: positive sets, U:universe (computed from S)
def find_init_cover(S, num_element):
    # compute U
    marked = [0] * num_element
    for a_set in S:
        for item in a_set:
            marked[item] = 1
    U = set([item for item in range(num_element) if marked[item] == 1])
    print "len(S) =", len(S)
    print "len(U) =", len(U)
    
    R = U
    
    C_0 = []
    
    # NON-BOOTSTRAP
    # sort S
    start = time.clock()
    
    S = sorted(S, key=lambda a_set:len(a_set), reverse=True)    
    
    S = truncate_list(S, option.INIT_COVER_KEEP_RATIO)
    
    print "Sorting - elapsed :", (time.clock() - start)
    
    start = time.clock()
    while len(R) != 0:
        S_i, max_inter_id = find_max_uncovered(S, R)
        if max_inter_id == -1:
            break
        
        C_0.append(S_i)
        R = R.difference(S_i)
        del S[max_inter_id]
    
    print "find_max_uncovered - elapsed :", (time.clock() - start)
    print "len(R) =", len(R)
    print "Cover.len: ", len(C_0)
    num_cloaked_users = len(U) - len(R)
    print "Total elements in C_0 =", sum(len(a_set) for a_set in C_0)
    
    return C_0, num_cloaked_users            
            
            
# timestamp > 0, S: positive sets, U:universe (compute from S), C_0: cover_set from prev.step
def find_next_cover(S, num_element, C_0, k_global):
    # compute U
    marked = [0] * num_element
    for a_set in S:
        for item in a_set:
            marked[item] = 1
    U = set([item for item in range(num_element) if marked[item] == 1])
    print "len(S) =", len(S)
    print "len(U) =", len(U)
    
    R = U
        
    # result
    C_1 = []
    total_W = 0
    
    # compute weights
    start = time.clock()
    
    list_pairs = []     # list of lists, for MMB/MAB checking
    
    W = [0] * len(S)
    for i, s_set in enumerate(S):
        W[i] = 0
        list_pairs.append([])   #have list_pairs[i]
        for j, c_set in enumerate(C_0):
            inter_len = len(s_set.intersection(c_set))
            if inter_len > 0 and inter_len < k_global:
                W[i] = W[i] + 1
            if inter_len >= k_global:
                list_pairs[i].append(j)
                    
    print "Computing W - elapsed :", (time.clock() - start)   
                
    # sort W
    start = time.clock()
    
    z = sorted(zip(W,S,list_pairs))     #ZIP: simultaneous sorting
    
    W_temp, S_temp, list_pairs_temp = zip(*z)
    i = 0
    while W_temp[i] == 0 and i < len(W_temp):
        i = i + 1
    print "i = ", i
    
    W = list(W_temp[0:i])
    S = list(S_temp[0:i])
    list_pairs = list(list_pairs_temp[0:i])
    
    #updated Mar 6: sort by S (descending)
    z = sorted(zip(S,W,list_pairs), key=lambda a_tuple:len(a_tuple[0]), reverse=True)
    S, W, list_pairs = zip(*z)
    
    S = truncate_list(S, option.NEXT_COVER_KEEP_RATIO)
    W = truncate_list(W, option.NEXT_COVER_KEEP_RATIO)
    list_pairs = truncate_list(list_pairs, option.NEXT_COVER_KEEP_RATIO)
    
    print "Sorting - elapsed :", (time.clock() - start)            
    
    # compute C_1
    start = time.clock()
    
    checking_pairs = []
    while len(R) != 0:
        S_i, min_element = find_max_uncovered(S, R)
        if min_element == -1:
            break
        
        C_1.append(S_i)
        total_W = total_W + W[min_element]
        R = R.difference(S_i)
        
        del S[min_element]
        del W[min_element]
        checking_pairs.append(list_pairs[min_element])  # synchronized with C_1 ! 
        del list_pairs[min_element]
    
    print "find_next_cover - elapsed :", (time.clock() - start)
    print "len(R) =", len(R)
    print "Cover.len: ", len(C_1)
    print "checking_pairs.len: ", len(checking_pairs)
    num_cloaked_users = len(U) - len(R)
    print "Total elements in C_0 =", sum(len(a_set) for a_set in C_0)
    print "Total weight = ", total_W
        
    return C_1, num_cloaked_users, checking_pairs            
            

##
def prepare_universe(S, num_element):
    marked = [0] * num_element
    for a_set in S:
        for item in a_set:
            marked[item] = 1
    U = set([item for item in range(num_element) if marked[item] == 1])
    
    return U        
            
## (sorted) S: set list, U: universe
def parallelized_compute_cover_set(S, U):
    R = U
    C_0 = []
    
    while len(R) != 0:
        S_i, max_inter_id = find_max_uncovered(S, R)
        if max_inter_id == -1:
            break
        
        C_0.append(S_i)
        R = R.difference(S_i)
        del S[max_inter_id]
    
    return C_0    
                
            
###############################################################
if __name__ == '__main__':
    
#    ### 0 ###
#    positive_mc_set = read_set_list(SET_LIST_PATH,
#                             "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300",
#                             "positive_mc_set", 0)
#    
##    print positive_mc_set[0]
#    
#    num_element = get_max_id(positive_mc_set)          
#                
#    #            
#    cover_set, num_cloaked_users = find_init_cover(positive_mc_set, num_element)
#    print "num_cloaked_users = ", num_cloaked_users
#    
#    ### 1 ###
#    positive_mc_set_1 = read_set_list(SET_LIST_PATH,
#                             "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300",
#                             "positive_mc_set", 1)
#    num_element_1 = get_max_id(positive_mc_set_1)
#    
#    cover_set_1, num_cloaked_users_1, checking_pairs = \
#            find_next_cover(positive_mc_set_1, num_element_1, cover_set, option.K_GLOBAL)
#    
#    print "num_cloaked_users = ", num_cloaked_users_1
    
    
    ### PARALLEL PYTHON
    num_groups = 2  # = ncpus
    
    ppservers = ()
    job_server = pp.Server(ncpus = num_groups, ppservers=ppservers)
    print "Starting pp with", job_server.get_ncpus(), "workers"
    
    positive_mc_set = read_set_list(SET_LIST_PATH,
                             "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300",
                             "positive_mc_set", 0)
    num_element = get_max_id(positive_mc_set)   
    
    start_time = time.clock()
    
    #1. prepare S, U
    U = prepare_universe(positive_mc_set, num_element)
    
    S = sorted(positive_mc_set, key=lambda a_set:len(a_set), reverse=True)    
    S = truncate_list(S, option.INIT_COVER_KEEP_RATIO)
    
    ### solution 1
    #2. devide S into sub lists of sets
    sub_set_lists = []
    for i in range(num_groups):
        sub_set_lists.append([])
    for i in range(len(S)):
        sub_set_lists[i % num_groups].append(S[i]) 
    
    #3. submit jobs
    jobs = [job_server.submit(parallelized_compute_cover_set, 
                    (sub_set_list,U,), 
                    (find_max_uncovered,), 
                    ()) for sub_set_list in sub_set_lists]
    
    ### solution 2
#    #2. devide U into sub universes
#    sub_universes = []
#    list_U = list(U)
#    num_items = len(list_U)
#    for i in range(num_groups-1):
#        start = i*num_items/num_groups
#        end = (i+1)*num_items/num_groups
#        sub_universes.append(list_U[start:end])
#    sub_universes.append(list_U[end:num_items])
#    
#    #3. submit jobs
#    jobs = [job_server.submit(parallelized_compute_cover_set, 
#                    (S,set(sub_universe),), 
#                    (find_max_uncovered,), 
#                    ()) for sub_universe in sub_universes]
    
    #4. aggregate 
    results = []
    for job in jobs:
        res = job()
        for a_set in res:
            results.append(a_set)
        print "DONE one sub_set_list !"
    
    
    print "len(results) = ", len(results)
    
#    #5. remove duplicates
#    results = sorted(results, key=lambda a_set:len(a_set), reverse=True)
#    print "sorting results - DONE"
#    print "1 - elapsed : ", (time.clock() - start_time)  
#    
#    i = 0
#    while i < len(results)-1:
#        if len(results[i]) == len(results[i+1]):
#            if len(results[i]) == len(results[i] & results[i+1]): 
#                del results[i+1]
#        else:
#            i += 1
#    
#    print "len(results) AFTER = ", len(results)
    
    #
    print "2 - elapsed : ", (time.clock() - start_time)       
    
    
    
    
    
    
    