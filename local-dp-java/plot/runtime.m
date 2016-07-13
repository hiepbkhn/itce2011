%
% Jun 25, 2016
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
diam_list = [6, 7];
alpha_list = [0.25, 0.5, 0.75, 1.0];
beta_list = [0.5, 1.0];
% fp_list = [0.1, 0.01, 0.001];   % false positive

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
figure;
set(gcf,'units','points','position',[x0,y0,width,height])

% sort nodes by index
for k = 1:2
    dataname = dataname_list{k};
    round = diam_list(k);
    m = m_list(k);
     
            
    for i1 = 1:length(alpha_list)
        alpha = alpha_list(i1);
        for i2 = 1:length(beta_list)
            beta = beta_list(i2);
            
            % Baseline 
            file = ['../_runtime/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_10_100_runtime.mat'];
            load(file)
            
            BS_exchange = mean(exchangeArr,2);
            BS_count = mean(countArr,2);
            
            % BloomFilter 
            file = ['../_runtime/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.10_10_100_runtime.mat'];
            load(file)
            
            BF_exchange = mean(exchangeArr,2);
            BF_count = mean(countArr,2);
            % plot
            xAxis = 1:round;
            h = plot(xAxis, BS_exchange, '-o', xAxis, BS_count, '--o', xAxis, BF_exchange, '-s', xAxis, BF_count, '--s');
            
            xlabel('round');
            h_legend = legend('BS-exchange','BS-count','BF-exchange','BF-count', 'Location', 'NorthWest');
            set(h_legend,'FontSize', fontsize);
            set(gca,'FontSize', fontsize);
            set(gca, 'XLim', [1 round]);
            set(gca, 'XTick', xAxis);
     
            % save
            eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_runtime.eps'];
            saveas(gcf, eps_file, 'epsc')

        end
    end
end