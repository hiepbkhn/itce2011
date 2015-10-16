
% Oct 15, 2015
% draw mod,f1,nmi vs. epsilon for different values of k


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 1;

n = n_list(pos);
dataname = dataname_list{1};

log_n = log(n);
epsArr = [0.25*log_n, 0.5*log_n, log_n];
kArr = [8,16,32,64];
    
modArr = zeros(length(kArr), length(epsArr));
f1Arr = zeros(length(kArr), length(epsArr));
nmiArr = zeros(length(kArr), length(epsArr));

for i1=1:length(kArr)
    k = kArr(i1);
    for i2=1:length(epsArr)
        eps = epsArr(i2);
        [modArr(i1,i2), f1Arr(i1,i2), nmiArr(i1,i2)] = aaa_readfile([dataname '_ldp_' sprintf('%.1f', eps) '_' int2str(k)]);
    end
end

plot(epsArr, modArr(1,:), epsArr, modArr(2,:), epsArr, modArr(3,:), epsArr, modArr(4,:), epsArr, modArr(5,:), epsArr, modArr(6,:), epsArr, modArr(7,:));
legend('k=2','k=4','k=8','k=16','k=32','k=64','k=128');

plot(epsArr, f1Arr(1,:), epsArr, f1Arr(2,:), epsArr, f1Arr(3,:), epsArr, f1Arr(4,:), epsArr, f1Arr(5,:), epsArr, f1Arr(6,:), epsArr, f1Arr(7,:));
legend('k=2','k=4','k=8','k=16','k=32','k=64','k=128');

