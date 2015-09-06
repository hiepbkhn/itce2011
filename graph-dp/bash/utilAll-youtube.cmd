
echo YOUTUBE
rem python ../compare/utility_measure_igraph.py ../data/com_youtube_ungraph.gr 20 0 1134890 > ../console2/com_youtube_ungraph-UTIL.txt

echo YOUTUBE-entropy
rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_entropy_0001_2_001.out 20 0 1134890 > ../console2/com_youtube_ungraph_entropy_0001_2_001-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_entropy_001_2_001.out 20 0 1134890 > ../console2/com_youtube_ungraph_entropy_001_2_001-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_entropy_01_2_001.out 20 0 1134890 > ../console2/com_youtube_ungraph_entropy_01_2_001-UTIL.txt

echo YOUTUBE-cvxopt-nb
rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out 20 0 1134890 > ../console2/com_youtube_ungraph_cvxopt_600000_60_nb_missing-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out 20 0 1134890 > ../console2/com_youtube_ungraph_cvxopt_1200000_60_nb_missing-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out 20 0 1134890 > ../console2/com_youtube_ungraph_cvxopt_1800000_60_nb_missing-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out 20 0 1134890 > ../console2/com_youtube_ungraph_cvxopt_2400000_60_nb_missing-UTIL.txt

rem python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out 20 0 1134890 > ../console2/com_youtube_ungraph_cvxopt_3000000_60_nb_missing-UTIL.txt

python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_3600000_60_nb_missing.out 5 0 1134890 > ../console/com_youtube_ungraph_cvxopt_3600000_60_nb_missing-UTIL.txt

python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_4200000_60_nb_missing.out 5 0 1134890 > ../console/com_youtube_ungraph_cvxopt_4200000_60_nb_missing-UTIL.txt

python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_4800000_60_nb_missing.out 5 0 1134890 > ../console/com_youtube_ungraph_cvxopt_4800000_60_nb_missing-UTIL.txt

python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_5400000_60_nb_missing.out 5 0 1134890 > ../console/com_youtube_ungraph_cvxopt_5400000_60_nb_missing-UTIL.txt

python ../compare/utility_measure_igraph.py ../out/com_youtube_ungraph_cvxopt_6000000_60_nb_missing.out 5 0 1134890 > ../console/com_youtube_ungraph_cvxopt_6000000_60_nb_missing-UTIL.txt

echo YOUTUBE-randwalk-replace
python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.2.out 20 2 1134890 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.2-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.4.out 20 2 1134890 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.4-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.6.out 20 2 1134890 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.6-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.8.out 20 2 1134890 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.8-UTIL.txt