%
% Jul 13, 2016
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
round_list = [6, 7];
alpha_list = [0.5, 1.0];
beta_list = [0.5, 1.0];
nRun = 2;

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
set(gcf,'units','points','position',[x0,y0,width,height])



for k = 1:2
    dataname = dataname_list{k};
    round = round_list(k);
    m = m_list(k);
    
    mean_F1 = zeros(2,2,round);
    std_F1 = zeros(2,2,round);
    for i1 = 1:length(alpha_list)
        alpha = alpha_list(i1);
        for i2 = 1:length(beta_list)
            beta = beta_list(i2);
            
            TP = zeros(100,1);  % true positive            
            TN = zeros(100,1);
            FP = zeros(100,1);
            FN = zeros(100,1);
            Prec = zeros(100,1);
            Recall = zeros(100,1);
            
            for t = 1:round
                for i = 0:nRun-1
                    file = ['../_attack/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_100-' int2str(t) '_attack.' int2str(i) '.mat'];
                    load(file)

                    TP = TP + atArr(:,1);
                    TN = TN + atArr(:,2);
                    FP = FP + atArr(:,3);
                    FN = FN + atArr(:,4);

                end

                Prec = TP ./ (TP + FP);
                Recall = TP ./ (TP + FN);
                F1 = 2*Prec.*Recall./(Prec + Recall);
                mean_F1(i1,i2,t) = mean(F1);
                std_F1(i1,i2,t) = std(F1);

%                 sprintf('%s (%.1f, %.1f): %f + %f', dataname, alpha, beta, mean(F1), std(F1))
            end
            
%             figure;
% %             plot(1:100, Prec, 1:100, Recall);
%             plot(1:100, Prec, 1:100, Recall, 1:100, F1);                        
        end
        
        figure;
        
        errorbar(1:round, mean_F1(i1,1,:), std_F1(i1,1,:), 'b-'); hold on;
        errorbar(1:round, mean_F1(i1,2,:), std_F1(i1,2,:), 'g-'); hold on;
        plot(1:round, ones(round,1)/(1+0.5), 'k--', 1:round, ones(round,1)/(1+1.0), 'k--'); 
        xlabel('round', 'FontSize', fontsize);
        ylabel('F1 score', 'FontSize', fontsize);
        legend('beta=0.5', 'beta=1.0');
        set(gca,'FontSize', fontsize);
        set(gca, 'XLim', [0.9 round+0.1]);
        set(gca, 'XTick', 1:round);
        set(gca, 'YLim', [0.3 0.9]);
        
        eps_file = [dataname '_' sprintf('%.2f', alpha) '_attack.eps'];
        saveas(gcf, eps_file, 'epsc')
    end
    
end