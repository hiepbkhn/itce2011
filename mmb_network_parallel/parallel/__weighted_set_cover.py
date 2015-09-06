'''
Created on Dec 11, 2012

greedy 'Weighted Set Cover' algorithm
source: http://stackoverflow.com/questions/7942312/how-do-i-make-my-implementation-of-greedy-set-cover-faster

Dec 21:
    - some ideas implemented after computing weights
    1. use of non-zero W[i] is allowed --> WEIGHTED
    2. no use of non-zero W[i] is not allowed --> UNWEIGHTED

Dec 26:
    - add public functions, used in graph_naiveclique_network.py 
Jan 11:
    - MMB/MAB to find_next_cover()
Mar 06
    - sort S in descending order (init)
    - avoid fluctuations after init step --> keep 90% of init candidate sets, 95% of weighted candidate sets
    
'''

import random
import time
import math
from numpy.ma.core import floor
import option

#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-400-300_positive_mc_set_0.out"
# result: 1684, 4903
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-500-300_positive_mc_set_0.out"
# result: 2923, 7558
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_0.out"
# result: 3685, 8628
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_1.out"
# result: 5469, 8884
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_2.out"
# result: 6225, 9054
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_3.out"
# result: 6743, 9253
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_4.out"
# result: 7162, 9412
#filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_5.out"
# result: 7614, 9523

def read_positive_sets(filepath):
    
    num_element = 11000
    
    f = open(filepath, 'r')
    fstr = f.read()
    f.close()
    
    set_lines = fstr.split("\n")
    set_lines.pop()
    print "len(set_lines)=", len(set_lines)
    
    set_list = []
    for line in set_lines:
        items = line.split(",")
        items.pop()     #remove the last (empty) item
        set_list.append(set([int(item) for item in items]))
    
    marked = [0] * num_element
    for a_set in set_list:
        for item in a_set:
            marked[item] = 1
    
    print "len(marked) =", len(marked)        
    print "num of existing items =", sum(marked)
    
    universe = set([item for item in range(num_element) if marked[item] == 1])
    
    print "len(universe) =", len(universe)
    
    #
    return universe, set_list


# UNWEIGHTED
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

# WEIGHTED
def find_max_weighted_uncovered(S, R, W):
    min_cost = 999999.0
    min_element = -1
    for i, s in enumerate(S):
#        try:
#            cost = W[i]/(len(s.intersection(R)))
#            if cost < min_cost:
#                min_cost = cost
#                minElement = i
#        except:
#            # Division by zero, ignore
#            pass
        
        inter_len = len(s.intersection(R))
        if inter_len > 0:
            cost = W[i]/inter_len
            if cost < min_cost:
                min_cost = cost
                min_element = i
                
                
    if min_element == -1:
        return -1, -1
    else:            
        return S[min_element], W[min_element]


#TRUNCATE (updated Mar 6)
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
    
    S = sorted(S, key=lambda a_set:len(a_set), reverse=True)    #updated Mar 6: reverse=True
    
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


# (FOR PUBLIC USE)
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
    
        
###################################################################
if __name__ == '__main__':
    # U : universal
    # S : input sets
    # w : associated weights
    # R : 
    
#    #1.
#    U = set([1,2,3,4])
#    R = U
#    S = [set([1,2]), 
#         set([1]), 
#         set([1,2,3]), 
#         set([1]), 
#         set([3,4]), 
#         set([4]), 
#         set([1,2]), 
#         set([3,4]), 
#         set([1,2,3,4])]
#    w = [1, 1, 2, 2, 2, 3, 3, 4, 4]
#    # result
#    C = []
#    costs = []


    #2. FOR PROFILING
#    print 'generating test data'    
#    num_sets = 1000
#    set_size = 40
#    elements = range(5000)
#    U = set(elements)
#    R = U
#    S = []
#    for i in range(num_sets):
#        random.shuffle(elements)
#        S.append(set(elements[:set_size]))
#    w = [random.randint(1,100) for i in xrange(num_sets)]
#    # result
#    C = []
#    costs = []
#    
#    while len(R) != 0:
#        S_i, cost = findMin(S, R)
#        if S_i == -1 and cost == -1:
#            break
#        
#        C.append(S_i)
#        costs.append(cost)
#        R = R.difference(S_i)
#    
#    print "Cover: ", C
#    print "Total Cost: ", sum(costs), costs


    #3. FROM MESHCLOAK - POSITIVE SETS
    #3.1 timestamp = 0
    print ">>> TIMESTAMP = 0"
    (U, S) = read_positive_sets(
            "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_0.out")
    R = U
    
    # result
    C_0 = []
    
    # NON-BOOTSTRAP
    # sort S
    start = time.clock()
    
    S = sorted(S, key=lambda a_set:len(a_set))
    print "Sorting - Elapsed :", (time.clock() - start)
    
    start = time.clock()
    while len(R) != 0:
        S_i, max_inter_id = find_max_uncovered(S, R)
        if max_inter_id == -1:
            break
        
        C_0.append(S_i)
        R = R.difference(S_i)
        del S[max_inter_id]
    
    print "Elapsed :", (time.clock() - start)
    print "len(R) =", len(R)
#    print "Cover: ", C_0
    print "Cover.len: ", len(C_0)
    print "Total elements in C_0 =", sum(len(a_set) for a_set in C_0)
    
    
    
    #3.2 timestamp = 1,2,...
    for timestamp in range(1,6):
        print ">>> TIMESTAMP =", timestamp
        (U, S) = read_positive_sets(
                "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_" 
                + str(timestamp) + ".out")
        R = U
        
        # result
        C_1 = []
        total_W = 0
        
        # compute weights
        start = time.clock()
        
        W = [0] * len(S)
        for i, s_set in enumerate(S):
            W[i] = 0
            for c_set in C_0:
                if len(s_set.intersection(c_set)) == 1:
                    W[i] = W[i] + 1
        print "Computing W - Elapsed :", (time.clock() - start)   
                    
        # sort W
        start = time.clock()
        
        z = sorted(zip(W,S))
        W_temp, S_temp = zip(*z)
        i = 0
        while W_temp[i] == 0 and i < len(W_temp):
            i = i + 1
        print "i = ", i
        W = list(W_temp[0:i])
        S = list(S_temp[0:i])
        
#        W = list(W_temp)
#        S = list(S_temp)

        print "Sorting - Elapsed :", (time.clock() - start)            
        
        # compute C_1
        start = time.clock()
        
        while len(R) != 0:
#            S_i, min_element = find_max_weighted_uncovered(S, R, W)    #ERROR: always use W[1] !
            S_i, min_element = find_max_uncovered(S, R)
            if min_element == -1:
                break
            
            C_1.append(S_i)
            total_W = total_W + W[min_element]
            R = R.difference(S_i)
            del S[min_element]
            del W[min_element]
        
        print "Elapsed :", (time.clock() - start)
        print "len(R) =", len(R)
#        print "Cover: ", C_1
        print "Cover.len: ", len(C_1)
        print "Total elements in C_1 =", sum(len(a_set) for a_set in C_1)
        print "Total weight = ", total_W
#        print "Remaining length, len(S)=", len(S), "len(W)=", len(W)

        # save for next step
        C_0 = C_1
