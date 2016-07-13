%%%
% Jun 22, 2016
% - copied from uncertain-graph-java\_matlab\_code\aa_compute_util.m
%
%
function [ rel_err, e_degArr, e_distArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam ] = compute_util(graph_name, sample_name, n_samples)

% graph_name = 'as20graph';
% sample_name = 'as20graph_dendro_50_1000_6474_1.0_sample';
% n_samples = 10;

load(graph_name);

distArr = distArr/sum(distArr);         % normalize
distArr = [distArr zeros(1,50-length(distArr))];    % padding

rename_util;


a_s_MD = double(a_s_MD);
a_s_EDiam = double(a_s_EDiam);
a_s_Diam = double(a_s_Diam);

% load n_samples
sum_degArr = zeros(size(a_degArr,1),1);
sum_distArr = zeros(size(a_distArr,1),1);
sum_AD = 0;
sum_MD = 0;
sum_DV = 0;
sum_CC = 0;
sum_PL = 0;
sum_APD = 0;
sum_CL = 0;
sum_EDiam = 0;
sum_Diam = 0;

for i=0:n_samples-1
%     fprintf('sample %d\n',i);   

    load([sample_name '.' int2str(i) '.mat'] );
    sum_degArr = sum_degArr + degArr;
    
    distArr = distArr/sum(distArr);     % normalize
    distArr = [distArr zeros(1,50-length(distArr))];
    
    sum_distArr = sum_distArr + distArr;    
    
    sum_AD = sum_AD + s_AD;
    sum_MD = sum_MD + double(s_MD);
    sum_DV = sum_DV + s_DV;
    sum_CC = sum_CC + s_CC;
    sum_PL = sum_PL + s_PL;
    sum_APD = sum_APD + s_APD;
    sum_CL = sum_CL + s_CL;
    sum_EDiam = sum_EDiam + double(s_EDiam);
    sum_Diam = sum_Diam + double(s_Diam);   
end

sum_degArr = sum_degArr/n_samples;
sum_distArr = sum_distArr/n_samples;

sum_AD = sum_AD/n_samples;
sum_MD = sum_MD/n_samples;
sum_DV = sum_DV/n_samples;
sum_CC = sum_CC/n_samples;
sum_PL = sum_PL/n_samples;
sum_APD = sum_APD/n_samples;
sum_CL = sum_CL/n_samples;
sum_EDiam = sum_EDiam/n_samples;
sum_Diam = sum_Diam/n_samples;

% relative errors 
e_degArr = norm(sum_degArr-a_degArr,1)/2;
e_distArr = norm(sum_distArr-a_distArr,1)/2;

e_AD = abs(sum_AD - a_s_AD)/a_s_AD;
e_MD = abs(sum_MD - a_s_MD)/a_s_MD;
e_DV = abs(sum_DV - a_s_DV)/a_s_DV;
e_CC = abs(sum_CC - a_s_CC)/a_s_CC;
e_PL = abs(sum_PL - a_s_PL)/a_s_PL;
e_APD = abs(sum_APD - a_s_APD)/a_s_APD;
e_CL = abs(sum_CL - a_s_CL)/a_s_CL;
e_EDiam = abs(sum_EDiam - a_s_EDiam)/a_s_EDiam;
e_Diam = abs(sum_Diam - a_s_Diam)/a_s_Diam;

% no e_PL
% rel_err = (e_degArr + e_distArr + e_AD + e_MD + e_DV + e_CC + e_APD + e_CL + e_EDiam + e_Diam)/10;                % w/o e_PL

% rel_err = (e_degArr + e_distArr + e_AD + e_MD + e_DV + e_CC + e_PL + e_APD + e_CL + e_EDiam + e_Diam)/11.0;


% rel_err = (e_degArr + e_distArr + e_cutArr + e_AD + e_MD + e_DV + e_CC + e_APD + e_CL + e_EDiam + e_Diam)/11.0;     % w/o e_PL

rel_err = (e_degArr + e_distArr + e_AD + e_MD + e_DV + e_CC + e_PL + e_APD + e_CL + e_EDiam + e_Diam)/11.0;

end

