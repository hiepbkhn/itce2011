function [avg_deg_distr, avg_dist_distr] = aa_deg_dist(sample_name, n_samples)

% load n_samples

load([sample_name '.0.mat'] );
sum_deg_distr = deg_distr;
sum_dist_distr = dist_distr;

for i=1:n_samples-1
    load([sample_name '.' int2str(i) '.mat'] );
    sum_deg_distr = sum_deg_distr + deg_distr;
    sum_dist_distr = sum_dist_distr + dist_distr;
    
end

avg_deg_distr = sum_deg_distr/n_samples;
avg_dist_distr = sum_dist_distr/n_samples;

end