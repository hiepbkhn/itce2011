'''
Created on Apr 1, 2014

@author: huunguye
'''

import time
import networkx as nx
import matplotlib.pyplot as plt

#######################################################
if __name__ == '__main__':

#    in_file = "er_1000_001.gr"        # ER
#    in_file = "sm_1000_005_11.gr"      # SM
#    in_file = "ba_1000_5.gr"           # BA     
#    in_file = "pl_1000_5_01.gr"        # PL
#    in_file = "ff_1000_05.gr"  
    
    ## 10k nodes
    in_file = "er_10000_0001.gr"        # ER    29k edges missing, redistribute: 59s
#    in_file = "sm_10000_005_11.gr"      # SM    4.4k edges missing, redistribute: 3s
#    in_file = "ba_10000_5.gr"           # BA
#    in_file = "pl_10000_5_01.gr"        # PL
#    in_file = "ff_10000_05_connected.gr"        # FF, 17k edges missing, redistribute: 14s
    
    #
    start = time.clock()
    G = nx.read_edgelist("../data/" + in_file, '#', '\t', None, nodetype=int, data=False) 
    print "read_edgelist - elapsed :", time.clock() - start
    
    start = time.clock()
#    pos = nx.spring_layout(G)        
    pos = nx.random_layout(G, dim=2)
    print "layout - elapsed :", time.clock() - start
    
    start = time.clock()
#    nx.draw(G, pos)  # networkx draw()
    node_size = [30 for i in range(G.number_of_nodes())]
    nx.draw_networkx(G, pos, with_labels=False, node_size=node_size)
    print "nx.draw - elapsed :", time.clock() - start
    
    start = time.clock()
#    plt.savefig("../fig/er_1000_001.png") # save as png
#    plt.savefig("../fig/sm_1000_005_11.png") # save as png
#    plt.savefig("../fig/ba_1000_5.png") # save as png
#    plt.savefig("../fig/pl_1000_5_01.png") # save as png
#    plt.savefig("../fig/ff_1000_05.png") # save as png
    
    plt.savefig("../fig/er_10000_0001.png") # save as png
    #plt.show() # display
    print "plt.savefig - elapsed :", time.clock() - start
    
    print "DONE"