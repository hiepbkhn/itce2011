'''
Created on Jan 18, 2013

@author: Nguyen Huu Hiep
'''

import time
import win32com.client as win32

# constants (tags)
TIME_STAMP = "TIMESTAMP :"
#
NUM_QUERY = "self.num_user ="
SUCCESS_RATE = "Success rate ="
AVG_MESH_LENGTH = "average_mesh_query ="
#
EXPAND_LIST = "expanding_list - elapsed :"
LIST_EDGE = "list_edges NEW - elapsed :"
ADD_TO_MC_SET = "add_to_mc_set - elapsed :"
FIND_CLOAKING_SETS = "find_cloaking_sets - elapsed :"
COVER_SET = "compute cover_set - elapsed :"
CLOAKING_MESH = "Compute CLOAKING MBR - elapsed :"

#PARALLEL
START_TIME = "START - Now:"
EXPAND_LIST_TIME = "expanding_list - Now:"
LIST_EDGE_TIME = "list_edges - Now:"
ADD_TO_MC_SET_TIME = "add_to_mc_set - Now:"
FIND_CLOAKING_SETS_TIME = "find positive/negative cliques - Now:"
COVER_SET_TIME = "cover_set - Now:"
CLOAKING_MESH_TIME = "CLOAKING MBR - Now:"
END_TIME = "END - Now:"


###############################################
def get_fixed_decimal(num_str):
    point_loc = num_str.index(".")
    return num_str[0:point_loc+5]

def parse_elapse_time(file_name):
    f = open(file_name, "r")
    fstr = f.read()
    f.close()
    
    result = []     # list of tuples (time_stamp, expand_list,...)
    
    time_stamp = -1
    expand_list = ""
    list_edge = ""
    for line in fstr.split("\n"):
        if line.startswith(TIME_STAMP):
            time_stamp = int(line[len(TIME_STAMP)+2:])
        #
        if line.startswith(NUM_QUERY):
            num_query = int(line[len(NUM_QUERY)+2:])
        if line.startswith(SUCCESS_RATE):
            success_rate = get_fixed_decimal(line[len(SUCCESS_RATE)+1:])
        if line.startswith(AVG_MESH_LENGTH):
            avg_mesh_len = get_fixed_decimal(line[len(AVG_MESH_LENGTH)+1:])  
            avg_mesh_len = avg_mesh_len[0:-2]   #remove 2 digits after decimal point  
            
        #    
        if line.startswith(EXPAND_LIST):
            expand_list = get_fixed_decimal(line[len(EXPAND_LIST)+2:])
        if line.startswith(LIST_EDGE):
            list_edge = get_fixed_decimal(line[len(LIST_EDGE)+2:])
        if line.startswith(ADD_TO_MC_SET):
            add_to_mc_set = get_fixed_decimal(line[len(ADD_TO_MC_SET)+2:])
        if line.startswith(FIND_CLOAKING_SETS):
            find_cloaking_sets = get_fixed_decimal(line[len(FIND_CLOAKING_SETS)+2:])
        if line.startswith(COVER_SET):
            cover_set = get_fixed_decimal(line[len(COVER_SET)+2:])
        if line.startswith(CLOAKING_MESH):
            cloaking_mesh = get_fixed_decimal(line[len(CLOAKING_MESH)+2:])
            #    
            result.append((time_stamp, num_query, success_rate, avg_mesh_len,\
                           expand_list, list_edge, add_to_mc_set, find_cloaking_sets, cover_set, cloaking_mesh))
        
    return result

def parse_parallel_time(file_name):
    f = open(file_name, "r")
    fstr = f.read()
    f.close()
    
    result = []     # list of tuples (time_stamp, expand_list,...)
    
    time_stamp = -1
    expand_list = ""
    list_edge = ""
    for line in fstr.split("\n"):
        if line.startswith(TIME_STAMP):
            time_stamp = int(line[len(TIME_STAMP)+2:])
        #
        if line.startswith(START_TIME):
            start_time = line[len(START_TIME)+2:]

        if line.startswith(EXPAND_LIST_TIME):
            expand_list = line[len(EXPAND_LIST_TIME)+2:]
        if line.startswith(LIST_EDGE_TIME):
            list_edge = line[len(LIST_EDGE_TIME)+2:]
        if line.startswith(ADD_TO_MC_SET_TIME):
            add_to_mc_set = line[len(ADD_TO_MC_SET_TIME)+2:]
        if line.startswith(FIND_CLOAKING_SETS_TIME):
            find_cloaking_sets = line[len(FIND_CLOAKING_SETS_TIME)+2:]
        if line.startswith(COVER_SET_TIME):
            cover_set = line[len(COVER_SET_TIME)+2:]
        if line.startswith(CLOAKING_MESH_TIME):
            cloaking_mesh = line[len(CLOAKING_MESH_TIME)+2:]
            #    
            result.append((time_stamp, start_time, \
                           expand_list, list_edge, add_to_mc_set, find_cloaking_sets, cover_set, cloaking_mesh))
        
    return result


# content_type: [pc] or [parallel]
def write_to_excel_sheet(path, file_name_list, output_file, content_type):
    
    #open Excel
    xl = win32.gencache.EnsureDispatch('Excel.Application')
    xl.Visible = True

    ss = xl.Workbooks.Add()
    
    #add worksheets
    for i in range(len(file_name_list) - len(ss.Worksheets)):
        ss.Worksheets.Add()
    
    #one sheet for a file 
    i = 0
    for file_name in file_name_list:
        if content_type == "pc":
            result = parse_elapse_time(path + file_name)
        if content_type == "parallel":
            result = parse_parallel_time(path + file_name)    
        #
        i += 1
        sh = ss.Worksheets[i]
        sh.Name = file_name
     
    #    sh.Cells(1,1).Value = 'Hacking Excel with Python Demo'
     
        row = 1
        for tuple in result:
            row += 1
            sh.Cells(row,1).Value = tuple[0]
            for col in range(1, len(tuple)):
                sh.Cells(row,1+col).Value = tuple[col]
     
 
#    ss.Close(False)
    ss.SaveAs(output_file)
    ss.Close(False)
    xl.Application.Quit()
    

if __name__ == '__main__':
    
#    result = parse_elapse_time("../out/5000_600_2_5.txt")
#    for tuple in result:
#        for item in tuple:
#            print item,
#        print

    #1.
#    file_name_list = ["5000_600_2_5.txt", "5000_800_2_5.txt", "5000_1000_2_5.txt",
#                      "5000_600_2_10.txt", "5000_800_2_10.txt", "5000_1000_2_10.txt", 
#                      "5000_600_2_15.txt", "5000_800_2_15.txt", "5000_1000_2_15.txt",  
#                      "10000_500_2_5.txt", "10000_600_2_5.txt", "10000_700_2_5.txt",
#                      "10000_500_2_10.txt", "10000_600_2_10.txt", "10000_700_2_10.txt",
#                      "10000_500_2_15.txt", "10000_600_2_15.txt", "10000_700_2_15.txt",
#                      "20000_400_2_5.txt", "20000_500_2_5.txt", "20000_600_2_5.txt",
#                      "20000_400_2_10.txt", "20000_500_2_10.txt", "20000_600_2_10.txt",
#                      "20000_400_2_15.txt", "20000_500_2_15.txt", "20000_600_2_15.txt"]
#
#    write_to_excel_sheet("../out/", file_name_list, "pc_run_time.xlsx", "pc")
    
    #2.
#    file_name_list = ["parallel_10000_600_2_5_c2.txt", "parallel_10000_600_2_5_c4.txt", "parallel_10000_600_2_5_c8.txt",
#                      "parallel_20000_500_2_5_c2.txt", "parallel_20000_500_2_5_c4.txt", "parallel_20000_500_2_5_c8.txt",]
#
#    write_to_excel_sheet("D:/workspace-python/mmb_network_parallel/out/", 
#                         file_name_list, "parallel_run_time.xlsx", "parallel")
    
    #3.
#    file_name_list = ["serial_cal_10000_15000_2_5.txt", "serial_cal_10000_20000_2_5.txt", "serial_cal_10000_25000_2_5.txt",
#                      "serial_cal_10000_15000_2_10.txt", "serial_cal_10000_20000_2_10.txt", "serial_cal_10000_25000_2_10.txt",
#                      "serial_cal_10000_15000_2_15.txt", "serial_cal_10000_20000_2_15.txt", "serial_cal_10000_25000_2_15.txt"]

    #4.
#    file_name_list = ["mbr_10000_600_2_5_medium.txt", "mbr_10000_600_2_10_medium.txt", "mbr_10000_600_2_15_medium.txt"]
#
#    write_to_excel_sheet("../out/", file_name_list, "pc_oldenburg_10k_medium.xlsx", "pc")

    #5.
#    file_name_list = ["parallel_5000_800_2_5_c2.txt", "parallel_5000_800_2_5_c4.txt", "parallel_5000_800_2_5_c8.txt"]
#
#    write_to_excel_sheet("../out/", file_name_list, "parallel_run_time.xlsx", "parallel")
    
    #6.
    file_name_list = ["serial_5000_800_2_5.txt"]

    write_to_excel_sheet("../out/", file_name_list, "serial_5k_800.xlsx", "pc")
    
    
    print "DONE"

#    a = "1.234"
#    print get_fixed_decimal(a) + "|"

