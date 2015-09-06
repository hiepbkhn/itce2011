
echo AMAZON-switch-rand
rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_rand_100000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_rand_100000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_rand_200000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_rand_200000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_rand_300000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_rand_300000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_rand_400000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_rand_400000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_rand_500000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_rand_500000-UTIL-EXTRA.txt

echo AMAZON-switch-nb
rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_nb_100000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_nb_100000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_nb_200000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_nb_200000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_nb_300000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_nb_300000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_nb_400000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_nb_400000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_amazon_ungraph_switch_nb_500000.out 20 0.01 1 > ../console/com_amazon_ungraph_switch_nb_500000-UTIL-EXTRA.txt

echo AMAZON-randwalk
rem python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_2_10.out 20 0.01 2 > ../console/com_amazon_ungraph_randwalk_2_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_3_10.out 20 0.01 2 > ../console/com_amazon_ungraph_randwalk_3_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_5_10.out 20 0.01 2 > ../console/com_amazon_ungraph_randwalk_5_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_10_10.out 20 0.01 2 > ../console/com_amazon_ungraph_randwalk_10_10-UTIL-EXTRA.txt


python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_800000_20_rw_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_rw_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_1000000_20_rw_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_rw_missing-UTIL-EXTRA.txt

echo AMAZON-cvxopt-rdwalk-first
python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_200000_20_rw_first_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_400000_20_rw_first_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_600000_20_rw_first_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_800000_20_rw_first_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing-UTIL-EXTRA.txt

