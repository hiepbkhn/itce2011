
% Oct 16, 2015
% comparative evaluation
% Jan 4, 2016

% amazon, dblp, youtube
dataname_list = {'as20graph', 'ca-AstroPh-wcc', 'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
name_list = {'as20graph', 'ca-AstroPh', 'amazon', 'dblp', 'youtube'};
n_list = [6474, 17903, 334863, 317080, 1134890];
m_list = [12572, 196972, 925872, 1049866, 2987624];
n_com = [30, 37, 257, 375, 13485];

dKmod = zeros(1, 5);   % 1K-series
dKf1 = zeros(1, 5);
dKnmi = zeros(1, 5);
dKcom = zeros(1, 5);

TmFmod = zeros(1, 5);
TmFf1 = zeros(1, 5);
TmFnmi = zeros(1, 5);
TmFcom = zeros(1, 5);

EFmod = zeros(1, 5);
EFf1 = zeros(1, 5);
EFnmi = zeros(1, 5);
EFcom = zeros(1, 5);

DERmod = zeros(1, 5);
DERf1 = zeros(1, 5);
DERnmi = zeros(1, 5);
DERcom = zeros(1, 5);

LDPmod = zeros(2, 5); % 2 values of k (8,16)
LDPf1 = zeros(2, 5);
LDPnmi = zeros(2, 5);
LDPcom = zeros(2, 5);

MDmod = zeros(1, 5);
MDf1 = zeros(1, 5);
MDnmi = zeros(1, 5);
MDcom = zeros(1, 5);

MD2mod = zeros(1, 5);
MD2f1 = zeros(1, 5);
MD2nmi = zeros(1, 5);
MD2com = zeros(1, 5);

HRGmod = zeros(1, 5);
HRGf1 = zeros(1, 5);
HRGnmi = zeros(1, 5);
HRGcom = zeros(1, 5);

% read .mat files

%%% 1 - AS20GRAPH
% pos = 1;
% dataname = dataname_list{pos};
% n = n_list(pos);
% log_n = log(n);
% epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [dKmod(i2), dKf1(i2), dKnmi(i2), dKcom(i2)] = aaa_readfile([dataname '_1k_' sprintf('%.1f', eps)]);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [TmFmod(i2), TmFf1(i2), TmFnmi(i2), TmFcom(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [EFmod(i2), EFf1(i2), EFnmi(i2), EFcom(i2)] = aaa_readfile([dataname '_ef_' sprintf('%.1f', eps)]);
% end
% 
% epsStr = {'0.5_0.3_0.1', '1.0_0.5_0.3', '1.5_0.8_0.4', '2.0_1.0_0.5' ,'2.5_1.3_0.6'};   
% for i2=1:5
%     eps_str = epsStr{i2};
%     [DERmod(i2), DERf1(i2), DERnmi(i2), DERcom(i2)] = aaa_readfile([dataname '_der_' eps_str] );
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [LDPmod(1,i2), LDPf1(1,i2), LDPnmi(1,i2), LDPcom(1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_8']);   % k = 8
%     [LDPmod(2,i2), LDPf1(2,i2), LDPnmi(2,i2), LDPcom(2,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_64']);   % k = 64
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [MDmod(i2), MDf1(i2), MDnmi(i2), MDcom(i2),~,~,~,~] = aaa_readfile2([dataname '_md_50_6_2_' sprintf('%.1f', eps) '_2.00']);    %  as20graph
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [HRGmod(i2), HRGf1(i2), HRGnmi(i2), HRGcom(i2),~,~,~,~] = aaa_readfile3([dataname '_dendro_20_6474_1000_' sprintf('%.1f', eps)]);    
% end
% 
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% % plot(epsArr/log_n, dKmod, 's-', epsArr/log_n, TmFmod, 's-', epsArr/log_n, LDPmod(1,:), '*-', epsArr/log_n, LDPmod(2,:), '*-', epsArr/log_n, MDmod, 'o-', epsArr/log_n, HRGmod, 's-', ...
% %     epsArr/log_n, DERmod, 's-', epsArr/log_n, EFmod, 'd-');
% % h_legend = legend('1K','TmF','LDP (k=8)','LDP (k=64)','MD (2,6)', 'HRG-MCMC', 'DER', 'EF', 'Location','northwest');
% % set(h_legend,'FontSize',14);
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
% % axis([0.1 0.5 -0.01 0.2]);
% % 
% % figure;
% % plot(epsArr/log_n, dKf1, 's-', epsArr/log_n, TmFf1, 's-', epsArr/log_n, LDPf1(1,:), '*-', epsArr/log_n, LDPf1(2,:), '*-', epsArr/log_n, MDf1, 'o-', epsArr/log_n, HRGf1, 's-', ...
% %     epsArr/log_n, DERf1, 's-', epsArr/log_n, EFf1, 'd-');
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)
% 
% figure;
% plot(epsArr/log_n, dKnmi, 's-', epsArr/log_n, TmFnmi, 's-', epsArr/log_n, LDPnmi(1,:), '*-', epsArr/log_n, LDPnmi(2,:), '*-', epsArr/log_n, MDnmi, 'o-', epsArr/log_n, HRGnmi, 's-', ...
%     epsArr/log_n, DERnmi, 's-', epsArr/log_n, EFnmi, 'd-');
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','NMI','fontsize',14)
% 
% % figure;
% % semilogy(epsArr/log_n, dKcom, 's-', epsArr/log_n, TmFcom, 's-', epsArr/log_n, LDPcom(1,:), '*-', epsArr/log_n, LDPcom(2,:), '*-', epsArr/log_n, MDcom, 'o-', epsArr/log_n, HRGcom, 's-', ...
% %     epsArr/log_n, DERcom, 's-', epsArr/log_n, EFcom, 'd-', epsArr/log_n, ones(1,5)*n_com(pos), '--');
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','#communities','fontsize',14)
% 
% 
% % set(gcf,'units','points','position',[x0,y0,width,height])
% % set(gca,'FontSize', 12)
% % xlabel('\epsilon / ln(n)')
% % % axis([0 3 -0.05 1.05]);


%%% 2 - CA-ASTROPH
% pos = 2;
% dataname = dataname_list{pos};
% n = n_list(pos);
% log_n = log(n);
% epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [dKmod(i2), dKf1(i2), dKnmi(i2), dKcom(i2)] = aaa_readfile([dataname '_1k_' sprintf('%.1f', eps)]);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [TmFmod(i2), TmFf1(i2), TmFnmi(i2), TmFcom(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
% end
% 
% for i2=3:5
%     eps = epsArr(i2);
%     [EFmod(i2), EFf1(i2), EFnmi(i2), EFcom(i2)] = aaa_readfile([dataname '_ef_' sprintf('%.1f', eps)]);
% end
% 
% epsStr = {'0.6_0.3_0.1', '1.1_0.6_0.3', '1.7_0.8_0.4', '2.2_1.1_0.6' ,'2.8_1.4_0.7'};
% for i2=1:5
%     eps_str = epsStr{i2};
%     [DERmod(i2), DERf1(i2), DERnmi(i2), DERcom(i2)] = aaa_readfile(strcat(dataname, strcat('_der_', eps_str))); 
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [LDPmod(1,i2), LDPf1(1,i2), LDPnmi(1,i2), LDPcom(1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_8']);   % k = 8
%     [LDPmod(2,i2), LDPf1(2,i2), LDPnmi(2,i2), LDPcom(2,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_64']);   % k = 64
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [MDmod(i2), MDf1(i2), MDnmi(i2), MDcom(i2),~,~,~,~] = aaa_readfile2([dataname '_md_50_7_2_' sprintf('%.1f', eps) '_2.00']);    % ca-AstroPh
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [HRGmod(i2), HRGf1(i2), HRGnmi(i2), HRGcom(i2),~,~,~,~] = aaa_readfile3([dataname '_dendro_20_17903_1000_' sprintf('%.1f', eps)]);    %  ca-AstroPh
% end
% 
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% % plot(epsArr/log_n, dKmod, 's-', epsArr/log_n, TmFmod, 's-', epsArr/log_n, LDPmod(1,:), '*-', epsArr/log_n, LDPmod(2,:), '*-', epsArr/log_n, MDmod, 'o-', epsArr/log_n, HRGmod, 's-', ...
% %     epsArr/log_n, DERmod, 's-', epsArr(3:5)/log_n, EFmod(3:5), 'd-');
% % h_legend = legend('1K','TmF','LDP (k=8)','LDP (k=64)','MD (2,7)', 'HRG-MCMC', 'DER', 'EF', 'Location','northwest');
% % set(h_legend,'FontSize',14);
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
% 
% % figure;
% % plot(epsArr/log_n, dKf1, 's-', epsArr/log_n, TmFf1, 's-', epsArr/log_n, LDPf1(1,:), '*-', epsArr/log_n, LDPf1(2,:), '*-', epsArr/log_n, MDf1, 'o-', epsArr/log_n, HRGf1, 's-', ...
% %     epsArr/log_n, DERf1, 's-', epsArr(3:5)/log_n, EFf1(3:5), 'd-');
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)
% 
% figure;
% plot(epsArr/log_n, dKnmi, 's-', epsArr/log_n, TmFnmi, 's-', epsArr/log_n, LDPnmi(1,:), '*-', epsArr/log_n, LDPnmi(2,:), '*-', epsArr/log_n, MDnmi, 'o-', epsArr/log_n, HRGnmi, 's-', ...
%     epsArr/log_n, DERnmi, 's-', epsArr(3:5)/log_n, EFnmi(3:5), 'd-');
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','NMI','fontsize',14)
% 
% % figure;
% % semilogy(epsArr/log_n, dKcom, 's-', epsArr/log_n, TmFcom, 's-', epsArr/log_n, LDPcom(1,:), 's-', epsArr/log_n, LDPcom(2,:), 's-', epsArr/log_n, MDcom, 'o-', epsArr/log_n, HRGcom, 's-', ...
% %     epsArr/log_n, DERcom, 's-', epsArr(3:5)/log_n, EFcom(3:5), 'd-', epsArr/log_n, ones(1,5)*n_com(pos), '--');
% % xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% % set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% % ylhand = get(gca,'ylabel'); set(ylhand,'string','#communities','fontsize',14)
% 
% 
% % set(gcf,'units','points','position',[x0,y0,width,height])
% % set(gca,'FontSize', 12)
% % xlabel('\epsilon / ln(n)')
% % % axis([0 3 -0.05 1.05]);



%%% 3 - AMAZON, DBLP, YOUTUBE (May 11, 2016: fixed typos (HRGmod) in F1, NMI figures)
pos = 5;    % 3,4,5
dataname = dataname_list{pos};
n = n_list(pos);
log_n = log(n);
epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];

for i2=1:5
    eps = epsArr(i2);
    [dKmod(i2), dKf1(i2), dKnmi(i2), dKcom(i2)] = aaa_readfile([dataname '_1k_' sprintf('%.1f', eps)]);
end

for i2=1:5
    eps = epsArr(i2);
    [TmFmod(i2), TmFf1(i2), TmFnmi(i2), TmFcom(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
end

for i2=1:5
    eps = epsArr(i2);
    [EFmod(i2), EFf1(i2), EFnmi(i2), EFcom(i2)] = aaa_readfile([dataname '_ef_shrink_' sprintf('%.1f', eps) '_1.0']);
end

for i2=1:5
    eps = epsArr(i2);
    [LDPmod(1,i2), LDPf1(1,i2), LDPnmi(1,i2), LDPcom(1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_8']);   % k = 8
    [LDPmod(2,i2), LDPf1(2,i2), LDPnmi(2,i2), LDPcom(2,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_64']);   % k = 64
end

for i2=1:5
    eps = epsArr(i2);
    [MDmod(i2), MDf1(i2), MDnmi(i2), MDcom(i2),MD2mod(i2), MD2f1(i2), MD2nmi(i2), MD2com(i2)] = aaa_readfile2([dataname '_md_50_10_2_' sprintf('%.1f', eps) '_2.00']);    % K=50
end

for i2=1:5
    eps = epsArr(i2);    
    [HRGmod(i2), HRGf1(i2), HRGnmi(i2), HRGcom(i2),~,~,~,~] = aaa_readfile3([dataname '_fixed_20_' num2str(n) '_1000_' sprintf('%.1f', eps)]);
end


x0=400;
y0=200;
width=400;
height=300;

% plot(epsArr/log_n, dKmod, 's-', epsArr/log_n, TmFmod, 's-', epsArr/log_n, LDPmod(1,:), '*-', epsArr/log_n, LDPmod(2,:), '*-', epsArr/log_n, MDmod, 'o-', epsArr/log_n, HRGmod, 's-', epsArr/log_n, EFmod, 'd-');
% h_legend = legend('1K','TmF','LDP (k=8)','LDP (k=64)','MD (2,10)', 'HRG-Fixed', 'EFShrink', 'Location','northwest');
% set(h_legend,'FontSize',14);
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
% 
% figure;
% plot(epsArr/log_n, dKf1, 's-', epsArr/log_n, TmFf1, 's-', epsArr/log_n, LDPf1(1,:), '*-', epsArr/log_n, LDPf1(2,:), '*-', epsArr/log_n, MDf1, 'o-', epsArr/log_n, HRGf1, 's-', epsArr/log_n, EFf1, 'd-');
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)

figure;
plot(epsArr/log_n, dKnmi, 's-', epsArr/log_n, TmFnmi, 's-', epsArr/log_n, LDPnmi(1,:), '*-', epsArr/log_n, LDPnmi(2,:), '*-', epsArr/log_n, MDnmi, 'o-', epsArr/log_n, HRGnmi, 's-', epsArr/log_n, EFnmi, 'd-');
xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
ylhand = get(gca,'ylabel'); set(ylhand,'string','NMI','fontsize',14)

% figure;
% semilogy(epsArr/log_n, dKcom, 's-', epsArr/log_n, TmFcom, 's-', epsArr/log_n, LDPcom(1,:), '*-', epsArr/log_n, LDPcom(2,:), '*-', epsArr/log_n, MDcom, 'o-', epsArr/log_n, HRGcom, 's-', epsArr/log_n, EFcom, 'd-', ...
%     epsArr/log_n, ones(1,5)*n_com(pos), '--');
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','#communities','fontsize',14)

% set(gcf,'units','points','position',[x0,y0,width,height])
% set(gca,'FontSize', 14)
% xlabel('\epsilon / ln(n)')
% % axis([0 3 -0.05 1.05]);


% May 11, 2016
%%% 4 - AMAZON, DBLP, YOUTUBE - WITHOUT HRG-Fixed
% pos = 5;
% dataname = dataname_list{pos};
% n = n_list(pos);
% log_n = log(n);
% epsArr = [0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n];
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [dKmod(i2), dKf1(i2), dKnmi(i2), dKcom(i2)] = aaa_readfile([dataname '_1k_' sprintf('%.1f', eps)]);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [TmFmod(i2), TmFf1(i2), TmFnmi(i2), TmFcom(i2)] = aaa_readfile([dataname '_tmf_' sprintf('%.1f', eps)]);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [EFmod(i2), EFf1(i2), EFnmi(i2), EFcom(i2)] = aaa_readfile([dataname '_ef_shrink_' sprintf('%.1f', eps) '_1.0']);
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [LDPmod(1,i2), LDPf1(1,i2), LDPnmi(1,i2), LDPcom(1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_8']);   % k = 8
%     [LDPmod(2,i2), LDPf1(2,i2), LDPnmi(2,i2), LDPcom(2,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_16']);   % k = 16
% end
% 
% for i2=1:5
%     eps = epsArr(i2);
%     [MDmod(i2), MDf1(i2), MDnmi(i2), MDcom(i2),MD2mod(i2), MD2f1(i2), MD2nmi(i2), MD2com(i2)] = aaa_readfile2([dataname '_md_50_10_2_' sprintf('%.1f', eps) '_2.00']);    % K=50
% end
% 
% 
% x0=400;
% y0=200;
% width=400;
% height=300;
% 
% plot(epsArr/log_n, dKmod, 's-', epsArr/log_n, TmFmod, 's-', epsArr/log_n, LDPmod(1,:), '*-', epsArr/log_n, LDPmod(2,:), '*-', epsArr/log_n, MDmod, 'o-', epsArr/log_n, EFmod, 'd-');
% h_legend = legend('1K','TmF','LDP (k=8)','LDP (k=16)','MD (2,10)', 'EFShrink', 'Location','northwest');
% set(h_legend,'FontSize',14);
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','modularity','fontsize',14)
% 
% figure;
% plot(epsArr/log_n, dKf1, 's-', epsArr/log_n, TmFf1, 's-', epsArr/log_n, LDPf1(1,:), '*-', epsArr/log_n, LDPf1(2,:), '*-', epsArr/log_n, MDf1, 'o-', epsArr/log_n, EFf1, 'd-');
% xlhand = get(gca,'xlabel'); set(xlhand,'string','\epsilon / ln(n)','fontsize',14); 
% set(gca,'Xtick',0.1:0.1:0.5); set(gca,'FontSize', 14);
% ylhand = get(gca,'ylabel'); set(ylhand,'string','avg.F1score','fontsize',14)

