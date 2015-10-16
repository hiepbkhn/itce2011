
% Oct 15, 2015
% LouvainOpt
% draw mod,f1,nmi vs. epsilon for different values of (k, max_level)


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 1;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
kArr = [2,3,4,5,6,10];
levelArr = [10,7,5,4,4,3];
burnfactor = 20;
ratio = 2.0;
    
modArrBest = zeros(3,length(kArr));
f1ArrBest = zeros(3,length(kArr));
nmiArrBest = zeros(3,length(kArr));
comArrBest = zeros(3,length(kArr));

modArrPart2 = zeros(3,length(kArr));
f1ArrPart2 = zeros(3,length(kArr));
nmiArrPart2 = zeros(3,length(kArr));
comArrPart2 = zeros(3,length(kArr));

for pos=1:length(n_list)
    dataname = dataname_list{pos};
    for i1=1:length(kArr)
        k = kArr(i1);
        level = levelArr(i1);
        [modArrBest(pos,i1), f1ArrBest(pos,i1), nmiArrBest(pos,i1), comArrBest(pos,i1), modArrPart2(pos,i1), f1ArrPart2(pos,i1), nmiArrPart2(pos,i1), comArrPart2(pos,i1)] = ...
            aaa_readfile2([dataname '_nmd_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k)]);

    end
end

x0=400;
y0=200;
width=400;
height=300;


figure;
% plot(1:6, modArrBest(1,:), 's-', 1:6, f1ArrBest(1,:), 's-', 1:6, nmiArrBest(1,:), 's-');
% hold on
% plot(1:6, modArrBest(2,:), 's-', 1:6, f1ArrBest(2,:), 's-', 1:6, nmiArrBest(2,:), 's-');
% hold on
plot(1:6, modArrBest(3,:), 's-', 1:6, f1ArrBest(3,:), 's-', 1:6, nmiArrBest(3,:), 's-');

h_legend = legend('modularity','avg.f1score','NMI');
set(h_legend,'FontSize',14);
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'YTick',[0.2 0.4 0.6 0.8])
set(gca,'XTickLabel',{'(2,10)','(3,7)','(5,4)','(5,4)','(6,4)','(10,3)'}, 'FontSize', 14)

% figure;
% plot(1:6, comArrBest);

% figure;
% plot(f1ArrBest);
% legend('k=2','k=3','k=4','k=5','k=6','k=10');