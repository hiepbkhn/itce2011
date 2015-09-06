
%%%
% n = 105; name = 'comp-polbooks';
% n = 1124; name = 'comp-polblogs';
% n = 6474; name = 'comp-as20graph';
% n = 7115; name = 'comp-wikiVote';
% n = 12006; name = 'comp-caHepPh';
n = 18771; name = 'comp-caAstroPh';

nMax = n*n/4;
if mod(n,2) == 1
    nMax = (n*n-1)/4;
end
dU = log(nMax) + (nMax-1)*log(1+1.0/(nMax-1));

eps = [2*dU + log(n), 2*dU + 1, dU + log(n), dU + 1, dU/2 + log(n), dU/2 + 1, 1 + log(n), 1 + 1, ...
    3, 4, 7, 11, 13, 14, ...
    3, 5, 9, 17, ...
    2, 4, 8, 16, ...
    3*log(n), 3*log(n)];

% plot(eps(1:8), rel_err(1:8),'r--',eps(9:14), rel_err(9:14),'b',eps(15:18), rel_err(15:18),'g-s',eps(19:22), rel_err(19:22),'c-o', eps(23),rel_err(23),'k+', eps(24),rel_err(24),'m*');
% legend('HRG-MCMC', 'DER', 'TmF', 'ORBIS', 'CONFIG', 'ORBIS-true');

plot(eps([1,2,3,4,5,7,6,8]), rel_err([1,2,3,4,5,7,6,8]),'r--',eps(9:14), rel_err(9:14),'b',eps(15:18), rel_err(15:18),'g-s');
legend('HRG-MCMC', 'DER', 'TmF');

% xlhand = get(gca,'xlabel'); set(xlhand,'string','epsilon','fontsize',18) 
% ylhand = get(gca,'ylabel'); set(ylhand,'string','rel.err','fontsize',12)

set(gcf, 'PaperSize', [3.5 3]);
set(gcf, 'PaperPosition', [-0.3 0.0 4.1 3.0]);
saveas(gcf, name, 'epsc')
