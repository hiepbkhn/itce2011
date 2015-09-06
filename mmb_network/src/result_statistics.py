'''
Created on Dec 4, 2012

@author: Nguyen Huu Hiep
'''

import os.path
import time
from pyautocad import Autocad, APoint
from query_loader import QueryLog
from map_loader import Map 
import option
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query
import scipy.io
from numpy import *

class Color:
    WHITE = 0
    RED = 1
    YELLOW = 2
    GREEN = 3
    CYAN = 4
    BLUE = 5
    MAGENTA = 6

class StatisticsReader:
    def __init__(self, map_data, query_log):
        self.map_data = map_data
        self.query_log = query_log
        #
        self.positive_mc_set = []    #list of sets
        self.user_mc_set = {}        #dict of clique_id
        self.user_mesh = {}  #dict of seg lists
        
        #
        
        
    #######################################################        
    def read_result(self, config_name, distance_constraint, max_speed, timestamp):
        file_name = config_name + "-" + str(distance_constraint) + "-" + str(max_speed)
        
        #1. read positive_mc_set
        positive_mc_set_file_name = "C:/" + file_name + "_positive_mc_set" + "_" + str(timestamp) + ".out"
        if not os.path.exists(positive_mc_set_file_name):
            print "File Not Found !"
            return
        
        f = open(positive_mc_set_file_name, "r")
        fstr = f.read()
        f.close()
        
        lines = fstr.split("\n")
        for line in lines:
            if line == "": # EOF
                break
            items = line.split(",")
            items.pop() #remove last empty item
            self.positive_mc_set.append(set([int(item) for item in items]))
        
        print "positive_mc_set.len =", len(self.positive_mc_set)        
        
        #2. read user_mc_set (remember: max clique for each id) 
        user_mc_set_file_name = "C:/" + file_name + "_user_mc_set" + "_" + str(timestamp) + ".out"
        if not os.path.exists(user_mc_set_file_name):
            print "File Not Found !"
            return
        
        f = open(user_mc_set_file_name, "r")
        fstr = f.read()
        f.close()
        
        lines = fstr.split("\n")
        for line in lines:
            if line == "": # EOF
                break
            items = line.split(" ")
            self.user_mc_set[int(items[0])] = int(items[1])
        
        print "user_mc_set.len =", len(self.user_mc_set)        
        
        
        #3. read user_mesh 
        user_mesh_file_name = "C:/" + file_name + "_user_mesh" + "_" + str(timestamp) + ".out"
        if not os.path.exists(user_mesh_file_name):
            print "File Not Found !"
            return
        
        f = open(user_mesh_file_name, "r")
        fstr = f.read()
        f.close()
        
        lines = fstr.split("\n")
        for i in range (len(lines) / 2):
            obj_id = int(lines[i * 2])
            seg_list_str = lines[i * 2 + 1]   
                 
            self.user_mesh[obj_id] = []
            for seg_str in seg_list_str.split(":"):
                if len(seg_str) > 0:
                    items = seg_str.split(",")
                    self.user_mesh[obj_id].append(EdgeSegment(float(items[0]), float(items[1]),
                            float(items[2]), float(items[3]), int(items[4])))
                    
        print "user_mesh.len =", len(self.user_mesh)            



    #######################################################        
    def draw_a_cloaking_mesh(self, timestamp, user_obj_id):
        acad = Autocad()
        acad.prompt("Autocad from Python - Draw A Cloaking Mesh\n")
        print acad.doc.Name
        
        #1. draw user locations
        query_list = self.query_log.frames[timestamp]       # timestamp
        point_dict = {}     #[obj_id] -> location
        for query in query_list:
            point_dict[query.obj_id] = (query.x, query.y)
            
        acad.prompt("Autocad from Python - Draw Users in Node Layer\n")
        for obj_id in self.positive_mc_set[self.user_mc_set[user_obj_id]]:
            p1 = APoint(point_dict[obj_id][0], point_dict[obj_id][1])
            p = acad.model.AddPoint(p1)
            p.Layer = "Node"
            p.Color = Color.RED    
        
        #2. draw mesh's segments
        acad.prompt("Autocad from Python - Draw Users in Node Layer\n")
        for seg in self.user_mesh[user_obj_id]:
            p1 = APoint(seg.start_x, seg.start_y)
            p2 = APoint(seg.end_x, seg.end_y)
            line = acad.model.AddLine(p1, p2)
            line.Layer = "Mesh"
            line.Color = Color.GREEN
        

        print "DONE"    


#######################################################
def read_and_export_mesh_area():
    
    #
#    f = open("../out/oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002-800-300_cover_mesh_0.out", "r")
#    f = open("../out/oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002-600-300_cover_mesh_0.out", "r")
#    f = open("../out/oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300_cover_mesh_0.out", "r")    
    
    
#    file_list = ["../out/oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002-800-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002-800-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002-800-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002-800-300_cover_mesh_3.out"]
    
#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002-600-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002-600-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002-600-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002-600-300_cover_mesh_3.out"]
    
#    file_list = ["../out/oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002-500-300_cover_mesh_3.out"]
    
    ###################
#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_3.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_4.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-600-300_cover_mesh_5.out"]

#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_3.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_4.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-600-300_cover_mesh_5.out"]
    
#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_3.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_4.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-600-300_cover_mesh_5.out"]        


    ###################
#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_3.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_4.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_mesh_5.out"]

#    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_0.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_1.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_2.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_3.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_4.out",
#                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4-700-300_cover_mesh_5.out"]
    
    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_0.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_1.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_2.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_3.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_4.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4-700-300_cover_mesh_5.out"]              
    
    list_area = []
    list_len = []
    for file_name in file_list:
        f = open(file_name, "r")
        
    
        fstr = f.read()
        f.close()
        
        row = 0
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            row += 1
            if row % 2 == 1:
                area = line[0:line.index(",")]
                list_area.append(float(area))
            else:
                list_seg = line.split(":")
                list_seg.pop()
                mesh_len = 0
                for seg_str in list_seg:
                    items = seg_str.split(",")
                    x1 = float(items[0])
                    y1 = float(items[1])
                    x2 = float(items[2])
                    y2 = float(items[3])
                    mesh_len += math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))     
                list_len.append(mesh_len)
                
                
    
    # export to MATLAB
    scipy.io.savemat('C:/list_area_len_10k_50_2_15_700.mat', dict(list_area=array(list_area), list_len=array(list_len)))        
    
    print "DONE"


#######################################################
def read_and_export_iclique_area():
    file_path = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/iclique_cpp/"
    file_list = [file_path + "iclique_out_10000_50_2_15_90s_10_1.txt"]
    
    list_area = []
    list_len = []
    for file_name in file_list:
        f = open(file_name, "r")
        
    
        fstr = f.read()
        f.close()
        
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            
            paren_loc = line.find("(")
            if paren_loc > -1:
                str = line[paren_loc + 1:-1]
                coords = str.split(",")
                x1 = float(coords[0])
                y1 = float(coords[1])
                x2 = float(coords[2])
                y2 = float(coords[3])
                area = (x2-x1)*(y2-y1)
                list_area.append(float(area))
                
                #debug
#                print x1, y1, x2, y2
#                break
#                if area < 0:
#                    print line
#                    print x1, y1, x2, y2
            
    
    # export to MATLAB
    scipy.io.savemat('C:/iclique_list_area_10k_50_2_15_90s_10_1.mat', dict(list_area=array(list_area)))        
    
    print "DONE"
    
#######################################################
def read_and_export_effective_k():
    
    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_0.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_1.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_2.out"]              
    
    list_effective_k = []
    list_max_k = []
    
    for file_name in file_list:
        f = open(file_name, "r")
        
    
        fstr = f.read()
        f.close()
        
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            dash_loc = line.find("---")
            #str_eff_k = line[:dash_loc]
            str_max_k = line[dash_loc+3:-1] #remove last comma
            list_k_str = str_max_k.split(",")
            max_k = 0 
            for k_str in list_k_str:
                k = int(k_str)
                if max_k < k:
                    max_k = k
            
            list_effective_k.append(len(str_max_k))
            list_max_k.append(max_k)
            
                
                
    
    # export to MATLAB
    scipy.io.savemat('C:/list_effective_k_2_5.mat', dict(list_effective_k=array(list_effective_k), 
                                                                  list_max_k=array(list_max_k)))        
    
    print "DONE"
        

#######################################################
def read_and_export_effective_k_global():
    
    file_list = ["../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_0.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_1.out",
                 "../out/oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4-700-300_cover_set_2.out"]              
    
    list_intersection = []

    list_list_sets = []  #list of lists    
    for file_name in file_list:
        f = open(file_name, "r")
        
    
        fstr = f.read()
        f.close()
        
        list_sets = []
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            dash_loc = line.find("---")
            str_eff_k = line[:dash_loc-1]
            list_k_str = str_eff_k.split(",")
            a_list = []
            for k_str in list_k_str:
                a_list.append(int(k_str))
                
            list_sets.append(set(a_list))
            
        list_list_sets.append(list_sets)
        
    #compute intersection sizes
    for i in range(1,len(list_list_sets)):
        for set_1 in list_list_sets[i-1]:
            for set_2 in list_list_sets[i]:
                list_intersection.append(len(set_1 & set_2))
                
    
    # export to MATLAB
    scipy.io.savemat('C:/list_intersection_k2.mat', dict(list_intersection=array(list_intersection)))        
    
    print "DONE"

#######################################################
if __name__ == '__main__':
    
#    start_time = time.clock()
#        
#    map_data = Map()
#    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
#    print "Load Map : DONE"
#    query_log = QueryLog(map_data)
#    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp=10)    
#    print "Load Query : DONE"
#    
#    print "max_speed = ", query_log.max_speed
#    print "elapsed : ", (time.clock() - start_time)
#    
#    
#    reader = StatisticsReader(map_data, query_log)
#    reader.read_result("oldenburgGen_5000_0_10_0_100_20_0_1000_250", 600, 300, 0)
#    
#    # TEST
#    #reader.draw_a_cloaking_mesh(0, 1)
#    
#    reader.draw_a_cloaking_mesh(0, 20)


    # MATLAB
#    read_and_export_mesh_area()

    # MATLAB - IClique
#    read_and_export_iclique_area()

    # MATLAB - effective k
#    read_and_export_effective_k()

    # MATLAB - effective k_global
    read_and_export_effective_k_global()


