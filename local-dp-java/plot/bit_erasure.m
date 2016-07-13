
%
% Jun 29, 2016
%

x0=400;
y0=200;
width=400;
height=300;
fontsize = 20;
figure;
set(gcf,'units','points','position',[x0,y0,width,height])

kArr = [4,7,10];
alphaArr = 0.1:0.1:1.0;

s = 1 - repmat(alphaArr,length(kArr),1) .^ repmat((1./kArr)',1,length(alphaArr));

plot(alphaArr, s(1,:), '-+', alphaArr, s(2,:), '-s', alphaArr, s(3,:), '-*');
xlabel('alpha', 'FontSize', fontsize);
ylabel('fraction of bits erased', 'FontSize', fontsize);
legend('k=4', 'k=7', 'k=10');
set(gca,'FontSize', fontsize);
set(gca, 'XLim', [alphaArr(1) 1]);
set(gca, 'XTick', alphaArr);

saveas(gcf, 'bit-erasure.eps', 'epsc')


