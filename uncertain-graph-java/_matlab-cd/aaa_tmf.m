


% Oct 16, 2015
% TmF
% draw mod,f1,nmi vs. epsilon


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 1;



dataname = dataname_list{pos};
n = n_list(pos);
log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
    
modArr = zeros(1, length(epsArr));
f1Arr = zeros(1, length(epsArr));
nmiArr = zeros(1, length(epsArr));
comArr = zeros(1, length(epsArr));


    
    

for i2=1:length(epsArr)
    eps = epsArr(i2);
    [modArr(i2), f1Arr(i2), nmiArr(i2), comArr(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
end



x0=400;
y0=200;
width=400;
height=300;

plot(epsArr/log_n, modArr(:), 's-', epsArr/log_n, f1Arr(:), 's-', epsArr/log_n, nmiArr(:), 's-');
h_legend = legend('modularity','avg.f1score','NMI','Location','southeast');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'YTick',[0.0 0.2 0.4 0.6 0.8 1.0])
set(gca,'FontSize', 14)
xlabel('\epsilon / ln(n)')
axis([0 3 -0.05 1.05])


