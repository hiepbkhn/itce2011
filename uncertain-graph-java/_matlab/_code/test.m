
% Aug 10, 2016
%   - update eps_arr, eps_str, eps_str_der for eps in [0, 1.5ln]

% [rel_err, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../polbooks', '../polbooks_1k_1.2', 20);  


r_str = {'_0.20', '', '_0.80'};
r_pos = 3;

%%% polbooks
x0=400;
y0=200;
width=400;
height=300;
% eps_arr=[1.2 2.0 2.3 4.7 7.0 9.3 14.0];
% eps_str = {'1.2', '2.0', '2.3', '4.7' ,'7.0', '9.3', '14.0'};
% eps_str_der = {'0.7_0.3_0.2', '1.1_0.6_0.3', '1.3_0.7_0.3', '2.7_1.3_0.7', '4.0_2.0_1.0', '5.3_2.7_1.3', '8.0_4.0_2.0'};
eps_arr=[1.2 2.0 2.3 3.5 4.7 5.8 7.0];
eps_str = {'1.2', '2.0', '2.3', '3.5', '4.7', '5.8', '7.0'};
eps_str_der = {'0.7_0.3_0.2', '1.1_0.6_0.3', '1.3_0.7_0.3', '2.0_1.0_0.5', '2.7_1.3_0.7', '3.3_1.7_0.8', '4.0_2.0_1.0'};


[rel_err_louvain, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../polbooks', '../polbooks_louvain_dendro_10_sample_10.0', 1);  
[rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../polbooks', '../polbooks_fixed_np_20_105_1000_tree_sample_10.0', 20);

[rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_1k', 20, eps_str, '');  

[rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_sg1k', 20, eps_str, ['_4' r_str{r_pos}]);  % _4 (r=0.5), _4_0.80 (r=0.8), 
[rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_per1k', 20, eps_str, ['_4' r_str{r_pos}]);  % _4 (r=0.5), _4_0.80 (r=0.8), 
            
[rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_tmf', 20, eps_str, '');  

[rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_ef', 20, eps_str(4:7), '');  

[rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_der', 20, eps_str_der, '');  
            
% [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_dendro_20_105_1000', 20, eps_str, '_tree_sample_1.0');     
[rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_dendro_20_105_1000', 20, eps_str, '_tree_sample_4.0');     
            
% [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_fixed_20_105_1000', 20, eps_str, '_tree_sample_1.0');                 
[rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_fixed_20_105_1000', 20, eps_str, '_tree_sample_4.0'); 
            
% [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polbooks', '../polbooks_hrgdiv_20_4_2', 20, eps_str, '_2.00_tree_sample_4.0');             
            
display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);

eps_arr = eps_arr/log(105);
plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
    eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');  % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-', 
axis([0.2 1.6 0 0.7]) 
legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed','SG1k');   % ,'HRG-Div'
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')


%%% polblogs-wcc
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[1.8 2.0 3.6 7.1 10.7 14.2 21.3];
% % eps_str = {'1.8', '2.0', '3.6', '7.1' ,'10.7', '14.2', '21.3'};
% % eps_str_der = {'1.0_0.5_0.3', '1.1_0.6_0.3', '2.0_1.0_0.5', '4.1_2.0_1.0', '6.1_3.0_1.5', '8.1_4.1_2.0', '12.2_6.1_3.0'};
% eps_arr=[1.8 2.0 3.6 5.3 7.1 8.9 10.7];
% eps_str = {'1.8', '2.0', '3.6', '5.3', '7.1', '8.9', '10.7'};
% eps_str_der = {'1.0_0.5_0.3', '1.1_0.6_0.3', '2.0_1.0_0.5', '3.0_1.5_0.8', '4.1_2.0_1.0', '5.1_2.5_1.3', '6.1_3.0_1.5'};
% 
% [rel_err_louvain, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../polblogs-wcc', '../polblogs-wcc_louvain_dendro_20_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../polblogs-wcc', '../polblogs-wcc_fixed_np_20_1222_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_1k', 20, eps_str, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_sg1k', 20, eps_str, ['_10' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_per1k', 20, eps_str, ['_10' r_str{r_pos}]);  
%             
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_tmf', 20, eps_str, '');  
% 
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_ef', 20, eps_str(4:7), '');  
% 
% [rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_der', 20, eps_str_der, '');  
%             
% % [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_dendro_20_1222_1000', 20, eps_str, '_tree_sample_1.0');     
% [rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_dendro_20_1222_1000', 20, eps_str, '_tree_sample_4.0');     
%             
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_fixed_20_1222_1000', 20, eps_str, '_tree_sample_1.0');                 
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_fixed_20_1222_1000', 20, eps_str, '_tree_sample_4.0'); 
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../polblogs-wcc', '../polblogs-wcc_hrgdiv_20_6_2', 20, eps_str, '_2.00_tree_sample_4.0');             
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(1222);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
%     eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');      % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-', 
% % legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed');   % ,'HRG-Div'
% % axis([1.5 10.8 0 0.7]) 
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


% %%% as20graph
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 2.2 4.4 8.8 13.2 17.6 26.3];
% % eps_str = {'2.0', '2.2', '4.4', '8.8' ,'13.2', '17.6', '26.3'};
% % eps_str_der = {'1.1_0.6_0.3', '1.3_0.6_0.3', '2.5_1.3_0.6', '5.0_2.5_1.3', '7.5_3.8_1.9', '10.0_5.0_2.5', '15.0_7.5_3.8'};
% eps_arr=[2.0 2.2 4.4 6.6 8.8 11.0 13.2];
% eps_str = {'2.0', '2.2', '4.4', '6.6', '8.8', '11.0', '13.2'};
% eps_str_der = {'1.1_0.6_0.3', '1.3_0.6_0.3', '2.5_1.3_0.6', '3.8_1.9_0.9', '5.0_2.5_1.3', '6.3_3.1_1.6', '7.5_3.8_1.9'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../as20graph', '../as20graph_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../as20graph', '../as20graph_fixed_np_20_6474_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_1k', 20, eps_str, '');  
%             
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_sg1k', 20, eps_str, ['_18' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_per1k', 20, eps_str, ['_18' r_str{r_pos}]);  
% 
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_tmf', 20, eps_str, '');  
% 
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_ef', 20, eps_str(4:7), '');   
% 
% [rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_der', 20, eps_str_der, '');  
% 
% % [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_dendro_20_6474_1000', 20, eps_str, '_tree_sample_1.0');     
% [rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_dendro_20_6474_1000', 20, eps_str, '_tree_sample_4.0');     
%             
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_fixed_20_6474_1000', 20, eps_str, '_tree_sample_1.0');                 
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_fixed_20_6474_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../as20graph', '../as20graph_hrgdiv_20_7_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
%             
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(6474);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
%     eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');      % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-'
% % legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed');       % ,'HRG-Div'
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


% %%% wiki-Vote-wcc
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 2.2 4.4 8.9 13.3 17.7 26.6];
% % eps_str = {'2.0', '2.2', '4.4', '8.9' ,'13.3', '17.7', '26.6'};
% % eps_str_der = {'1.1_0.6_0.3', '1.3_0.6_0.3', '2.5_1.3_0.6', '5.1_2.5_1.3', '7.6_3.8_1.9', '10.1_5.1_2.5', '15.2_7.6_3.8'};
% eps_arr=[2.0 2.2 4.4 6.6 8.9 11.1 13.3];
% eps_str = {'2.0', '2.2', '4.4', '6.6', '8.9', '11.1', '13.3'};
% eps_str2 = {'2.0', '2.2', '4.4', '6.7', '8.9', '11.1', '13.3'};
% eps_str_der = {'1.1_0.6_0.3', '1.3_0.6_0.3', '2.5_1.3_0.6', '3.8_1.9_0.9', '5.1_2.5_1.3', '6.3_3.2_1.6', '7.6_3.8_1.9'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../wiki-Vote-wcc', '../wiki-Vote-wcc_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../wiki-Vote-wcc', '../wiki-Vote-wcc_fixed_np_20_7066_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_1k', 20, eps_str2, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_sg1k', 20, eps_str2, ['_19' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_per1k', 20, eps_str2, ['_19' r_str{r_pos}]);  
% 
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_tmf', 20, eps_str2, '');  
% 
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_ef', 20, eps_str2(4:7), '');   
% 
% [rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_der', 20, eps_str_der, '');  
% 
% % [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_dendro_20_7066_1000', 20, eps_str, '_tree_sample_1.0');     
% [rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_dendro_20_7066_1000', 20, eps_str, '_tree_sample_4.0');
%             
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_fixed_20_7066_1000', 20, eps_str, '_tree_sample_1.0');                            
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_fixed_20_7066_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../wiki-Vote-wcc', '../wiki-Vote-wcc_hrgdiv_20_7_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(7066);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
%     eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');      % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-'
% % legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed');       % ,'HRG-Div'
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


% %%% ca-HepPh-wcc
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 2.3 4.7 9.3 14.0 18.6 28.0];
% % eps_str = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.6', '28.0'};
% % eps_str2 = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.7', '28.0'};
% % eps_str_der = {'1.1_0.6_0.3', '1.3_0.7_0.3', '2.7_1.3_0.7', '5.3_2.7_1.3', '8.0_4.0_2.0', '10.7_5.3_2.7', '16.0_8.0_4.0'};
% eps_arr=[2.0 2.3 4.7 7.0 9.3 11.7 14.0];
% eps_str = {'2.0', '2.3', '4.7', '7.0', '9.3', '11.7', '14.0'};
% eps_str2 = {'2.0', '2.3', '4.7', '7.0', '9.3', '11.7', '14.0'};
% eps_str_der = {'1.1_0.6_0.3', '1.3_0.7_0.3', '2.7_1.3_0.7', '4.0_2.0_1.0', '5.3_2.7_1.3', '6.7_3.3_1.7', '8.0_4.0_2.0'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../ca-HepPh-wcc', '../ca-HepPh-wcc_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_np_20_11204_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_1k', 20, eps_str2, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_sg1k', 20, eps_str2, ['_22' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_per1k', 20, eps_str2, ['_22' r_str{r_pos}]); 
% 
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_tmf', 20, eps_str2, '');  
% 
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_ef', 20, eps_str2(4:7), '');   
% 
% [rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_der', 20, eps_str_der, '');  
% 
% % [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_dendro_20_11204_1000', 20, eps_str, '_tree_sample_1.0');     
% [rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_dendro_20_11204_1000', 20, eps_str, '_tree_sample_4.0');     
%             
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_1.0');                            
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_hrgdiv_20_8_2', 20, eps_str, '_2.00_tree_sample_4.0');                
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(11204);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
%     eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');      % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-'
% % legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed');       % ,'HRG-Div'
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


% %%% ca-AstroPh-wcc
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 2.5 4.9 9.8 14.7 19.6 29.4];
% % eps_str = {'2.0', '2.4', '4.9', '9.8' ,'14.7', '19.6', '29.4'};
% % eps_str2 = {'2.0', '2.5', '4.9', '9.8' ,'14.7', '19.6', '29.4'};
% % eps_str_der = {'1.1_0.6_0.3', '1.4_0.7_0.3', '2.8_1.4_0.7', '5.6_2.8_1.4', '8.4_4.2_2.1', '11.2_5.6_2.8', '16.8_8.4_4.2'};
% eps_arr=[2.0 2.5 4.9 7.3 9.8 12.2 14.7];
% eps_str = {'2.0', '2.4', '4.9', '7.3', '9.8', '12.2', '14.7'};
% eps_str2 = {'2.0', '2.5', '4.9', '7.3', '9.8', '12.2', '14.7'};
% eps_str_der = {'1.1_0.6_0.3', '1.4_0.7_0.3', '2.8_1.4_0.7', '4.2_2.1_1.0', '5.6_2.8_1.4', '7.0_3.5_1.7', '8.4_4.2_2.1'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_fixed_np_20_17903_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_1k', 20, eps_str, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_sg1k', 20, eps_str2, ['_26' r_str{r_pos}]); 
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_per1k', 20, eps_str2, ['_26' r_str{r_pos}]); 
% 
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_tmf', 20, eps_str2, '');  
% 
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_ef', 20, eps_str(4:7), '');   
% 
% [rel_err_der, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_der', 20, eps_str_der, '');  
% 
% % [rel_err_dendro, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_dendro_20_17903_1000', 20, eps_str, '_tree_sample_1.0');     
% [rel_err_dendro_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_dendro_20_17903_1000', 20, eps_str, '_tree_sample_4.0');     
%             
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_fixed_20_17903_1000', 20, eps_str, '_tree_sample_1.0');                            
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_fixed_20_17903_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../ca-AstroPh-wcc', '../ca-AstroPh-wcc_hrgdiv_20_8_2', 20, eps_str, '_2.00_tree_sample_4.0');      
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_dendro_4), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(17903);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(4:7), rel_err_ef, 'd-', eps_arr, rel_err_der, 'x-', ...
%     eps_arr, rel_err_dendro_4, '+:', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-');      % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-'
% % legend('1K','TmF','EF','DER','HRG-MCMC','HRG-Fixed');       % ,'HRG-Div'
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% amazon (5s)
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 3.2 6.4 12.7 19.1 25.4 38.2];
% % eps_str = {'2.0', '3.2', '6.4', '12.7' ,'19.1', '25.4', '38.2'};
% eps_arr=[2.0 3.2 6.4 9.5 12.7 15.9 19.1];
% eps_str = {'2.0', '3.2', '6.4', '9.5', '12.7', '15.9', '19.1'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../com_amazon_ungraph', '../com_amazon_ungraph_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../com_amazon_ungraph', '../com_amazon_ungraph_fixed_np_20_334863_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_1k', 20, eps_str, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_sg1k', 20, eps_str, ['_69' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_per1k', 20, eps_str, ['_69' r_str{r_pos}]);  
%             
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_tmf', 20, eps_str, '');  
%             
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_ef', 20, eps_str(5:7), '');             % eps_str(4:7)
% 
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_fixed_20_334863_1000', 20, eps_str, '_tree_sample_1.0');                 
%             
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_fixed_20_334863_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_amazon_ungraph', '../com_amazon_ungraph_hrgdiv_20_11_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
%             
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(334863);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(5:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-'); % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-'
% axis([0 1.6 0 0.7]) 
% legend('1K','TmF','EF','HRG-Fixed','SG1K');    % ,'HRG-Div'
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


%%% dblp (5s)
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 3.2 6.3 12.7 19.0 25.3 38.0];
% % eps_str = {'2.0', '3.2', '6.3', '12.7' ,'19.0', '25.3', '38.0'};
% eps_arr=[2.0 3.2 6.3 9.5 12.7 15.8 19.0];
% eps_str = {'2.0', '3.2', '6.3', '9.5', '12.7', '15.8', '19.0'};
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../com_dblp_ungraph', '../com_dblp_ungraph_louvain_dendro_50_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_np_20_317080_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_1k', 20, eps_str, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_sg1k', 20, eps_str, ['_68' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_per1k', 20, eps_str, ['_68' r_str{r_pos}]); 
% 
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_tmf', 20, eps_str, '');  
%             
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_ef', 20, eps_str(5:7), '');         % eps_str(4:7)    
% 
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps_str, '_tree_sample_1.0');                 
%             
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_hrgdiv_20_11_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(317080);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(5:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-'); % , eps_arr, rel_err_hrgdiv_4, 's--' , eps_arr, rel_err_sg1k, '^-' 
% % legend('1K','TmF','EF','HRG-Fixed');        % ,'HRG-Div'
% axis([0 1.6 0 0.6]) 
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')


%%% youtube (12s)
% x0=400;
% y0=200;
% width=400;
% height=300;
% % eps_arr=[2.0 3.5 7.0 13.9 20.9 27.9 41.8];
% % eps_str = {'2.0', '3.5', '7.0', '13.9' ,'20.9', '27.9', '41.8'};
% eps_arr=[2.0 3.5 7.0 10.5 13.9 17.4 20.9];
% eps_str = {'2.0', '3.5', '7.0', '10.5', '13.9', '17.4', '20.9'};
% 
% 
% [rel_err_louvain, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam] = aa_compute_util('../com_youtube_ungraph', '../com_youtube_ungraph_louvain_dendro_100_sample_10.0', 1);  
% [rel_err_fixed_np, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util('../com_youtube_ungraph', '../com_youtube_ungraph_fixed_np_20_1134890_1000_tree_sample_10.0', 20);  
% 
% [rel_err_1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_1k', 20, eps_str, '');  
% 
% [rel_err_sg1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_sg1k', 20, eps_str, ['_104' r_str{r_pos}]);  
% [rel_err_per1k, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_per1k', 20, eps_str, ['_104' r_str{r_pos}]);  
%             
% [rel_err_tmf, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_tmf', 20, eps_str, '');  
%             
% [rel_err_ef, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_ef', 20, eps_str(5:7), '');            % eps_str(4:7)     
% 
% % [rel_err_fixed, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_fixed_20_1134890_1000', 20, eps_str, '_tree_sample_1.0');                 
%             
% [rel_err_fixed_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_fixed_20_1134890_1000', 20, eps_str, '_tree_sample_4.0');                   
%             
% % [rel_err_hrgdiv_4, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~, ~] = aa_compute_util_all('../com_youtube_ungraph', '../com_youtube_ungraph_hrgdiv_20_12_2', 20, eps_str, '_2.00_tree_sample_4.0');                   
% 
% display([rel_err_louvain, rel_err_fixed_np, min(rel_err_1k), min(rel_err_tmf), min(rel_err_ef), min(rel_err_fixed_4)]); %, min(rel_err_hrgdiv_4)]);
% 
% figure;
% eps_arr = eps_arr/log(1134890);
% plot(eps_arr, rel_err_1k, 's-', eps_arr, rel_err_tmf, 'o-', eps_arr(5:7), rel_err_ef, 'x-', eps_arr, rel_err_fixed_4, '*--', eps_arr, rel_err_per1k, '>-'); % , eps_arr, rel_err_hrgdiv_4, 's--', eps_arr, rel_err_sg1k, '^-' 
% % legend('1K','TmF','EF','HRG-Fixed');        % ,'HRG-Div'
% axis([0 1.6 0 0.7]) 
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% set(gca,'XTick', 0:0.25:1.5)
% ylabel('rel.err')
% xlabel('\epsilon/ln n')



