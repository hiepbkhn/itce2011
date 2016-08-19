


% Oct 15, 2015
% LouvainModDiv
% draw mod,f1,nmi vs. epsilon for different values of (k, max_level)


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 3;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
% epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];

kArr = [2,3,4,5,6,10];
levelArr = [10,7,5,4,4,3];
burnfactor = 50;
ratio = 2.0;

%%%%%%%%%%%%%%%%%%%%%%%%%% MOD-DIVISIVE %%%%%%%%%%%%%%%%%%%%%%%%%%
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


plot(epsArr/log_n, modArrBest(1,:), 's-', epsArr/log_n, modArrBest(2,:), 's-', epsArr/log_n, modArrBest(3,:), 's-', ...
    epsArr/log_n, modArrBest(4,:), 's-', epsArr/log_n, modArrBest(5,:), 's-', epsArr/log_n, modArrBest(6,:), 's-');
h_legend = legend('(2,10)','(3,7)','(4,5)','(5,4)','(6,4)','(10,3)', 'Location','northwest');
set(h_legend,'FontSize',14); 
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14)
ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
axis([0.1 0.5 0 0.4])

figure;
plot(epsArr/log_n, f1ArrBest(1,:), 's-', epsArr/log_n, f1ArrBest(2,:), 's-', epsArr/log_n, f1ArrBest(3,:), 's-', ...
    epsArr/log_n, f1ArrBest(4,:), 's-', epsArr/log_n, f1ArrBest(5,:), 's-', epsArr/log_n, f1ArrBest(6,:), 's-');
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14)
ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)

figure;
plot(epsArr/log_n, nmiArrBest(1,:), 's-', epsArr/log_n, nmiArrBest(2,:), 's-', epsArr/log_n, nmiArrBest(3,:), 's-', ...
    epsArr/log_n, nmiArrBest(4,:), 's-', epsArr/log_n, nmiArrBest(5,:), 's-', epsArr/log_n, nmiArrBest(6,:), 's-');
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14)
ylhand = get(gca,'ylabel'); set(ylhand,'string','NMI','fontsize',14)

figure;
plot(epsArr/log_n, comArrBest(1,:), 's-', epsArr/log_n, comArrBest(2,:), 's-', epsArr/log_n, comArrBest(3,:), 's-', ...
    epsArr/log_n, comArrBest(4,:), 's-', epsArr/log_n, comArrBest(5,:), 's-', epsArr/log_n, comArrBest(6,:), 's-');
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); set(gca,'Xtick',0.1:0.1:0.5);set(gca,'FontSize', 14)
ylhand = get(gca,'ylabel'); set(ylhand,'string','#communities','fontsize',14)
axis([0.1 0.5 0 12]);

% % h_legend = legend('k=2,maxL=10','k=3,maxL=7','k=4,maxL=5','k=5,maxL=4','k=6,maxL=4','k=10,maxL=3');
% h_legend = legend('(2,10)','(3,7)','(4,5)','(5,4)','(6,4)','(10,3)', 'Location','northwest');
set(gcf,'units','points','position',[x0,y0,width,height])




%%%%%%%%%%%%%%%%%%%%%%%%%% RATIO %%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%% ratio = 1.0

% dataname = 'com_amazon_ungraph';
% n = 334863;
% log_n = log(n);
% % epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
% epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];
% burnfactor = 20;
% % ratio = 1.0;
%     
% modArrBest = zeros(1, length(epsArr));
% f1ArrBest = zeros(1, length(epsArr));
% nmiArrBest = zeros(1, length(epsArr));
% comArrBest = zeros(1, length(epsArr));
% 
% modArrBest1 = zeros(1, length(epsArr));
% f1ArrBest1 = zeros(1, length(epsArr));
% nmiArrBest1 = zeros(1, length(epsArr));
% comArrBest1 = zeros(1, length(epsArr));
% 
% modArrBest2 = zeros(1, length(epsArr));
% f1ArrBest2 = zeros(1, length(epsArr));
% nmiArrBest2 = zeros(1, length(epsArr));
% comArrBest2 = zeros(1, length(epsArr));
% 
% 
% k = 4;
% level = 5;
% for i2=1:length(epsArr)
%     eps = epsArr(i2);
%     [modArrBest(i2), f1ArrBest(i2), nmiArrBest(i2), comArrBest(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_1.00']);
%     [modArrBest1(i2), f1ArrBest1(i2), nmiArrBest1(i2), comArrBest1(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_1.50']);
%     [modArrBest2(i2), f1ArrBest2(i2), nmiArrBest2(i2), comArrBest2(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
% end
% 
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% plot(epsArr/log_n, modArrBest(:), 's-', epsArr/log_n, modArrBest1(:), 'd-', epsArr/log_n, modArrBest2(:), 'o-');
% h_legend = legend('modularity (\lambda=1.0)','modularity (\lambda=1.5)', 'modularity (\lambda=2.0)', 'Location','northwest');
% 
% % plot(epsArr/log_n, modArrBest(:), 's-', epsArr/log_n, modArrBest2(:), 'o--', ...
% %     epsArr/log_n, f1ArrBest(:), 's-', epsArr/log_n, f1ArrBest2(:), 'o--', ...
% %     epsArr/log_n, nmiArrBest(:), 's-', epsArr/log_n, nmiArrBest2(:), 'o--');
% % h_legend = legend('modularity (\lambda=1)','modularity (\lambda=2)','avg.f1score (\lambda=1)', 'avg.f1score (\lambda=2)', ...
% %     'NMI (\lambda=1)', 'NMI (\lambda=2)','Location','northwest');
% set(gcf,'units','points','position',[x0,y0,width,height])
% % set(gca,'YTick',[0.0 0.2 0.4 0.6 0.8 1.0])
% % axis([0 3 0 1.1])
% set(gca,'FontSize', 14)
% xlabel('\epsilon / ln(n)')


%%%%%%%%%%%%%%%%%%%%%%%%%% BURN FACTOR %%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%% ratio = 1.0

% dataname = 'com_amazon_ungraph';
% n = 334863;
% log_n = log(n);
% % eps = log_n;
% eps = 0.5*log_n;
% burnfactor = 20;
% % ratio = 1.0;
%     
% kArr = [2,3,4,5,6,10];
% levelArr = [10,7,5,4,4,3];
% 
% modArrBest = zeros(1, length(kArr));
% f1ArrBest = zeros(1, length(kArr));
% nmiArrBest = zeros(1, length(kArr));
% comArrBest = zeros(1, length(kArr));
% 
% modArrBest50 = zeros(1, length(kArr));
% f1ArrBest50 = zeros(1, length(kArr));
% nmiArrBest50 = zeros(1, length(kArr));
% comArrBest50 = zeros(1, length(kArr));
% 
% modArrBest100 = zeros(1, length(kArr));
% f1ArrBest100 = zeros(1, length(kArr));
% nmiArrBest100 = zeros(1, length(kArr));
% comArrBest100 = zeros(1, length(kArr));
% 
% 
% for i2=1:length(kArr)
%     k = kArr(i2);
%     level = levelArr(i2);
%     [modArrBest(i2), f1ArrBest(i2), nmiArrBest(i2), comArrBest(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_20_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
%     [modArrBest50(i2), f1ArrBest50(i2), nmiArrBest50(i2), comArrBest50(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_50_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
%     [modArrBest100(i2), f1ArrBest100(i2), nmiArrBest100(i2), comArrBest100(i2), ~, ~, ~, ~] = ...
%         aaa_readfile2([dataname '_md_100_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
% end
% 
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% hdl = plot(1:6, modArrBest(:), 's-', 1:6, modArrBest50(:), 'o-', 1:6, modArrBest100(:), '+-');
% %     1:6, f1ArrBest(:), 's-', 1:6, f1ArrBest50(:), 'o-', 1:6, f1ArrBest100(:), '+-', ...
% %     1:6, nmiArrBest(:), 's-', 1:6, nmiArrBest50(:), 'o-', 1:6, nmiArrBest100(:), '+-');
% 
% h_legend = legend('modularity (K=20)','modularity (K=50)','modularity (K=100)','Location','northwest');
% %     'f1  (20)', 'f1  (50)', 'f1  (100)', ...
% %     'NMI (20)', 'NMI (50)','NMI (100)',);
% set(h_legend,'FontSize', 14)
% 
% set(gcf,'units','points','position',[x0,y0,width,height])
% % set(gca,'YTick',[0.0 0.2 0.4 0.6 0.8 1.0])
% axis([1 6 0 0.8])
% set(gca,'FontSize', 14)
% xlabel('(k, maxL)')
% set(gca,'XTickLabel',{'(2,10)','(3,7)','(4,5)','(5,4)','(6,4)','(10,3)'});
% 
% % str = [{'mod (20)'};{ 'f1     (20)'};{ 'NMI  (20)'}; {'(50)'};{ '(50)'};{ '(50)'}; ...
% %     {'(100)'};{ '(100)'}; {'(100)'}];
% % columnlegend(3, str, 'Location','NorthWest');       % reset to 1-cloumn when exported to .eps
% % gridLegend(hdl,3,'Orientation','Horizontal');


