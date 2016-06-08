
% p = [1 2 3];    % price
p = [2 2 5];    % price

e = [4 5 6];    % money

u = [1 2 3; 2 4 1; 2 5 4];  % utility

n = length(p);  % j
n2 = length(e); % i
s = n+n2+1;
t = n+n2+2;

alpha = u./repmat(p,n2,1);

row_max = max(alpha,[],2);



idr = [];
idc = [];
val = [];
% edges from s to A
for j=1:n
    idr = [idr s];
    idc = [idc j];
    val = [val p(j)];
end

% edges from B to t
for i=1:n2
    idr = [idr n+i];
    idc = [idc t];
    val = [val e(i)];
end

% edges from A to B
max_val = max(sum(e),sum(p));
for i=1:n2
    for j=1:n
        if alpha(i,j) == row_max(i)
            idr = [idr j];
            idc = [idc n+i];
            val = [val max_val];
        end 
    end
end

cm = sparse(idr, idc, val, t, t);

[M,F,K] = graphmaxflow(cm,s,t);