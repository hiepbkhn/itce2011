
% Oct 16, 2015
% comparative evaluation

% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
name_list = {'amazon', 'dblp', 'youtube'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 3;
dataname = dataname_list{pos};
n = n_list(pos);
log_n = log(n);

epsArr1 = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
epsArr2 = [log_n, 1.5*log_n, 2*log_n, 3*log_n];
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];

EFmod = zeros(1, length(epsArr2));
EFf1 = zeros(1, length(epsArr2));
EFnmi = zeros(1, length(epsArr2));
EFcom = zeros(1, length(epsArr2));

TmFmod = zeros(1, length(epsArr));
TmFf1 = zeros(1, length(epsArr));
TmFnmi = zeros(1, length(epsArr));
TmFcom = zeros(1, length(epsArr));

LDPmod = zeros(2, length(epsArr1)); % 2 values of k (8,16)
LDPf1 = zeros(2, length(epsArr1));
LDPnmi = zeros(2, length(epsArr1));
LDPcom = zeros(2, length(epsArr1));

MDmod = zeros(1, length(epsArr));
MDf1 = zeros(1, length(epsArr));
MDnmi = zeros(1, length(epsArr));
MDcom = zeros(1, length(epsArr));

% read .mat files
for i2=1:length(epsArr2)
    eps = epsArr2(i2);
    [EFmod(i2), EFf1(i2), EFnmi(i2), EFcom(i2)] = aaa_readfile([dataname '_ef_' sprintf('%.1f', eps)]);
end

for i2=1:length(epsArr)
    eps = epsArr(i2);
    [TmFmod(i2), TmFf1(i2), TmFnmi(i2), TmFcom(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
end

for i2=1:length(epsArr1)
    eps = epsArr1(i2);
    [LDPmod(1,i2), LDPf1(1,i2), LDPnmi(1,i2), LDPcom(1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_8']);   % k = 8
    [LDPmod(2,i2), LDPf1(2,i2), LDPnmi(2,i2), LDPcom(2,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_16']);   % k = 16
end

for i2=1:length(epsArr)
    eps = epsArr(i2);
    [MDmod(i2), MDf1(i2), MDnmi(i2), MDcom(i2),~,~,~,~] = aaa_readfile2([dataname '_md_20_10_2_' sprintf('%.1f', eps) '_2.00']);    % read Best (ignore Part2)
end


x0=400;
y0=200;
width=400;
height=300;

% plot(epsArr2/log_n, EFmod, 's-', epsArr/log_n, TmFmod, 's-', epsArr1/log_n, LDPmod(1,:), 's-', epsArr1/log_n, LDPmod(2,:), 's-', epsArr/log_n, MDmod, 's-');
% ylabel('modularity')

% plot(epsArr2/log_n, EFf1, 's-', epsArr/log_n, TmFf1, 's-', epsArr1/log_n, LDPf1(1,:), 's-', epsArr1/log_n, LDPf1(2,:), 's-', epsArr/log_n, MDf1, 's-');
% ylabel('avg.F1score')
% 
% plot(epsArr2/log_n, EFnmi, 's-', epsArr/log_n, TmFnmi, 's-', epsArr1/log_n, LDPnmi(1,:), 's-', epsArr1/log_n, LDPnmi(2,:), 's-', epsArr/log_n, MDnmi, 's-');
% ylabel('NMI')
% 
semilogy(epsArr2/log_n, EFcom, 's-', epsArr/log_n, TmFcom, 's-', epsArr1/log_n, LDPcom(1,:), 's-', epsArr1/log_n, LDPcom(2,:), 's-', epsArr/log_n, MDcom, 's-');
ylabel('#communities')

h_legend = legend('EF','TmF','LDP (k=8)','LDP (k=16)','MD', 'Location','southeast');
set(gcf,'units','points','position',[x0,y0,width,height])
set(gca,'FontSize', 12)
xlabel('\epsilon / ln(n)')
% axis([0 3 -0.05 1.05]);


