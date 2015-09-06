

echo YOUTUBE-randwalk
python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_5_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_5_10-UTIL.txt

python ../compare/utility_measure_igraph.py ../randwalk/com_youtube_ungraph_randwalk_10_10.out 20 2 > ../console/com_youtube_ungraph_randwalk_10_10-UTIL.txt

echo YOUTUBE-randwalk-keep
rem python ../compare/utility_measure_multigraph.py ../randwalk/com_youtube_ungraph_randwalk_keep_2_0.5.out 5 2 1134890 > ../console/com_youtube_ungraph_randwalk_keep_2_0.5-UTIL.txt

rem python ../compare/utility_measure_multigraph.py ../randwalk/com_youtube_ungraph_randwalk_keep_3_0.5.out 5 2 1134890 > ../console/com_youtube_ungraph_randwalk_keep_3_0.5-UTIL.txt

rem python ../compare/utility_measure_multigraph.py ../randwalk/com_youtube_ungraph_randwalk_keep_5_0.5.out 5 2 1134890 > ../console/com_youtube_ungraph_randwalk_keep_5_0.5-UTIL.txt

rem python ../compare/utility_measure_multigraph.py ../randwalk/com_youtube_ungraph_randwalk_keep_10_0.5.out 5 2 1134890 > ../console/com_youtube_ungraph_randwalk_keep_10_0.5-UTIL.txt
