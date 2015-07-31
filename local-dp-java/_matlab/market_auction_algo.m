
%%%%%%%% MAIN
% inputs: a (endowment), v (per-unit utility)
function [x,p] = market_auction_algo(a,v)

% traders (i)
n = size(a,1);  
% goods (j)
m = size(a,2);

% a_min
a_min = min(sum(a));

% 

% init
x = zeros(n,m);
y = zeros(n,m);
h = zeros(n,m);

% prices
p = ones(1,m);

% money surplus
r = zeros(1,n);
for i=1:n
    r(i) = a(i,:).*p;
end

% alpha
alpha = max(v./repmat(p,n,1), [], 2);

while 1==1
    i = 1;
    for i=1:n
        if r(i) > 0
            break
        end
    end
    
    alpha(i) = max(v(i,:)./p);
    
    % demand set D_i
    D = find(v(i,:)./p == alpha(i));
    
    j = D(1);
    
    if sum(x(:,j)) < a(j)
        assign(i,j)
    else
        K = find(y(:,j) > 0);
        if isempty(K) == 0
            k = K(1);
            outbid(i,j,k);
        else
            raise_price(j);
        end
    end
end

end

%%%%%%%%%%%%%%%%%%%
function assign(i, j)
    
end

%%%%%%%%%%%%%%%%%%%
function outbid(i, j, k)

end

%%%%%%%%%%%%%%%%%%%
function raise_price(j)

end

