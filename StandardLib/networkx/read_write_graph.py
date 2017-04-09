'''
Created on Nov 27, 2012

@author: Nguyen Huu Hiep
'''
import matplotlib.pyplot as plt
import networkx as nx

def read_write_adjlist():
#    G = nx.path_graph(4)
#    nx.write_adjlist(G, "data/test.adjlist")
#    G = nx.read_adjlist("data/test.adjlist")

#    G = nx.read_adjlist("data/test.adjlist", nodetype=int)
    G = nx.read_adjlist("data/hot", nodetype=int)
    print G.nodes()
    print len(G.edges())
    #
    pos = nx.spectral_layout(G)     #spectral_layout is the best for HOT graph data
    nx.draw(G, pos, node_color='b', node_size=5, with_labels=False)
    plt.savefig("fig/hot.png") # save as png
    plt.show() # display


if __name__ == '__main__':
    read_write_adjlist()
    
    