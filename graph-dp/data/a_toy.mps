NAME          TESTPROB
ROWS
 N  COST
 E  E0
 E  E1
 E  E2
 E  E3
 E  E4
COLUMNS
    X0      COST              0.5
    X0      E0              1
    X0      E1              1
    X1      COST              1.0
    X1      E1              1
    X1      E2              1
    X2      COST              1.0
    X2      E1              1
    X2      E3              1
    X3      COST              1.0
    X3      E2              1
    X3      E3              1
    X4      COST              0.1
    X4      E3              1
    X4      E4              1
    X5      COST              -0.5
    X5      E1              1
    X5      E4              1
    X6      COST              -0.5
    X6      E0              1
    X6      E3              1
    X7      COST              -0.5
    X7      E0              1
    X7      E2              1
RHS
    RHS1      E0              1
    RHS1      E1              3
    RHS1      E2              2
    RHS1      E3              3
    RHS1      E4              1
BOUNDS
 LO BND1      X0           0
 UP BND1      X0           1
 LO BND1      X1           0
 UP BND1      X1           1
 LO BND1      X2           0
 UP BND1      X2           1
 LO BND1      X3           0
 UP BND1      X3           1
 LO BND1      X4           0
 UP BND1      X4           1
 LO BND1      X5           0
 UP BND1      X5           1
 LO BND1      X6           0
 UP BND1      X6           1
 LO BND1      X7           0
 UP BND1      X7           1
ENDATA