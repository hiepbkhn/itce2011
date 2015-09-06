'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''

from _read_map import Map
from symbol import try_stmt



class QueryLog:
    
    def __init__(self, map_data):
        self.map_data = map_data
        
        self.nodes = map_data.get_nodes()
        self.edges = map_data.get_edges()
        #
        self.trajs = {}
        self.frames = {}
    
    #######################################################
    def read_query(self, path, log_file_name):
        
        f = open(path + log_file_name, "r")
        fstr = f.read()
        f.close()
        
        maxNodeId = -1
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            items = line.split("\t")
            
            obj_id = int(items[1])
            maxNodeId = max( obj_id, maxNodeId)

            x = float(items[5])
            y = float(items[6])
            timestamp = int(items[4])
            next_node_x = int(items[8])
            next_node_y = int(items[9])

            # TODO: find next_node_id and current_edge
            next_node_id = self.map_data.get_next_node_id(next_node_x, next_node_y)
            current_edge = self.map_data.get_nearest_edge_id(next_node_x, next_node_y, x, y)
            
            # Trajectories
            try:
                self.trajs[obj_id].append( (x, y, timestamp, next_node_x, next_node_y, next_node_id, current_edge))
            except:
                self.trajs[obj_id] = []
                self.trajs[obj_id].append( (x, y, timestamp, next_node_x, next_node_y, next_node_id, current_edge))

            # Time frames
            try:
                self.frames[timestamp].append((obj_id, x, y, next_node_x, next_node_y, next_node_id, current_edge))
            except:
                self.frames[timestamp] = []
                self.frames[timestamp].append((obj_id, x, y, next_node_x, next_node_y, next_node_id, current_edge))

                    
class MapVisualizer:
    
    def __init__(self, map_data, query_log):
        self.query_log = query_log
        self.map_data = map_data
        
        self.min_x = self.map_data.min_x
        self.min_y = self.map_data.min_y
        self.dx = self.map_data.dx
        self.dy = self.map_data.dy
        
    #######################################################
    def draw_map(self, canvas, width, height):
        nodes = self.query_log.map_data.nodes
        edges = self.query_log.map_data.edges
       
        
        for edge_id, edge_nodes in edges.iteritems():
            start_node_id = edge_nodes[0]
            end_node_id = edge_nodes[1]
            
            start_x = (nodes[start_node_id][0] - self.min_x) * width / self.dx
            start_y = (nodes[start_node_id][1] - self.min_y) * height / self.dy 
            end_x = (nodes[end_node_id][0] - self.min_x) * width / self.dx
            end_y = (nodes[end_node_id][1] - self.min_y) * height / self.dy 
            
            canvas.create_line(start_x, height-start_y, end_x, height-end_y)
    
    #######################################################    
    def draw_trajectory(self, canvas, width, height, obj_id):
        nodes = self.query_log.map_data.nodes
        edges = self.query_log.map_data.edges
        traj = self.query_log.trajs[obj_id]
        
        for i, point in enumerate(traj):
            edge = edges[point[6]]
            start_node_id = edge[0]
            end_node_id = edge[1]
            
            start_x = (nodes[start_node_id][0] - self.min_x) * width / self.dx
            start_y = (nodes[start_node_id][1] - self.min_y) * height / self.dy 
            end_x = (nodes[end_node_id][0] - self.min_x) * width / self.dx
            end_y = (nodes[end_node_id][1] - self.min_y) * height / self.dy     
            
            canvas.create_line(start_x, height-start_y, end_x, height-end_y, fill="red")        
                    
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    map_data = Map()
    map_data.read_map("D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/", 
                 "oldenburgGen")
    
    query_log = QueryLog(map_data)
    query_log.read_query("D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/oldenburgGen/",
                         "oldenburgGen_500_0_50_0_60_20_3_1000_50.txt")
    
    #
    master = Tk()
    map_visualizer = MapVisualizer(map_data, query_log)
    
    width = 500
    height = 600
    w = Canvas(master, width=width, height=height)
    w.pack()
    
    map_visualizer.draw_map(w, width, height)
    
    #
    map_visualizer.draw_trajectory(w, width, height, 1)
    
    mainloop()

    












