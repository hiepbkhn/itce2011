rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_nb_900000.out 20 1 > ../console/com_youtube_ungraph_switch_nb_900000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_nb_1200000.out 20 1 > ../console/com_youtube_ungraph_switch_nb_1200000-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../switch/com_youtube_ungraph_switch_nb_1500000.out 20 1 > ../console/com_youtube_ungraph_switch_nb_1500000-PRIV.txt

echo YOUTUBE-randwalk
rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_2_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_2_10-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_3_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_3_10-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_5_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_5_10-PRIV.txt

rem python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_10_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_10_10-PRIV.txt



python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_2400000_60_rw_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_rw_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_3000000_60_rw_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_rw_missing-PRIV.txt

echo YOUTUBE-cvxopt-rdwalk-first
python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_600000_60_rw_first_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_rw_first_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing-PRIV.txt

python ../compare/incorrectness_measure_igraph.py ../data/com_youtube_ungraph.gr ../out/com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing.out 20 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing-PRIV.txt

echo YOUTUBE-randwalk-keep
rem python ../compare/incorrectness_measure_multigraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_keep_2_0.5.out 5 2 > ../console/com_youtube_ungraph_randwalk_keep_2_0.5-PRIV.txt

rem python ../compare/incorrectness_measure_multigraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_keep_3_0.5.out 5 2 > ../console/com_youtube_ungraph_randwalk_keep_3_0.5-PRIV.txt

rem python ../compare/incorrectness_measure_multigraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_keep_5_0.5.out 5 2 > ../console/com_youtube_ungraph_randwalk_keep_5_0.5-PRIV.txt

rem python ../compare/incorrectness_measure_multigraph.py ../data/com_youtube_ungraph.gr ../randwalk/com_youtube_ungraph_randwalk_keep_10_0.5.out 5 2 > ../console/com_youtube_ungraph_randwalk_keep_10_0.5-PRIV.txt
