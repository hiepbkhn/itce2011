'''
Created on Mar 19, 2014

@author: huunguye

Jan 14 2015    
    - convert to directed: G.degree() -> G.out_degree(), Graph() -> DiGraph()
    - DIRECTED in prepare_QP_MPS_file()
    - added in-degree constraints in prepare_QP_MPS_file()
'''

#from cvxopt import matrix, spmatrix, solvers
import cvxopt       # avoid "from..import", because of Parallel Python
import cvxopt.solvers
import time
import random
import math
import networkx as nx
import scipy.io
import numpy as np

# degree_constraint
DEG_OUT = 0
DEG_INOUT = 1

#######################################################
def test():
    Q = 2*cvxopt.matrix([ [2, .5], [.5, 1] ])
    p = cvxopt.matrix([1.0, 1.0])
    G = cvxopt.matrix([[-1.0,0.0],[0.0,-1.0]])
    h = cvxopt.matrix([0.0,0.0])
    A = cvxopt.matrix([1.0, 1.0], (1,2))
    b = cvxopt.matrix(1.0)
    
    print Q
    print p
    print G
    print h
    print A
    print b
    
    sol=cvxopt.solvers.qp(Q, p, G, h, A, b)
#    sol=cvxopt.solvers.qp(Q, p, G, h, A, b, 'mosek')    # MOSEK not installed

    print "sol =", sol['x']


#######################################################
def random_new_edges(G, n_new):
    aG = nx.DiGraph()
    aG.add_edges_from(G.edges_iter())
    
    n_nodes = G.number_of_nodes()
    node_list = [u for u in G.nodes_iter()]
    new_edges = []
    #
    count = 0
    while count < n_new:
        while True:
            u = random.randint(0, n_nodes-1)
            v = random.randint(0, n_nodes-1)
            if u != v and not aG.has_edge(node_list[u], node_list[v]):
                new_edges.append((node_list[u], node_list[v]))
                count += 1
                aG.add_edge(node_list[u], node_list[v])    # mark selected
                break
    
    #
    return new_edges

#######################################################
# only select nearby edges (d=2)
def nearby_new_edges(G, n_new):
    aG = nx.DiGraph()
    aG.add_edges_from(G.edges_iter())
    
    n_nodes = G.number_of_nodes()
    node_list = [u for u in G.nodes_iter()]
    new_edges = []
    #
    count = 0
    while count < n_new:
        # select random node
        u = node_list[random.randint(0, n_nodes-1)]
        if not aG.has_node(u):      # avoid error "The node XX is not in the graph."
            continue
        nbr_list = aG.neighbors(u)  # successors
        num_nbrs = len(nbr_list)
        if num_nbrs == 0:
            continue                # avoid error "empty range for randrange() (0,0, 0)"
        # select v,w
        v = nbr_list[random.randint(0, num_nbrs-1)]
        w = nbr_list[random.randint(0, num_nbrs-1)]
        if v != w and not aG.has_edge(v, w):
            new_edges.append((v,w))
            count += 1
            aG.add_edge(v,w)    # mark selected
    
    #
    return new_edges

#######################################################
# new edges by random walks (NDSS'13)
def randwalk_new_edges(G, n_new, t=2, M=10):
    # call random_walk_transform
    start = time.clock()
    
    aG = nx.DiGraph()
    
    deg_dict = G.out_degree()     # out-degrees
    nb_dict = {}    # neighbor dict: dict of lists
    nb_len = {}
    for u in G.nodes_iter():
        nb_dict[u] = G.neighbors(u)
        nb_len[u] = len(G.neighbors(u))
    #
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            loop = 1
            z = u
            while (u == z or G.has_edge(u, z)) and (loop <= M):     # G not aG to avoid duplicate edges
                # perform (t-1) hop random walk from v
                z = v
                for i in range(t-1):
                    z = nb_dict[z][random.randint(0,nb_len[z]-1)]
                loop += 1
            #
            if loop <= M:
                if count == 1:
                    aG.add_edge(u, z)
                else:
                    if deg_dict[u] > 1:
                        val = random.random()
                        if val < (0.5*deg_dict[u] - 1)/(deg_dict[u] - 1):
                            aG.add_edge(u, z)
            #
            count += 1
    print "call random_walk_transform: DONE, elapsed:", time.clock() - start
    
    # select p percentages
    new_edges = []
    p = float(n_new)/float(aG.number_of_edges())
    for e in aG.edges_iter():
        val = random.random()
        if val <= p:
            new_edges.append((e[0], e[1]))
    
    #
    return new_edges

#######################################################
# prepare MPS file for MOSEK quadratic programming solver
# degree_constraint: 0(DEG_OUT), 1 (DEG_INOUT)
def prepare_QP_MPS_file(G, new_edges, out_file, degree_constraint=DEG_INOUT, n_deleted=0):
    deg_list = G.out_degree()     # out-degrees
    indeg_list = G.in_degree()      # in-degrees
    
    # 
    aG = nx.DiGraph()
    aG.add_edges_from(G.edges_iter())
    
    
    # 1. index edge variables
    idx = {}        # for col ids in matrix A
    idr = {}        # for row ids in matrix A (nodes)
    i = 0
    all_edges = []      # = aG.edges + new_edges
    for e in aG.edges_iter():
        idx[(e[0], e[1])] = i
#        idx[(e[1], e[0])] = i        # DIRECTED
        i += 1
        all_edges.append((e[0], e[1]))
    for e in new_edges:
        idx[(e[0], e[1])] = i
#        idx[(e[1], e[0])] = i        # DIRECTED
        i += 1
        all_edges.append((e[0], e[1]))
    
    i = 0   
    for u in G.nodes_iter():            # IMPORTANT ! use G not aG (nodes may disappear)
        idr[u] = i
        i += 1
    
    n_nodes = G.number_of_nodes()       # G not aG, to avoid error "error 1112: The constraint name 'E12942' was specified multiple times in the ROWS section."
    n_old = aG.number_of_edges()
    n_new = len(new_edges)
    n_vars = n_old + n_new
    print "n_old =", n_old, " n_new =", n_new
    print "n_nodes =", n_nodes
    print "n_vars =", n_vars
    
    # write all_edges file to help the parse of .sol_file
    all_edges_file = "../qp/" + out_file[0:-4] + ".all_edge"
    f = open(all_edges_file, "w")
    for i in range(len(all_edges)):
        e = all_edges[i]
        f.write("%d %d\n"%(e[0], e[1]))
    f.close()
    
    
    # 3. write to MPS file
    f = open(out_file, "w")
    f.write("NAME          TESTPROB\n")
    f.write("ROWS\n")
    f.write(" N  COST\n")
    for u in G.nodes_iter():        # use G, out-degrees
        row_id = idr[u]  
        f.write(" E  E" + str(row_id) + "\n")
    for u in G.nodes_iter():        # use G, in-degrees
        row_id = idr[u]  
        f.write(" E  E" + str(n_nodes+row_id) + "\n")    
        
    
    f.write("COLUMNS\n")
    
    ###  LEFT JUSTIFY    (2,5,15,25)
    # for each variable Xi
    for i in range(n_vars):
        e = all_edges[i]
        var_name = "X" + str(i)
        f.write("    " + var_name.ljust(10) + "E" + str(idr[e[0]]).ljust(20) + "1" + "\n")          # out-degrees
        f.write("    " + var_name.ljust(10) + "E" + str(n_nodes+idr[e[1]]).ljust(20) + "1" + "\n")      # in-degrees
    
    f.write("RHS\n")
    # for each variable
    for u in G.nodes_iter():        # IMPORTANT ! use G for degrees
        row_id = idr[u]  
        f.write("    RHS1".ljust(14) + "E" + str(row_id).ljust(20) + str(deg_list[u]) + "\n")       # out-degrees
        f.write("    RHS1".ljust(14) + "E" + str(n_nodes+row_id).ljust(20) + str(indeg_list[u]) + "\n")       # in-degrees
        
    
    f.write("QUADOBJ\n")
    # for each X^2
    for i in range(n_vars):
        var_name = "X" + str(i)
        f.write("    " + var_name.ljust(10) + var_name.ljust(21) + "1" + "\n")
    
    f.write("BOUNDS\n")
    # for each variable
    for i in range(n_vars):
        var_name = "X" + str(i)
        f.write(" UP BND1      " + var_name.ljust(21) + "1" + "\n")        
    
    f.write("ENDATA")
    
    f.close()
    # 
    return [] # deleted_edges
    
    

#######################################################
# k: number of new edges to be obfuscated
# lower: lower bound for new edges
def max_variance_opt(aG, new_edges, lower=0.0, out_file=None, old_edges_limit=6000, feastol=1e-4, abstol=10.0, reltol=1e-2):
    
    # fix edges to keep aG.number_of_edges() <= old_edges_limit
    if aG.number_of_edges() > old_edges_limit:
        n_fixed_edges = aG.number_of_edges() - old_edges_limit
        all_edges = aG.edges()
        fix_edges = []
        for i in range(n_fixed_edges):
            e_id = random.randint(0, len(all_edges)-1)
            fix_edges.append(all_edges[e_id])
            del all_edges[e_id]
        #
        for e in fix_edges:
            aG.remove_edge(e[0], e[1])
    
    # for zero-deg nodes
    for e in new_edges:
        if not aG.has_node(e[0]):
            aG.add_node(e[0])
        if not aG.has_node(e[1]):
            aG.add_node(e[1])
    
    #
    deg_list = aG.out_degree()     # out-degrees
    
    # indexing edge variables
    idx = {}        # for col ids in matrix A
    idr = {}        # for row ids in matrix A (nodes)
    i = 0
    all_edges = []
    for e in aG.edges_iter():
        idx[(e[0], e[1])] = i
        idx[(e[1], e[0])] = i
        i += 1
        all_edges.append((e[0], e[1]))
    for e in new_edges:
        idx[(e[0], e[1])] = i
        idx[(e[1], e[0])] = i
        i += 1
        all_edges.append((e[0], e[1]))
    
    i = 0   
    for u in aG.nodes_iter():
        idr[u] = i
        i += 1
    
    n_nodes = aG.number_of_nodes()
    n_old = aG.number_of_edges()
    n_new = len(new_edges)
    n_vars = n_old + n_new
    print "n_old =", n_old, " n_new =", n_new
    print "n_nodes =", n_nodes
    print "n_vars =", n_vars
    
    # matrix Q
    Q = cvxopt.spmatrix(1.0, range(n_vars), range(n_vars))
    p = cvxopt.matrix(0.0, (n_vars, 1))
#    G = np.vstack(( spmatrix(-1.0, range(n_vars), range(n_vars)), spmatrix(1.0, range(n_vars), range(n_vars)) ))
    G = cvxopt.matrix(np.vstack(( -np.eye(n_vars), np.eye(n_vars)) ))
    h = cvxopt.matrix(np.vstack(( cvxopt.matrix(0.0, (n_old,1)), cvxopt.matrix(-lower, (n_new,1)), cvxopt.matrix(1.0, (n_vars,1)) )))
    
    row_ids = []
    col_ids = []
    for e in all_edges:
        row_ids.append(idr[e[0]])
        row_ids.append(idr[e[1]])
        col_ids.append(idx[(e[0],e[1])])
        col_ids.append(idx[(e[1],e[0])])
#    A = spmatrix([1.0 for i in 2*range(n_vars)], row_ids, col_ids)
    A = cvxopt.matrix( cvxopt.spmatrix([1.0 for i in 2*range(n_vars)], row_ids, col_ids), (n_nodes, n_vars))
    
    b = np.zeros((n_nodes, 1))
    i = 0
    for u in aG.nodes_iter():
        b[i] = float(deg_list[u])
        i += 1
        
    b = cvxopt.matrix(b)
    
    #
#    print Q
#    print p
#    print G
#    print h
#    print A
#    print b
        
    # 
    cvxopt.solvers.options['feastol'] = feastol      # to stop soon
    cvxopt.solvers.options['abstol'] = abstol      
    cvxopt.solvers.options['reltol'] = reltol      
    sol=cvxopt.solvers.qp(Q, p, G, h, A, b)
    cvxopt.solvers.options['feastol'] = 1e-7    # reset to default values
    cvxopt.solvers.options['abstol'] = 1e-7     
    cvxopt.solvers.options['reltol'] = 1e-6      

#    print "sol =", sol['x']
    
    # write to file
    aG = nx.DiGraph()
    for i in range(len(all_edges)):
        e = all_edges[i]
        aG.add_edge(e[0], e[1], {'p':sol['x'][i]})
    nx.write_edgelist(aG, out_file, '#', '\t', True, 'utf-8')
    
    print "Write to file: DONE"
    

#######################################################
if __name__ == '__main__':
    
##    filename = "F:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/karate/karate.gml"
#    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polbooks/polbooks.gml"
#    aG = nx.read_gml(filename, 'UTF-8', False)
    
#    filename = "../data/er_200_002.gr"           # 405 edges
#    filename = "../data/er_200_02.gr"           # 4002 edges
#    filename = "../data/er_500_005.gr"          # 6117 edges
#    filename = "../data/er_1000_001.gr"           # 4965 edges
#    filename = "../data/ff_1000_05.gr"           # 3455 edges
    
    # 1. check error (for failed part)
##    filename = "../part/ff_10000_05_connected.9"
##    filename = "../part/temp.97"    # in er_10000_0001.gr
##    filename = "../part/temp.142"    # in com_dblp_ungraph.gr  
##    filename = "../part/temp.56"    # 500, in er_100000_00001_cvxopt_100000_100_triad_missing
#    filename = "../part/temp.33"    # 500, in er_100000_00001_cvxopt_100000_nb_missing
#    
#    
##    n_nodes = 200
##    n_nodes = 1000
#    
#    aG = nx.DiGraph()
##    aG.add_nodes_from(range(n_nodes))   
#    aG = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
#    
##    aG = normalize_graph(aG)
#    
#    print "#nodes =", aG.number_of_nodes()
#    print "#edges =", aG.number_of_edges()
#    print "#components =", nx.number_connected_components(aG)
#    
##    new_edges = random_new_edges(aG, 50)     #karate(50)
##    new_edges = random_new_edges(aG, 200)    #polbooks(200)
##    new_edges = random_new_edges(aG, 100)   #er_200_002(100)      time: ?? s  
##    new_edges = random_new_edges(aG, 1000)   #er_200_02(1000)      time: 29s  
##    new_edges = random_new_edges(aG, 1000)   #er_500_005(1000)    time: 114s
##    new_edges = random_new_edges(aG, 1000)   #er_1000_001(1000)    time: 99s
##    new_edges = random_new_edges(aG, 1000)           #ff_1000_05(1000)    time: 99s
#    
##    new_edges = nearby_new_edges(aG, 1000)    #er_1000_001(1000)    time: 75s
##    new_edges = nearby_new_edges(aG, 1000)   #ff_1000_05(1000)    time: ??s
#
##    new_edges = random_new_edges(aG, 1000)  # for failed part
#    new_edges = random_new_edges(aG, 500)  # for failed part
#
#    print "select new edges: DONE"
#    
##    out_file="../out/er_1000_001_cvxopt_1000.out"
##    out_file="../out/ff_1000_05_cvxopt_1000.out"
##    out_file="../out/er_1000_001_cvxopt_1000_nb.out"    # nearby 
##    out_file="../out/ff_1000_05_cvxopt_1000_nb.out"    # nearby 
#
#    # check error 
##    out_file="../part/ff_10000_05_connected_out.9"
##    out_file="../part/temp_out.97"   # in er_10000_0001.gr
##    out_file="../part/temp_out.142"   # in com_dblp_ungraph.gr
##    out_file="../part/temp_out.56"     # 500, in er_100000_00001_cvxopt_100000_100_triad_missing
#    out_file="../part/temp_out.33"     # 500, in er_100000_00001_cvxopt_100000_nb_missing
#    
#    start = time.clock()
#    max_variance_opt(aG, new_edges, lower=0.0, out_file=out_file)
##    max_variance_opt(aG, new_edges, lower=0.0, out_file=out_file, old_edges_limit=1600)     # test old_edges_limit !
#    print "QP run - Elapsed :", time.clock() - start
#    
##    after_file = "../out/er_200_002_cvxopt.out"    
##    after_file = "../out/er_200_02_cvxopt.out"
##    after_file = "../out/er_1000_001_cvxopt_rep_1000.out"
#    after_file = "../out/er_1000_001_cvxopt_rep_1000_nb.out"           # nearby 
##    after_file = "../out/ff_1000_05_cvxopt_rep_1000.out"
##    after_file = "../out/ff_1000_05_cvxopt_rep_1000_nb.out"       # nearby
#    
##    get_representative_graph(out_file, after_file)

    
    # 2. TEST max_variance_opt()
#    aG = nx.DiGraph()
##    aG.add_edges_from([(0,1),(0,2),(0,3),(1,2)])
##    new_edges = [(1,3)]
###    new_edges = [(1,3),(2,3)]
#
##    aG.add_edges_from([(0,2),(1,2),(0,3)])
###    new_edges = [(0,1),(2,3)]
##    new_edges = [(0,1),(1,3),(2,3)]
#
#    aG.add_edges_from([(0,1),(1,2),(1,3),(2,3),(3,4)])
##    new_edges = [(1,4),(0,3),(0,2)]        #sol (lower=0.0):  0.5 1.0 1.0 1.0 0.5 | 0.5 0.5 0.0
#    new_edges = [(1,4),(0,3),(0,2),(1,4)]   #sol (lower=0.0): 0.3.85 1.0 1.0 1.0 0.3.85 | 0.0 0.615 0.0 0.308
#
#
##    aG.add_edges_from([(0,1),(1,2),(2,3)])  
##    new_edges = [(0,3)]     #sol (lower=0.0):  NO SOLUTION
#
##    aG.add_edges_from([(0,1),(1,2)])  
##    new_edges = [(0,2)]     #sol (lower=0.0):  NO SOLUTION
#
#    max_variance_opt(aG, new_edges, lower=0.0)

    # 3. TEST prepare_QP_MPS_file
    aG = nx.DiGraph()
    aG.add_edges_from([(0,1),(1,2),(2,3),(1,3),(3,4)])
    new_edges = [(1,4),(0,3),(0,2)]        #sol (lower=0.0):  0.5 1.0 1.0 1.0 0.5 | 0.5 0.5 0.0

    prepare_QP_MPS_file(aG, new_edges, "../qp/test_mps.mps")

#    filename = "../part/temp.4"
#    aG = nx.DiGraph()
#    aG = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
#    print "#nodes =", aG.number_of_nodes()
#    print "#edges =", aG.number_of_edges()
#    print "#components =", nx.number_connected_components(aG)
#    
#    new_edges = random_new_edges(aG, 20000)  
#    
#    prepare_QP_MPS_file(aG, new_edges, "C:/Tailieu/Paper-code/Optimization/TEST-SET/qp/max_var_part4_sm100k_5.mps")
#    print "DONE"
    
    
    
    