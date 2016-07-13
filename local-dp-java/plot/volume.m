
%
% Jun 22, 2016
%

dataname_list = {'pl_10000_3_01', 'pl_10000_5_01', 'pl_10000_10_01', 'er_10000_00006', 'er_10000_0001', 'er_10000_0002'};
n = 10000;
m_list = [29990, 49970, 99872, 30076, 50424, 99615];
diam_list = [7, 6, 5, 10, 7, 5];
alpha_list = [0.25, 0.5, 0.75, 1.0];
beta_list = [0.5, 1.0];
falsePos = 0.1;

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
set(gcf,'units','points','position',[x0,y0,width,height])

for k = 1:6
    dataname = dataname_list{k};
    round = diam_list(k);
    m = m_list(k);
    
    for i1 = 1:length(alpha_list)
        alpha = alpha_list(i1);
        for i2 = 1:length(beta_list)
            beta = beta_list(i2);
            
            nEdge = n*m*(1 + 2*beta);
            
            % Baseline 
            file = ['../_stat/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_100-count.mat'];
%             display(file);
            load(file)
            BS_true = sum(trueLinks,2);
            BS_false = sum(falseLinks,2);
            % Bloom Filter
            file = ['../_stat/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_' sprintf('%.2f', falsePos) '-count.mat'];
            load(file);
            BF_true = sum(trueLinks,2);
            BF_false = sum(falseLinks,2);
            
            % plot
            xAxis = linspace(1,round,round);
            [ax, h1, h2] = plotyy([xAxis',xAxis', xAxis', xAxis'], [BS_true/nEdge, BS_false/nEdge, BF_true/nEdge, BF_false/nEdge],  [xAxis', xAxis'], [BS_true./BS_false, BF_true./BF_false]);
            set(h1(1),'LineStyle', '--');
            set(h1(1),'Marker', '*');
            set(h1(2),'Marker', '*');
            set(h1(3),'LineStyle', '--');
            set(h1(3),'Marker', 's');
            set(h1(4),'Marker', 's');

            set(h2(1),'Marker', '^');
            set(h2(2),'Marker', '^');
            
            set(ax(1), 'XLim', [1 round]);
            set(ax(2), 'XLim', [1 round]);
            set(ax(1), 'XTick', xAxis);
            set(ax(2), 'XTick', xAxis);
            
            set(ax(2), 'YLim', [0 2]);
            set(ax(2), 'YTick', [0 0.5 1 1.5 2]);

            xlabel('round');
            h_legend = legend('BS-true','BS-fake','BF-true','BF-fake', 'BS-ratio', 'BF-ratio', 'Location', 'SouthWest');
            set(h_legend,'FontSize', fontsize);
            set(ax(1),'FontSize', fontsize);
            set(ax(2),'FontSize', fontsize);

            % save
            eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '.eps'];
            saveas(gcf, eps_file, 'epsc')
            
        end
    end
end




%%%%
% m = 29990;
% beta = 0.5;
% nEdge = n*m*(1 + 2*beta);
% 
% load('../../_stat/pl_10000_3_01-nodup-7_1.00_0.50_100-count.mat');
% 
% load('../../_stat/pl_10000_3_01-bf-7_1.00_0.50_0.10-count.mat');
% 
% 
% [ax, h1, h2] = plotyy([xAxis',xAxis', xAxis', xAxis'], [BS_true/nEdge, BS_false/nEdge, BF_true/nEdge, BF_false/nEdge],  [xAxis', xAxis'], [BS_true./BS_false, BF_true./BF_false]);
% set(h1(1),'LineStyle', '--');
% set(h1(1),'Marker', '*');
% set(h1(2),'Marker', '*');
% set(h1(3),'LineStyle', '--');
% set(h1(3),'Marker', 's');
% set(h1(4),'Marker', 's');
% 
% set(h2(1),'Marker', '^');
% set(h2(2),'Marker', '^');
% 
% xlabel('round');
% h_legend = legend('BS-true','BS-fake','BF-true','BF-fake', 'BS-ratio', 'BF-ratio');
% set(h_legend,'FontSize',11);
% 
% saveas(gcf, 'test', 'epsc')



% x = linspace(0,10);
% y1 = 200*exp(-0.05*x).*sin(x);
% y2 = 0.8*exp(-0.5*x).*sin(10*x);
% y3 = 0.2*exp(-0.5*x).*sin(10*x);
% 
% figure
% [hAx,hLine1,hLine2] = plotyy(x,y1,[x',x'],[y2',y3']);


