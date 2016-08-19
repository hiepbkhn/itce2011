
% Oct 17, 2015
% draw runtime
%

%%% amazon, dblp, youtube
dataname_list = {'ca-AstroPh-wcc', 'com_amazon_ungraph', 'com_youtube_ungraph'}; % com_dblp_ungraph
n_list = [17903, 334863, 1134890];   % 317080
m_list = [196972, 925872, 2987624];    % 1049866

pos = 2;

dKtime = zeros(1, length(n_list));
dKlv = zeros(1, length(n_list));
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
HRGlv = zeros(1, length(n_list));

% ca-AstroPh
pos = 1;
dataname = dataname_list{pos};
n = n_list(pos);
eps = 0.5*log(n);
[dKtime(pos), dKlv(pos)] = aaa_readtime([dataname '_1k_' sprintf('%.1f', eps) '_runtime'], 1);
[EFtime(pos), EFlv(pos)] = aaa_readtime([dataname '_ef_' sprintf('%.1f', eps) '_runtime'], 1);
[TmFtime(pos), TmFlv(pos)] = aaa_readtime([dataname '_tmf_' sprintf('%.1f', eps) '_runtime'], 1);
[LDPtime(pos), LDPlv(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_8_runtime'], 1);    % k = 8
[LDPtime2(pos), LDPlv2(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_16_runtime'], 1);    % k = 16
[~, MDlv(pos)] = aaa_readtime([dataname '_md_50_5_3_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    % k=3, maxL=5
[~, HRGlv(pos)] = aaa_readtime([dataname '_fixed_20_17903_1000_' sprintf('%.1f', eps) '_runtime'], 2);   

% amazon, youtube
for pos=2:3
    dataname = dataname_list{pos};
    n = n_list(pos);
    eps = 0.5*log(n);
    [dKtime(pos), dKlv(pos)] = aaa_readtime([dataname '_1k_' sprintf('%.1f', eps) '_runtime'], 1);
    [EFtime(pos), EFlv(pos)] = aaa_readtime([dataname '_ef_shrink_' sprintf('%.1f', eps) '_1.0_runtime'], 1);
    [TmFtime(pos), TmFlv(pos)] = aaa_readtime([dataname '_tmf_' sprintf('%.1f', eps) '_runtime'], 1);
    [LDPtime(pos), LDPlv(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_8_runtime'], 1);    % k = 8
    [LDPtime2(pos), LDPlv2(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_16_runtime'], 1);    % k = 16
%     [~, NMDlv(pos)] = aaa_readtime([dataname '_nmd_20_5_4_runtime'], 2); 
    [~, MDlv(pos)] = aaa_readtime([dataname '_md_50_5_4_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    % k=4, maxL=5
    [~, HRGlv(pos)] = aaa_readtime([dataname '_fixed_20_' num2str(n) '_1000_' sprintf('%.1f', eps) '_runtime'], 2);   
end

x0=400;
y0=200;
width=400;
height=300;

bar(1:3, [(dKtime + dKlv)' (EFtime + EFlv)' (TmFtime + TmFlv)' (LDPtime + LDPlv)' (LDPtime2 + LDPlv2)' MDlv' HRGlv']/1000, 1);        % /1000 --> in second
set(gcf,'units','points','position',[x0,y0,width,height])
h_legend = legend('1K', 'EF','TmF','LDP (k=8)', 'LDP (k=16)', 'MD (4,5)', 'HRG-Fixed', 'Location','northwest');
set(h_legend,'FontSize', 14)
ylhand = get(gca,'ylabel'); set(ylhand,'string','runtime (s)','fontsize',14);
set(gca,'XTickLabel',{'ca-AstroPh','amazon','youtube'});
set(gca,'FontSize', 14)


%%%%%%%%%%%%%%%%%%%%%%%%%% MOD-DIV AMAZON %%%%%%%%%%%%%%%%%%%%%%%%%
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% dataname = 'com_amazon_ungraph';
% n = 334863;     % amazon
% log_n = log(n);
% epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];
% kArr = [2,3,4,5,6,10];
% levelArr = [10,7,5,4,4,3];
% 
% MDlv = zeros(length(kArr), length(epsArr));
% 
% for i1=1:length(kArr)
%     k = kArr(i1);
%     level = levelArr(i1);
%     for i2=1:length(epsArr)    
%         eps = epsArr(i2);
%         [~, MDlv(i1,i2)] = aaa_readtime([dataname '_md_50_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    
%         MDlv(i1,i2) = MDlv(i1,i2)/1000;  % /1000 --> in second
%     end
% end
% 
% 
% plot(epsArr/log_n, MDlv(1,:), 's-', epsArr/log_n, MDlv(2,:), 's-', epsArr/log_n, MDlv(3,:), 's-', ...
%     epsArr/log_n, MDlv(4,:), 's-', epsArr/log_n, MDlv(5,:), 's-', epsArr/log_n, MDlv(6,:), 's-');       
% set(gcf,'units','points','position',[x0,y0,width,height])
% h_legend = legend('(2,10)','(3,7)','(5,4)','(5,4)','(6,4)','(10,3)','Location','northeast');
% set(h_legend,'FontSize', 14)
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','runtime (s)','fontsize',14);
% set(gca,'FontSize', 14)






%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% May 11, 2016
% draw runtime
%

%%% amazon, dblp, youtube
% dataname_list = {'as20graph', 'com_amazon_ungraph', 'com_youtube_ungraph'}; % com_amazon_ungraph
% n_list = [6474, 334863, 1134890];   % 334863
% m_list = [12572, 925872, 2987624];    % 925872
% 
% pos = 2;
% 
% dKtime = zeros(1, length(n_list));
% dKlv = zeros(1, length(n_list));
% EFtime = zeros(1, length(n_list));
% EFlv = zeros(1, length(n_list));
% TmFtime = zeros(1, length(n_list));
% TmFlv = zeros(1, length(n_list));
% LDPtime = zeros(1, length(n_list));     % k = 8
% LDPlv = zeros(1, length(n_list));
% LDPtime2 = zeros(1, length(n_list));    % k = 16
% LDPlv2 = zeros(1, length(n_list));
% 
% NMDlv = zeros(1, length(n_list));
% MDlv = zeros(1, length(n_list));
% HRGlv = zeros(1, length(n_list));
% 
% % as20graph
% pos = 1;
% dataname = dataname_list{pos};
% n = n_list(pos);
% eps = 0.5*log(n);
% [dKtime(pos), dKlv(pos)] = aaa_readtime([dataname '_1k_' sprintf('%.1f', eps) '_runtime'], 1);
% [EFtime(pos), EFlv(pos)] = aaa_readtime([dataname '_ef_' sprintf('%.1f', eps) '_runtime'], 1);
% [TmFtime(pos), TmFlv(pos)] = aaa_readtime([dataname '_tmf_' sprintf('%.1f', eps) '_runtime'], 1);
% [LDPtime(pos), LDPlv(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_8_runtime'], 1);    % k = 8
% [LDPtime2(pos), LDPlv2(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_16_runtime'], 1);    % k = 16
% [~, MDlv(pos)] = aaa_readtime([dataname '_md_50_4_3_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    % k=3, maxL=5
% % [~, HRGlv(pos)] = aaa_readtime([dataname '_fixed_20_6474_1000_' sprintf('%.1f', eps) '_runtime'], 2);   
% 
% % amazon, youtube
% for pos=2:3
%     dataname = dataname_list{pos};
%     n = n_list(pos);
%     eps = 0.5*log(n);
%     [dKtime(pos), dKlv(pos)] = aaa_readtime([dataname '_1k_' sprintf('%.1f', eps) '_runtime'], 1);
%     [EFtime(pos), EFlv(pos)] = aaa_readtime([dataname '_ef_shrink_' sprintf('%.1f', eps) '_1.0_runtime'], 1);
%     [TmFtime(pos), TmFlv(pos)] = aaa_readtime([dataname '_tmf_' sprintf('%.1f', eps) '_runtime'], 1);
%     [LDPtime(pos), LDPlv(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_8_runtime'], 1);    % k = 8
%     [LDPtime2(pos), LDPlv2(pos)] = aaa_readtime([dataname '_ldp_' sprintf('%.1f', eps) '_16_runtime'], 1);    % k = 16
% %     [~, NMDlv(pos)] = aaa_readtime([dataname '_nmd_20_5_4_runtime'], 2); 
%     [~, MDlv(pos)] = aaa_readtime([dataname '_md_50_5_4_' sprintf('%.1f', eps) '_2.00_runtime'], 2);    % k=4, maxL=5
% %     [~, HRGlv(pos)] = aaa_readtime([dataname '_fixed_20_' num2str(n) '_1000_' sprintf('%.1f', eps) '_runtime'], 2);   
% end
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% bar(1:3, [(dKtime + dKlv)' (EFtime + EFlv)' (TmFtime + TmFlv)' (LDPtime + LDPlv)' (LDPtime2 + LDPlv2)' MDlv']/1000, 1);        % /1000 --> in second
% set(gcf,'units','points','position',[x0,y0,width,height])
% h_legend = legend('1K', 'EF','TmF','LDP (k=8)', 'LDP (k=16)', 'MD (4,5)', 'Location','northwest');
% set(h_legend,'FontSize', 14)
% ylhand = get(gca,'ylabel'); set(ylhand,'string','runtime (s)','fontsize',14);
% set(gca,'XTickLabel',{'as20graph','amazon','youtube'});
% set(gca,'FontSize', 14)
