
echo DBLP-cvxopt-nearby
python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 10000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 30000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 40000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 50000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 60000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 70000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_1400000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 80000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_1600000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 90000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_1800000_20_nb_missing-CVXOPT.txt

python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 100000 20 1 0 > ../console/com_dblp_ungraph_cvxopt_2000000_20_nb_missing-CVXOPT.txt


echo DBLP-cvxopt-rdwalk
rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 10000 20 3 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_rw_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 3 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_rw_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 30000 20 3 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_rw_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 40000 20 3 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_rw_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 50000 20 3 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_rw_missing-CVXOPT.txt

echo DBLP-cvxopt-rdwalk-first
rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 10000 20 3 1 > ../console/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 3 1 > ../console/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 30000 20 3 1 > ../console/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 40000 20 3 1 > ../console/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 50000 20 3 1 > ../console/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing-CVXOPT.txt

echo DBLP-cvxopt-nb-delete (TEST)
rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 10000 > ../console/com_dblp_ungraph_cvxopt_400000_20_200000_nb_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 5000 > ../console/com_dblp_ungraph_cvxopt_400000_20_100000_nb_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 1000 > ../console/com_dblp_ungraph_cvxopt_400000_20_20000_nb_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 500 > ../console/com_dblp_ungraph_cvxopt_400000_20_10000_nb_missing-CVXOPT.txt

rem python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 20000 20 1 0 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_0_nb_missing-CVXOPT.txt

rem AMAZON
rem python ../uncertain/uncertain_convex_opt_parallel.py com_amazon_ungraph.gr 20000 20 1 0 500 > ../console/com_amazon_ungraph_cvxopt_400000_20_10000_nb_missing-CVXOPT.txt

rem YOUTUBE
rem python ../uncertain/uncertain_convex_opt_parallel.py com_youtube_ungraph.gr 20000 60 1 0 500 > ../console/com_youtube_ungraph_cvxopt_1200000_60_30000_nb_missing-CVXOPT.txt


echo DBLP-cvxopt-nb-switch-nb
python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 10000 20 1 2 0 0.1 1 > ../console/com_dblp_ungraph_cvxopt_200000_20_nb_switch_0.1_nb_missing-CVXOPT.txt



