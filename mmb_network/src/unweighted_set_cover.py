'''
Created on Dec 11, 2012

@author: Nguyen Huu Hiep

'''

import random
import time
import scipy.io
from numpy import *


def read_positive_sets():
    #filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-400-300_positive_mc_set_0.out"
    # result: 1684, 4903
    #filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-500-300_positive_mc_set_0.out"
    # result: 2923, 7558
    filepath = "C:/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001-600-300_positive_mc_set_0.out"
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
        items.pop()
        set_list.append(set([int(item) for item in items]))
    
    
    marked = [0] * num_element
    for a_set in set_list:
        for item in a_set:
            marked[item] = 1
    
    print "len(marked) =", len(marked)        
    print "num of existing items =", sum(marked)
    
    universe = set([item for item in range(num_element) if marked[item] == 1])
    
    print "len(universe) =", len(universe)
    
    # save to .mat (MATLAB)file
#    C = []
#    for a_set in set_list:
#        C.append(array(list(a_set), dtype=int16))
#    L = range(1,len(set_list)+1)
#       
#    scipy.io.savemat('C:/oldenburgGen_positive_sets.mat', dict(C=C, L=L))
#    print "save to MAT file: DONE"
    
    #
    return universe, set_list
    

def test_save_to_matlab():
#    x = np.linspace(0, 2 * np.pi, 100)
#    y = np.cos(x)
#    
#    print x
#    print len(x)
#    print type(x)
#    print x.dtype, y.dtype
#    
#    scipy.io.savemat('C:/test.mat', dict(x=x, y=y))

    arr1 = array(list(set([1, 3, 4])), dtype=int16)
    arr2 = array(list(set([1, 2, 3, 6])), dtype=int16)
    scipy.io.savemat('C:/test.mat', dict(arr=[arr1, arr2])) # save to cells ^^ (list of array)



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
#    # result
#    C = []

    #2. FOR PROFILING
#    print 'generating test data'    
#    num_sets = 6000
#    set_size = 20
#    elements = range(10000)
#    U = set(elements)
#    R = U
#    S = []
#    for i in range(num_sets):
#        random.shuffle(elements)
#        S.append(set(elements[:set_size]))
#    # result
#    C = []
#    
#    start = time.clock()
#    while len(R) != 0:
#        S_i, max_inter_id = find_max_uncovered(S, R)
#        if max_inter_id == -1:
#            break
#        
#        C.append(S_i)
#        R = R.difference(S_i)
#    
#    print "Elapsed :", (time.clock() - start)
#    print "len(R) =", len(R)
##    print "Cover: ", C
#    print "Cover.len: ", len(C)

    #3. FROM MESHCLOAK - POSITIVE SETS
    (U, S) = read_positive_sets()
    R = U
    
    # result
    C = []
    
    #3.1 NON-BOOTSTRAP
    # sort S
    start = time.clock()
    
    S = sorted(S, key=lambda a_set:len(a_set))
    print "Sorting - Elapsed :", (time.clock() - start)
    
    start = time.clock()
    while len(R) != 0:
        S_i, max_inter_id = find_max_uncovered(S, R)
        if max_inter_id == -1:
            break
        
        C.append(S_i)
        R = R.difference(S_i)
        del S[max_inter_id]
    
    print "Elapsed :", (time.clock() - start)
    print "len(R) =", len(R)
#    print "Cover: ", C
    print "Cover.len: ", len(C)
    print "Total elements in C =", sum(len(a_set) for a_set in C)


    #3.2 BOOTSTRAP
#    # sort S
#    start = time.clock()
#    
#    S = sorted(S, key=lambda a_set:len(a_set))
#    print "Sorting - Elapsed :", (time.clock() - start)
#    
#    start = time.clock()
#    k_boot = 500    #100,200,300,500
#    step = len(S)/k_boot
#    for i in range(k_boot):
#        S_i = S[i*step]
#        C.append(S_i)
#        R = R.difference(S_i)
#    
#    #
#    while len(R) != 0:
#        S_i, max_inter_id = find_max_uncovered(S, R)
#        if max_inter_id == -1:
#            break
#        
#        C.append(S_i)
#        R = R.difference(S_i)
#        del S[max_inter_id]
#    
#    print "Elapsed :", (time.clock() - start)
#    print "len(R) =", len(R)
##    print "Cover: ", C
#    print "Cover.len: ", len(C)
#    print "Total elements in C =", sum(len(a_set) for a_set in C)


    #4. TEST test_save_to_matlab()
#    test_save_to_matlab()

