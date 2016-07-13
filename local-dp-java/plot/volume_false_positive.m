
%
% Jun 24, 2016
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
diam_list = [6, 7];
alpha_list = [0.5, 1.0];
beta_list = [0.5, 1.0];
% fp_list = [0.1, 0.01, 0.001];   % false positive

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
set(gcf,'units','points','position',[x0,y0,width,height])


for k = 1:2
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
            file = ['../_stat/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.10-count.mat'];
            load(file);
            BF1_true = sum(trueLinks,2);
            BF1_false = sum(falseLinks,2);
            
            file = ['../_stat/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.01-count.mat'];
            load(file);
            BF2_true = sum(trueLinks,2);
            BF2_false = sum(falseLinks,2);
            
            file = ['../_stat/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.00-count.mat'];
            load(file);
            BF3_true = sum(trueLinks,2);
            BF3_false = sum(falseLinks,2);
            
            % plot
            xAxis = 1:round;
            h = plot(xAxis, BS_true/nEdge, '-o', xAxis, BS_false/nEdge, '--o', xAxis, BF1_true/nEdge, '-s', xAxis, BF1_false/nEdge, '--s',...
                     xAxis, BF2_true/nEdge, '-^', xAxis, BF2_false/nEdge, '--^', xAxis, BF3_true/nEdge, '-*', xAxis, BF3_false/nEdge, '--*');
            

            xlabel('round');
            h_legend = legend('BS-true','BS-fake','BF-true-0.1','BF-fake-0.1','BF-true-0.01','BF-fake-0.01','BF-true-0.001','BF-fake-0.001', 'Location', 'NorthWest');
            set(h_legend,'FontSize', fontsize);
            set(gca,'FontSize', fontsize);
            set(gca, 'XLim', [1 round]);
            set(gca, 'XTick', xAxis);
            set(gca, 'YLim', [0 1]);
            set(gca,'YTick', [0 0.2 0.4 0.6 0.8 1]);
            
            

            % save
            eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_fp.eps'];
            saveas(gcf, eps_file, 'epsc')
            
        end
    end
end