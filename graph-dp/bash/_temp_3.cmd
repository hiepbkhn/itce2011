
echo "run cvxopt"
python ../uncertain/uncertain_convex_opt_parallel.py com_dblp_ungraph.gr 60000 20 1 2 0 0.1 1 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing-CVXOPT.txt

echo "gen 5 samples"
python ../compare/graph_generator_igraph.py com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing 317080 5 0 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing-CONSOLE.txt

echo "privacy 5 samples"
python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing-PRIV.txt

echo "utility 5 samples"
python ../compare/utility_measure_igraph.py ../out/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing.out 5 0 317080 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_switch_0.1_nb_missing-UTIL.txt

