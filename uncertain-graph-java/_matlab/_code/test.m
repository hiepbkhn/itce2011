

[rel_err, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../polbooks', '../polbooks_1k_1.2', 20);  

%%% polbooks
eps_arr=[1.2 2.0 2.3 4.7 7.0 9.3 14.0];
eps_str = {'1.2', '2.0', '2.3', '4.7' ,'7.0', '9.3', '14.0'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_tmf', 20, eps_str, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_ef', 20, eps_str(4:7), '');  
            
[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_dendro_20_105_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_dendro_20_105_1000', 20, eps_str, '_tree_sample_4.0');     
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_fixed_20_105_1000', 20, eps_str, '_tree_sample_1.0');                 
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_fixed_20_105_1000', 20, eps_str, '_tree_sample_4.0'); 
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polbooks', '../polbooks_hrgdiv_20_4_2', 20, eps_str, '_2.00_tree_sample_4.0');             
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro_4, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 15 0 1])  
legend('1k','tmf','ef','dendro_4','fixed_4','hrgdiv_4');


%%% polblogs-wcc
eps_arr=[1.8 2.0 3.6 7.1 10.7 14.2 21.3];
eps_str = {'1.8', '2.0', '3.6', '7.1' ,'10.7', '14.2', '21.3'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_tmf', 20, eps_str, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_ef', 20, eps_str(4:7), '');  
            
[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_dendro_20_1222_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_dendro_20_1222_1000', 20, eps_str, '_tree_sample_4.0');     
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_fixed_20_1222_1000', 20, eps_str, '_tree_sample_1.0');                 
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_fixed_20_1222_1000', 20, eps_str, '_tree_sample_4.0'); 
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_hrgdiv_20_6_2', 20, eps_str, '_2.00_tree_sample_4.0');             
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro_4, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 22 0 1])  
legend('1k','tmf','ef','dendro_4','fixed_4','hrgdiv_4');


%%% as20graph
eps_arr=[2.0 2.2 4.4 8.8 13.2 17.6 26.3];
eps_str = {'2.0', '2.2', '4.4', '8.8' ,'13.2', '17.6', '26.3'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_tmf', 20, eps_str, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_ef', 20, eps_str(4:7), '');   

[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_dendro_20_6474_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_dendro_20_6474_1000', 20, eps_str, '_tree_sample_4.0');     
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_fixed_20_6474_1000', 20, eps_str, '_tree_sample_1.0');                 
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_fixed_20_6474_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../as20graph', '../as20graph_hrgdiv_20_7_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro_4, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 30 0 1])  
legend('1k','tmf','ef','dendro_4','fixed_4','hrgdiv_4');

%%% wiki-Vote-wcc
eps_arr=[2.0 2.2 4.4 8.9 13.3 17.7 26.6];
eps_str = {'2.0', '2.2', '4.4', '8.9' ,'13.3', '17.7', '26.6'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_tmf', 20, eps_str, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_ef', 20, eps_str(4:7), '');   

[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_dendro_20_7066_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_dendro_20_7066_1000', 20, eps_str, '_tree_sample_4.0');
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_fixed_20_7066_1000', 20, eps_str, '_tree_sample_1.0');                            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_fixed_20_7066_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_hrgdiv_20_7_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro_4, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 30 0 1])  
legend('1k','tmf','ef','dendro_4','fixed_4','hrgdiv_4');


%%% ca-HepPh-wcc
eps_arr=[2.0 2.3 4.7 9.3 14.0 18.6 28.0];
eps_str = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.6', '28.0'};
eps_str2 = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.7', '28.0'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_1k', 20, eps_str2, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_tmf', 20, eps_str2, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_ef', 20, eps_str2(4:7), '');   

[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_dendro_20_11204_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_dendro_20_11204_1000', 20, eps_str, '_tree_sample_4.0');     
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_1.0');                            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_hrgdiv_20_8_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro_4, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 30 0 1])  
legend('1k','tmf','ef','dendro_4','fixed_4','hrgdiv_4');

%%% ca-AstroPh-wcc
eps_arr=[2.0 2.5 4.9 9.8 14.7 19.6 29.4];
eps_str = {'2.0', '2.4', '4.9', '9.8' ,'14.7', '19.6', '29.4'};
eps_str2 = {'2.0', '2.5', '4.9', '9.8' ,'14.7', '19.6', '29.4'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_tmf', 20, eps_str2, '');  

[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_ef', 20, eps_str(4:7), '');   

[rel_err_dendro, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_dendro_20_17903_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_dendro_20_17903_1000', 20, eps_str, '_tree_sample_4.0');     
            
[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_fixed_20_17903_1000', 20, eps_str, '_tree_sample_1.0');                            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_fixed_20_17903_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_hrgdiv_20_8_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_dendro, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 30 0 1])  
legend('1k','tmf','ef','dendro','fixed_4','hrgdiv_4');


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% amazon (5s)
eps_arr=[2.0 3.2 6.4 12.7 19.1 25.4 38.2];
eps_str = {'2.0', '3.2', '6.4', '12.7' ,'19.1', '25.4', '38.2'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_tmf', 20, eps_str, '');  
            
[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_ef', 20, eps_str(4:7), '');             

[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_fixed_20_334863_1000', 20, eps_str, '_tree_sample_1.0');                 
            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_fixed_20_334863_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_hrgdiv_20_11_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 40 0 1])  
legend('1k','tmf','ef','fixed_4','hrgdiv_4');

%%% dblp (5s)
eps_arr=[2.0 3.2 6.3 12.7 19.0 25.3 38.0];
eps_str = {'2.0', '3.2', '6.3', '12.7' ,'19.0', '25.3', '38.0'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_tmf', 20, eps_str, '');  
            
[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_ef', 20, eps_str(4:7), '');             

[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps_str, '_tree_sample_1.0');                 
            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_hrgdiv_20_11_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 40 0 1])  
legend('1k','tmf','ef','fixed_4','hrgdiv_4');

%%% youtube (12s)
eps_arr=[2.0 3.5 7.0 13.9 20.9 27.9 41.8];
eps_str = {'2.0', '3.5', '7.0', '13.9' ,'20.9', '27.9', '41.8'};

[rel_err_1k, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_1k', 20, eps_str, '');  
            
[rel_err_tmf, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_tmf', 20, eps_str, '');  
            
[rel_err_ef, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_ef', 20, eps_str(4:7), '');             

[rel_err_fixed, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_fixed_20_1134890_1000', 20, eps_str, '_tree_sample_1.0');                 
            
[rel_err_fixed_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_fixed_20_1134890_1000', 20, eps_str, '_tree_sample_4.0');                   
            
[rel_err_hrgdiv_4, e_degArr, e_cutArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_hrgdiv_20_12_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
            
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '--', eps_arr, rel_err_hrgdiv_4, 's--'); axis([0 42 0 1])  
legend('1k','tmf','ef','fixed_4','hrgdiv_4');

