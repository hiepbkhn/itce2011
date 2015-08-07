
% AGT (page 76)
A = [3 3; 2 5; 0 6];
B = [3 2; 2 6; 3 1];

% x = [4/5 1/5 0];
% y = [2/3 1/3]';
% x*A*y;
% x*B*y;
% A*y;
% x*B;

% linear program for A (maximin)
% min f*x s.t. A1*x <= b1, A2*x = b2

f = [0 0 0 1];

% A1 = [[-eye(3); 0 0 0]';[-A; -1 -1]'];
A1 = [[-eye(3); 0 0 0]';[-B; -1 -1]'];
b1 = [0 0 0 0 0]';

A2 = [1 1 1 0];
b2 = 1;


x = linprog(f,A1,b1,A2,b2);

