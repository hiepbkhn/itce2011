function [ avg_mod_best, avg_f1_best, avg_nmi_best, avg_com_best, avg_mod_part2, avg_f1_part2, avg_nmi_part2, avg_com_part2] = aaa_readfile2( file_name )
% read .mat files containing .best + .part2



load([file_name '.mat'] );
avg_mod_best = mean(modArrBest);    
avg_f1_best = mean(f1ArrBest);   
avg_nmi_best= mean(nmiArrBest);   
avg_com_best= mean(comArrBest); 

avg_mod_part2 = mean(modArrPart2);    
avg_f1_part2 = mean(f1ArrPart2);   
avg_nmi_part2= mean(nmiArrPart2);
avg_com_part2= mean(comArrPart2);

end

