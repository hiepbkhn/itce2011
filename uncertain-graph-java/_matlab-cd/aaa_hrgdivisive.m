
% Ot 17, 2015
% 

% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 1;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
level = 10;
burnfactor = 20;
ratioArr = [2.0, 1.5, 1.0];


modArrBest = zeros(length(ratioArr), length(epsArr));
f1ArrBest = zeros(length(ratioArr), length(epsArr));
nmiArrBest = zeros(length(ratioArr), length(epsArr));
comArrBest = zeros(length(ratioArr), length(epsArr));

modArrPart2 = zeros(length(ratioArr), length(epsArr));
f1ArrPart2 = zeros(length(ratioArr), length(epsArr));
nmiArrPart2 = zeros(length(ratioArr), length(epsArr));
comArrPart2 = zeros(length(ratioArr), length(epsArr));

for i1=1:length(ratioArr)
    ratio = ratioArr(i1);
    for i2=1:length(epsArr)
        eps = epsArr(i2);
        [modArrBest(i1,i2), f1ArrBest(i1,i2), nmiArrBest(i1,i2), comArrBest(i1,i2), modArrPart2(i1,i2), f1ArrPart2(i1,i2), nmiArrPart2(i1,i2), comArrPart2(i1,i2)] = ...
            aaa_readfile2([dataname '_hd_' int2str(burnfactor) '_' int2str(level) '_' sprintf('%.1f', eps) '_' sprintf('%.2f', ratio)]);
    end
end

x0=400;
y0=200;
width=400;
height=300;

%%%% Best
% figure;
% plot(epsArr/log_n, modArrBest(1,:), 's-', epsArr/log_n, modArrBest(2,:), 's-', epsArr/log_n, modArrBest(3,:), 's-');
% ylabel('modularity')
% set(gca,'FontSize', 12)
% xlabel('\epsilon / ln(n)')
% 
% figure;
% plot(epsArr/log_n, f1ArrBest(1,:), 's-', epsArr/log_n, f1ArrBest(2,:), 's-', epsArr/log_n, f1ArrBest(3,:), 's-');
% ylabel('avg.F1score')
% legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
% xlabel('\epsilon / ln(n)')
% 
% figure;
% plot(epsArr/log_n, nmiArrBest(1,:), 's-', epsArr/log_n, nmiArrBest(2,:), 's-', epsArr/log_n, nmiArrBest(3,:), 's-');
% ylabel('NMI')
% legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
% xlabel('\epsilon / ln(n)')
% 
% figure;
% plot(epsArr/log_n, comArrBest(1,:), 's-', epsArr/log_n, comArrBest(2,:), 's-', epsArr/log_n, comArrBest(3,:), 's-');
% ylabel('#communities')
% legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
% xlabel('\epsilon / ln(n)')


%%%% Part2
figure;
plot(epsArr/log_n, modArrPart2(1,:), 's-', epsArr/log_n, modArrPart2(2,:), 's-', epsArr/log_n, modArrPart2(3,:), 's-');
ylabel('modularity')
legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
xlabel('\epsilon / ln(n)')

figure;
plot(epsArr/log_n, f1ArrPart2(1,:), 's-', epsArr/log_n, f1ArrPart2(2,:), 's-', epsArr/log_n, f1ArrPart2(3,:), 's-');
ylabel('avg.F1score')
legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
xlabel('\epsilon / ln(n)')

figure;
plot(epsArr/log_n, nmiArrPart2(1,:), 's-', epsArr/log_n, nmiArrPart2(2,:), 's-', epsArr/log_n, nmiArrPart2(3,:), 's-');
ylabel('NMI')
legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
xlabel('\epsilon / ln(n)')

figure;
plot(epsArr/log_n, comArrPart2(1,:), 's-', epsArr/log_n, comArrPart2(2,:), 's-', epsArr/log_n, comArrPart2(3,:), 's-');
ylabel('#communities')
legend('\lambda=2.0','\lambda=1.5','\lambda=1.0');
xlabel('\epsilon / ln(n)')

% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 12)


