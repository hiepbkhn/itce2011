


% Oct 15, 2015
% LouvainModDiv
% draw mod,f1,nmi vs. epsilon for different values of (k, max_level)


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 2;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
kArr = [2,3,4,5,6,10];
levelArr = [10,7,5,4,4,3];
burnfactor = 20;
ratio = 2.0;
    
modArrBest = zeros(length(kArr), length(epsArr));
f1ArrBest = zeros(length(kArr), length(epsArr));
nmiArrBest = zeros(length(kArr), length(epsArr));
comArrBest = zeros(length(kArr), length(epsArr));

modArrPart2 = zeros(length(kArr), length(epsArr));
f1ArrPart2 = zeros(length(kArr), length(epsArr));
nmiArrPart2 = zeros(length(kArr), length(epsArr));
comArrPart2 = zeros(length(kArr), length(epsArr));

for i1=1:length(kArr)
    k = kArr(i1);
    level = levelArr(i1);
    for i2=1:length(epsArr)
        eps = epsArr(i2);
        [modArrBest(i1,i2), f1ArrBest(i1,i2), nmiArrBest(i1,i2), comArrBest(i1,i2), modArrPart2(i1,i2), f1ArrPart2(i1,i2), nmiArrPart2(i1,i2), comArrPart2(i1,i2)] = ...
            aaa_readfile2([dataname '_md_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
    end
end

x0=400;
y0=200;
width=400;
height=300;

% plot(epsArr/log_n, modArrBest(1,:), 's-', epsArr/log_n, modArrBest(2,:), 's-', epsArr/log_n, modArrBest(3,:), 's-', ...
%     epsArr/log_n, modArrBest(4,:), 's-', epsArr/log_n, modArrBest(5,:), 's-', epsArr/log_n, modArrBest(6,:), 's-');
% ylabel('modularity')

% plot(epsArr/log_n, f1ArrBest(1,:), 's-', epsArr/log_n, f1ArrBest(2,:), 's-', epsArr/log_n, f1ArrBest(3,:), 's-', ...
%     epsArr/log_n, f1ArrBest(4,:), 's-', epsArr/log_n, f1ArrBest(5,:), 's-', epsArr/log_n, f1ArrBest(6,:), 's-');
% ylabel('avg.F1score')

% plot(epsArr/log_n, nmiArrBest(1,:), 's-', epsArr/log_n, nmiArrBest(2,:), 's-', epsArr/log_n, nmiArrBest(3,:), 's-', ...
%     epsArr/log_n, nmiArrBest(4,:), 's-', epsArr/log_n, nmiArrBest(5,:), 's-', epsArr/log_n, nmiArrBest(6,:), 's-');
% ylabel('NMI')

plot(epsArr/log_n, comArrBest(1,:), 's-', epsArr/log_n, comArrBest(2,:), 's-', epsArr/log_n, comArrBest(3,:), 's-', ...
    epsArr/log_n, comArrBest(4,:), 's-', epsArr/log_n, comArrBest(5,:), 's-', epsArr/log_n, comArrBest(6,:), 's-');
ylabel('#communities')

% h_legend = legend('k=2,maxL=10','k=3,maxL=7','k=4,maxL=5','k=5,maxL=4','k=6,maxL=4','k=10,maxL=3');
h_legend = legend('(2,10)','(3,7)','(4,5)','(5,4)','(6,4)','(10,3)');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
xlabel('\epsilon / ln(n)')



% figure;
% plot(epsArr, f1ArrBest(1,:), epsArr, f1ArrBest(2,:), epsArr, f1ArrBest(3,:), epsArr, f1ArrBest(4,:), epsArr, f1ArrBest(5,:), epsArr, f1ArrBest(6,:));
% legend('k=2','k=3','k=4','k=5','k=6','k=10');

