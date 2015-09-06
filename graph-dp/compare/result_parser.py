'''
Created on Jan 18, 2013

@author: Nguyen Huu Hiep
'''

import time
import win32com.client as win32
import networkx as nx

# constants (tags)
# PRIVACY
SCORE_H1 = "score_H1 = "
SCORE_H2 = "score_H2 = "
EDGE_DIFF = "["
EPS_ARR = "eps_arr = "    

# UTILITY
STR_NE = "s_NE = "
STR_AD = "s_AD = "
STR_MD = "s_MD = "
STR_DV = "s_DV = "
STR_CC = "s_CC = "
STR_APD = "s_APD = "
STR_EDIAM = "s_EDiam = "
STR_CL = "s_CL = "
STR_DIAM = "s_Diam = "
STR_PL = "s_PL = "

STR_M_TIME = "m_time = "
STR_ASSORTATIVITY = "assortativity = "

# CVXOPT
STR_MPS = "prepare MPS files, DONE, elapsed : "
STR_SOLVE = "prepare_mps_and_solve - DONE, elapsed : "
STR_COMBINE = "combine_sol_files - DONE, elapsed : "

    
###############################################
def parse_privacy_utility(file_name_list, output_file):
    
    result = []
    
    for data_name in file_name_list:
        # PRIVACY
        privacy_name = "../console/" + data_name + "-PRIV.txt"
        f = open(privacy_name, "r")
        fstr = f.read()
        f.close()
        
        count = 0
        for line in fstr.split("\n"):
            if line.startswith(SCORE_H1):
                score_H1 = float(line[len(SCORE_H1):])
            if line.startswith(SCORE_H2):
                score_H2 = float(line[len(SCORE_H2):])
                
                
            if line.startswith(EDGE_DIFF):
                astr = line[1:-1]
                if count == 0:
                    edge_diff_b = [int(item) for item in astr.split(",")]
                    count += 1
                elif count == 1:
                    edge_diff_a = [int(item) for item in astr.split(",")]
                    
            if line.startswith(EPS_ARR):
                astr = line[len(SCORE_H1)+1:-1]
                eps_arr = [float(item) for item in astr.split(",")]
        # debug
        print score_H1
        print score_H2
        print edge_diff_b
        print edge_diff_a
        print eps_arr
        
        edge_diff_b_avg = sum(edge_diff_b)/float(len(edge_diff_b))
        edge_diff_a_avg = sum(edge_diff_a)/float(len(edge_diff_a))
        
        
        # UTILITY
        privacy_name = "../console/" + data_name + "-UTIL.txt"
        f = open(privacy_name, "r")
        fstr = f.read()
        f.close()
    
        for line in fstr.split("\n"):
            if line.startswith(STR_NE):
                s_NE = float(line[len(STR_NE):])
            if line.startswith(STR_AD):
                s_AD = float(line[len(STR_AD):])
            if line.startswith(STR_MD):
                s_MD = float(line[len(STR_MD):])
            if line.startswith(STR_DV):
                s_DV = float(line[len(STR_DV):])
            if line.startswith(STR_CC):
                s_CC = float(line[len(STR_CC):])
            if line.startswith(STR_APD):
                s_APD = float(line[len(STR_APD):])
            if line.startswith(STR_EDIAM):
                s_EDiam = float(line[len(STR_EDIAM):])
            if line.startswith(STR_CL):
                s_CL = float(line[len(STR_CL):])
            if line.startswith(STR_DIAM):
                s_Diam = float(line[len(STR_DIAM):])
            if line.startswith(STR_PL):
                s_PL = float(line[len(STR_PL):])
    
        #
        print s_NE
        print s_AD
        print s_MD
        print s_DV
        print s_CC
        print s_APD
        print s_EDiam
        print s_CL
        print s_Diam
        print s_PL
    
        # 
        result.append((score_H1, score_H2, str(edge_diff_b_avg) + " " + str(edge_diff_a_avg), str(eps_arr[0]) + ", " + str(eps_arr[1]) + ", " + str(eps_arr[2]),  
                       s_NE, s_AD, s_MD, s_DV, s_CC, s_PL, s_APD, s_EDiam, s_CL, s_Diam))
    
    #open Excel
    xl = win32.gencache.EnsureDispatch('Excel.Application')
    xl.Visible = True

    ss = xl.Workbooks.Add()
    
    #
    sh = ss.Worksheets[1]
    row = 1
    for r in result:
        row += 1
        sh.Cells(row,1).Value = r[0]
        sh.Cells(row,2).Value = r[1]
        sh.Cells(row,3).Value = r[2]
        sh.Cells(row,4).Value = r[3]
        
        sh.Cells(row,5).Value = r[4]
        sh.Cells(row,6).Value = r[5]
        sh.Cells(row,7).Value = r[6]
        sh.Cells(row,8).Value = r[7]
        sh.Cells(row,9).Value = r[8]
        sh.Cells(row,10).Value = r[9]
        
        sh.Cells(row,12).Value = r[10]
        sh.Cells(row,13).Value = r[11]
        sh.Cells(row,14).Value = r[12]
        sh.Cells(row,15).Value = r[13]
     
 
#    ss.Close(False)
    ss.SaveAs(output_file)
    ss.Close(False)
    xl.Application.Quit()
    
###############################################
def parse_utility_extra(file_name_list, output_file):
    
    result = []
    
    for data_name in file_name_list:
        privacy_name = "../console/" + data_name + "-UTIL-EXTRA.txt"
        f = open(privacy_name, "r")
        fstr = f.read()
        f.close()
    
        for line in fstr.split("\n"):
            if line.startswith(STR_M_TIME):
                s_m_time = float(line[len(STR_M_TIME):])    # get the last item (average)
            if line.startswith(STR_ASSORTATIVITY):
                s_assortativity = float(line[len(STR_ASSORTATIVITY):])
                
        result.append((s_m_time, s_assortativity))
        
    #open Excel
    xl = win32.gencache.EnsureDispatch('Excel.Application')
    xl.Visible = True

    ss = xl.Workbooks.Add()
    
    #
    sh = ss.Worksheets[1]
    row = 1
    for r in result:
        row += 1
        sh.Cells(row,1).Value = r[0]
        sh.Cells(row,2).Value = r[1]
 
#    ss.Close(False)
    ss.SaveAs(output_file)
    ss.Close(False)
    xl.Application.Quit()       
    
###############################################
def parse_time_cvxopt(file_name_list, output_file):
    
    result = []
    
    for data_name in file_name_list:
        privacy_name = "../console/" + data_name + "-CVXOPT.txt"
        f = open(privacy_name, "r")
        fstr = f.read()
        f.close()
    
        s_MPS = 0
        s_SOLVE = 0
        s_COMBINE = 0
        for line in fstr.split("\n"):
            if line.startswith(STR_MPS):
                s_MPS = float(line[len(STR_MPS):])    
            if line.startswith(STR_SOLVE):
                s_SOLVE = float(line[len(STR_SOLVE):])
            if line.startswith(STR_COMBINE):
                s_COMBINE = float(line[len(STR_COMBINE):])
                
        result.append((s_MPS, s_SOLVE-s_MPS, s_COMBINE))
        
    #open Excel
    xl = win32.gencache.EnsureDispatch('Excel.Application')
    xl.Visible = True

    ss = xl.Workbooks.Add()
    
    #
    sh = ss.Worksheets[1]
    row = 1
    for r in result:
        row += 1
        sh.Cells(row,1).Value = r[0]
        sh.Cells(row,2).Value = r[1]
        sh.Cells(row,3).Value = r[2]
 
#    ss.Close(False)
    ss.SaveAs(output_file)
    ss.Close(False)
    xl.Application.Quit()         

###############################################
def check_and_parse_randwalk_variance(file_name_list, T):
    
    for t in range(2,T+1):
        total_sum = 0.0
        total_sum2 = 0.0
        total_non_zero = 0
        total_selfloop = 0.0
        total_multiedge = 0.0
        
        current_node = 0
        for data_name in file_name_list:
            file_name = "../variance/" + data_name + "." + str(t)
            f = open(file_name, "r")
            fstr = f.read()
            f.close()
            
            for line in fstr.split("\n"):
                if line.startswith("start_node = "):    
                    loc1 = line.index("n_node = ") 
                    start_node = int(line[13:loc1])
                    n_node = int(line[loc1+9:])
                    
#                    print "start_node =", start_node, "n_node =", n_node
                    assert current_node == start_node   # CHECK

                    current_node += n_node
                    
                if line.startswith("n_non_zero = "):        
                    loc1 = line.index("n_selfloop = ") 
                    loc2 = line.index("n_multiedge = ")
                    n_non_zero = int(line[13:loc1])
                    n_selfloop = float(line[loc1+13:loc2])
                    n_multiedge = float(line[loc2+14:])
                    
#                    print "n_non_zero =", n_non_zero, "n_selfloop =", n_selfloop, "n_multiedge =", n_multiedge
                    total_non_zero += n_non_zero
                    total_selfloop += n_selfloop
                    total_multiedge += n_multiedge
    
                if line.startswith("sum = "):
                    loc1 = line.index("sum2 = ")
                    sum = float(line[6:loc1])
                    sum2 = float(line[loc1+7:])
                    total_sum += sum
                    total_sum2 += sum2
        #
        print "t =", t, "total_variance =", total_sum/2 ,"total_variance2 =", total_sum2/2 
#        print "total_non_zero =", total_non_zero, "total_selfloop =", total_selfloop/2, "total_multiedge =", total_multiedge/2    # for floor(p)
        print "total_non_zero =", total_non_zero, "total_selfloop =", total_selfloop/2, "total_multiedge =", total_multiedge/4      # for floor(2*p)
        
                    

###############################################
if __name__ == '__main__':
    
#    "com_amazon_ungraph_entropy_0001_2_001",
#       "com_amazon_ungraph_entropy_001_2_001",
#       "com_amazon_ungraph_entropy_01_2_001", 
#       "com_amazon_ungraph_cvxopt_1000000_20_nb_missing", 
#       "com_amazon_ungraph_cvxopt_800000_20_nb_missing", 
#       "com_amazon_ungraph_cvxopt_600000_20_nb_missing",
#       "com_amazon_ungraph_cvxopt_400000_20_nb_missing",
#       "com_amazon_ungraph_cvxopt_200000_20_nb_missing", 
#       "com_amazon_ungraph_cvxopt_1000000_20_rand_missing", 
#       "com_amazon_ungraph_cvxopt_800000_20_rand_missing", 
#       "com_amazon_ungraph_cvxopt_600000_20_rand_missing",
#       "com_amazon_ungraph_cvxopt_400000_20_rand_missing", 
#       "com_amazon_ungraph_cvxopt_200000_20_rand_missing",
#       "com_amazon_ungraph_switch_rand_100000",
#       "com_amazon_ungraph_switch_rand_200000",
#       "com_amazon_ungraph_switch_rand_300000",
#       "com_amazon_ungraph_switch_rand_400000",
#       "com_amazon_ungraph_switch_rand_500000",
#       "com_amazon_ungraph_switch_nb_100000",
#       "com_amazon_ungraph_switch_nb_200000",
#       "com_amazon_ungraph_switch_nb_300000",
#       "com_amazon_ungraph_switch_nb_400000",
#       "com_amazon_ungraph_switch_nb_500000",
#       "com_amazon_ungraph_randwalk_2_10",
#       "com_amazon_ungraph_randwalk_3_10",
#       "com_amazon_ungraph_randwalk_5_10",
#       "com_amazon_ungraph_randwalk_10_10",
#       "com_amazon_ungraph_randwalk_replace_2_10_0.2",
#       "com_amazon_ungraph_randwalk_replace_2_10_0.4",
#       "com_amazon_ungraph_randwalk_replace_2_10_0.6",
#       "com_amazon_ungraph_randwalk_replace_2_10_0.8",
#       "com_amazon_ungraph_cvxopt_1000000_20_rw_missing",
#       "com_amazon_ungraph_cvxopt_800000_20_rw_missing",
#       "com_amazon_ungraph_cvxopt_600000_20_rw_missing",
#       "com_amazon_ungraph_cvxopt_400000_20_rw_missing",
#       "com_amazon_ungraph_cvxopt_200000_20_rw_missing",
#       "com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing",
#       "com_amazon_ungraph_cvxopt_800000_20_rw_first_missing",
#       "com_amazon_ungraph_cvxopt_600000_20_rw_first_missing",
#       "com_amazon_ungraph_cvxopt_400000_20_rw_first_missing",
#       "com_amazon_ungraph_cvxopt_200000_20_rw_first_missing"

#    parse_privacy_utility([
#                            "com_amazon_ungraph_randwalk_2_10",
#                            "com_amazon_ungraph_randwalk_3_10",
#                            "com_amazon_ungraph_randwalk_5_10",
#                            "com_amazon_ungraph_randwalk_10_10",
#                           "com_amazon_ungraph_randwalk_replace_3_10_0.2",
#                           "com_amazon_ungraph_randwalk_replace_3_10_0.4",
#                           "com_amazon_ungraph_randwalk_replace_3_10_0.6",
#                           "com_amazon_ungraph_randwalk_replace_3_10_0.8",
#                           "com_amazon_ungraph_randwalk_replace_5_10_0.2",
#                           "com_amazon_ungraph_randwalk_replace_5_10_0.4",
#                           "com_amazon_ungraph_randwalk_replace_5_10_0.6",
#                           "com_amazon_ungraph_randwalk_replace_5_10_0.8",
#                           ],
#                           "C:/Tailieu/Box Sync/Research/Meeting/com_amazon_ungraph.xlsx")
    
#    "com_dblp_ungraph_entropy_0001_2_001",
#       "com_dblp_ungraph_entropy_001_2_001",
#       "com_dblp_ungraph_entropy_01_2_001", 
#       "com_dblp_ungraph_cvxopt_1000000_20_nb_missing", 
#       "com_dblp_ungraph_cvxopt_800000_20_nb_missing", 
#       "com_dblp_ungraph_cvxopt_600000_20_nb_missing",
#       "com_dblp_ungraph_cvxopt_400000_20_nb_missing",
#       "com_dblp_ungraph_cvxopt_200000_20_nb_missing", 
#       "com_dblp_ungraph_cvxopt_1000000_20_rand_missing", 
#       "com_dblp_ungraph_cvxopt_800000_20_rand_missing", 
#       "com_dblp_ungraph_cvxopt_600000_20_rand_missing",
#       "com_dblp_ungraph_cvxopt_400000_20_rand_missing", 
#       "com_dblp_ungraph_cvxopt_200000_20_rand_missing",
#       "com_dblp_ungraph_switch_rand_100000",
#       "com_dblp_ungraph_switch_rand_200000",
#       "com_dblp_ungraph_switch_rand_300000",
#       "com_dblp_ungraph_switch_rand_400000",
#       "com_dblp_ungraph_switch_rand_500000",
#       "com_dblp_ungraph_switch_nb_100000",
#       "com_dblp_ungraph_switch_nb_200000",
#       "com_dblp_ungraph_switch_nb_300000",
#       "com_dblp_ungraph_switch_nb_400000",
#       "com_dblp_ungraph_switch_nb_500000",
#       "com_dblp_ungraph_randwalk_2_10",
#       "com_dblp_ungraph_randwalk_3_10",
#       "com_dblp_ungraph_randwalk_5_10",
#       "com_dblp_ungraph_randwalk_10_10",
#        "com_dblp_ungraph_randwalk_replace_2_10_0.2",
#        "com_dblp_ungraph_randwalk_replace_2_10_0.4",
#        "com_dblp_ungraph_randwalk_replace_2_10_0.6",
#        "com_dblp_ungraph_randwalk_replace_2_10_0.8",
#        "com_dblp_ungraph_cvxopt_1000000_20_rw_missing",
#        "com_dblp_ungraph_cvxopt_800000_20_rw_missing",
#        "com_dblp_ungraph_cvxopt_600000_20_rw_missing",
#        "com_dblp_ungraph_cvxopt_400000_20_rw_missing",
#        "com_dblp_ungraph_cvxopt_200000_20_rw_missing",
#        "com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing",
#        "com_dblp_ungraph_cvxopt_800000_20_rw_first_missing",
#        "com_dblp_ungraph_cvxopt_600000_20_rw_first_missing",
#        "com_dblp_ungraph_cvxopt_400000_20_rw_first_missing",
#        "com_dblp_ungraph_cvxopt_200000_20_rw_first_missing"
#     "com_dblp_ungraph_randwalk_keep_replace_2_0.5_0.2",
#     "com_dblp_ungraph_randwalk_keep_replace_2_0.5_0.5",
#     "com_dblp_ungraph_randwalk_keep_replace_2_0.5_0.8",
#     "com_dblp_ungraph_randwalk_keep_replace_3_0.5_0.2",
#     "com_dblp_ungraph_randwalk_keep_replace_3_0.5_0.5",
#     "com_dblp_ungraph_randwalk_keep_replace_3_0.5_0.8",
#     "com_dblp_ungraph_randwalk_keep_replace_5_0.5_0.2",
#     "com_dblp_ungraph_randwalk_keep_replace_5_0.5_0.5",
#     "com_dblp_ungraph_randwalk_keep_replace_5_0.5_0.8",
#     "com_amazon_ungraph_randwalk_keep_replace_2_0.5_0.2",
#     "com_amazon_ungraph_randwalk_keep_replace_2_0.5_0.5",
#     "com_amazon_ungraph_randwalk_keep_replace_2_0.5_0.8",
#     "com_amazon_ungraph_randwalk_keep_replace_3_0.5_0.2",
#     "com_amazon_ungraph_randwalk_keep_replace_3_0.5_0.5",
#     "com_amazon_ungraph_randwalk_keep_replace_3_0.5_0.8",
#     "com_amazon_ungraph_randwalk_keep_replace_5_0.5_0.2",
#     "com_amazon_ungraph_randwalk_keep_replace_5_0.5_0.5",
#     "com_amazon_ungraph_randwalk_keep_replace_5_0.5_0.8",
#     "com_youtube_ungraph_randwalk_keep_replace_2_0.5_0.2",
#     "com_youtube_ungraph_randwalk_keep_replace_2_0.5_0.5",
#     "com_youtube_ungraph_randwalk_keep_replace_2_0.5_0.8",
#     "com_youtube_ungraph_randwalk_keep_replace_3_0.5_0.2",
#     "com_youtube_ungraph_randwalk_keep_replace_3_0.5_0.5",
#     "com_youtube_ungraph_randwalk_keep_replace_3_0.5_0.8",
#     "com_youtube_ungraph_randwalk_keep_replace_5_0.5_0.2",
#     "com_youtube_ungraph_randwalk_keep_replace_5_0.5_0.5",
#     "com_youtube_ungraph_randwalk_keep_replace_5_0.5_0.8",
#                     

<<<<<<< .mine
    parse_privacy_utility(["com_dblp_ungraph_linopt_200000_nb_only_org_min",
                           "com_dblp_ungraph_linopt_400000_nb_only_org_min",
                           "com_dblp_ungraph_linopt_600000_nb_only_org_min",
                           "com_dblp_ungraph_linopt_800000_nb_only_org_min",
                        ],
                        "C:/Tailieu/Box Sync/Research/Meeting/com_dblp_ungraph.xlsx")
=======
#    parse_privacy_utility([
#     "com_dblp_ungraph_cvxopt_1200000_20_nb_missing",
#     "com_dblp_ungraph_cvxopt_1400000_20_nb_missing",
#     "com_dblp_ungraph_cvxopt_1600000_20_nb_missing",
#     "com_dblp_ungraph_cvxopt_1800000_20_nb_missing",
#     "com_dblp_ungraph_cvxopt_2000000_20_nb_missing",
#     "com_amazon_ungraph_cvxopt_1200000_20_nb_missing",
#     "com_amazon_ungraph_cvxopt_1400000_20_nb_missing",
#     "com_amazon_ungraph_cvxopt_1600000_20_nb_missing",
#     "com_amazon_ungraph_cvxopt_1800000_20_nb_missing",
#     "com_amazon_ungraph_cvxopt_2000000_20_nb_missing",
#     "com_youtube_ungraph_cvxopt_3600000_60_nb_missing",
#     "com_youtube_ungraph_cvxopt_4200000_60_nb_missing",
#     "com_youtube_ungraph_cvxopt_4800000_60_nb_missing",
#     "com_youtube_ungraph_cvxopt_5400000_60_nb_missing",
#     "com_youtube_ungraph_cvxopt_6000000_60_nb_missing",
#                        
#                        ],
#                        "C:/Tailieu/Box Sync/Research/Meeting/com_dblp_ungraph.xlsx")
>>>>>>> .r645

#                        "com_dblp_ungraph_cvxopt_1000000_20_nb_switch_0.2_nb_missing",
#                        "com_dblp_ungraph_cvxopt_1000000_20_nb_switch_0.3_nb_missing"

#    parse_privacy_utility([
#                        "com_dblp_ungraph_cvxopt_1000000_20_nb_switch_0.2_nb_missing",
#                        "com_dblp_ungraph_cvxopt_1000000_20_nb_switch_0.3_nb_missing"
#                        ],
#                        "C:/Tailieu/Box Sync/Research/Meeting/temp.xlsx")


#    "com_youtube_ungraph_entropy_0001_2_001",
#       "com_youtube_ungraph_entropy_001_2_001",
#       "com_youtube_ungraph_entropy_01_2_001", 
#       "com_youtube_ungraph_cvxopt_3000000_60_nb_missing", 
#       "com_youtube_ungraph_cvxopt_2400000_60_nb_missing", 
#       "com_youtube_ungraph_cvxopt_1800000_60_nb_missing",
#       "com_youtube_ungraph_cvxopt_1200000_60_nb_missing",
#       "com_youtube_ungraph_cvxopt_600000_60_nb_missing", 
#       "com_youtube_ungraph_cvxopt_3000000_60_rand_missing", 
#       "com_youtube_ungraph_cvxopt_2400000_60_rand_missing", 
#       "com_youtube_ungraph_cvxopt_1800000_60_rand_missing",
#       "com_youtube_ungraph_cvxopt_1200000_60_rand_missing", 
#       "com_youtube_ungraph_cvxopt_600000_60_rand_missing",
#       "com_youtube_ungraph_switch_rand_300000",
#       "com_youtube_ungraph_switch_rand_600000",
#       "com_youtube_ungraph_switch_rand_900000",
#       "com_youtube_ungraph_switch_rand_1200000",
#       "com_youtube_ungraph_switch_rand_1500000",
#       "com_youtube_ungraph_switch_nb_300000",
#       "com_youtube_ungraph_switch_nb_600000",
#       "com_youtube_ungraph_switch_nb_900000",
#       "com_youtube_ungraph_switch_nb_1200000",
#       "com_youtube_ungraph_switch_nb_1500000",
#       "com_youtube_ungraph_randwalk_2_10",
#       "com_youtube_ungraph_randwalk_3_10",
#       "com_youtube_ungraph_randwalk_5_10",
#       "com_youtube_ungraph_randwalk_10_10"],
#       "com_youtube_ungraph_randwalk_replace_2_10_0.2",
#       "com_youtube_ungraph_randwalk_replace_2_10_0.4",
#       "com_youtube_ungraph_randwalk_replace_2_10_0.6",
#       "com_youtube_ungraph_randwalk_replace_2_10_0.8",
#       "com_youtube_ungraph_cvxopt_3000000_60_rw_missing",
#       "com_youtube_ungraph_cvxopt_2400000_60_rw_missing",
#       "com_youtube_ungraph_cvxopt_1800000_60_rw_missing",
#       "com_youtube_ungraph_cvxopt_1200000_60_rw_missing",
#       "com_youtube_ungraph_cvxopt_600000_60_rw_missing",
#       "com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing",
#       "com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing",
#       "com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing",
#       "com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing",
#       "com_youtube_ungraph_cvxopt_600000_60_rw_first_missing"],

#        "com_dblp_ungraph_randwalk_keep_2_0.5",
#        "com_dblp_ungraph_randwalk_keep_3_0.5",
#        "com_dblp_ungraph_randwalk_keep_5_0.5",
#        "com_dblp_ungraph_randwalk_keep_10_0.5",
#       "com_amazon_ungraph_randwalk_keep_2_0.5",
#        "com_amazon_ungraph_randwalk_keep_3_0.5",
#        "com_amazon_ungraph_randwalk_keep_5_0.5",
#        "com_amazon_ungraph_randwalk_keep_10_0.5", 
#       "com_youtube_ungraph_randwalk_keep_2_0.5",
#        "com_youtube_ungraph_randwalk_keep_3_0.5",
#        "com_youtube_ungraph_randwalk_keep_5_0.5",
#        "com_youtube_ungraph_randwalk_keep_10_0.5"],


#    parse_privacy_utility([
#                           "com_dblp_ungraph_detwalk_keep_2_0.5",
#                            "com_dblp_ungraph_detwalk_keep_3_0.5",
#                            "com_dblp_ungraph_detwalk_keep_5_0.5",
#                            "com_dblp_ungraph_detwalk_keep_10_0.5"
#                            ],
#                           "C:/Tailieu/Box Sync/Research/Meeting/com_youtube_ungraph.xlsx")


    # MISSING cases
#    parse_privacy_utility(["com_dblp_ungraph_randwalk_2_10",
#                           "com_youtube_ungraph_cvxopt_600000_60_nb_missing"],
#                          "C:/temp.xlsx")


    # UTIL-EXTRA
#    parse_utility_extra(["com_amazon_ungraph",
#                        "com_amazon_ungraph_entropy_0001_2_001",
#                        "com_amazon_ungraph_entropy_001_2_001",
#                        "com_amazon_ungraph_entropy_01_2_001", 
#                        "com_amazon_ungraph_cvxopt_1000000_20_nb_missing", 
#                        "com_amazon_ungraph_cvxopt_800000_20_nb_missing", 
#                        "com_amazon_ungraph_cvxopt_600000_20_nb_missing",
#                        "com_amazon_ungraph_cvxopt_400000_20_nb_missing",
#                        "com_amazon_ungraph_cvxopt_200000_20_nb_missing", 
#                        "com_amazon_ungraph_cvxopt_1000000_20_rand_missing", 
#                        "com_amazon_ungraph_cvxopt_800000_20_rand_missing", 
#                        "com_amazon_ungraph_cvxopt_600000_20_rand_missing",
#                        "com_amazon_ungraph_cvxopt_400000_20_rand_missing", 
#                        "com_amazon_ungraph_cvxopt_200000_20_rand_missing",
#                        "com_amazon_ungraph_switch_rand_100000",
#                        "com_amazon_ungraph_switch_rand_200000",
#                        "com_amazon_ungraph_switch_rand_300000",
#                        "com_amazon_ungraph_switch_rand_400000",
#                        "com_amazon_ungraph_switch_rand_500000",
#                        "com_amazon_ungraph_switch_nb_100000",
#                        "com_amazon_ungraph_switch_nb_200000",
#                        "com_amazon_ungraph_switch_nb_300000",
#                        "com_amazon_ungraph_switch_nb_400000",
#                        "com_amazon_ungraph_switch_nb_500000",
#                        "com_amazon_ungraph_randwalk_2_10",
#                        "com_amazon_ungraph_randwalk_3_10",
#                        "com_amazon_ungraph_randwalk_5_10",
#                        "com_amazon_ungraph_randwalk_10_10",
#                        "com_dblp_ungraph",
#                        "com_dblp_ungraph_entropy_0001_2_001",
#                        "com_dblp_ungraph_entropy_001_2_001",
#                        "com_dblp_ungraph_entropy_01_2_001", 
#                        "com_dblp_ungraph_cvxopt_1000000_20_nb_missing", 
#                        "com_dblp_ungraph_cvxopt_800000_20_nb_missing", 
#                        "com_dblp_ungraph_cvxopt_600000_20_nb_missing",
#                        "com_dblp_ungraph_cvxopt_400000_20_nb_missing",
#                        "com_dblp_ungraph_cvxopt_200000_20_nb_missing", 
#                        "com_dblp_ungraph_cvxopt_1000000_20_rand_missing", 
#                        "com_dblp_ungraph_cvxopt_800000_20_rand_missing", 
#                        "com_dblp_ungraph_cvxopt_600000_20_rand_missing",
#                        "com_dblp_ungraph_cvxopt_400000_20_rand_missing", 
#                        "com_dblp_ungraph_cvxopt_200000_20_rand_missing",
#                        "com_dblp_ungraph_switch_rand_100000",
#                        "com_dblp_ungraph_switch_rand_200000",
#                        "com_dblp_ungraph_switch_rand_300000",
#                        "com_dblp_ungraph_switch_rand_400000",
#                        "com_dblp_ungraph_switch_rand_500000",
#                        "com_dblp_ungraph_switch_nb_100000",
#                        "com_dblp_ungraph_switch_nb_200000",
#                        "com_dblp_ungraph_switch_nb_300000",
#                        "com_dblp_ungraph_switch_nb_400000",
#                        "com_dblp_ungraph_switch_nb_500000",
#                        "com_dblp_ungraph_randwalk_2_10",
#                        "com_dblp_ungraph_randwalk_3_10",
#                        "com_dblp_ungraph_randwalk_5_10",
#                        "com_dblp_ungraph_randwalk_10_10",
#                        "com_youtube_ungraph",
#                        "com_youtube_ungraph_entropy_0001_2_001",
#                        "com_youtube_ungraph_entropy_001_2_001",
#                        "com_youtube_ungraph_entropy_01_2_001", 
#                        "com_youtube_ungraph_cvxopt_3000000_60_nb_missing", 
#                        "com_youtube_ungraph_cvxopt_2400000_60_nb_missing", 
#                        "com_youtube_ungraph_cvxopt_1800000_60_nb_missing",
#                        "com_youtube_ungraph_cvxopt_1200000_60_nb_missing",
#                        "com_youtube_ungraph_cvxopt_600000_60_nb_missing", 
#                        "com_youtube_ungraph_cvxopt_3000000_60_rand_missing", 
#                        "com_youtube_ungraph_cvxopt_2400000_60_rand_missing", 
#                        "com_youtube_ungraph_cvxopt_1800000_60_rand_missing",
#                        "com_youtube_ungraph_cvxopt_1200000_60_rand_missing", 
#                        "com_youtube_ungraph_cvxopt_600000_60_rand_missing",
#                        "com_youtube_ungraph_switch_rand_300000",
#                        "com_youtube_ungraph_switch_rand_600000",
#                        "com_youtube_ungraph_switch_rand_900000",
#                        "com_youtube_ungraph_switch_rand_1200000",
#                        "com_youtube_ungraph_switch_rand_1500000",
#                        "com_youtube_ungraph_switch_nb_300000",
#                        "com_youtube_ungraph_switch_nb_600000",
#                        "com_youtube_ungraph_switch_nb_900000",
#                        "com_youtube_ungraph_switch_nb_1200000",
#                        "com_youtube_ungraph_switch_nb_1500000",
#                        "com_youtube_ungraph_randwalk_2_10",
#                        "com_youtube_ungraph_randwalk_3_10",
#                        "com_youtube_ungraph_randwalk_5_10",
#                        "com_youtube_ungraph_randwalk_10_10"],
#                        "C:/Tailieu/Box Sync/Research/Meeting/util-extra-all.xlsx")
    
#    parse_utility_extra([
#                        "com_amazon_ungraph_randwalk_replace_2_10_0.2",
#                        "com_amazon_ungraph_randwalk_replace_2_10_0.4",
#                        "com_amazon_ungraph_randwalk_replace_2_10_0.6",
#                        "com_amazon_ungraph_randwalk_replace_2_10_0.8",
#                        "com_amazon_ungraph_cvxopt_1000000_20_rw_missing",
#                        "com_amazon_ungraph_cvxopt_800000_20_rw_missing",
#                        "com_amazon_ungraph_cvxopt_600000_20_rw_missing",
#                        "com_amazon_ungraph_cvxopt_400000_20_rw_missing",
#                        "com_amazon_ungraph_cvxopt_200000_20_rw_missing",
#                        "com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing",
#                        "com_amazon_ungraph_cvxopt_800000_20_rw_first_missing",
#                        "com_amazon_ungraph_cvxopt_600000_20_rw_first_missing",
#                        "com_amazon_ungraph_cvxopt_400000_20_rw_first_missing",
#                        "com_amazon_ungraph_cvxopt_200000_20_rw_first_missing",    
#                        "com_dblp_ungraph_randwalk_replace_2_10_0.2",
#                        "com_dblp_ungraph_randwalk_replace_2_10_0.4",
#                        "com_dblp_ungraph_randwalk_replace_2_10_0.6",
#                        "com_dblp_ungraph_randwalk_replace_2_10_0.8",
#                        "com_dblp_ungraph_cvxopt_1000000_20_rw_missing",
#                        "com_dblp_ungraph_cvxopt_800000_20_rw_missing",
#                        "com_dblp_ungraph_cvxopt_600000_20_rw_missing",
#                        "com_dblp_ungraph_cvxopt_400000_20_rw_missing",
#                        "com_dblp_ungraph_cvxopt_200000_20_rw_missing",
#                        "com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing",
#                        "com_dblp_ungraph_cvxopt_800000_20_rw_first_missing",
#                        "com_dblp_ungraph_cvxopt_600000_20_rw_first_missing",
#                        "com_dblp_ungraph_cvxopt_400000_20_rw_first_missing",
#                        "com_dblp_ungraph_cvxopt_200000_20_rw_first_missing",    
#                        "com_youtube_ungraph_randwalk_replace_2_10_0.2",
#                        "com_youtube_ungraph_randwalk_replace_2_10_0.4",
#                        "com_youtube_ungraph_randwalk_replace_2_10_0.6",
#                        "com_youtube_ungraph_randwalk_replace_2_10_0.8",
#                        "com_youtube_ungraph_cvxopt_3000000_60_rw_missing",
#                        "com_youtube_ungraph_cvxopt_2400000_60_rw_missing",
#                        "com_youtube_ungraph_cvxopt_1800000_60_rw_missing",
#                        "com_youtube_ungraph_cvxopt_1200000_60_rw_missing",
#                        "com_youtube_ungraph_cvxopt_600000_60_rw_missing",
#                        "com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing",
#                        "com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing",
#                        "com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing",
#                        "com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing",
#                        "com_youtube_ungraph_cvxopt_600000_60_rw_first_missing"],
#                        "C:/Tailieu/Box Sync/Research/Meeting/util-extra-all.xlsx")
    
    
#    parse_time_cvxopt([
#                           "com_dblp_ungraph_cvxopt_200000_20_nb_missing",
#                           "com_dblp_ungraph_cvxopt_400000_20_nb_missing",
#                           "com_dblp_ungraph_cvxopt_600000_20_nb_missing",
#                           "com_dblp_ungraph_cvxopt_800000_20_nb_missing",
#                           "com_dblp_ungraph_cvxopt_1000000_20_nb_missing",
#                           "com_amazon_ungraph_cvxopt_200000_20_nb_missing",
#                           "com_amazon_ungraph_cvxopt_400000_20_nb_missing",
#                           "com_amazon_ungraph_cvxopt_600000_20_nb_missing",
#                           "com_amazon_ungraph_cvxopt_800000_20_nb_missing",
#                           "com_amazon_ungraph_cvxopt_1000000_20_nb_missing",
#                           "com_youtube_ungraph_cvxopt_600000_60_nb_missing",
#                           "com_youtube_ungraph_cvxopt_1200000_60_nb_missing",
#                           "com_youtube_ungraph_cvxopt_1800000_60_nb_missing",
#                           "com_youtube_ungraph_cvxopt_2400000_60_nb_missing",
#                           "com_youtube_ungraph_cvxopt_3000000_60_nb_missing",],
#                           "C:/Tailieu/Box Sync/Research/Meeting/com_youtube_ungraph.xlsx")    
    
    
    # TEST check_and_parse_randwalk_variance
#    check_and_parse_randwalk_variance(["_com_amazon_ungraph_var_0",
##                                       "__com_amazon_ungraph_var_55000",
#                                        "_com_amazon_ungraph_var_110000",
##                                        "__com_amazon_ungraph_var_165000",                                        
#                                        "_com_amazon_ungraph_var_220000",
##                                        "__com_amazon_ungraph_var_275000"
#                                        ], 5)
    
#    check_and_parse_randwalk_variance(["__com_dblp_ungraph_var_0",
#                                    "__com_dblp_ungraph_var_53000",
#                                    "__com_dblp_ungraph_var_106000",
#                                    "__com_dblp_ungraph_var_159000",
#                                    "__com_dblp_ungraph_var_212000",
#                                    "__com_dblp_ungraph_var_265000"
#                                    ], 5)

#    check_and_parse_randwalk_variance(["com_youtube_ungraph_var_0",
#                                    "com_youtube_ungraph_var_19300",
#                                    "com_youtube_ungraph_var_106200",
#                                    "com_youtube_ungraph_var_193100",
#                                    "com_youtube_ungraph_var_280000",
#                                    "com_youtube_ungraph_var_560000",
#                                    "com_youtube_ungraph_var_840000"
#                                    ], 3)

#    check_and_parse_randwalk_variance(["ba_1000_5_var_0"
#                                    ], 10)


    # TEST read MultiGraph and count number of selfloops/multiedges 
    data_name = "com_amazon_ungraph_randwalk_keep_20_0.5_sample"
#    data_name = "com_dblp_ungraph_randwalk_keep_10_0.5_sample"
#    data_name = "ba_1000_5_randwalk_keep_10_0.5_sample"
    
    n_samples = 20
    sum_selfloop = 0.0
    sum_multiedge = 0.0
    for i in range(n_samples):
        file_name = "../randwalk/" + data_name + "." + str(i)
        print "file_name =", file_name
        G = nx.read_edgelist(file_name, '#', '\t', create_using=nx.MultiGraph(), nodetype=int, data=False)      # allow selfloops, multiedges
        # test data
#        G = nx.MultiGraph()
#        G.add_edges_from([(0,1),(1,1),(1,1),(0,2),(0,2),(2,2),(1,3)])
        #
#        print "#nodes :", G.number_of_nodes()
        print "#edges :", G.number_of_edges()
        n_selfloop_1 = G.number_of_selfloops()
        print "n_selfloop_1 =", n_selfloop_1
        # remove multi edges
        print "===remove multi edges"
        aG = nx.Graph()
        aG.add_edges_from(G.edges_iter())
#        print "#nodes :", aG.number_of_nodes()
        print "#edges :", aG.number_of_edges()
        n_selfloop_2 = aG.number_of_selfloops()
        print "n_selfloop_2 =", n_selfloop_2
        
        n_multiedges = G.number_of_edges() - aG.number_of_edges() - (n_selfloop_1 - n_selfloop_2)
        print "#self-loops :", n_selfloop_1
        print "#multi-edges :", n_multiedges
        
        sum_selfloop += n_selfloop_1
        sum_multiedge += n_multiedges
        
    print "AVERAGE"
    print "#self-loops :", sum_selfloop/n_samples
    print "#multi-edges :", sum_multiedge/n_samples
   


