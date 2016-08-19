
% Aug 19, 2016
%   - for each metric, draw relative error against eps


x0=400;
y0=200;
width=400;
height=300;
fontsize=20;

%%%%% ca-HepPh-wcc
% dataname = 'ca-HepPh';
% eps_arr=[2.0 2.3 4.7 7.0 9.3 11.7 14.0];
% eps_arr = eps_arr/log(11204);
% eps_str = {'2.0', '2.3', '4.7', '7.0', '9.3', '11.7', '14.0'};
% 
% [rel_err_1k, e_degArr_1k, e_distArr_1k, e_cutArr_1k, e_AD_1k, e_MD_1k, e_DV_1k, e_CC_1k, e_PL_1k, e_APD_1k, e_CL_1k, e_EDiam_1k, e_Diam_1k] = ...
%     aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_1k', 20, eps_str, ''); 
%             
% [rel_err_tmf, e_degArr_tmf, e_distArr_tmf, e_cutArr_tmf, e_AD_tmf, e_MD_tmf, e_DV_tmf, e_CC_tmf, e_PL_tmf, e_APD_tmf, e_CL_tmf, e_EDiam_tmf, e_Diam_tmf] = ...
%     aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_tmf', 20, eps_str, ''); 
% 
% [rel_err_fixed, e_degArr_fixed, e_distArr_fixed, e_cutArr_fixed, e_AD_fixed, e_MD_fixed, e_DV_fixed, e_CC_fixed, e_PL_fixed, e_APD_fixed, e_CL_fixed, e_EDiam_fixed, e_Diam_fixed] = ...
%     aa_compute_util_all('../ca-HepPh-wcc', '../ca-HepPh-wcc_fixed_20_11204_1000', 20, eps_str, '_tree_sample_4.0');   
              
%%%%% dblp
dataname = 'dblp';
eps_arr=[2.0 3.2 6.3 9.5 12.7 15.8 19.0];
eps_arr = eps_arr/log(317080);
eps_str = {'2.0', '3.2', '6.3', '9.5', '12.7', '15.8', '19.0'};
[rel_err_1k, e_degArr_1k, e_distArr_1k, e_cutArr_1k, e_AD_1k, e_MD_1k, e_DV_1k, e_CC_1k, e_PL_1k, e_APD_1k, e_CL_1k, e_EDiam_1k, e_Diam_1k] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_1k', 20, eps_str, '');
            
[rel_err_tmf, e_degArr_tmf, e_distArr_tmf, e_cutArr_tmf, e_AD_tmf, e_MD_tmf, e_DV_tmf, e_CC_tmf, e_PL_tmf, e_APD_tmf, e_CL_tmf, e_EDiam_tmf, e_Diam_tmf] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_tmf', 20, eps_str, '');  

[rel_err_fixed, e_degArr_fixed, e_distArr_fixed, e_cutArr_fixed, e_AD_fixed, e_MD_fixed, e_DV_fixed, e_CC_fixed, e_PL_fixed, e_APD_fixed, e_CL_fixed, e_EDiam_fixed, e_Diam_fixed] = ...
    aa_compute_util_all('../com_dblp_ungraph', '../com_dblp_ungraph_fixed_20_317080_1000', 20, eps_str, '_tree_sample_4.0');   


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% s_AD             
plot(eps_arr, e_AD_1k, 's-', eps_arr, e_AD_tmf, 'o-', eps_arr, e_AD_fixed, '*-'); axis([0 1.6 0 1]);
h_legend = legend('1K','TmF','HRG-Fixed');
set(h_legend,'FontSize', 24);
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-AD_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_MD             
figure;
plot(eps_arr, e_MD_1k, 's-', eps_arr, e_MD_tmf, 'o-', eps_arr, e_MD_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-MD_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_DV             
figure;
plot(eps_arr, e_DV_1k, 's-', eps_arr, e_DV_tmf, 'o-', eps_arr, e_DV_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-DV_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')


%%% s_CC             
figure;
plot(eps_arr, e_CC_1k, 's-', eps_arr, e_CC_tmf, 'o-', eps_arr, e_CC_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-CC_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_PL             
figure;
plot(eps_arr, e_PL_1k, 's-', eps_arr, e_PL_tmf, 'o-', eps_arr, e_PL_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-PL_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_APD             
figure;
plot(eps_arr, e_APD_1k, 's-', eps_arr, e_APD_tmf, 'o-', eps_arr, e_APD_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-APD_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_CL             
figure;
plot(eps_arr, e_CL_1k, 's-', eps_arr, e_CL_tmf, 'o-', eps_arr, e_CL_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-CL_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_EDiam             
figure;
plot(eps_arr, e_EDiam_1k, 's-', eps_arr, e_EDiam_tmf, 'o-', eps_arr, e_EDiam_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-EDiam_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% s_Diam             
figure;
plot(eps_arr, e_Diam_1k, 's-', eps_arr, e_Diam_tmf, 'o-', eps_arr, e_Diam_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-Diam_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')


%%% degArr (s_DD)            
figure;
plot(eps_arr, e_degArr_1k, 's-', eps_arr, e_degArr_tmf, 'o-', eps_arr, e_degArr_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-DD_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% distArr (s_PDD)            
figure;
plot(eps_arr, e_distArr_1k, 's-', eps_arr, e_distArr_tmf, 'o-', eps_arr, e_distArr_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-PDD_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')

%%% cutArr             
figure;
plot(eps_arr, e_cutArr_1k, 's-', eps_arr, e_cutArr_tmf, 'o-', eps_arr, e_cutArr_fixed, '*-'); axis([0 1.6 0 1]);
% legend('caHepPh:1K','caHepPh:TmF','caHepPh:HRG-Fixed','dblp:1K','dblp:TmF','dblp:HRG-Fixed');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', fontsize)
set(gca,'XTick', 0:0.25:1.5)
ylabel('rel.err')
xlabel('\epsilon/ln n')
eps_file = ['s-cut_' dataname '.eps'];
saveas(gcf, eps_file, 'epsc')
