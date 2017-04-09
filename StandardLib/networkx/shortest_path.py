'''
Created on Jul 17, 2013

@author: Nguyen Huu Hiep
'''

import networkx as nx

if __name__ == '__main__':
    G=nx.Graph()
    G.add_edge(1,2,weight=1)
    G.add_edge(1,6,weight=4)
    G.add_edge(1,7,weight=10)
    G.add_edge(2,3,weight=6)
    G.add_edge(2,7,weight=2)
    G.add_edge(3,4,weight=3)
    G.add_edge(3,7,weight=5)
    G.add_edge(4,5,weight=10)
    G.add_edge(5,6,weight=7)
    G.add_edge(5,7,weight=7)
    
    print(nx.shortest_path(G,source=1,target=5, weight="weight"))
