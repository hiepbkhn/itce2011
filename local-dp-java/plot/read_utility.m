% Jun 26, 2016

function [ arrCC, arrPL, arrAPD, arrDist  ] = read_utility(round, file_name )

arrAD = zeros(round,100);
arrAPD = zeros(round,100);
arrCC = zeros(round,100);
arrPL = zeros(round,100);
arrDist = zeros(round,100,50);

for t=1:round
    sumDist = zeros(100,50);
    
    for i=0:9
        file = [file_name int2str(t) '.' int2str(i) '.mat'];      
%         display(file);
        load(file);
        
        arrAD(t,:) = arrAD(t,:) + a_AD;
        arrAPD(t,:) = arrAPD(t,:) + a_APD;
        arrCC(t,:) = arrCC(t,:) + a_CC;
        arrPL(t,:) = arrPL(t,:) + a_PL;
        sumDist = sumDist + (distArr ./ repmat(sum(distArr,2),1,50));
    end
    arrAD(t,:) = arrAD(t,:)/10;
    arrAPD(t,:) = arrAPD(t,:)/10;
    arrCC(t,:) = arrCC(t,:)/10;
    arrPL(t,:) = arrPL(t,:)/10;
    arrDist(t,:,:) = sumDist/10;
end

end

