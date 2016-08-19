function [ avg_time, avg_louvain] = aaa_readtime( file_name, type )
% read .mat file containing .part


load([ '../_runtime-cd/' file_name '.mat'] );
if type == 1    % EF, TmF, LDP
    avg_time = mean(timeArr);    
    avg_louvain = mean(louvainArr);   
else
    avg_time = -1;
    avg_louvain = mean(louvainArr);   
end



end

