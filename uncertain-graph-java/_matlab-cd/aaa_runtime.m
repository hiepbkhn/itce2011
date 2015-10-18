
% Oct 17, 2015
% draw runtime
%

% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 2;

EFtime = zeros(1, length(n_list));
EFlv = zeros(1, length(n_list));
TmFtime = zeros(1, length(n_list));
TmFlv = zeros(1, length(n_list));
LDPtime = zeros(1, length(n_list));     % k = 8
LDPlv = zeros(1, length(n_list));
LDPtime2 = zeros(1, length(n_list));    % k = 16
LDPlv2 = zeros(1, length(n_list));

NMDlv = zeros(1, length(n_list));
MDlv = zeros(1, length(n_list));

for pos=1:3
    dataname = dataname_list{pos};
    n = n_list(pos);
    eps = log(n);
    [EFtime(pos), EFlv(pos)] = aaa_readtime([dataname '_ef_' sprintf('%.1f', eps) '_runtime'], 1);
    [TmFtime(pos), TmFlv(pos)] = aaa_readtime([dataname '_tmf_' sprintf('%.1f', eps) '_runtime'], 1);
    [LDPtime(pos), LDPlv(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_8_runtime'], 1);    % k = 8
    [LDPtime2(pos), LDPlv2(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_16_runtime'], 1);    % k = 16
    [~, NMDlv(pos)] = aaa_readtime([dataname '_nmd_20_5_4_runtime'], 2); 
    [~, MDlv(pos)] = aaa_readtime([dataname '_md_20_5_4_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    % k=4, maxL=5
end

x0=400;
y0=200;
width=400;
height=300;

bar(1:3, [(EFtime + EFlv)' (TmFtime + TmFlv)' (LDPtime + LDPlv)' (LDPtime2 + LDPlv2)' NMDlv' MDlv']/1000, 1);        % /1000 --> in second
set(gcf,'units','points','position',[x0,y0,width,height])
h_legend = legend('EF','TmF','LDP (k=8)', 'LDP (k=16)', 'NMD','MD','Location','northwest');
set(h_legend,'FontSize', 12)
ylabel('runtime (s)');
set(gca,'XTickLabel',{'amazon','dblp','youtube'});
set(gca,'FontSize', 12)


%%%%%%%%%%%%%%%%%%%%%%%%%% MOD-DIV AMAZON %%%%%%%%%%%%%%%%%%%%%%%%%
x0=400;
y0=200;
width=400;
height=300;

dataname = 'com_amazon_ungraph';
n = 334863;     % amazon
log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
kArr = [2,3,4,5,6,10];
levelArr = [10,7,5,4,4,3];

MDlv = zeros(length(kArr), length(epsArr));

for i1=1:length(kArr)
    k = kArr(i1);
    level = levelArr(i1);
    for i2=1:length(epsArr)    
        eps = epsArr(i2);
        [~, MDlv(i1,i2)] = aaa_readtime([dataname '_md_20_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    
        MDlv(i1,i2) = MDlv(i1,i2)/1000;  % /1000 --> in second
    end
end


plot(epsArr/log_n, MDlv(1,:), 's-', epsArr/log_n, MDlv(2,:), 's-', epsArr/log_n, MDlv(3,:), 's-', ...
    epsArr/log_n, MDlv(4,:), 's-', epsArr/log_n, MDlv(5,:), 's-', epsArr/log_n, MDlv(6,:), 's-');       
set(gcf,'units','points','position',[x0,y0,width,height])
h_legend = legend('(2,10)','(3,7)','(5,4)','(5,4)','(6,4)','(10,3)','Location','northeast');
set(h_legend,'FontSize', 12)
xlabel('\epsilon / ln(n)')
ylabel('runtime (s)');
set(gca,'FontSize', 12)

