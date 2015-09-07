'''
Created on Mar 26, 2015

@author: huunguye
    - http://perso.crans.org/aynaud/communities/
'''

import community
import time
import networkx as nx
import matplotlib.pyplot as plt

#######################################################
if __name__ == '__main__':
    # better with karate_graph() as defined in networkx example.
    # erdos renyi don't have true community structure
    
#    G = nx.erdos_renyi_graph(30, 0.05)
#    G = nx.karate_club_graph();

    dataname = "polbooks"       # (105, 441)     build_dendrogram 0.0052s
    dataname = "polblogs"       # (1224,16715) build_dendrogram 0.306
    dataname = "as20graph"      # (6474,12572) build_dendrogram 0.16s, 75k fitting (424s)
#    dataname = "wiki-Vote"     # (7115,100762)
#    dataname = "ca-HepPh"      # (12006,118489)     
#    dataname = "ca-AstroPh"    # (18771,198050)    
#    dataname = "com_amazon_ungraph" # (334863,925872) 
#    dataname = "com_dblp_ungraph"  # (317080,1049866) 
#    dataname = "com_youtube_ungraph"# (1134890,2987624) (5.0GB mem, 230s Acer)
    
    filename = "../_data/" + dataname + ".gr"

    start = time.clock()
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "read graph - DONE, elapsed", time.clock() - start
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    # first compute the best partition
    start = time.clock()
    partition = community.best_partition(G)
    print "commynity detection - DONE, elapsed", time.clock() - start
    
    for com in set(partition.values()) :
        print "community", com,
        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
        print "size :", len(list_nodes)
        print list_nodes
    
    #
    louvain_file = "../_data/" + dataname + ".louvain"
    f = open(louvain_file, 'w')
    for com in set(partition.values()) :
        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
        for u in list_nodes:
            f.write("%d,"%u)
        f.write("\n")
    f.close()
    print "write communities to file: DONE",
    
    
    # DRAWING (for up to polblogs)
#    color = ["red", "green", "blue", "cyan", "white", "black", "yellow", "magenta"]
#    size = float(len(set(partition.values())))
#    pos = nx.spring_layout(G)
#    count = 0.
#    for com in set(partition.values()) :
#        print "community", com
#        count = count + 1.
#        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
#        print list_nodes
##        nx.draw_networkx_nodes(G, pos, list_nodes, node_size = 20, node_color = str(count / size))
#        nx.draw_networkx_nodes(G, pos, list_nodes, node_size = 40, node_color = color[com % 8])
#    
#    nx.draw_networkx_edges(G,pos, alpha=0.5)
#    plt.show()