
% Dec 1, 2015
%   - draw runtime

clear


t_1k = zeros(9,1);
t_tmf = zeros(9,1);
t_ef = zeros(9,1);
t_der = zeros(9,1);
t_dendro = zeros(9,1);
t_hrgdiv = zeros(9,1);
t_fixed = zeros(9,1);

[ t_1k(1), t_tmf(1), t_ef(1), t_der(1), t_dendro(1), t_hrgdiv(1), t_fixed(1)  ] = aa_read_runtime('polbooks');
[ t_1k(2), t_tmf(2), t_ef(2), t_der(2), t_dendro(2), t_hrgdiv(2), t_fixed(2)  ] = aa_read_runtime('polblogs-wcc');
[ t_1k(3), t_tmf(3), t_ef(3), t_der(3), t_dendro(3), t_hrgdiv(3), t_fixed(3)  ] = aa_read_runtime('as20graph');
[ t_1k(4), t_tmf(4), t_ef(4), t_der(4), t_dendro(4), t_hrgdiv(4), t_fixed(4)  ] = aa_read_runtime('wiki-Vote-wcc');
[ t_1k(5), t_tmf(5), t_ef(5), t_der(5), t_dendro(5), t_hrgdiv(5), t_fixed(5)  ] = aa_read_runtime('ca-HepPh-wcc');
[ t_1k(6), t_tmf(6), t_ef(6), t_der(6), t_dendro(6), t_hrgdiv(6), t_fixed(6)  ] = aa_read_runtime('ca-AstroPh-wcc');

[ t_1k(7), t_tmf(7), t_ef(7), t_der(7), t_dendro(7), t_hrgdiv(7), t_fixed(7)  ] = aa_read_runtime('com_amazon_ungraph');
[ t_1k(8), t_tmf(8), t_ef(8), t_der(8), t_dendro(8), t_hrgdiv(8), t_fixed(8)  ] = aa_read_runtime('com_dblp_ungraph');
[ t_1k(9), t_tmf(9), t_ef(9), t_der(9), t_dendro(9), t_hrgdiv(9), t_fixed(9)  ] = aa_read_runtime('com_youtube_ungraph');

t_der(7)=1;
t_dendro(7)=1;
t_der(8)=1;
t_dendro(8)=1;
t_der(9)=1;
t_dendro(9)=1;

x0=400;
y0=200;
width=800;
height=400;
bar(1:9, log10([t_1k t_tmf t_ef t_der t_dendro t_hrgdiv t_fixed]));
axis([0 10 0 10]);
% bar(1:9, [t_1k t_tmf t_ef t_der t_dendro t_hrgdiv t_fixed]);
set(gcf,'units','points','position',[x0,y0,width,height])
h_legend = legend('1K','TmF','EF', 'DER', 'HRG-MCMC', 'HRG-Div','HRG-Fixed','Location','northwest');
set(h_legend,'FontSize', 12)
ylabel('runtime (ms) in log10 scale', 'FontSize', 12);
set(gca,'XTickLabel',{'polbooks','polblogs','as20graph','wiki-Vote','ca-HepPh','ca-AstroPh','amazon','dblp','youtube'});
% set(gca,'YTickLabel',{'10^0','10^1','10^2','10^3','10^4','10^5','10^6','10^7','10^8','10^9','10^10'});
set(gca,'FontSize', 12)