
% Fig 4.3 (Essentials of Game Theory)

% A = [3 3 8 8; 3 3 8 8; 5 2 5 2; 5 1 5 1];
% B = [8 8 3 3; 8 8 3 3; 5 10 5 10; 5 0 5 0];
% nA = 4;
% nB = 4;

% A = [-4 -1; -5 -2];
% B = [-4 -5; -1 -2];
% nA = 2;
% nB = 2;

% examples of link disclosure (zero-sum)
% strategies
sA = [5 2 3 1];
sB = [4 2 2];
nA = length(sA);
nB = length(sB);
A = zeros(nA, nB);
for i=1:nA
    for j=1:nB
        A(i,j) = sA(i) - sB(j);
    end
end
A = -A;     % to find max-row, min-col
B = -A;     % zero-sum game

% cell (i,j)
for i=1:nA
    for j=1:nB
        is_equilibrium = 1;
        for k=1:nA
            if A(k,j) > A(i,j)
                is_equilibrium = 0;
                break
            end
        end
        
        if is_equilibrium == 0
            continue
        end
        
        for k=1:nB
            if B(i,k) > B(i,j)
                is_equilibrium = 0;
                break
            end
        end
        
        %
        if is_equilibrium == 1
            fprintf('[%d %d](%d,%d)\n', i,j, A(i,j), B(i,j));
        end
    end
end


