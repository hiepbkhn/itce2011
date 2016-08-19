
% Dec 1, 2015
%   - for each metric, draw relative error against eps


x0=400;
y0=200;
width=400;
height=300;

%%%%% ca-HepPh-wcc
eps_arr=[2.0 2.3 4.7 9.3 14.0 18.6 28.0];
eps_str = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.6', '28.0'};
eps_str2 = {'2.0', '2.3', '4.7', '9.3' ,'14.0', '18.7', '28.0'};

[rel_err_1k, e_degArr_1k, e_distArr_1k, e_cutArr_1k, e_AD_1k, e_MD_1k, e_DV_1k, e_CC_1k, e_PL_1k, e_APD_1k, e_CL_1k, e_EDiam_1k, e_Diam_1k] = ...
    aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_1k', 20, eps_str2, ''); 
            
[rel_err_tmf, e_degArr_tmf, e_distArr_tmf, e_cutArr_tmf, e_AD_tmf, e_MD_tmf, e_DV_tmf, e_CC_tmf, e_PL_tmf, e_APD_tmf, e_CL_tmf, e_EDiam_tmf, e_Diam_tmf] = ...
    aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_tmf', 20, eps_str2, ''); 

[rel_err_fixed, e_degArr_fixed, e_distArr_fixed, e_cutArr_fixed, e_AD_fixed, e_MD_fixed, e_DV_fixed, e_CC_fixed, e_PL_fixed, e_APD_fixed, e_CL_fixed, e_EDiam_fixed, e_Diam_fixed] = ...
    aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_4.0');   
              
%%%%% dblp
eps2_arr=[2.0 3.2 6.3 12.7 19.0 25.3 38.0];
eps2_str = {'2.0', '3.2', '6.3', '12.7' ,'19.0', '25.3', '38.0'};
[rel2_err_1k, e2_degArr_1k, e2_distArr_1k, e2_cutArr_1k, e2_AD_1k, e2_MD_1k, e2_DV_1k, e2_CC_1k, e2_PL_1k, e2_APD_1k, e2_CL_1k, e2_EDiam_1k, e2_Diam_1k] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_1k', 20, eps2_str, '');
            
[rel2_err_tmf, e2_degArr_tmf, e2_distArr_tmf, e2_cutArr_tmf, e2_AD_tmf, e2_MD_tmf, e2_DV_tmf, e2_CC_tmf, e2_PL_tmf, e2_APD_tmf, e2_CL_tmf, e2_EDiam_tmf, e2_Diam_tmf] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_tmf', 20, eps2_str, '');  

[rel2_err_fixed, e2_degArr_fixed, e2_distArr_fixed, e2_cutArr_fixed, e2_AD_fixed, e2_MD_fixed, e2_DV_fixed, e2_CC_fixed, e2_PL_fixed, e2_APD_fixed, e2_CL_fixed, e2_EDiam_fixed, e2_Diam_fixed] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps2_str, '_tree_sample_4.0');   

% %%% s_AD             
% plot(eps_arr, e_AD_1k, 's-', eps_arr, e_AD_tmf, 'o-', eps_arr, e_AD_fixed, '*-', eps2_arr, e2_AD_1k, 's-.', eps2_arr, e2_AD_tmf, 'o-.', eps2_arr, e2_AD_fixed, '*-.'); axis([0 40 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_MD             
% figure;
% plot(eps_arr, e_MD_1k, 's-', eps_arr, e_MD_tmf, 'o-', eps_arr, e_MD_fixed, '*-', eps2_arr, e2_MD_1k, 's-.', eps2_arr, e2_MD_tmf, 'o-.', eps2_arr, e2_MD_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_DV             
% figure;
% plot(eps_arr, e_DV_1k, 's-', eps_arr, e_DV_tmf, 'o-', eps_arr, e_DV_fixed, '*-', eps2_arr, e2_DV_1k, 's-.', eps2_arr, e2_DV_tmf, 'o-.', eps2_arr, e2_DV_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')



% %%% s_CC             
% figure;
% plot(eps_arr, e_CC_1k, 's-', eps_arr, e_CC_tmf, 'o-', eps_arr, e_CC_fixed, '*-', eps2_arr, e2_CC_1k, 's-.', eps2_arr, e2_CC_tmf, 'o-.', eps2_arr, e2_CC_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_PL             
% figure;
% plot(eps_arr, e_PL_1k, 's-', eps_arr, e_PL_tmf, 'o-', eps_arr, e_PL_fixed, '*-', eps2_arr, e2_PL_1k, 's-.', eps2_arr, e2_PL_tmf, 'o-.', eps2_arr, e2_PL_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_APD             
% figure;
% plot(eps_arr, e_APD_1k, 's-', eps_arr, e_APD_tmf, 'o-', eps_arr, e_APD_fixed, '*-', eps2_arr, e2_APD_1k, 's-.', eps2_arr, e2_APD_tmf, 'o-.', eps2_arr, e2_APD_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')


% %%% s_CL             
% figure;
% plot(eps_arr, e_CL_1k, 's-', eps_arr, e_CL_tmf, 'o-', eps_arr, e_CL_fixed, '*-', eps2_arr, e2_CL_1k, 's-.', eps2_arr, e2_CL_tmf, 'o-.', eps2_arr, e2_CL_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_EDiam             
% figure;
% plot(eps_arr, e_EDiam_1k, 's-', eps_arr, e_EDiam_tmf, 'o-', eps_arr, e_EDiam_fixed, '*-', eps2_arr, e2_EDiam_1k, 's-.', eps2_arr, e2_EDiam_tmf, 'o-.', eps2_arr, e2_EDiam_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')
% 
% %%% s_Diam             
% figure;
% plot(eps_arr, e_Diam_1k, 's-', eps_arr, e_Diam_tmf, 'o-', eps_arr, e_Diam_fixed, '*-', eps2_arr, e2_Diam_1k, 's-.', eps2_arr, e2_Diam_tmf, 'o-.', eps2_arr, e2_Diam_fixed, '*-.'); axis([0 40 0 1]);
% % legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)
% ylabel('rel.err')
% xlabel('\epsilon')



%%% degArr (s_DD)            
figure;
plot(eps_arr, e_degArr_1k, 's-', eps_arr, e_degArr_tmf, 'o-', eps_arr, e_degArr_fixed, '*-', eps2_arr, e2_degArr_1k, 's-.', eps2_arr, e2_degArr_tmf, 'o-.', eps2_arr, e2_degArr_fixed, '*-.'); axis([0 40 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
ylabel('rel.err')
xlabel('\epsilon')

%%% distArr (s_PDD)            
figure;
plot(eps_arr, e_distArr_1k, 's-', eps_arr, e_distArr_tmf, 'o-', eps_arr, e_distArr_fixed, '*-', eps2_arr, e2_distArr_1k, 's-.', eps2_arr, e2_distArr_tmf, 'o-.', eps2_arr, e2_distArr_fixed, '*-.'); axis([0 40 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
ylabel('rel.err')
xlabel('\epsilon')

%%% cutArr             
figure;
plot(eps_arr, e_cutArr_1k, 's-', eps_arr, e_cutArr_tmf, 'o-', eps_arr, e_cutArr_fixed, '*-', eps2_arr, e2_cutArr_1k, 's-.', eps2_arr, e2_cutArr_tmf, 'o-.', eps2_arr, e2_cutArr_fixed, '*-.'); axis([0 40 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
ylabel('rel.err')
xlabel('\epsilon')
