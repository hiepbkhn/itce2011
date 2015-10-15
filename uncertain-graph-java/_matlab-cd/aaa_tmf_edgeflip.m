
% Oct 15, 2015
% draw number of edges (n_edge_total) and number of passing 1-edges
% (n_edge1)

n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];



for i=2:2
    n = n_list(i);
    m = m_list(i);
    log_n = log(n);
    % epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
    epsArr = 0.1:1:3*log_n;
    
    
    sArr = 2./(exp(epsArr) + 1);
    
    % TmF
    n_edge1_TmF = zeros(1,length(epsArr));
    n_edge_total_TmF = ones(1,length(epsArr)) * m;
    eps_t = log(n*(n-1)/(2*m)-1);
    for j=1:length(epsArr)
        eps = epsArr(j);
        if eps > eps_t
            theta = 1/(2*eps)*log(n*(n-1)/(2*m) - 1) + 1/2;
            n_edge1_TmF(j) = m - m/2*exp(-eps*(1-theta));
        else
            theta = 1/eps*log(n*(n-1)/(4*m) + 1/2*(exp(eps)-1));
            n_edge1_TmF(j) = m/2*exp(-eps*(theta-1));
        end
    end 
    
    
    % EdgeFlip
    n_edge1_EF = (1-sArr/2)*m;
    n_edge_total_EF = (1-sArr)*m + n*(n-1)/4*sArr;
    plot (epsArr/log_n, n_edge1_EF/m, '-', epsArr/log_n, n_edge_total_EF/m ,'-', epsArr/log_n, n_edge1_TmF/m ,'--', epsArr/log_n, n_edge_total_TmF/m ,'--');
%     axis([0 epsArr(length(epsArr)) 0 3])
    axis([0 3 0 3])
    h = legend('n_1 (EF)','total-edges (EF)', 'n_1 (TmF)','total-edges (TmF)');
    xlabel('\epsilon / ln(n)');
    ylabel('normalized number of edges');
    line([1 1], [0 3], 'LineStyle', '--');
    line([2 2], [0 3], 'LineStyle', '--');
%     set(h,'Interpreter','latex');
%     hold on
end
