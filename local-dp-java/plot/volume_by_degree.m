
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
set(gcf,'units','points','position',[x0,y0,width,height])

% sort nodes by index
for k = 1:2
    dataname = dataname_list{k};
    round = diam_list(k);
    m = m_list(k);
    
    % load degSeq and sort
    file = ['../_data/' dataname '_deg.mat'];
    load(file)
    
    [B,I] = sort(degSeq);
    
%     xAxis = 1:10000;
    xAxis = 1:100:10000;    % 100 samples
%     plot(xAxis, degSeq(I));
    subI = I(xAxis);
    
            
    for i1 = 1:length(alpha_list)
        alpha = alpha_list(i1);
        for i2 = 1:length(beta_list)
            beta = beta_list(i2);
            
            % Baseline 
            file = ['../_stat/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_100-count.mat'];
            load(file)
            total = trueLinks + falseLinks;
            
%             figure;
            if round == 6
                h = plot(xAxis, total(1,subI), '.', xAxis, total(2,subI),  '+', xAxis, total(3,subI),  'o', ...
                    xAxis, total(4,subI),  's', xAxis, total(5,subI),  'd', xAxis, total(6,subI), '^');
    %             set(h,'Marker','.')
            else
                h = plot(xAxis, total(1,subI), '.', xAxis, total(2,subI),  '+', xAxis, total(3,subI),  'o', ...
                    xAxis, total(4,subI),  's', xAxis, total(5,subI),  'd', xAxis, total(6,subI), '^', xAxis, total(7,subI), '*');
            end
            
            % save
            eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_deg.eps'];
            saveas(gcf, eps_file, 'epsc')
        end
    end
end