

% AGT (page 66)
A = [4 1; 5 0];
B = [4 5; 1 0];

% linear program
f = reshape(-(A+B),1,4);

A1 = [1 -1 0 0; 0 0 -1 1; 1 0 -1 0; 0 -1 0 1];
A1 = [-eye(4); A1];

b1 = [0 0 0 0 0 0 0 0]';

A2 = [1 1 1 1];
b2 = 1;


x = linprog(f,A1,b1,A2,b2);