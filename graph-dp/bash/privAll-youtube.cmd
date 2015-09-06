
echo YOUTUBE
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../data/com_youtube_ungraph.gr > ../console/com_youtube_ungraph-PRIV.txt

echo YOUTUBE-entropy
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_entropy_0001_2_001.out 20 0 > ../console/com_youtube_ungraph_entropy_0001_2_001-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_entropy_001_2_001.out 20 0 > ../console/com_youtube_ungraph_entropy_001_2_001-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_entropy_01_2_001.out 20 0 > ../console/com_youtube_ungraph_entropy_01_2_001-PRIV.txt

echo YOUTUBE-cvxopt-nb
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_nb_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_3600000_60_nb_missing.out 5 0 > ../console/com_youtube_ungraph_cvxopt_3600000_60_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_4200000_60_nb_missing.out 5 0 > ../console/com_youtube_ungraph_cvxopt_4200000_60_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_4800000_60_nb_missing.out 5 0 > ../console/com_youtube_ungraph_cvxopt_4800000_60_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_5400000_60_nb_missing.out 5 0 > ../console/com_youtube_ungraph_cvxopt_5400000_60_nb_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_6000000_60_nb_missing.out 5 0 > ../console/com_youtube_ungraph_cvxopt_6000000_60_nb_missing-PRIV.txt

echo YOUTUBE-cvxopt-rand
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_600000_60_rand_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1200000_60_rand_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1800000_60_rand_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_2400000_60_rand_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_rand_missing-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_3000000_60_rand_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_rand_missing-PRIV.txt

echo YOUTUBE-switch-rand
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_rand_300000.out 20 1 > ../console/com_youtube_ungraph_switch_rand_300000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_rand_600000.out 20 1 > ../console/com_youtube_ungraph_switch_rand_600000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_rand_900000.out 20 1 > ../console/com_youtube_ungraph_switch_rand_900000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_rand_1200000.out 20 1 > ../console/com_youtube_ungraph_switch_rand_1200000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_rand_1500000.out 20 1 > ../console/com_youtube_ungraph_switch_rand_1500000-PRIV.txt

echo YOUTUBE-switch-nb
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_nb_300000.out 20 1 > ../console/com_youtube_ungraph_switch_nb_300000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_nb_600000.out 20 1 > ../console/com_youtube_ungraph_switch_nb_600000-PRIV.txt


echo YOUTUBE-randwalk-replace
python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.2.out 20 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.2-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.4.out 20 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.4-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.6.out 20 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.6-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.8.out 20 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.8-PRIV.txt

echo YOUTUBE-cvxopt-rdwalk
python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_600000_60_rw_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_rw_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1200000_60_rw_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_rw_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1800000_60_rw_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_rw_missing-PRIV.txt


