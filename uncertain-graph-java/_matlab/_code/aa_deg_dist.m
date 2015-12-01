function [avg_degArr, avg_distArr] = aa_deg_dist(sample_name, n_samples)

% load n_samples

load([sample_name '.0.mat'] );
sum_degArr = degArr;
distArr = [distArr zeros(1,50-length(distArr))];    % padding
sum_distArr = distArr;

for i=1:n_samples-1
    load([sample_name '.' int2str(i) '.mat'] );
    sum_degArr = sum_degArr + degArr;
    distArr = [distArr zeros(1,50-length(distArr))];    % padding
    sum_distArr = sum_distArr + distArr;
    
end

avg_degArr = sum_degArr/n_samples;
avg_distArr = sum_distArr/n_samples;

end