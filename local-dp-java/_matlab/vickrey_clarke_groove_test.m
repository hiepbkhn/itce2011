% Aug 12, 2015

% AGT (section 9.3)

% alternative set A = {1,2}
m = 2;

% players
n = 2;

% |V1| = 2, |V2| = 2
nV1 = 2;
nV2 = 2;

% 1-test values
% V1 = [1 2; 3 2];
% V2 = [2 1; 2 4];

% 2-random values
V1 = rand(nV1,m);
V2 = rand(nV2,m);

% social welfare f
f = zeros(nV1,nV2);

for i=1:nV1
    for j=1:nV2
        [C, f(i,j)] = max(V1(i,:) + V2(j,:) );
    end
end


% h1, h2 (max by row)
h1 = max(V2,[],2);
h2 = max(V1,[],2);

% payments p1, p2
p1 = zeros(nV1,nV2);
p2 = zeros(nV1,nV2);

for i=1:nV1
    for j=1:nV2
        p1(i,j) = max(V2(j,:)) - V2(j,f(i,j));
        p2(i,j) = max(V1(i,:)) - V1(i,f(i,j));
    end
end

% utility
u1 = zeros(nV1,nV2);
u2 = zeros(nV1,nV2);

for i=1:nV1
    for j=1:nV2
        u1(i,j) = V1(i,f(i,j)) - p1(i,j);
        u2(i,j) = V2(j,f(i,j)) - p2(i,j);
    end
end



