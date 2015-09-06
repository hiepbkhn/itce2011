

echo YOUTUBE-switch-nb
python ../compare/utility_measure_igraph.py ../switch/com_youtube_ungraph_switch_nb_300000.out 20 1 1134890 > ../console2/com_youtube_ungraph_switch_nb_300000-UTIL.txt

python ../compare/utility_measure_igraph.py ../switch/com_youtube_ungraph_switch_nb_600000.out 20 1 1134890 > ../console2/com_youtube_ungraph_switch_nb_600000-UTIL.txt

python ../compare/utility_measure_igraph.py ../switch/com_youtube_ungraph_switch_nb_900000.out 20 1 1134890 > ../console2/com_youtube_ungraph_switch_nb_900000-UTIL.txt

python ../compare/utility_measure_igraph.py ../switch/com_youtube_ungraph_switch_nb_1200000.out 20 1 1134890 > ../console2/com_youtube_ungraph_switch_nb_1200000-UTIL.txt

python ../compare/utility_measure_igraph.py ../switch/com_youtube_ungraph_switch_nb_1500000.out 20 1 1134890 > ../console2/com_youtube_ungraph_switch_nb_1500000-UTIL.txt

echo YOUTUBE-randwalk
python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_2_10.out 20 2 1134890 > ../console2/com_youtube_ungraph_randwalk_2_10-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_3_10.out 20 2 1134890 > ../console2/com_youtube_ungraph_randwalk_3_10-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_5_10.out 20 2 1134890 > ../console2/com_youtube_ungraph_randwalk_5_10-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_10_10.out 20 2 1134890 > ../console2/com_youtube_ungraph_randwalk_10_10-UTIL.txt