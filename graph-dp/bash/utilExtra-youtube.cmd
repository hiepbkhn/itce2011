
echo YOUTUBE
rem python ../compare/utility_extra_igraph.py ../data/com_youtube_ungraph.gr 20 0.01 0 > ../console/com_youtube_ungraph-UTIL-EXTRA.txt

echo YOUTUBE-entropy
rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_entropy_0001_2_001.out 20 0.01 0 > ../console/com_youtube_ungraph_entropy_0001_2_001-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_entropy_001_2_001.out 20 0.01 0 > ../console/com_youtube_ungraph_entropy_001_2_001-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_entropy_01_2_001.out 20 0.01 0 > ../console/com_youtube_ungraph_entropy_01_2_001-UTIL-EXTRA.txt

echo YOUTUBE-cvxopt-nb
rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out 20 0.01 0 > ../console/com_youtube_ungraph_cvxopt_600000_60_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out 20 0.01 0 > ../console/com_youtube_ungraph_cvxopt_1200000_60_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out 20 0.01 0 > ../console/com_youtube_ungraph_cvxopt_1800000_60_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out 20 0.01 0 > ../console/com_youtube_ungraph_cvxopt_2400000_60_nb_missing-UTIL-EXTRA.txt

rem python ../compare/utility_extra_igraph.py ../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out 20 0.01 0 > ../console/com_youtube_ungraph_cvxopt_3000000_60_nb_missing-UTIL-EXTRA.txt


echo YOUTUBE-randwalk-replace
python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.2.out 20 0.02 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.2-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.4.out 20 0.02 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.4-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.6.out 20 0.02 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.6-UTIL-EXTRA.txt

python ../compare/utility_extra_igraph.py ../randwalk/com_youtube_ungraph_randwalk_replace_2_10_0.8.out 20 0.02 2 > ../console/com_youtube_ungraph_randwalk_replace_2_10_0.8-UTIL-EXTRA.txt


