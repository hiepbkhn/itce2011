
echo YOUTUBE-switch-nb
rem python ../compare/utility_extra_igraph.py ../switch/com_youtube_ungraph_switch_nb_300000.out 20 0.01 1 > ../console/com_youtube_ungraph_switch_nb_300000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_youtube_ungraph_switch_nb_600000.out 20 0.01 1 > ../console/com_youtube_ungraph_switch_nb_600000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_youtube_ungraph_switch_nb_900000.out 20 0.01 1 > ../console/com_youtube_ungraph_switch_nb_900000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_youtube_ungraph_switch_nb_1200000.out 20 0.01 1 > ../console/com_youtube_ungraph_switch_nb_1200000-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../switch/com_youtube_ungraph_switch_nb_1500000.out 20 0.01 1 > ../console/com_youtube_ungraph_switch_nb_1500000-UTIL-EXTRA.txt

echo YOUTUBE-randwalk
rem python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_2_10.out 20 0.01 2 > ../console/com_youtube_ungraph_randwalk_2_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_3_10.out 20 0.01 2 > ../console/com_youtube_ungraph_randwalk_3_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_5_10.out 20 0.01 2 > ../console/com_youtube_ungraph_randwalk_5_10-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_10_10.out 20 0.01 2 > ../console/com_youtube_ungraph_randwalk_10_10-UTIL-EXTRA.txt



echo YOUTUBE-cvxopt-rdwalk-first
python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_600000_60_rw_first_missing.out 20 0.02 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing.out 20 0.02 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing.out 20 0.02 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing.out 20 0.02 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_rw_first_missing-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing.out 20 0.02 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_rw_first_missing-UTIL-EXTRA.txt
