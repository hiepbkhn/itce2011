'''
Created on Jun 2, 2014

@author: huunguye
- implement the paper "Injecting Uncertainty in Graphs for Identity Obfuscation" (VLDB'12)
- globally central anonymization

Apr 8     
    - update uniqueness()
    - add compute_X_sparse(), compute_Y_sparse(), compute_entropy_sparse()        
Apr 16
    - add compute_eps_multi(), check_X_and_Y()
    - fix compute_Y_sparse()
Jun 2
    - convert to igraph
'''

import time
import random
import math
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np

N_TRIES = 3

#######################################################
def compute_deg_index(G):
    deg_dict = {}       # deg_dict: mark 0/1
    deg_count = {}      # deg_count[d] = number of nodes of degree d
    for deg_u in G.degree(G.vs):
        if not deg_dict.has_key(deg_u):
            deg_dict[deg_u] = 1
            deg_count[deg_u] = 1
        else:
            deg_count[deg_u] += 1
    #
    deg_index = {}
    i = 0
    for deg_u in deg_dict:
        deg_index[deg_u] = i
        i += 1
    #
    return deg_index, deg_count
    

#######################################################
def compute_X_dense(G, n_nodes, max_deg):
    
    node_list = G.nodes()

    X = [[0.0 for j in range(max_deg+1)] for i in range(n_nodes)]
    
    for i in range(n_nodes):    # for each node v
        v = node_list[i]
        adj_list = G.neighbors(v)   # list of v's neighbors
#        print adj_list

        
        # use two rows
        if len(adj_list) == 0:      # special case: deg[v] = 0
            X[i] = [0.0 for j in range(max_deg+1)]
            X[i][0] = 1.0
            continue
        
        d = [[0.0 for j in range(max_deg+1)] for l in range(2)]     # two rows
        # first neighbor (edge)
        p = G.es[G.get_eid(v, adj_list[0])]['p']         
        d[0][0] = 1.0 - p
        d[0][1] = p
        
        for l in range(2,len(adj_list)+1):
            p = G.es[G.get_eid(v, adj_list[l-1])]['p']
            d[1][0] = d[0][0]*(1.0-p)     # column j=0  
            for j in range(1,l+1):
                d[1][j] = d[0][j-1]*p + d[0][j]*(1.0-p)
            # copy    
            for j in range(l+1):
                d[0][j] = d[1][j]
        #
        for j in range(max_deg+1):
            X[i][j] = d[0][j]
            
    #
    return X

#######################################################
def compute_X_sparse(G, n_nodes, max_deg, deg_list, deg_limit=5000):
    
    node_list = [u.index for u in G.vs]

    # 1. Scipy sparse matrix
#    row = []
#    col = []
#    data = []
#    for v in node_list:
#        for j in range(deg_list[v]+1):
#            row.append(v)
#            col.append(j)
#            data.append(0.0)
#    X = sp.coo_matrix( (np.array(data),(np.array(row), np.array(col))), shape=(n_nodes, max_deg+1)).tolil()    # lil_matrix()
#    print X[0][0]
#    print "X.dtype =", X.dtype
#    X_size = X.data.nbytes + X.rows.nbytes
#    print "X_size =", X_size

    # 2. list of lists
    X = [[0.0 for j in range(deg_list[v]+1)] for v in range(n_nodes)]
    
    #
    start = time.clock()
    for i in range(n_nodes):    # for each node v
        v = node_list[i]
        adj_list = G.neighbors(v)   # list of v's neighbors
        
        # use two rows
        if len(adj_list) == 0:      # special case: deg[v] = 0
            for j in range(deg_list[v]+1):
                X[i][j] = 0.0
            X[i][0] = 1.0
            continue
        # use deg_limit
        if len(adj_list) > deg_limit:
            for j in range(deg_list[v]+1):
                X[i][j] = 1.0/(deg_list[v]+1)
            continue
        
        #
        d = [[0.0 for j in range(deg_list[v]+1)] for l in range(2)]     # two rows
        
        # first neighbor (edge)
        p = G.es[G.get_eid(v, adj_list[0])]['p']         
        d[0][0] = 1.0 - p
        d[0][1] = p
        
        for l in range(2,len(adj_list)+1):
            p = G.es[G.get_eid(v, adj_list[l-1])]['p']
            d[1][0] = d[0][0]*(1.0-p)     # column j=0  
            for j in range(1,l+1):
                d[1][j] = d[0][j-1]*p + d[0][j]*(1.0-p)
            # copy    
            for j in range(l+1):
                d[0][j] = d[1][j]
        #
        for j in range(deg_list[v]+1):
            X[i][j] = d[0][j]
    
        # DEBUG - timing
#        if i % 1000 == 0:
#            print i, " elapsed :", time.clock() - start
#            start = time.clock()
    #
#    for i in range(n_nodes):
#        print X[i]
    
    #
    return X
    
#######################################################
def compute_Y_dense(X, n_nodes, max_deg):
    Y = [[0.0 for j in range(max_deg+1)] for i in range(n_nodes)]
    # row-based
    for j in range(1,max_deg+1):
        s = 0.0
        for i in range(n_nodes):    # sum of the column
            s += X[i][j]
        for i in range(n_nodes):
            if s > 0:
                Y[i][j] = X[i][j]/s
            else:
                Y[i][j] = -1.0/n_nodes  # mark to remove this column from ent[]
            
    #
    return Y

#######################################################
# X: sparse
def compute_Y_sparse(X, n_nodes, max_deg, deg_list):
    Y = [[0.0 for j in range(deg_list[i]+1)] for i in range(n_nodes)]

    # item-based
    s = [0.0 for j in range(max_deg+1)]
    for i in range(n_nodes):
        for j in range(deg_list[i]+1):
            s[j] += X[i][j]
    #
    for i in range(n_nodes):
        for j in range(deg_list[i]+1):
            if s[j] > 0:
                Y[i][j] = X[i][j]/s[j]
            else:
                Y[i][j] = -1.0/n_nodes  # mark to remove this column from ent[]
            
    #
    return Y

#######################################################
# ent[deg]: entropy of deg
def compute_entropy_dense(Y, n_nodes, max_deg):
    ent = [0.0 for j in range(max_deg+1)]
    for j in range(1,max_deg+1):
        if Y[0][j] < 0:
            ent[j] = 0.0            # marked column in Y (see compute_Y())
        else:
            s = 0.0
            for i in range(n_nodes):
                if Y[i][j] > 0.000001:
                    s += -Y[i][j]*math.log(Y[i][j],2)
            #
            ent[j] = s
    #
    return ent   

#######################################################
# Y: sparse
def compute_entropy_sparse(Y, n_nodes, max_deg, deg_list):
    ent = [0.0 for j in range(max_deg+1)]
    
    for i in range(n_nodes):
        for j in range(deg_list[i]+1):
            if Y[i][j] > 0.000001:
                ent[j] += -Y[i][j]*math.log(Y[i][j],2)

    for i in range(n_nodes):
        for j in range(deg_list[i]+1):
            if Y[i][j] < 0:
                ent[j] = 0.0    # marked column in Y (see compute_Y())
#                ent[j] = math.log(n_nodes,2)        # or set to MAX entropy !
        
        
    #
    return ent   


#######################################################
def compute_X(G, n_nodes, max_deg, deg_list):
    
#    return compute_X_dense(G, n_nodes, max_deg)
    return compute_X_sparse(G, n_nodes, max_deg, deg_list)

#######################################################
def compute_Y(X, n_nodes, max_deg, deg_list):
    
#    return compute_Y_dense(X, n_nodes, max_deg)
    return compute_Y_sparse(X, n_nodes, max_deg, deg_list)

#######################################################
def compute_entropy(Y, n_nodes, max_deg, deg_list):
    
#    return compute_entropy_dense(Y, n_nodes, max_deg)
    return compute_entropy_sparse(Y, n_nodes, max_deg, deg_list)

#######################################################
# check X and Y: sum row(X) = 1, sum col(Y) = 1
def check_X_and_Y(X, Y, n_nodes, max_deg, deg_list):
    # check X
    print "check X..."
    for i in range(n_nodes):
        s = 0.0
        for j in range(deg_list[i]+1):
            s += X[i][j]
        if abs(s-1.0) > 0.001:
            print "error at row i=", i
    
    # check Y
    print "check Y..."
    s = [0.0 for j in range(max_deg+1)]
    for i in range(n_nodes):
        for j in range(deg_list[i]+1):
            s[j] += Y[i][j]
    for j in range(max_deg+1):        
        if s[j] > 0 and abs(s[j]-1.0) > 0.001:
            print "error at col j=", j

#######################################################
# unique[deg]
def uniqueness(sigma, G, max_deg, deg_index, deg_count):
    common = {}
    unique = {}
    for i in deg_index.iterkeys():
        s = 0.0
        for (deg, count) in deg_count.iteritems():
            d = abs(i-deg)
            s += count * 1.0/(math.sqrt(2*math.pi)*sigma) * math.exp(-(d*d)/(2*sigma*sigma))
        #
        common[i] = s
        if common[i] > 0:
            unique[i] = 1.0/common[i]
        else:
            unique[i] = 1e10
    #
    return unique


#######################################################
def compute_eps(G, G2, k):
    max_deg_G2 = G2.maxdegree()
    print "max_deg_G2 =", max_deg_G2
    deg_list = G.degree(G.vs)     # G not G2
    deg_list_G2 = G2.degree(G2.vs)  
    
    X = compute_X(G2, G2.vcount(), max_deg_G2, deg_list_G2)
    Y = compute_Y(X, G2.vcount(), max_deg_G2, deg_list_G2)
    ent = compute_entropy(Y, G2.vcount(), max_deg_G2, deg_list_G2)
    print "len(ent) =", len(ent)
    
    num_violated = 0
    LOG2K = math.log(k,2)
    print "LOG2K =", LOG2K
    for deg in deg_list:   # check the original graph
        if ent[deg] > 0.0 and ent[deg] < LOG2K:   # do not check zero-column of ent
            num_violated += 1
    # check and update eps_min (if ok)
    eps2 = float(num_violated)/G2.vcount()
    
    #
    return eps2

#######################################################
def compute_eps_multi(G, G2, k_arr):
    max_deg_G2 = G2.maxdegree()
    print "max_deg_G2 =", max_deg_G2
    deg_list = G.degree(G.vs)     # G not G2
    deg_list_G2 = G2.degree(G.vs)     
    
    X = compute_X(G2, G2.vcount(), max_deg_G2, deg_list_G2)
    Y = compute_Y(X, G2.vcount(), max_deg_G2, deg_list_G2)
    # check X, Y
    check_X_and_Y(X, Y, G2.vcount(), max_deg_G2, deg_list_G2)
    
    ent = compute_entropy(Y, G2.vcount(), max_deg_G2, deg_list_G2)
    print "len(ent) =", len(ent)
#    print "entropy =", ent
    
    eps_arr = []
    for k in k_arr:
        num_violated = 0
        LOG2K = math.log(k,2)
#        print "LOG2K =", LOG2K
        for deg in deg_list:   # check the original graph
            if deg <= max_deg_G2:               # in the case of max_deg_G2 < max_deg_G
                if ent[deg] > 0.0 and ent[deg] < LOG2K:   # do not check zero-column of ent
                    num_violated += 1
        # check and update eps_min (if ok)
        eps2 = float(num_violated)/G2.vcount()
    
        eps_arr.append(eps2)
    #
    return eps_arr
 

#######################################################
#  
def sample_on_list(L, sum_list):      # binary search
    val = random.random()
#    print "val =", val
    #
    lo = 0
    hi = len(sum_list)
    if val >= sum_list[-1]:
        return L[-1]
    
    while True:
        if lo+1 == hi:
            mid = lo
            break
        mid = (lo + hi)/2
#        print "mid, lo, hi", mid, lo, hi
        
        if sum_list[mid] == val:
            break
        if sum_list[mid] < val:
            lo = mid
        if sum_list[mid] > val:
            hi = mid
    #
    return L[mid] 
    
       


#######################################################
# G: original graph
def generate_obfuscation(G, sigma, k, eps, c, q):
    
    deg_list = G.degree(G.vs)         # dict[node] = deg
    n_nodes = G.vcount()
    max_deg = max(deg_list)
    
    #
    print "START - compute and sort uniqueness"
    deg_index, deg_count = compute_deg_index(G)
    
    unique = uniqueness(sigma, G, max_deg, deg_index, deg_count)   # uniqueness for degs
    unique_list = []
    for v in range(n_nodes): 
        deg = deg_list[v]
        unique_list.append((v, unique[deg]))    # tuples (v,unique[deg]) for sorting
        
    # sort unique_list (descending by uniqueness)
    unique_list = sorted(unique_list, key=lambda pair:pair[1], reverse=True)
    
    print "compute and sort uniqueness: DONE"
    
    # build H, V\H
    H = []
    V_H = []
    n_discard = int(math.ceil(eps*n_nodes))
    for i in range(n_discard):
        H.append(unique_list[i][0])
    for i in range(n_discard, n_nodes):
        V_H.append(unique_list[i][0])
    
    print "build H, V_H: DONE"
    print "len(H) :", len(H)
    print "len(V_H) :", len(V_H)
    
    # compute Q
    Q = []
    sum_list = [0.0]
    for u in V_H:
        Q.append(u)
        sum_list.append(unique[deg_list[u]])
    # normalize sum_list
    for i in range(1,len(sum_list)):
        sum_list[i] = sum_list[i-1] + sum_list[i]
    for i in range(0,len(sum_list)):
        sum_list[i] = sum_list[i]/sum_list[-1]  # sum_list[-1] is the total
    print "compute Q, sum_list: DONE"
#    print Q
    print sum_list[1:10], sum_list[-1]
    
    # 
    eps_min = 1e10     # infinity
    G_min = ig.Graph()
    
    for t in range(N_TRIES):    # try N_TRIES times
        start = time.clock()
        print "try: t =", t
        
        EC = {}
        E = {}
        for e in G.es:
            u = e.source
            v = e.target
            E[(u,v)] = 1
            E[(v,u)] = 1
            EC[(u,v)] = 1
            EC[(v,u)] = 1
        print "len(E) =", len(E)
        
        # sample from V_H according to Q
        count = 0
        while len(EC) < c*len(E):         # 
            u = sample_on_list(Q, sum_list)
            v = sample_on_list(Q, sum_list)
            if u == v:
                continue
            if (u,v) in E:
                if (u,v) in EC:
                    del EC[(u,v)]
                    del EC[(v,u)]
            else:
                EC[(u,v)] = 1
                EC[(v,u)] = 1
                
#            count += 1
#            if count % 100 == 0:
#                print "count =", count
#                print "len(EC) =", len(EC)
            
        print "FINISH: len(EC) =", len(EC)
        
        # re-distribute Uncertainty
        EC_list = []    # keep 1 edge for a pair (u,v),(v,u) in EC
        for (u,v) in EC.iterkeys():
            if u < v:   
                EC_list.append((u,v))
        
        unique_edge_list = []
        unique_edge_sum = 0.0
        for (u,v) in EC_list:
            unique_edge = (unique[deg_list[u]] + unique[deg_list[v]])/2     # formula (7)
            unique_edge_list.append([u,v,unique_edge])
            unique_edge_sum += unique_edge
        #
        for i in range(len(unique_edge_list)):
            unique_edge_list[i][2] = sigma * len(EC)/2 * unique_edge_list[i][2]/unique_edge_sum     # formula (7) 
            
        #
        G2 = ig.Graph()
        G2.add_vertices(n_nodes)
        print "G2.number_of_nodes() =", G2.vcount() 
        edge_list_G2 = []
        p_list_G2 = []
            
        # draw r_e for each edge
        print "len(unique_edge_list) =", len(unique_edge_list)
        for i in range(len(unique_edge_list)):
            val = random.random()
            if val < q:
                r_e = random.random()   # uniform from [0,1]
            else:
                while True:
                    r_e = random.gauss(0,unique_edge_list[i][2])    #
                    if r_e >= 0.0 and r_e <= 1.0:
                        break
            #
            u = unique_edge_list[i][0]
            v = unique_edge_list[i][1]
            if (u,v) in E:
#                G2.add_edge(u, v)
#                G2.es[G2.get_eid(u,v)]['p'] = 1-r_e     # very SLOW !
                edge_list_G2.append((u,v))
                p_list_G2.append(1-r_e)
            else:
#                G2.add_edge(u, v)
#                G2.es[G2.get_eid(u,v)]['p'] = r_e       # very SLOW !
                edge_list_G2.append((u,v))
                p_list_G2.append(r_e)
        
        G2.add_edges(edge_list_G2)
        e_id = 0    
        for e in G2.es:    
            e['p'] = p_list_G2[e_id]
            e_id += 1 
        
        # count the number of violated nodes 
        print "prepare to call compute_eps()"
        eps2 = compute_eps(G, G2, k) 
        print "eps2 =", eps2
        
        if eps2 <= eps and eps2 < eps_min:
            eps_min = eps2
            G_min = G2
        elif t == 0:        # save the first try (t=0)
            G_min = G2
        
        print "Elapsed ", (time.clock() - start)
    # end of N_TRIES
    
    #
    return eps_min, G_min    
        
        
#######################################################
def post_processing_attack(G_min, out_file):    
    aG = ig.Graph()
    aG.add_vertices(G_min.vcount())
    
    edge_list_aG = []
    for e in G_min.es:
        if e['p'] > 0.5:
            edge_list_aG.append((e.source, e.target))
    aG.add_edges(edge_list_aG)
    #
    aG.write_edgelist(out_file)
    
    
#######################################################
def sanitize(G):
    # remove self-loops
#    remove_list = []
#    for e in G.edges_iter():    
#        if e[0] == e[1]:
#            remove_list.append((e[0],e[1]))
#    for (u,v) in remove_list:
#        G.remove_edge(u,v)
        
    # remove zero-deg nodes --> NOT NECESSARY
#    remove_list = []
#    for n in G.nodes_iter():
#        if G.degree(n) == 0:
#            remove_list.append(n)
#    for n in remove_list:
#        G.remove_node(n)
    
    # assign weights to edges
    n_edges = G.ecount()
    G.es['p'] = [1.0 for _ in range(n_edges)]

    #
    return G

#######################################################
if __name__ == '__main__':
    
#    # init graph
#    N_NODES = 100
#    N_NEW_EDGES = 5
#    G = nx.barabasi_albert_graph(N_NODES, N_NEW_EDGES)
#    # assign probabilities (weights) to edges
#    weights = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
#    n_weight = len(weights)
#    for e in G.edges_iter():
#        G.edge[e[0]][e[1]]['p'] = weights[random.randint(0,n_weight-1)]
#     
#    # write to file
#    nx.write_edgelist(G, "../data/ba-weighted-100-5.gr", None, '\t', True)  # True: write weights to file 
         
    # read from file
#    G = nx.read_edgelist("../data/ba-weighted-100-5.gr", '#', '\t', None, nodetype=int)
         
    # SAMPLE
#    G = ig.Graph()
#    G.add_vertices(4)
#    G.add_edges([(0,1),(0,2),(0,3),(1,2),(1,3)])
#    G.es[0]['p'] = 0.7
#    G.es[1]['p'] = 0.9
#    G.es[2]['p'] = 0.8
#    G.es[3]['p'] = 0.8
#    G.es[4]['p'] = 0.1

#    print nx.nodes(G)
#    print nx.degree(G)
    
#    X = compute_X(G)
#    Y = compute_Y(X, G.number_of_nodes())
#    ent = compute_entropy(Y, G.number_of_nodes())
#    unique = uniqueness(1.0, G)
#    
#    print "X", X
#    print "Y", Y
#    print "deg entropy", ent
#    print "log_2(3) =", math.log(3,2)
#    print "unique =", unique
    
    # TEST generate_obfuscation()
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-GrQc.txt"         # (3s) 5,242 nodes, 28,980 edges, after sanitization: 5,242 nodes, 14,484 edges
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-HepPh.txt"         # (90s) 12,008 nodes, 237,010 edges, after sanitiz   12008 nodes, 118489
#    file_name = "C:/Tailieu/Paper-code/DATA-SET/SNAP/Social networks/facebook_combined.txt"     # 4,039 nodes, 88,234 edges, p=0.0108
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)   
    
    
#    file_name = "er_200_02"
#    file_name = "er_500_02"
#    file_name = "er_1000_02"
#    file_name = "er_1000_01"
#    file_name = "er_1000_005"
#    file_name = "er_1000_001"
#    file_name = "er_2000_005"
#    file_name = "er_5000_005"
#    file_name = "er_5000_0025"
#    file_name = "er_5000_001"
#    file_name = "er_5000_0005"
#    file_name = "er_10000_0005"
#    file_name = "er_10000_0002"
#    file_name = "er_10000_0001"        #10k nodes
#    file_name = "er_20000_0002"
#    file_name = "er_20000_00005"
#    file_name = "er_100000_00001"       #100k nodes

#    file_name = "sm_10000_005_11"       #10k nodes
#    file_name = "sm_100000_005_11"       #100k nodes

#    file_name = "ba_100000_5"       #100k nodes

#    file_name = "ff_1000_05"           # 3455 edges

    ## real graphs
    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
#    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  

    #
    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    G = sanitize(G)
    
    print "#nodes :", G.vcount()
    print "#edges :", G.ecount()
    print "#components :", len(G.clusters("weak"))
    n_nodes = G.vcount()
    deg_list = G.degree(G.vs)         # list[node] = deg
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    
    # export deg_list to MATLAB
#    print "export deg_list to MATLAB"
#    scipy.io.savemat("D:/" + file_name + "__deg_list.mat", dict(node_list=np.array(range(n_nodes)), deg_list=np.array(deg_list)))
    
    
    # TEST compute_X(), compute_Y(), compute_entropy(), uniqueness()
#    start = time.clock()
#    print "START compute_X"         # 10s for dblp, 162s for youtube (deg_limit = 5000)
#    X = compute_X(G, n_nodes, max_deg, deg_list)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START compute_Y"         # 1.6s for dblp, 6.3s for youtube
#    Y = compute_Y(X, n_nodes, max_deg, deg_list)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START compute_entropy"   # 0.5s for dblp, 1.5s for youtube
#    ent = compute_entropy(Y, n_nodes, max_deg, deg_list)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START uniqueness"        # 0.02s for dblp, 0.5s for youtube
#    deg_index, deg_count = compute_deg_index(G)
#    unique = uniqueness(0.01, G, max_deg, deg_index, deg_count)
#    print "Elapsed ", (time.clock() - start)
    
    # TEST generate_obfuscation()
#    print "START generate_obfuscation"
#    (eps_min, G_min) = generate_obfuscation(G, sigma=0.0001, k=50, eps=0.1, c=2, q=0.01)
#    
##    (eps_min, G_min) = generate_obfuscation(G, sigma=0.1, k=50, eps=0.1, c=2, q=0.01)
##    (eps_min, G_min) = generate_obfuscation(G, sigma=0.5, k=50, eps=0.1, c=2, q=0.01)
##    (eps_min, G_min) = generate_obfuscation(G, sigma=1.0, k=50, eps=0.01, c=2, q=0.01)
#    print "eps_min", eps_min
#    
#    # save uncertain graph for generate_sample (see: graph_generator.py)
#    out_file = "../out/" + file_name + "_entropy_00001_2_001.out"
#    G_min.write_edgelist(out_file)
#    
#    # post-processing attack: rounding
#    out_file = "../out/" + file_name + "_entropy_post_00001_2_001.out"
#    post_processing_attack(G_min, out_file)
#    print "post_processing_attack : DONE"
    
    
    # TEST compute eps of original graph
#    print "START compute_eps"
#    eps = compute_eps(G, G, k=50)      # k=10, 50
#    print "eps", eps
