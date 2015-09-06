
echo DBLP
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../data/com_dblp_ungraph.gr > ../console/com_dblp_ungraph-PRIV.txt

echo DBLP-entropy
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_entropy_0001_2_001.out 20 0 > ../console/com_dblp_ungraph_entropy_0001_2_001-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_entropy_001_2_001.out 20 0 > ../console/com_dblp_ungraph_entropy_001_2_001-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_entropy_01_2_001.out 20 0 > ../console/com_dblp_ungraph_entropy_01_2_001-PRIV.txt

echo DBLP-cvxopt-nb
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_400000_20_nb_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_600000_20_nb_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_800000_20_nb_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1200000_20_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_1200000_20_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1400000_20_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_1400000_20_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1600000_20_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_1600000_20_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1800000_20_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_1800000_20_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_2000000_20_nb_missing.out 5 0 > ../console/com_dblp_ungraph_cvxopt_2000000_20_nb_missing-PRIV.txt


echo DBLP-cvxopt-rand
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_400000_20_rand_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_600000_20_rand_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_800000_20_rand_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1000000_20_rand_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_rand_missing-PRIV.txt

echo DBLP-switch-rand
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_rand_100000.out 20 1 > ../console/com_dblp_ungraph_switch_rand_100000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_rand_200000.out 20 1 > ../console/com_dblp_ungraph_switch_rand_200000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_rand_300000.out 20 1 > ../console/com_dblp_ungraph_switch_rand_300000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_rand_400000.out 20 1 > ../console/com_dblp_ungraph_switch_rand_400000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_rand_500000.out 20 1 > ../console/com_dblp_ungraph_switch_rand_500000-PRIV.txt

echo DBLP-switch-nb
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_nb_100000.out 20 1 > ../console/com_dblp_ungraph_switch_nb_100000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_nb_200000.out 20 1 > ../console/com_dblp_ungraph_switch_nb_200000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_nb_300000.out 20 1 > ../console/com_dblp_ungraph_switch_nb_300000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_nb_400000.out 20 1 > ../console/com_dblp_ungraph_switch_nb_400000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../switch/com_dblp_ungraph_switch_nb_500000.out 20 1 > ../console/com_dblp_ungraph_switch_nb_500000-PRIV.txt

echo DBLP-randwalk
python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_2_10.out 20 2 > ../console/com_dblp_ungraph_randwalk_2_10-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_3_10.out 20 2 > ../console/com_dblp_ungraph_randwalk_3_10-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_5_10.out 20 2 > ../console/com_dblp_ungraph_randwalk_5_10-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_10_10.out 20 2 > ../console/com_dblp_ungraph_randwalk_10_10-PRIV.txt

echo DBLP-randwalk-replace
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2.out 20 2 > ../console/com_dblp_ungraph_randwalk_replace_2_10_0.2-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.4.out 20 2 > ../console/com_dblp_ungraph_randwalk_replace_2_10_0.4-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.6.out 20 2 > ../console/com_dblp_ungraph_randwalk_replace_2_10_0.6-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.8.out 20 2 > ../console/com_dblp_ungraph_randwalk_replace_2_10_0.8-PRIV.txt

echo DBLP-cvxopt-rdwalk
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_rw_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_400000_20_rw_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_rw_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_600000_20_rw_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_rw_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_800000_20_rw_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_rw_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1000000_20_rw_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_rw_missing-PRIV.txt

echo DBLP-cvxopt-rdwalk-first
rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_dblp_ungraph.gr ../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out 20 0 > ../console/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing-PRIV.txt

echo DBLP-randwalk-keep
python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_keep_2_0.5.out 5 2 > ../console/com_dblp_ungraph_randwalk_keep_2_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_keep_3_0.5.out 5 2 > ../console/com_dblp_ungraph_randwalk_keep_3_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_keep_5_0.5.out 5 2 > ../console/com_dblp_ungraph_randwalk_keep_5_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_randwalk_keep_10_0.5.out 5 2 > ../console/com_dblp_ungraph_randwalk_keep_10_0.5-PRIV.txt

echo DBLP-detwalk-keep
python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_detwalk_keep_2_0.5.out 5 2 > ../console/com_dblp_ungraph_detwalk_keep_2_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_detwalk_keep_3_0.5.out 5 2 > ../console/com_dblp_ungraph_detwalk_keep_3_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_detwalk_keep_5_0.5.out 5 2 > ../console/com_dblp_ungraph_detwalk_keep_5_0.5-PRIV.txt

python ../compare/incorrectness_measure_multigraph.py ../data/com_dblp_ungraph.gr ../randwalk/com_dblp_ungraph_detwalk_keep_10_0.5.out 5 2 > ../console/com_dblp_ungraph_detwalk_keep_10_0.5-PRIV.txt

