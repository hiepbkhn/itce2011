'''
Created on Dec 1, 2012

@author: Nguyen Huu Hiep
'''
import pylab as P #
import networkx as nx
import matplotlib.pyplot as plt

def test_draw():
    G = nx.dodecahedral_graph()
    nx.draw(G)  # networkx draw()
#    P.draw()    # pylab draw()
    plt.show()
#    plt.savefig("path.png")
    


if __name__ == '__main__':
    filename = "F:/Tailieu/Paper-code/Algorithm/Graph/maxial-clique-tomita/test15.grh"
    f = open(filename, "r")
    
    fstr = f.read()
    list_edges = []
    vertex = 0
    for line in fstr.split("\n"):
        for v in line.split(","):
            if v != "":
                list_edges.append((vertex, int(v)))
        vertex += 1
    
    f.close()
    
    #
    for e in list_edges:
        print e[0], e[1]

        

    #DRAW
    print "Drawing..."
    G = nx.Graph()
    G.add_nodes_from(range(15))
    G.add_edges_from(list_edges)

#    pos = nx.circular_layout(G)
#    pos = nx.spectral_layout(G)
#    pos = nx.random_layout(G)
#    pos = nx.shell_layout(G)
    pos = nx.spring_layout(G)
    nx.draw(G, pos)  # networkx draw()
    plt.show()


