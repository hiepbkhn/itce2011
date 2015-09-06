function [ rel_err, e_deg_distr, e_dist_distr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam ] = aa_compute_util(graph_name, sample_name, n_samples)
% function [ rel_err ] = aa_compute_util(graph_name, sample_name, n_samples)

% graph_name = 'as20graph';
% sample_name = 'as20graph_dendro_50_1000_6474_1.0_sample';
% n_samples = 10;

load(graph_name);

aa_rename;
a_s_MD = double(a_s_MD);
a_s_EDiam = double(a_s_EDiam);
a_s_Diam = double(a_s_Diam);

% load n_samples
sum_deg_distr = zeros(size(a_deg_distr,1),1);
sum_dist_distr = zeros(size(a_dist_distr,1),1);
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
    load([sample_name '.' int2str(i) '.mat'] );
    sum_deg_distr = sum_deg_distr + deg_distr;
    sum_dist_distr = sum_dist_distr + dist_distr;
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

sum_deg_distr = sum_deg_distr/n_samples;
sum_dist_distr = sum_dist_distr/n_samples;
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
e_deg_distr = norm(sum_deg_distr-a_deg_distr,1)/2;
e_dist_distr = norm(sum_dist_distr-a_dist_distr,1)/2;
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
% rel_err = (e_deg_distr + e_dist_distr + e_AD + e_MD + e_DV + e_CC + e_APD + e_CL + e_EDiam + e_Diam)/10;

rel_err = (e_deg_distr + e_dist_distr + e_AD + e_MD + e_DV + e_CC + e_PL + e_APD + e_CL + e_EDiam + e_Diam)/11.0;

% for TmF table
e_deg_distr = norm(sum_deg_distr-a_deg_distr,1);
e_dist_distr = norm(sum_dist_distr-a_dist_distr,1);
e_AD = sum_AD;
e_MD = sum_MD;
e_DV = sum_DV;
e_CC = sum_CC;
e_PL = sum_PL;
e_APD = sum_APD;
e_CL = sum_CL;
e_EDiam = sum_EDiam;
e_Diam = sum_Diam;

end

