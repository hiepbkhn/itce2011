'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''

from map_loader import Map
from query_loader import QueryLog


class MapVisualizer:
    
    def __init__(self, map_data, query_log, width):
        self.query_log = query_log
        self.map_data = map_data
        
        self.min_x = self.map_data.min_x
        self.min_y = self.map_data.min_y
        self.dx = self.map_data.dx
        self.dy = self.map_data.dy
        
        self.width = width
        self.height = self.dy * width / self.dx       
        
        
    #######################################################
    def draw_map(self, canvas):
        self.canvas = canvas        #also init canvas
        
        nodes = self.query_log.map_data.nodes
        edges = self.query_log.map_data.edges
       
        
        for edge_id, edge_nodes in edges.iteritems():
            start_node_id = edge_nodes.start_node_id
            end_node_id = edge_nodes.end_node_id
            
            start_x = (nodes[start_node_id].x - self.min_x) * self.width / self.dx
            start_y = (nodes[start_node_id].y - self.min_y) * self.height / self.dy 
            end_x = (nodes[end_node_id].x - self.min_x) * self.width / self.dx
            end_y = (nodes[end_node_id].y - self.min_y) * self.height / self.dy 
            
            self.canvas.create_line(start_x, self.height-start_y, end_x, self.height-end_y)
    
    #######################################################    
    def draw_trajectory(self, obj_id, color):
        nodes = self.query_log.map_data.nodes
        edges = self.query_log.map_data.edges
        traj = self.query_log.trajs[obj_id]
        
        for i, point in enumerate(traj):
            edge = edges[point.cur_edge_id]
            start_node_id = edge.start_node_id
            end_node_id = edge.end_node_id
            
            start_x = (nodes[start_node_id].x - self.min_x) * self.width / self.dx
            start_y = (nodes[start_node_id].y - self.min_y) * self.height / self.dy 
            end_x = (nodes[end_node_id].x - self.min_x) * self.width / self.dx
            end_y = (nodes[end_node_id].y - self.min_y) * self.height / self.dy    
            
            self.canvas.create_line(start_x, self.height-start_y, end_x, self.height-end_y, fill=color)  
            
    
    #######################################################    
    def draw_seg_list(self, seg_list, color):
        for seg in seg_list:
        
            start_x = (seg.start_x - self.min_x) * self.width / self.dx
            start_y = (seg.start_y - self.min_y) * self.height / self.dy
            end_x = (seg.end_x - self.min_x) * self.width / self.dx
            end_y = (seg.end_y - self.min_y) * self.height / self.dy
        
            self.canvas.create_line(start_x, self.height-start_y, end_x, self.height-end_y, fill=color)  
            
    #######################################################    
    def draw_line(self, x1, y1, x2, y2, color):
        start_x = (x1 - self.min_x) * self.width / self.dx
        start_y = (y1 - self.min_y) * self.height / self.dy
        end_x = (x2 - self.min_x) * self.width / self.dx
        end_y = (y2 - self.min_y) * self.height / self.dy
        
        self.canvas.create_line(start_x, self.height-start_y, end_x, self.height-end_y, fill=color)
                
                
    #######################################################    
    def draw_rectangle(self, x1, y1, x2, y2, color):
        self.draw_line(x1, y1, x1, y2, color)
        self.draw_line(x2, y1, x2, y2, color)
        self.draw_line(x1, y1, x2, y1, color)
        self.draw_line(x1, y2, x2, y2, color)
         


#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":            
    map_data = Map()
    map_data.read_map("D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/", 
                 "oldenburgGen")
    
    query_log = QueryLog(map_data)
    query_log.read_query("D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/oldenburgGen/",
                         "oldenburgGen.txt")
    
    #
    master = Tk()
   
    
    width = 700
    map_visualizer = MapVisualizer(map_data, query_log, width)
    
    
    w = Canvas(master, width=map_visualizer.width, height=map_visualizer.height)
    w.pack()
    
    
    map_visualizer.draw_map(w)
    
    #
    map_visualizer.draw_trajectory(1, "red")
    
    mainloop()
            
            
            
            
               