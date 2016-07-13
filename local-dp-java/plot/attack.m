%
% Jul 13, 2016
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
round_list = [3, 3];
alpha_list = [0.5];
beta_list = [0.5, 1.0];


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
            
            for i = 0:9
                file = ['../_attack/' dataname '-nodup-' int2str(round) '_' sprintf('%.1f', alpha) '_' sprintf('%.1f', beta) '_100_attack.' int2str(i) '.mat'];
                load(file)
                
                TP = TP + atArr(:,1);
                TN = TN + atArr(:,2);
                FP = FP + atArr(:,3);
                FN = FN + atArr(:,4);
                             
            end
            
            Prec = TP ./ (TP + FP);
            Recall = TP ./ (TP + FN);
            
            figure;
            plot(1:100, Prec, 1:100, Recall);
        end
    end
    
end