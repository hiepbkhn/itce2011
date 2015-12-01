
% draw degree and distance distributions of TmF scheme

clear

% graph_name = 'polbooks';
% graph_name = 'polblogs-wcc';
% graph_name = '../as20graph';
% graph_name = 'wiki-Vote-wcc';
% graph_name = 'ca-HepPh-wcc';
% graph_name = '../ca-AstroPh-wcc';
graph_name = '../com_amazon_ungraph';
% graph_name = 'com_dblp_ungraph';
% graph_name = 'com_youtube_ungraph';

eps_arr = {'2.0', '4.4', '8.8' , '17.6'};     % as20graph (2.0, 0.5ln, ln, 2ln)
eps_arr = {'2.0', '4.9', '9.8' , '19.6'};     % ca-AstroPh
eps_arr = {'2.0', '6.4', '12.7' , '25.4'};     % amazon

color = {'k'; 'g'; 'r'; 'c'};

x0=400;
y0=200;
width=400;
height=300;

load(graph_name);
aa_rename;
n_nodes = length(a_degArr);    
loglog(1:n_nodes-1,a_degArr(2:n_nodes),'b');
hold on;

for i=1:4
    eps_str = eps_arr{i};
    
    name = strcat(strcat(graph_name, '_tmf_'), eps_str);
    [avg_degArr, avg_distArr] = aa_deg_dist(name,10);

   
    
    loglog(1:n_nodes-1,avg_degArr(2:n_nodes), color{i});
    hold on
end
hold off
legend('true graph','\epsilon_1=2.0','\epsilon_1=0.5ln n','\epsilon_1=ln n','\epsilon_1=2ln n');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)

figure;
max_dist = max(find(a_distArr > 0));
a_distArr = a_distArr/sum(a_distArr);   % normalize
plot(1:max_dist,a_distArr(1:max_dist), 'b');
hold on;

for i=1:4
    eps_str = eps_arr{i};
    
    name = strcat(strcat(graph_name, '_tmf_'), eps_str);
    [avg_degArr, avg_distArr] = aa_deg_dist(name,10);
    avg_distArr = avg_distArr/sum(avg_distArr);     % normalize
    
    max_dist = max(find(avg_distArr > 0));
    
    plot(1:max_dist,avg_distArr(1:max_dist), color{i});
    hold on;
end
hold off;
legend('true graph','\epsilon_1=2.0','\epsilon_1=0.5ln n','\epsilon_1=ln n','\epsilon_1=2ln n');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)










