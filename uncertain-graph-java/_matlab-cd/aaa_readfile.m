function [ avg_mod, avg_f1, avg_nmi, avg_com] = aaa_readfile( file_name )
% read .mat file containing .part


load([file_name '.mat'] );
avg_mod = mean(modArr);    
avg_f1 = mean(f1Arr);   
avg_nmi= mean(nmiArr);   
avg_com= mean(comArr);   


end

