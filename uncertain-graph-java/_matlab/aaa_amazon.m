
clear

   
% FILTER, eps = 4.0
[rel_err15, e_deg_distr15, e_dist_distr15, e_AD15, e_MD15, e_DV15, e_CC15, e_PL15, e_APD15, e_CL15, e_EDiam15, e_Diam15 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_filter_4.0', 10);        
% FILTER, eps = 8.0
[rel_err16, e_deg_distr16, e_dist_distr16, e_AD16, e_MD16, e_DV16, e_CC16, e_PL16, e_APD16, e_CL16, e_EDiam16, e_Diam16 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_filter_8.0', 10);  
% FILTER, eps = 16.0
[rel_err17, e_deg_distr17, e_dist_distr17, e_AD17, e_MD17, e_DV17, e_CC17, e_PL17, e_APD17, e_CL17, e_EDiam17, e_Diam17 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_filter_16.0', 10);  
% FILTER, eps = 32.0
[rel_err18, e_deg_distr18, e_dist_distr18, e_AD18, e_MD18, e_DV18, e_CC18, e_PL18, e_APD18, e_CL18, e_EDiam18, e_Diam18 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_filter_32.0', 10);      
    
    
    
% ORBIS, eps = 2.0
[rel_err19, e_deg_distr19, e_dist_distr19, e_AD19, e_MD19, e_DV19, e_CC19, e_PL19, e_APD19, e_CL19, e_EDiam19, e_Diam19 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_orbis_1k_2.0', 10);        
% ORBIS, eps = 4.0
[rel_err20, e_deg_distr20, e_dist_distr20, e_AD20, e_MD20, e_DV20, e_CC20, e_PL20, e_APD20, e_CL20, e_EDiam20, e_Diam20 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_orbis_1k_4.0', 10);     
% ORBIS, eps = 8.0
[rel_err21, e_deg_distr21, e_dist_distr21, e_AD21, e_MD21, e_DV21, e_CC21, e_PL21, e_APD21, e_CL21, e_EDiam21, e_Diam21 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_orbis_1k_8.0', 10);     
% ORBIS, eps = 16.0
[rel_err22, e_deg_distr22, e_dist_distr22, e_AD22, e_MD22, e_DV22, e_CC22, e_PL22, e_APD22, e_CL22, e_EDiam22, e_Diam22 ] = ...
        aa_compute_util('com_amazon_ungraph', 'com_amazon_ungraph_orbis_1k_16.0', 10);     
    
    
    
[rel_err15, rel_err16, rel_err17, rel_err18,...
    rel_err19, rel_err20, rel_err21, rel_err22]     
    
    
    