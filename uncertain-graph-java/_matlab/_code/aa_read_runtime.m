% Dec 1, 2015

function [ t_1k, t_tmf, t_ef, t_der, t_dendro, t_hrgdiv, t_fixed  ] = aa_read_runtime( graph_name )
%AA_READ_RUNTIME Summary of this function goes here
%   Detailed explanation goes here

%%% 1k
path = ['../../_runtime/' graph_name '_1k_*.*'];
list = dir(path);

sum_t_1k = 0;
for i = 1:length(list)
    file = list(i).name;
    
    load(['../../_runtime/' file]);
    sum_t_1k = sum_t_1k + mean(timeArr);
end
t_1k = sum_t_1k/length(list);
% display(t_1k);

%%% tmf
path = ['../../_runtime/' graph_name '_tmf_*.*'];
list = dir(path);

sum_t_tmf = 0;
for i = 1:length(list)
    file = list(i).name;
    
    load(['../../_runtime/' file]);
    sum_t_tmf = sum_t_tmf + mean(timeArr);
end
t_tmf = sum_t_tmf/length(list);

%%% ef
path = ['../../_runtime/' graph_name '_ef_*.*'];
list = dir(path);

sum_t_ef = 0;
for i = 1:length(list)
    file = list(i).name;
    
    load(['../../_runtime/' file]);
    sum_t_ef = sum_t_ef + mean(timeArr);
end
t_ef = sum_t_ef/length(list);

%%% der
path = ['../../_runtime/' graph_name '_der_*.*'];
list = dir(path);

if isempty(list)
    t_der = -1;
else
    sum_t_der = 0;
    for i = 1:length(list)
        file = list(i).name;

        load(['../../_runtime/' file]);
        sum_t_der = sum_t_der + sum(timeAux) + mean(timeArr);
    end
    t_der = sum_t_der/length(list);
end

%%% dendro
path = ['../../_runtime/' graph_name '_dendro_*.*'];
list = dir(path);

if isempty(list)
    t_dendro = -1;
else
    sum_t_dendro = 0;
    for i = 1:length(list)
        file = list(i).name;

        load(['../../_runtime/' file]);
        sum_t_dendro = sum_t_dendro + timeFit + mean(sampleArr);
    end
    t_dendro = sum_t_dendro/length(list);
end

%%% hrgdiv
path = ['../../_runtime/' graph_name '_hrgdiv_*.*'];
list = dir(path);

sum_t_hrgdiv = 0;
for i = 1:length(list)
    file = list(i).name;
    
    load(['../../_runtime/' file]);
    sum_t_hrgdiv = sum_t_hrgdiv + mean(timeFit1) + mean(timeFit2) + mean(timeFit3) + mean(sampleArr);
end
t_hrgdiv = sum_t_hrgdiv/length(list);

%%% fixed
path = ['../../_runtime/' graph_name '_fixed_*.*'];
list = dir(path);

sum_t_fixed = 0;
for i = 1:length(list)
    file = list(i).name;
    
    load(['../../_runtime/' file]);
    sum_t_fixed = sum_t_fixed + timeFit + mean(sampleArr);
end
t_fixed = sum_t_fixed/length(list);

end

