'''
Created on Dec 1, 2014

@author: huunguye
DIRECTED graphs
'''

import time
import random
import math
import networkx as nx
import numpy as np
from socialModels import forestFire_mod
import snap as sn

#######################################################
# ER: Erdos-Renyi
def generate_ER_graph(N, p, filename, directed=True):
    G = nx.fast_gnp_random_graph(N, p, None, directed)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')

  
#######################################################
# SM: small-world
def generate_SM_graph(N, p, k, filename):
    G = nx.connected_watts_strogatz_graph(N, k, p, None)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# BA: Barabasi-Albert
def generate_BA_graph(N, m, filename):
    G = nx.barabasi_albert_graph(N, m, None)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# PL: power law
def generate_PL_graph(N, m, p, filename, directed=True):
    G = nx.powerlaw_cluster_graph(N, m, p, None, directed)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# FF: Forest Fire
def generate_FF_graph(N, p, filename, directed=True):
    G = forestFire_mod(N, p, None, directed)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')

        
#######################################################
if __name__ == '__main__':
        
    ### ER   
#    generate_ER_graph(200, 0.2, "../data/er_dir_200_02.gr")
#    generate_ER_graph(200, 0.02, "../data/er_dir_200_002.gr")
#    generate_ER_graph(500, 0.2, "../data/er_dir_500_02.gr")
#    generate_ER_graph(500, 0.1, "../data/er_dir_500_01.gr")
#    generate_ER_graph(500, 0.05, "../data/er_500_005.gr")

#    generate_ER_graph(1000, 0.2, "../data/er_1000_02.gr")
#    generate_ER_graph(1000, 0.1, "../data/er_1000_01.gr")
#    generate_ER_graph(1000, 0.05, "../data/er_1000_005.gr")
#    generate_ER_graph(1000, 0.01, "../data/er_1000_001.gr")

#    generate_ER_graph(2000, 0.05, "../data/er_dir_2000_005.gr")

#    generate_ER_graph(5000, 0.05, "../data/er_5000_005.gr")
#    generate_ER_graph(5000, 0.025, "../data/er_5000_0025.gr")
#    generate_ER_graph(5000, 0.01, "../data/er_5000_001.gr")
#    generate_ER_graph(5000, 0.005, "../data/er_5000_0005.gr")

#    generate_ER_graph(10000, 0.005, "../data/er_dir_10000_0005.gr")
#    generate_ER_graph(10000, 0.002, "../data/er_10000_0002.gr")
#    generate_ER_graph(10000, 0.001, "../data/er_10000_0001.gr")
    
#    generate_ER_graph(20000, 0.002, "../data/er_20000_0002.gr")
#    generate_ER_graph(20000, 0.0005, "../data/er_20000_00005.gr")

#    generate_ER_graph(100000, 0.0001, "../data/er_100000_00001.gr")


    ### SM
#    generate_SM_graph(1000, 0.05, 11, "../data/sm_1000_005_11.gr")
    
#    generate_SM_graph(10000, 0.05, 11, "../data/sm_10000_005_11.gr")
#    generate_SM_graph(100000, 0.05, 11, "../data/sm_100000_005_11.gr")
    
    ### BA
#    generate_BA_graph(1000, 5, "../data/ba_1000_5.gr")
    
#    generate_BA_graph(10000, 5, "../data/ba_10000_5.gr")
#    generate_BA_graph(100000, 5, "../data/ba_100000_5.gr")
    
    ### PL
#    generate_PL_graph(1000, 5, 0.1, "../data/pl_1000_5_01.gr")
    
#    generate_PL_graph(10000, 5, 0.1, "../data/pl_10000_5_01.gr")
#    generate_PL_graph(100000, 5, 0.1, "../data/pl_100000_5_01.gr")

    ### FF - disconnected graphs --> need convert_to_connected()
#    generate_FF_graph(1000, 0.1, "../data/ff_1000_01.gr")
#    generate_FF_graph(1000, 0.5, "../data/ff_1000_05.gr")
#    generate_FF_graph(1000, 0.7, "../data/ff_1000_07.gr")           # slow, 200k edges
#    generate_FF_graph(10000, 0.5, "../data/ff_10000_05.gr")
#    generate_FF_graph(100000, 0.4, "../data/ff_100000_04.gr")       # 175k edges
#    generate_FF_graph(100000, 0.45, "../data/ff_100000_045.gr")       # 230k edges
#    generate_FF_graph(100000, 0.5, "../data/ff_100000_05.gr")       # 1.6M edges

    ### ER with trust
#    generate_ER_graph_with_trust(200, 0.02, "../data/er_trust_200_002.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_1, "../data/er_trust_200_005_1.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_2, "../data/er_trust_200_005_2.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_3, "../data/er_trust_200_005_3.gr")
#    generate_ER_graph_with_trust(200, 0.1, "../data/er_trust_200_01.gr")

    ### SM with trust
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_1, "../data/sm_trust_200_005_5_1.gr")    # k=11 --> 200*(11-1)/2 = 1000 edges
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_2, "../data/sm_trust_200_005_5_2.gr")
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_3, "../data/sm_trust_200_005_5_3.gr")
    
    
    ##### convert_to_connected
##    in_file = "../data/ff_10000_05.gr"
##    out_file = "../data/ff_10000_05_connected.gr"
##    in_file = "../data/ff_100000_05.gr"
##    out_file = "../data/ff_100000_05_connected.gr"
#    in_file = "../data/ff_100000_045.gr"
#    out_file = "../data/ff_100000_045_connected.gr"
#    
#    G = nx.read_edgelist(in_file, '#', '\t', None, nodetype=int, data=False)
#    
#    convert_to_connected(G, out_file)
   
    
    #
    print "DONE"
    