


% Oct 15, 2015
% draw mod,f1,nmi vs. epsilon for different values of (k, max_level)


% amazon, dblp, youtube
dataname_list = {'com_amazon_ungraph', 'com_dblp_ungraph', 'com_youtube_ungraph'};
n_list = [334863, 317080, 1134890];
m_list = [925872, 1049866, 2987624];

pos = 3;

n = n_list(pos);
dataname = dataname_list{pos};

log_n = log(n);
epsArr = [2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n];
kArr = [2,3,4,5,6,10];
levelArr = [10,7,5,4,4,3];
burnfactor = 20;
ratio = 2.0;
    
modArrBest = zeros(length(kArr), length(epsArr));
f1ArrBest = zeros(length(kArr), length(epsArr));
nmiArrBest = zeros(length(kArr), length(epsArr));
modArrPart2 = zeros(length(kArr), length(epsArr));
f1ArrPart2 = zeros(length(kArr), length(epsArr));
nmiArrPart2 = zeros(length(kArr), length(epsArr));

for i1=1:length(kArr)
    k = kArr(i1);
    level = levelArr(i1);
    for i2=1:length(epsArr)
        eps = epsArr(i2);
        [modArrBest(i1,i2), f1ArrBest(i1,i2), nmiArrBest(i1,i2), modArrPart2(i1,i2), f1ArrPart2(i1,i2), nmiArrPart2(i1,i2)] = ...
            aaa_readfile2([dataname '_md_' int2str(burnfactor) '_' int2str(level) '_'  int2str(k) '_' sprintf('%.1f', eps) '_2.00']);
    end
end

plot(epsArr, modArrBest(1,:), epsArr, modArrBest(2,:), epsArr, modArrBest(3,:), epsArr, modArrBest(4,:), epsArr, modArrBest(5,:), epsArr, modArrBest(6,:));
legend('k=2','k=3','k=4','k=5','k=6','k=10');

figure;
plot(epsArr, f1ArrBest(1,:), epsArr, f1ArrBest(2,:), epsArr, f1ArrBest(3,:), epsArr, f1ArrBest(4,:), epsArr, f1ArrBest(5,:), epsArr, f1ArrBest(6,:));
legend('k=2','k=3','k=4','k=5','k=6','k=10');

