
%
% Jun 24, 2016
% Jul 13
%   - draw cumulative volume    
%

dataname_list = {'pl_10000_5_01', 'er_10000_0001'};
n = 10000;
m_list = [49970, 50424];
diam_list = [6, 7];
alpha_list = [0.25, 0.5, 0.75, 1.0];
beta_list = [0.5, 1.0];
fp_list = [0.1, 0.01, 0.001];   % false positive
falsePos = 0.1;

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
    
    % load degSeq
    file = ['../_data/' dataname '_deg.mat'];
    load(file)
            
    for i1 = 1:length(alpha_list)
        alpha = alpha_list(i1);
        for i2 = 1:length(beta_list)
            beta = beta_list(i2);
            
            % Baseline          
            file = ['../_stat/' dataname '-nodup-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_100-count.mat'];
            load(file)
            
            totalVolume = repmat(degSeq,round,1) .* (trueLinks + falseLinks);
            totalVolume = sum(totalVolume, 2);
            t0_Volume = alpha*(1+beta) * sum(degSeq .* degSeq);
            
            BSvol = [t0_Volume totalVolume(1:round-1)'] * 4;   % only use totalVolume(1-> round-1) and 4 bytes/edge
            BSvol_cumul = cumsum(BSvol);    % cumulative sum
            
            
            % BloomFilter
            file = ['../_compress/' dataname '-bf-' int2str(round) '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_0.1_compress.0.mat'];
            load(file)
            
            BFvol_full = sum(double(fullArr));
            BFvol_compress = sum(double(compressArr));
            
            download_Volume = n*(1+2*beta) * sum(degSeq) * 4;     % total number of bytes downloaded at the final round (4 bytes/edge)
            BFvol_full_cumul = cumsum(BFvol_full);
            BFvol_full_cumul = BFvol_full_cumul + download_Volume;
            BFvol_compress_cumul = cumsum(BFvol_compress);
            BFvol_compress_cumul = BFvol_compress_cumul + download_Volume;
            
            
            xAxis = 1:round;
%             figure;   % for inset plot !!!

%             plot(xAxis, BSvol, '-s', xAxis, BFvol_full, '-o', xAxis, BFvol_compress, '-*');  
%             semilogy(xAxis, BSvol, '-s', xAxis, BFvol_full, '-o', xAxis, BFvol_compress, '-*');                     % Jun 24
            semilogy(xAxis, BSvol_cumul, '-s', xAxis, BFvol_full_cumul, '-o', xAxis, BFvol_compress_cumul, '-*');     % Jul 13
            set(gcf,'units','points','position',[x0,y0,width,height])
            
            xlabel('round');
            h_legend = legend('BS-vol','BF-vol-full','BF-vol-compressed', 'Location', 'NorthWest');
            set(h_legend,'FontSize', fontsize);
            set(gca,'FontSize', fontsize);
            set(gca, 'XLim', [1 round]);
            set(gca, 'XTick', xAxis);
            
            % inset plot
%             axes('Position',[.2 .5 .3 .3]);
%             box on
%             plot(xAxis, BFvol_full, '-go', xAxis, BFvol_compress, '-r*')
%             set(gca, 'XLim', [1 round]);
%             set(gca, 'XTick', xAxis);

            % save
%             eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_compress-log.eps'];         % Jun 24
            eps_file = [dataname '_' sprintf('%.2f', alpha) '_' sprintf('%.2f', beta) '_compress-log-cumul.eps'];         % Jul 13
            saveas(gcf, eps_file, 'epsc')
        end
    end
end
            