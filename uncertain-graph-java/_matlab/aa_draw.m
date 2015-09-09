
% draw degree and distance distributions of TmF scheme

% polbooks
clear

% graph_name = 'polbooks';
% graph_name = 'polblogs';
% graph_name = 'as20graph';
% graph_name = 'wiki-Vote';
% graph_name = 'ca-HepPh';
graph_name = 'ca-AstroPh';

eps_arr = {'2.0', '4.0', '8.0', '16.0'};
color = {'k'; 'g'; 'r'; 'c'};

load(graph_name);
aa_rename;
n_nodes = size(a_deg_distr,1);    
loglog(1:n_nodes-1,a_deg_distr(2:n_nodes),'b');
hold on;

for i=1:4
    eps_str = eps_arr{i};
    
    name = strcat(strcat(graph_name, '_filter_'), eps_str);
    [avg_deg_distr, avg_dist_distr] = aa_deg_dist(name,10);

   
    
    loglog(1:n_nodes-1,avg_deg_distr(2:n_nodes), color{i});
    hold on
end
hold off
legend('true graph','\epsilon_1=2.0','\epsilon_1=4.0','\epsilon_1=8.0','\epsilon_1=16.0');


figure;
max_dist = max(find(a_dist_distr(1:n_nodes) > 0));
plot(1:max_dist,a_dist_distr(1:max_dist), 'b');
hold on;

for i=1:4
    eps_str = eps_arr{i};
    
    name = strcat(strcat(graph_name, '_filter_'), eps_str);
    [avg_deg_distr, avg_dist_distr] = aa_deg_dist(name,10);
    
    max_dist = max(find(avg_dist_distr(1:n_nodes) > 0));
    
    plot(1:max_dist,avg_dist_distr(1:max_dist), color{i});
    hold on;
end
hold off;
legend('true graph','\epsilon_1=2.0','\epsilon_1=4.0','\epsilon_1=8.0','\epsilon_1=16.0');










