
clear

% % HRG-FIT, eps1 = 2dU, Laplace=O(log n)
% [rel_err1, e_deg_distr1, e_dist_distr1, e_AD1, e_MD1, e_DV1, e_CC1, e_PL1, e_APD1, e_CL1, e_EDiam1, e_Diam1 ] = ...
%         aa_compute_util('polbooks', 'polbooks_hrg_50_1000_105_sample', 10);
% 
% % HRG-FIT, eps1 = 2dU, Laplace=1.0
% [rel_err2, e_deg_distr2, e_dist_distr2, e_AD2, e_MD2, e_DV2, e_CC2, e_PL2, e_APD2, e_CL2, e_EDiam2, e_Diam2 ] = ...
%         aa_compute_util('polbooks', 'polbooks_hrg_50_1000_105_1.0_sample', 10);
% 
%     
% % HRG-MCMC, eps1 = dU, Laplace=O(log n)
% [rel_err3, e_deg_distr3, e_dist_distr3, e_AD3, e_MD3, e_DV3, e_CC3, e_PL3, e_APD3, e_CL3, e_EDiam3, e_Diam3 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_8.92_sample', 10);
% 
% % HRG-MCMC, eps1 = dU, Laplace=1.0
% [rel_err4, e_deg_distr4, e_dist_distr4, e_AD4, e_MD4, e_DV4, e_CC4, e_PL4, e_APD4, e_CL4, e_EDiam4, e_Diam4 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_8.92_1.0_sample', 10);    
%     
% % HRG-MCMC, eps1 = dU/2, Laplace=O(log n)
% [rel_err5, e_deg_distr5, e_dist_distr5, e_AD5, e_MD5, e_DV5, e_CC5, e_PL5, e_APD5, e_CL5, e_EDiam5, e_Diam5 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_4.46_sample', 10);
% 
% % HRG-MCMC, eps1 = dU/2, Laplace=1.0
% [rel_err6, e_deg_distr6, e_dist_distr6, e_AD6, e_MD6, e_DV6, e_CC6, e_PL6, e_APD6, e_CL6, e_EDiam6, e_Diam6 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_4.46_1.0_sample', 10);        
%     
% % HRG-MCMC, eps1 = 1.0, Laplace=O(log n)
% [rel_err7, e_deg_distr7, e_dist_distr7, e_AD7, e_MD7, e_DV7, e_CC7, e_PL7, e_APD7, e_CL7, e_EDiam7, e_Diam7 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_sample', 10);
% 
% % HRG-MCMC, eps1 = 1.0, Laplace=1.0
% [rel_err8, e_deg_distr8, e_dist_distr8, e_AD8, e_MD8, e_DV8, e_CC8, e_PL8, e_APD8, e_CL8, e_EDiam8, e_Diam8 ] = ...
%         aa_compute_util('polbooks', 'polbooks_dendro_50_1000_105_1.0_sample', 10);          
%     
% 
% % DER, eps = 1.0-1.0-1.0
% [rel_err9, e_deg_distr9, e_dist_distr9, e_AD9, e_MD9, e_DV9, e_CC9, e_PL9, e_APD9, e_CL9, e_EDiam9, e_Diam9 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_1.0_1.0_1.0', 10);      
% % DER, eps = 2.0-1.0-1.0
% [rel_err10, e_deg_distr10, e_dist_distr10, e_AD10, e_MD10, e_DV10, e_CC10, e_PL10, e_APD10, e_CL10, e_EDiam10, e_Diam10 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_2.0_1.0_1.0', 10);       
% % DER, eps = 4.0-1.0-2.0
% [rel_err11, e_deg_distr11, e_dist_distr11, e_AD11, e_MD11, e_DV11, e_CC11, e_PL11, e_APD11, e_CL11, e_EDiam11, e_Diam11 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_4.0_1.0_2.0', 10);       
% % DER, eps = 8.0-1.0-2.0
% [rel_err12, e_deg_distr12, e_dist_distr12, e_AD12, e_MD12, e_DV12, e_CC12, e_PL12, e_APD12, e_CL12, e_EDiam12, e_Diam12 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_8.0_1.0_2.0', 10);       
% % DER, eps = 8.0-1.0-4.0
% [rel_err13, e_deg_distr13, e_dist_distr13, e_AD13, e_MD13, e_DV13, e_CC13, e_PL13, e_APD13, e_CL13, e_EDiam13, e_Diam13 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_8.0_1.0_4.0', 10);       
% % DER, eps = 8.0-2.0-4.0
% [rel_err14, e_deg_distr14, e_dist_distr14, e_AD14, e_MD14, e_DV14, e_CC14, e_PL14, e_APD14, e_CL14, e_EDiam14, e_Diam14 ] = ...
%         aa_compute_util('polbooks', 'polbooks_der_8.0_2.0_4.0', 10);           
%     
% 
%     
% % FILTER, eps = 2.0
% [rel_err15, e_deg_distr15, e_dist_distr15, e_AD15, e_MD15, e_DV15, e_CC15, e_PL15, e_APD15, e_CL15, e_EDiam15, e_Diam15 ] = ...
%         aa_compute_util('polbooks', 'polbooks_filter_2.0', 10);        
% % FILTER, eps = 4.0
% [rel_err16, e_deg_distr16, e_dist_distr16, e_AD16, e_MD16, e_DV16, e_CC16, e_PL16, e_APD16, e_CL16, e_EDiam16, e_Diam16 ] = ...
%         aa_compute_util('polbooks', 'polbooks_filter_4.0', 10);  
% % FILTER, eps = 8.0
% [rel_err17, e_deg_distr17, e_dist_distr17, e_AD17, e_MD17, e_DV17, e_CC17, e_PL17, e_APD17, e_CL17, e_EDiam17, e_Diam17 ] = ...
%         aa_compute_util('polbooks', 'polbooks_filter_8.0', 10);  
% % FILTER, eps = 16.0
% [rel_err18, e_deg_distr18, e_dist_distr18, e_AD18, e_MD18, e_DV18, e_CC18, e_PL18, e_APD18, e_CL18, e_EDiam18, e_Diam18 ] = ...
%         aa_compute_util('polbooks', 'polbooks_filter_16.0', 10);      
%     
%     
% % ORBIS, eps = 2.0
% [rel_err19, e_deg_distr19, e_dist_distr19, e_AD19, e_MD19, e_DV19, e_CC19, e_PL19, e_APD19, e_CL19, e_EDiam19, e_Diam19 ] = ...
%         aa_compute_util('polbooks', 'polbooks_orbis_1k_2.0', 10);        
% % ORBIS, eps = 4.0
% [rel_err20, e_deg_distr20, e_dist_distr20, e_AD20, e_MD20, e_DV20, e_CC20, e_PL20, e_APD20, e_CL20, e_EDiam20, e_Diam20 ] = ...
%         aa_compute_util('polbooks', 'polbooks_orbis_1k_4.0', 10);     
% % ORBIS, eps = 8.0
% [rel_err21, e_deg_distr21, e_dist_distr21, e_AD21, e_MD21, e_DV21, e_CC21, e_PL21, e_APD21, e_CL21, e_EDiam21, e_Diam21 ] = ...
%         aa_compute_util('polbooks', 'polbooks_orbis_1k_8.0', 10);     
% % ORBIS, eps = 16.0
% [rel_err22, e_deg_distr22, e_dist_distr22, e_AD22, e_MD22, e_DV22, e_CC22, e_PL22, e_APD22, e_CL22, e_EDiam22, e_Diam22 ] = ...
%         aa_compute_util('polbooks', 'polbooks_orbis_1k_16.0', 10);     
%     
% % CONFIG, TRUE DEGREE SEQUENCE
% [rel_err23, e_deg_distr23, e_dist_distr23, e_AD23, e_MD23, e_DV23, e_CC23, e_PL23, e_APD23, e_CL23, e_EDiam23, e_Diam23 ] = ...
%         aa_compute_util('polbooks', 'polbooks_config_1k', 10);      
% 
% % ORBIS, TRUE DEGREE SEQUENCE
% [rel_err24, e_deg_distr24, e_dist_distr24, e_AD24, e_MD24, e_DV24, e_CC24, e_PL24, e_APD24, e_CL24, e_EDiam24, e_Diam24 ] = ...
%         aa_compute_util('polbooks', 'polbooks_orbis_1k', 10);      
%     
%     
% rel_err = [rel_err1, rel_err2, rel_err3, rel_err4, rel_err5, rel_err6, rel_err7, rel_err8, ...
%     rel_err9, rel_err10, rel_err11, rel_err12, rel_err13, rel_err14,...
%     rel_err15, rel_err16, rel_err17, rel_err18,...
%     rel_err19, rel_err20, rel_err21, rel_err22,...
%     rel_err23, rel_err24]    


%%%%
% TmFPart, eps1 = 1, 2, 4
[rel_err1, e_deg_distr1, e_dist_distr1, e_AD1, e_MD1, e_DV1, e_CC1, e_PL1, e_APD1, e_CL1, e_EDiam1, e_Diam1 ] = ...
        aa_compute_util('polbooks', 'polbooks_tmfpart_1.0', 1);

[rel_err2, e_deg_distr2, e_dist_distr2, e_AD2, e_MD2, e_DV2, e_CC2, e_PL2, e_APD2, e_CL2, e_EDiam2, e_Diam2 ] = ...
        aa_compute_util('polbooks', 'polbooks_tmfpart_2.0', 1);

[rel_err3, e_deg_distr3, e_dist_distr3, e_AD3, e_MD3, e_DV3, e_CC3, e_PL3, e_APD3, e_CL3, e_EDiam3, e_Diam3 ] = ...
        aa_compute_util('polbooks', 'polbooks_tmfpart_4.0', 1);


    