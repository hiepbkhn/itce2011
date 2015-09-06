'''
Created on Nov 19, 2012

@author: Nguyen Huu Hiep
'''
from pyautocad import Autocad, APoint

def draw_map(nodes, edges):
    acad = Autocad()
    acad.prompt("Hello, Autocad from Python\n")
    print acad.doc.Name
    
    for edge in edges.itervalues():
        p1 = APoint(nodes[edge.start_node_id].x, nodes[edge.start_node_id].y)
        p2 = APoint(nodes[edge.end_node_id].x, nodes[edge.end_node_id].y)
        line = acad.model.AddLine(p1, p2)
        line.Color = edge.edge_class
        
        #break


def draw_points_in_layer():
    acad = Autocad()
    acad.prompt("Autocad from Python - Draw Points in Layer\n")
    print acad.doc.Name
    
    for i in range(8):
        p1 = APoint(i*1000, 1000)
        p = acad.model.AddPoint(p1)
        p.Layer = "Node"
        p.Color = i
    
    
if __name__ == '__main__':
    draw_points_in_layer()
