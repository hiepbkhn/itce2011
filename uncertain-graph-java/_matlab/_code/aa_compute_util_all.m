%%%
% Nov 25, 2015
%   - 
function [ rel_err, e_degArr, e_distArr, e_cutArr, e_AD, e_MD, e_DV, e_CC, e_PL, e_APD, e_CL, e_EDiam, e_Diam ] = aa_compute_util_all(graph_name, file_name, n_samples, eps_arr, suffix)
% return: each var is an array of length(eps_arr)


n_eps = length(eps_arr);

rel_err = zeros(n_eps,1);
e_degArr = zeros(n_eps,1);
e_distArr = zeros(n_eps,1);
e_cutArr = zeros(n_eps,1);
e_AD = zeros(n_eps,1);
e_MD = zeros(n_eps,1);
e_DV = zeros(n_eps,1);
e_CC = zeros(n_eps,1);
e_PL = zeros(n_eps,1);
e_APD = zeros(n_eps,1);
e_CL = zeros(n_eps,1);
e_EDiam = zeros(n_eps,1);
e_Diam = zeros(n_eps,1);

for i = 1:n_eps
    eps = char(eps_arr(i));
    
    sample_name = strcat(file_name, strcat('_', strcat(eps, suffix)));
    [rel_err(i), e_degArr(i), e_distArr(i), e_cutArr(i), e_AD(i), e_MD(i), e_DV(i), e_CC(i), e_PL(i), e_APD(i), e_CL(i), e_EDiam(i), e_Diam(i)] = aa_compute_util(graph_name, sample_name, n_samples);
      
    
end


end

