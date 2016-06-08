

% chicken game (Computing Correlated Equilibria in Multi-Player Games,
% 2008)

A = [4 1; 5 0];
B = [4 5; 1 0];

% correlated equilibrium
% x = [0 1; 0 0];
% x = [0 0; 1 0];
% x = [1/4 1/4; 1/4 1/4];
% x = [0 1/2; 1/2 0];
% x = [1/3 1/3; 1/3 0];

% NOT correlated equilibrium
x = [0.1 0.2; 0.3 0.4];


% p = 1
for i=1:2
    for j=1:2
        u1 = 0.0;
        for s=1:2
            u1 = u1 + (A(i,s)-A(j,s))*x(i,s);
        end
        fprintf('u1 = %f\n', u1);
    end
end

% p = 2
for i=1:2
    for j=1:2
        u2 = 0.0;
        for s=1:2
            u2 = u2 + (B(s,i)-B(s,j))*x(s,i);
        end
        fprintf('u2 = %f\n', u2);
    end
end
