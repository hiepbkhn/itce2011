
%
% Jun 25, 2016
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
diam_list = [6, 7];
alpha_list = [0.5, 1.0];    % 0.25, 0.5, 0.75, 1.0
beta_list = [0.5]; % 0.5, 1.0
% fp_list = [0.1, 0.01, 0.001];   % false positive

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
set(gcf,'units','points','position',[x0,y0,width,height])


for k = 1:1%2
    dataname = dataname_list{k};
    round = diam_list(k);
    m = m_list(k);
    
    
            
    for i1 = 2:2%length(alpha_list)
        alpha = alpha_list(i1);
        
        % GroundTruth (beta = 0.00)
        file_name = ['../_matlab/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_0.00_100-'];        
        [arrCC, arrPL, arrAPD, arrDist] = read_utility(round, file_name);
        
        %
        for i2 = 1:1%length(beta_list)
            beta = beta_list(i2);
            
            % Baseline
            file_name = ['../' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_100-'];
            [BS_CC, BS_PL, BS_APD, BS_Dist] = read_utility(round, file_name);
            
            BS_CC = abs(BS_CC - arrCC)./arrCC;
            BS_PL = abs(BS_PL - arrPL)./arrPL;
            BS_APD = abs(BS_APD - arrAPD)./arrAPD;
            BS_Dist = sum(abs(BS_Dist - arrDist), 3)/2;
            
            
            % Baseline - gamma = 0.0                       
            file_name = ['../_matlab/' dataname '-nodup-d2-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.00_100-'];
            [D2_CC, D2_PL, D2_APD, D2_Dist] = read_utility(round, file_name);

            D2_CC = abs(D2_CC - arrCC)./arrCC;
            D2_PL = abs(D2_PL - arrPL)./arrPL;
            D2_APD = abs(D2_APD - arrAPD)./arrAPD;
            D2_Dist = sum(abs(D2_Dist - arrDist), 3)/2;
            
            % Baseline - gamma = 0.5
            file_name = ['../_matlab/' dataname '-nodup-d2-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.50_100-'];
            [D3_CC, D3_PL, D3_APD, D3_Dist] = read_utility(round, file_name);

            D3_CC = abs(D3_CC - arrCC)./arrCC;
            D3_PL = abs(D3_PL - arrPL)./arrPL;
            D3_APD = abs(D3_APD - arrAPD)./arrAPD;
            D3_Dist = sum(abs(D3_Dist - arrDist), 3)/2;
            
            
            % BloomFilter             
            file_name = ['../_matlab/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.10_100-'];
            [BF_CC, BF_PL, BF_APD, BF_Dist] = read_utility(round, file_name);

            BF_CC = abs(BF_CC - arrCC)./arrCC;
            BF_PL = abs(BF_PL - arrPL)./arrPL;
            BF_APD = abs(BF_APD - arrAPD)./arrAPD;
            BF_Dist = sum(abs(BF_Dist - arrDist), 3)/2;
            
            
            % plot
            xAxis = 1:100;
            tArr = [1,2,3,round];     % time steps
            for it=1:length(tArr)
                t = tArr(it);
                
                % CC
%                 figure;
                plot(xAxis, BS_CC(t,:), 'o', xAxis, D2_CC(t,:), '*', xAxis, D3_CC(t,:), 's', xAxis, BF_CC(t,:), '+');

                title(['CC (t = ' int2str(t) ')']);
                xlabel('sample nodes');
                h_legend = legend('BS','D2-0.0','D2-0.5','BF', 'Location', 'NorthWest');
                set(h_legend,'FontSize', fontsize);
                set(gca,'FontSize', fontsize);
                set(gca, 'XLim', [1 100]);
                set(gca, 'YLim', [0 1]);
                
                % save
                eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '-' int2str(t) '_CC.eps'];
                saveas(gcf, eps_file, 'epsc')
                
                
                % PL
%                 figure;
                plot(xAxis, BS_PL(t,:), 'o', xAxis, D2_PL(t,:), '*', xAxis, D3_PL(t,:), 's', xAxis, BF_PL(t,:), '+');

                title(['PL (t = ' int2str(t) ')']);
                xlabel('sample nodes');
                h_legend = legend('BS','D2-0.0','D2-0.5','BF', 'Location', 'NorthWest');
                set(h_legend,'FontSize', fontsize);
                set(gca,'FontSize', fontsize);
                set(gca, 'XLim', [1 100]);
                set(gca, 'YLim', [0 1]);
                
                % save
                eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '-' int2str(t) '_PL.eps'];
                saveas(gcf, eps_file, 'epsc')
                
                % APD
%                 figure;
                plot(xAxis, BS_APD(t,:), 'o', xAxis, D2_APD(t,:), '*', xAxis, D3_APD(t,:), 's', xAxis, BF_APD(t,:), '+');

                title(['APD (t = ' int2str(t) ')']);
                xlabel('sample nodes');
                h_legend = legend('BS','D2-0.0','D2-0.5','BF', 'Location', 'NorthWest');
                set(h_legend,'FontSize', fontsize);
                set(gca,'FontSize', fontsize);
                set(gca, 'XLim', [1 100]);
                set(gca, 'YLim', [0 1]);
                
                % save
                eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '-' int2str(t) '_APD.eps'];
                saveas(gcf, eps_file, 'epsc')
                
                % Dist
%                 figure;
                plot(xAxis, BS_Dist(t,:), 'o', xAxis, D2_Dist(t,:), '*', xAxis, D3_Dist(t,:), 's', xAxis, BF_Dist(t,:), '+');

                title(['Distance (t = ' int2str(t) ')']);
                xlabel('sample nodes');
                h_legend = legend('BS','D2-0.0','D2-0.5','BF', 'Location', 'NorthWest');
                set(h_legend,'FontSize', fontsize);
                set(gca,'FontSize', fontsize);
                set(gca, 'XLim', [1 100]);
                set(gca, 'YLim', [0 1]);
                
                % save
                eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '-' int2str(t) '_Dist.eps'];
                saveas(gcf, eps_file, 'epsc')
            end
            
            
     
            
            
            
            
        end
    end
end



% arrAD = zeros(10,100);
% for i=0:9
%     load(['../er_10000_0001-bf-7_0.50_0.50_0.00_100-7.' int2str(i) '.mat']);
%     arrAD(i+1,:) = a_AD(1);
% end
