'''
Created on Mar 6, 2015

@author: huunguye
paper: Correlated Network Data Publication via Differential Privacy (PVLDB'14)

!!!NOTICE: 
    - matrix C offset 1 (1-based indexing)
    - count_1() uses Geometric mechanism
    - partition() uses adaptive step size (step_size[]) to boost speed and reduce score_list 
    - partition() uses [min_area =  float(n_nodes*n_nodes)/(4**(u.level+2))] instead of u.level+1 for deeper partitioning
    - arrange_area() adds LOW_DENSITY check
'''

import time
import itertools
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
from adj_matrix_visualizer import draw_adjacency_matrix
from graph_summarization import geometric_mechanism

cbr2 = 2.0**(1/3.0)     # cube root of 2
step_size = [16,8,4,2,1,1,1,1,1,1,1,1,1,1,1]    # used in partition() for large graphs (n_nodes ~ 5k)
LARGE_AREA = 40000

class Node:
    def __init__(self, y1=0, y2=0, x1=0, x2=0, level=0):
        # all are inclusive
        self.y1 = y1  # top
        self.y2 = y2
        self.x1 = x1  # left
        self.x2 = x2
        #
        self.children = [None, None, None, None] # four quadrants
#        self.noisy_count = 0.0   # number of 1-cells, Laplace
        self.noisy_count = 0     # number of 1-cells, Geometric
        self.level = level
        self.is_leaf = False
        self.stop_cond = 0      # 1,2,3 stop condition
        
########################################################
# combination
def nCr(n,r):
    if n == 0:
        return 1
    f = math.factorial
    return f(n) / f(r) / f(n-r)

########################################################
# combination
def nCr_log(n,r):
    if n == 0:
        return 0.0
    s = 0.0
    for i in range(n-r):
        s += math.log(float(n-i)/(n-r-i))
    return s

########################################################
def manhattan_distance(G, L):
    n_nodes = G.number_of_nodes()
    q = 0.0
    for e in G.edges_iter():
        i = L[e[0]]
        j = L[e[1]]
        q += abs(i-n_nodes/2)+ abs(j-n_nodes/2)
    
    q = q/(n_nodes-2)    
    #
    return q

#######################################################
def identify_vertex_labeling_nonprivate(G, n_trials, n_swaps):
    n_nodes = G.number_of_nodes()
    
    # generate a random vertex labeling L
#    L = range(n_nodes)
    L = random.permutation(range(n_nodes))
#    print "sum(L) =", sum(L)
    
    min_dist = manhattan_distance(G, L)
    for i in range(n_trials):
        # generate a candidate set C of swaps
        C = []
        marks = [0 for _ in range(n_nodes)] # ensure a node appears at most 1 time
        L2 = random.permutation(range(n_nodes))
        for j in range(n_swaps):
            u = L2[j]
            v = L2[j]
            if u != v and marks[u] < 1 and marks[v] < 1:
                marks[u] += 1
                marks[v] += 1
                C.append((u,v))
#        print "len(C) =", len(C)
        # check noisy count
        L_temp = [] 
        L_temp.extend(L)
        for (u,v) in C:
            L_temp[u] = v
            L_temp[v] = u
#        print "sum(L_temp) =", sum(L_temp)
            
        q = manhattan_distance(G, L_temp)    
        if q < min_dist:
            min_dist = q
            L = L_temp
            
    # re-label vertices of G
    G_temp = nx.Graph()
    G_temp.add_nodes_from(range(n_nodes))
    for e in G.edges_iter():
        G_temp.add_edge(L[e[0]], L[e[1]])
          
    #
    return G_temp, L 

#######################################################
def compute_h(n_nodes, mu, eps):
    
    RHS = (cbr2 - 1)*n_nodes*n_nodes*eps / (mu*sqrt(2)*2)
#    print RHS
    h = 1
    while True:
#        print cbr2* 2**(h*2) - (2**(h*5/3.0))
        if cbr2* 2**(h*2) - (2**(h*5/3.0)) > RHS:
            break
        h += 1
    #
    return h
    
#######################################################
# count summary matrix C
def compute_C(A):
    n_nodes = A.shape[0]
    C = np.zeros((n_nodes+1, n_nodes+1))
    for i in range(n_nodes):
        for j in range(n_nodes):
            C[i+1,j+1] = C[i,j+1] + C[i+1,j] - C[i,j] + A[i,j]      # C: 1-based indexing
    #
    return C            

#######################################################
# y1,y2,x1,x2: all are inclusive
def density(C, y1,y2,x1,x2):
    return (C[y2+1,x2+1] - C[y2+1,x1] - C[y1,x2+1] + C[y1,x1])/((y2-y1+1)*(x2-x1+1))    # C: 1-based indexing

#######################################################
# y1,y2,x1,x2: all are inclusive
def count_1(C, y1,y2,x1,x2,eps_cu):
    # LAPLACE 
#    value = C[y2+1,x2+1] - C[y2+1,x1] - C[y1,x2+1] + C[y1,x1] + random.laplace(0.0, 1.0/eps_cu)
#    # truncated
#    if value < 0.0:
#        value = 0.0
#    if value > (y2-y1+1)*(x2-x1+1):
#        value = float((y2-y1+1)*(x2-x1+1))

    # GEOMETRIC
    alpha = math.exp(-eps_cu)
    value = int(C[y2+1,x2+1] - C[y2+1,x1] - C[y1,x2+1] + C[y1,x1]) + geometric_mechanism(alpha, 1)      # integer, C: 1-based indexing
    # truncated
    if value < 0:
        value = 0
    if value > (y2-y1+1)*(x2-x1+1):
        value = (y2-y1+1)*(x2-x1+1)
    #
    return value

#######################################################
# partition non-leaf node u
def partition(u, eps_p, C, n_nodes, h, use_step=False):   
    score_list = [] # score for each splitting point p
#    GSq = 2.0* 4**(u.level+1)/(n_nodes*n_nodes)     # max sensitivity, 2
    GSq = 1.0* 4**(u.level+1)/(n_nodes*n_nodes)     # max sensitivity, 1 
    min_area =  float(n_nodes*n_nodes)/(4**(u.level+2))     # u.level+1
#    print "GSq =", GSq
#    print "min_area =", min_area
    
    step = 1
    if use_step:
        step = step_size[u.level]
    for y in range(u.y1+1, u.y2, step):
        for x in range(u.x1+1, u.x2, step):
            # constraint of region area
            if (y+1-u.y1)*(x+1-u.x1) < min_area or (y+1-u.y1)*(u.x2-x) < min_area or \
                (u.y2-y)*(x+1-u.x1) < min_area or (u.y2-y)*(u.x2-x) < min_area:
                continue
            
            density_list = [density(C, u.y1, y, u.x1, x), density(C, u.y1, y, x+1, u.x2),
                            density(C, y+1, u.y2, u.x1, x), density(C, y+1, u.y2, x+1, u.x2)]
            q = max(density_list) - min(density_list)
            score_list.append([y,x, math.exp(eps_p/(2*h*GSq)*q)] )     # (y,x) for exponential mechanism

    if len(score_list) == 0:
        return (0,0) 

    # normalize score_list
    sum_q = sum(item[2] for item in score_list)
#    print "sum_q =", sum_q
    for item in score_list:
        item[2] = item[2]/sum_q
        
    # exponential mechanism
    rand_val = random.random()
    cur_val = 0.0
    i = 0
    while True:
        if (i == len(score_list)-1) or (cur_val + score_list[i][2] > rand_val):
            break
        cur_val += score_list[i][2]
        i += 1
     
    # return 
    y = score_list[i][0]
    x = score_list[i][1]
    
    return (y,x)

#######################################################
def print_QT(QT):
    print QT.level, " [", QT.y1, QT.y2, QT.x1, QT.x2, "]", QT.noisy_count, QT.is_leaf
    if QT.children[0] is not None:
        print_QT(QT.children[0])
        print_QT(QT.children[1])
        print_QT(QT.children[2])
        print_QT(QT.children[3])
        
#######################################################
def print_QT_leaves(QT):
    if QT.is_leaf:
        print QT.level, QT.stop_cond, " [", QT.y1, QT.y2, QT.x1, QT.x2, "]", QT.noisy_count
        
    if QT.children[0] is not None:
        print_QT_leaves(QT.children[0])
        print_QT_leaves(QT.children[1])
        print_QT_leaves(QT.children[2])
        print_QT_leaves(QT.children[3])    

#######################################################
# quadtree
# eps_c (count), eps_p (partition)
def explore_dense_region(G, A, C, mu, eps_c, eps_p, use_step=False):
    n_nodes = G.number_of_nodes()
    
    # quad-tree
    QT = Node(0,n_nodes-1,0,n_nodes-1)  # QT.level = 0
    QT.noisy_count = C[n_nodes, n_nodes]                    # C: 1-based indexing
    
#    h = math.log(n_nodes)/math.log(2) # log2 of n_nodes
    h = compute_h(n_nodes, mu, eps_c)
    print "h =", h
    
    queue = [QT]
    count = 0
    while len(queue) > 0:
        u = queue.pop(0)
        count += 1
        if count % 10 == 0:
            print "# areas processed :", count
            
        #
        if u.level == h:
            u.is_leaf = True
            continue
            
        # check 2 stop conditions
        density_u = float(u.noisy_count)/((u.y2-u.y1+1)*(u.x2-u.x1+1))     # density(C, u.y1, u.y2, u.x1, u.x2)
        if density_u >= 0.8:
            u.is_leaf = True
            u.stop_cond = 1
            print "1st cond meet at u"
            continue
        
        if u.noisy_count < 0.8*n_nodes*n_nodes/(4**h):
            u.is_leaf = True
            print "2nd cond meet at u"
            u.stop_cond = 2
            continue
        
        # calculate eps_cu, eps_pu
        next_level = u.level + 1
        eps_cu = cbr2**next_level * (cbr2-1) * eps_c/(cbr2**(h+1) - 1)      # after Cormode's paper
        eps_pu = eps_p/h
        (y,x) = partition(u, eps_pu, C, n_nodes, h, use_step)                 # call partition()
        
        if y == 0 and x == 0:   # len(score_list) == 0
            u.is_leaf = True
            print "3rd cond meet at u"
            u.stop_cond = 3
            continue
        
        # four subregions
        s1 = Node(u.y1, y, u.x1, x, u.level+1)
        s1.noisy_count = count_1(C, u.y1, y, u.x1, x, eps_cu)
        s2 = Node(u.y1, y, x+1, u.x2, u.level+1)
        s2.noisy_count = count_1(C, u.y1, y, x+1, u.x2, eps_cu)
        s3 = Node(y+1, u.y2, u.x1, x, u.level+1)
        s3.noisy_count = count_1(C, y+1, u.y2, u.x1, x, eps_cu)
        s4 = Node(y+1, u.y2, x+1, u.x2, u.level+1)
        s4.noisy_count = count_1(C, y+1, u.y2, x+1, u.x2, eps_cu)
        
        u.children = [s1, s2, s3, s4]
        
        # add to queue
        queue.append(s1)
        queue.append(s2)
        queue.append(s3)
        queue.append(s4)
    #
    return QT

#######################################################
def random_arrange(u, m, l, c1):
    result = []
    list_1 = np.random.permutation(m*l)
    for i in list_1[0:c1]:
        y = u.y1 + i / l
        x = u.x1 + i % l
        result.append((y,x))
    #
    return result 

#######################################################
# arrange the area covered by u
# c: true count (computed from C), c1: noisy count
# return: list of 1-locs
def arrange_area(A, c, u, epsA):
    print u.level, u.stop_cond, " [", u.y1, u.y2, u.x1, u.x2, "]", u.noisy_count,
    
    # random if edge density is low
    m = u.y2-u.y1+1
    l = u.x2-u.x1+1
    c1 = int(u.noisy_count)
    
    if c1 == 0:
        print
        return []
    
#    if float(u.noisy_count)/(m*l) < LOW_DENSITY:
#        print float(u.noisy_count)/(m*l)
#        return random_arrange(u, m, l, c1) 
#    else:
#        print
    
    # otherwise
    GSq = 1.0   # 2.0
    lower_s = max([c1+c-m*l, m*l-c-c1])
    upper_s = max([m*l+c-c1, m*l+c1-c])
    # DEBUG
#    print "c =", c
#    print "c1 =", c1
#    print "m,l =", m, l
#    print lower_s, upper_s
    
    if m*l > LARGE_AREA:
        print "large", m*l
        return random_arrange(u, m, l, c1) 
    else:
        print
    
    score_list = []
    max_logGs = 0.0
    for s in range(lower_s, upper_s+1):
        if (s+c+c1-m*l) % 2 == 0:       # must be even
            if (s+c+c1-m*l)/2 >= 0 and (m*l+c1-s-c)/2 >= 0 and (s+c+c1-m*l)/2 <= c and (m*l+c1-s-c)/2 <= m*l-c:
#                Gs = math.log(nCr(c,(s+c+c1-m*l)/2) * nCr(m*l-c,(m*l+c1-s-c)/2))        # store log(Gs)
                Gs = nCr_log(c,(s+c+c1-m*l)/2) + nCr_log(m*l-c,(m*l+c1-s-c)/2)
                if max_logGs < Gs:
                    max_logGs = Gs
#                score_list.append([s, math.exp(s*epsA/(2*GSq)) * Gs])                   # may encounter OverflowError
                score_list.append([s, math.exp((s-upper_s)*epsA/(2*GSq)), Gs])  # Gs up to 1760 digits !   
            
#    print "score_list =", score_list
    # normalize score_list
    for item in score_list:
        item[2] = math.exp(item[2]-max_logGs)       
    sum_q = sum(item[1]*item[2] for item in score_list)         # item[1] * exp(log(Gs)-max_logGs), sum_q ~ 0.0 (Underflow !)
    for item in score_list:
        item[1] = item[1]/sum_q
    
#    print "after NORM, score_list =", score_list
        
    # exponential mechanism
    rand_val = random.random()
    cur_val = 0.0
    i = 0
    while True:
        if (i == len(score_list)-1) or (cur_val + score_list[i][1] > rand_val):
            break
        cur_val += score_list[i][1]
        i += 1
     
    s = score_list[i][0]
        
    # list of 1 locs, 0 locs
    loc_1 = []
    loc_0 = []
    for i in range(u.y1, u.y2+1):
        for j in range(u.x1, u.x2+1):
            if A[i,j] == 1.0:
                loc_1.append((i,j))
            else:
                loc_0.append((i,j))

    assert len(loc_1) == c
    #
    n1T = (s+c+c1-m*l)/2    # for Aij=1
    n1F = (m*l+c1-c-s)/2    # for Aij=0
    result = [] # list of 1 locs
    if c > 0:
        list_1T = np.random.permutation(c)
        for i in range(n1T):
            result.append(loc_1[list_1T[i]])
    if c < m*l:
        list_1F = np.random.permutation(m*l-c)
        for i in range(n1F):
            result.append(loc_0[list_1F[i]])
        
    # 
    return result 

#######################################################
def arrange_edge(n_nodes, A, C, QT, epsA):
    loc1 = []
    queue = [QT]
    count = 0
    while len(queue) > 0:
        u = queue.pop(0)
        c = int(C[u.y2+1,u.x2+1] - C[u.y2+1,u.x1] - C[u.y1,u.x2+1] + C[u.y1,u.x1])      # C: 1-based indexing
        
        if u.is_leaf:
            count += 1
            if count % 10 == 0:
                print "# areas arranged :", count
            result = arrange_area(A, c, u, epsA)                # call arrange_area()
            loc1.extend(result)    # edges
        #
        else:
            queue.append(u.children[0])
            queue.append(u.children[1])
            queue.append(u.children[2])
            queue.append(u.children[3])

    # post-process
    aG = nx.Graph()
    aG.add_nodes_from(range(n_nodes))
    e_list = []
    for (u,v) in loc1:
        if u != v:                      # no self-loops
            if random.random() < 0.5:
                e_list.append((u,v))
    aG.add_edges_from(e_list)
    
    #
    return aG

#######################################################
if __name__ == '__main__':
    
    # TOY graph
    G = nx.Graph()
##    G.add_edges_from([(0,1),(0,3),(1,2),(1,3)])
#    G.add_edges_from([(0,5),(0,6),(0,7),(1,5),(1,6),(1,7),(2,6),(2,7),(2,4),(3,4)]) # example in the paper 
    
#    filename = "../_data/polbooks.gr"       # (105,441), h=4 --> hard to re-label
#    filename = "../_data/polblogs.gr"       # DIRECTED, undirected (1224,16715) 
    filename = "../_data/as20graph.gr"      # (6474,12572), h=10, , w/o relabel: compute_C (75s), ~3070 nodes, explore(720s), arrange_edge(74s)
#    filename = "../_data/ca-HepPh.gr"                                         
                                            
#    filename = "../_data/er_200_02.gr"      # (200,4002), h=5 --> OK
#    filename = "../_data/er_1000_005.gr"      # (1000,24912), h=8 --> OK, w/o relabel: explore(37s), ~8720 nodes, arrange_edge(15s)
                                                #                         w relabel: explore(36s), ~10340 nodes, arrange_edge(10s)
#    filename = "../_data/sm_1000_005_11.gr"      # (1000,5000), h=8, --> OK, w/o relabel: explore(24s) ~900 nodes, arrange_edge(14s)
                                                #                             w relabel: explore(31s), ~2510 nodes, arrange_edge(7s)
#    filename = "../_data/ba_1000_5.gr"      # (1000,4975), h=8, --> OK, w/o relabel: explore(32s) ~2740 nodes, arrange_edge(5s) 
                                            #                            w relabel: explore(33s), ~2550 nodes, arrange_edge(5s)
                                            
                                            
#    out_file = "../_sample/polbooks_der_10_10_10"
    out_file = "../_sample/as20graph_der_10_10_10"    # 10 (eps_c=1.0), 10 (eps_p=1.0), 10 (epsA=1.0)

    
    print "filename =", filename
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    LOW_DENSITY = 0.5*2.0*G.number_of_edges()/(G.number_of_nodes()*G.number_of_nodes())
    print "LOW_DENSITY =", LOW_DENSITY
    
    # TEST noisy_count()
#    q = noisy_count(G, range(G.number_of_nodes()))
#    print q
#    L = random.permutation(range(G.number_of_nodes()))
#    print "L =", L
#    q = noisy_count(G, L)
#    print q

    # TEST identify_vertex_labeling_nonprivate()
#    start = time.clock()
#    G, L = identify_vertex_labeling_nonprivate(G, 100, 20)
#    print "identify_vertex_labeling_nonprivate - DONE, elapsed", time.clock() - start
#    print "#nodes =", G.number_of_nodes()
#    print "#edges =", G.number_of_edges()
##    print "permutation L:", L
#    draw_adjacency_matrix(G)
    
    
    # TEST compute_h()
#    h = compute_h(1000, 5, 1.0)
#    print "h =", h
    
    # TEST compute_C(), density()
    start = time.clock()
    A = nx.adj_matrix(G, None, None)
    print "compute A - DONE, elapsed", time.clock() - start
#    print A

    start = time.clock()
    C = compute_C(A)
    print "compute_C - DONE, elapsed", time.clock() - start
#    print C
#    print density(C, 3, 6, 3, 5)    # 1/6
    
    # TEST arrange_area()
#    u = Node(3, 4, 0, 4)
#    u.noisy_count = 2   # 2, 0, 5
#    result = arrange_area(A, 3, u, 1.0)
#    print "result =", result 

    # TEST explore_dense_region()
    n_samples = 1
    count = 0
    for _ in range(n_samples):
        print "sample", count
        start = time.clock()
#        QT = explore_dense_region(G, A, C, mu=5, eps_c=1.0, eps_p=1.0, use_step=False)   # for sm_1000_005_11, er_1000_005, ba_1000_5
        QT = explore_dense_region(G, A, C, mu=5, eps_c=1.0, eps_p=1.0, use_step=True)   # for as20graph
        print "explore_dense_region - DONE, elapsed", time.clock() - start
    #    print_QT(QT)
        
        print "LEAF NODES"
        print_QT_leaves(QT)
        
        # TEST arrange_edge()
#        print "arrange_edge - START"
#        start = time.clock()
#        aG = arrange_edge(G.number_of_nodes(), A, C, QT, epsA=1.0)
#        print "arrange_edge - DONE, elapsed", time.clock() - start
#        print "#nodes =", aG.number_of_nodes()
#        print "#edges =", aG.number_of_edges()
#        
#        nx.write_edgelist(aG, out_file + "." + str(count), '#', '\t', False, 'utf-8')
        
        count += 1

    
    
    