
% Oct 15, 2015
% draw mod,f1,nmi vs. epsilon for different values of k


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
name_list = {'amazon', 'dblp', 'youtube'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 3;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
epsArr = [0.25*log_n, 0.5*log_n, log_n];
kArr = [4,8,16,32,64];
    
modArr = zeros(length(kArr), length(epsArr));
f1Arr = zeros(length(kArr), length(epsArr));
nmiArr = zeros(length(kArr), length(epsArr));
comArr = zeros(length(kArr), length(epsArr));

for i1=1:length(kArr)
    k = kArr(i1);
    for i2=1:length(epsArr)
        eps = epsArr(i2);
        [modArr(i1,i2), f1Arr(i1,i2), nmiArr(i1,i2), comArr(i1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_' int2str(k)]);
    end
end

gcf = plot(epsArr/log_n, modArr(1,:), 's-', epsArr/log_n, modArr(2,:), 's-', epsArr/log_n, modArr(3,:), 's-', epsArr/log_n, modArr(4,:), 's-', epsArr/log_n, modArr(5,:), 's-');
h_legend = legend('k=4','k=8','k=16','k=32','k=64');
set(h_legend,'FontSize',12);
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',18) 
ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
% saveas(gcf, 'aaa_test', 'epsc')
    
figure;
plot(epsArr/log_n, f1Arr(1,:), 's-', epsArr/log_n, f1Arr(2,:), 's-', epsArr/log_n, f1Arr(3,:), 's-', epsArr/log_n, f1Arr(4,:), 's-', epsArr/log_n, f1Arr(5,:), 's-');
h_legend = legend('k=4','k=8','k=16','k=32','k=64');
set(h_legend,'FontSize',12);
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',18) 
ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)

figure;
plot(epsArr/log_n, nmiArr(1,:), 's-', epsArr/log_n, nmiArr(2,:), 's-', epsArr/log_n, nmiArr(3,:), 's-', epsArr/log_n, nmiArr(4,:), 's-', epsArr/log_n, nmiArr(5,:), 's-');
h_legend = legend('k=4','k=8','k=16','k=32','k=64');
set(h_legend,'FontSize',12);
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',18) 
ylhand = get(gca,'ylabel'); set(ylhand,'string','NMI','fontsize',14)

figure;
semilogy(epsArr/log_n, comArr(1,:), 's-', epsArr/log_n, comArr(2,:), 's-', epsArr/log_n, comArr(3,:), 's-', epsArr/log_n, comArr(4,:), 's-', epsArr/log_n, comArr(5,:), 's-');
h_legend = legend('k=4','k=8','k=16','k=32','k=64');
set(h_legend,'FontSize',12);
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',18) 
ylhand = get(gca,'ylabel'); set(ylhand,'string','#communities','fontsize',14)

