
echo AMAZON-cvxopt-nearby
python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 10000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 20000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 30000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 40000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 50000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 60000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_1200000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 70000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_1400000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 80000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_1600000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 90000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_1800000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 100000 20 1 0 > ../console/com_amazon_ungraph_cvxopt_2000000_20_nb_missing-CVXOPT.txt

echo AMAZON-cvxopt-rdwalk
python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 10000 20 3 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_rw_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 20000 20 3 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_rw_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 30000 20 3 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_rw_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 40000 20 3 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_rw_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 50000 20 3 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_rw_missing-CVXOPT.txt

echo AMAZON-cvxopt-rdwalk-first
python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 10000 20 3 1 > ../console/com_amazon_ungraph_cvxopt_200000_20_rw_first_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 20000 20 3 1 > ../console/com_amazon_ungraph_cvxopt_400000_20_rw_first_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 30000 20 3 1 > ../console/com_amazon_ungraph_cvxopt_600000_20_rw_first_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 40000 20 3 1 > ../console/com_amazon_ungraph_cvxopt_800000_20_rw_first_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 50000 20 3 1 > ../console/com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing-CVXOPT.txt


