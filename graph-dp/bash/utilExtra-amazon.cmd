
echo AMAZON
rem python ../compare/utility_extra_igraph.py ../data/com_amazon_ungraph.gr 20 0.01 0 > ../console/com_amazon_ungraph-UTIL-EXTRA.txt

echo AMAZON-entropy
rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_entropy_0001_2_001.out 20 0.01 0 > ../console/com_amazon_ungraph_entropy_0001_2_001-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_entropy_001_2_001.out 20 0.01 0 > ../console/com_amazon_ungraph_entropy_001_2_001-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_entropy_01_2_001.out 20 0.01 0 > ../console/com_amazon_ungraph_entropy_01_2_001-UTIL-EXTRA.txt

echo AMAZON-cvxopt-nb
rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_200000_20_nb_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_400000_20_nb_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_600000_20_nb_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_800000_20_nb_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_1000000_20_nb_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_nb_missing-UTIL-EXTRA.txt

echo AMAZON-cvxopt-rand
rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_200000_20_rand_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_rand_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_400000_20_rand_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_rand_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_600000_20_rand_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_rand_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_800000_20_rand_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_800000_20_rand_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_1000000_20_rand_missing.out 20 0.01 0 > ../console/com_amazon_ungraph_cvxopt_1000000_20_rand_missing-UTIL-EXTRA.txt


echo AMAZON-randwalk-replace
python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_replace_2_10_0.2.out 20 0.02 2 > ../console/com_amazon_ungraph_randwalk_replace_2_10_0.2-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_replace_2_10_0.4.out 20 0.02 2 > ../console/com_amazon_ungraph_randwalk_replace_2_10_0.4-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_replace_2_10_0.6.out 20 0.02 2 > ../console/com_amazon_ungraph_randwalk_replace_2_10_0.6-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_amazon_ungraph_randwalk_replace_2_10_0.8.out 20 0.02 2 > ../console/com_amazon_ungraph_randwalk_replace_2_10_0.8-UTIL-EXTRA.txt

echo AMAZON-cvxopt-rdwalk
python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_200000_20_rw_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_200000_20_rw_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_400000_20_rw_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_400000_20_rw_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_amazon_ungraph_cvxopt_600000_20_rw_missing.out 20 0.02 0 > ../console/com_amazon_ungraph_cvxopt_600000_20_rw_missing-UTIL-EXTRA.txt


