
echo COMMUNITY-FIT (DIVISIVE)
java dp.comm.CommunityFit ../ polbooks 10 20 > ..\_console\polbooks_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ polblogs 10 20 > ..\_console\polblogs_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ as20graph 10 20 > ..\_console\as20graph_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ wiki-Vote 10 20 > ..\_console\wiki-Vote_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ ca-HepPh 10 20 > ..\_console\ca-HepPh_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ ca-AstroPh 10 20 > ..\_console\ca-AstroPh_divisive_10_20-CONSOLE.txt


java dp.comm.CommunityFit ../ com_amazon_ungraph 10 20 > ..\_console\com_amazon_ungraph_divisive_10_20-CONSOLE.txt

java dp.comm.CommunityFit ../ com_youtube_ungraph 10 20 > ..\_console\com_youtube_ungraph_divisive_10_20-CONSOLE.txt

rem ---- USE limit_size
java dp.comm.CommunityFit ../ polbooks 10 20 1 > ..\_console\polbooks_divisive_10_20_1-CONSOLE.txt
java dp.comm.CommunityFit ../ polbooks 10 20 4 > ..\_console\polbooks_divisive_10_20_4-CONSOLE.txt

java dp.comm.CommunityFit ../ polblogs 10 20 1 > ..\_console\polblogs_divisive_10_20_1-CONSOLE.txt
java dp.comm.CommunityFit ../ polblogs 10 20 4 > ..\_console\polblogs_divisive_10_20_4-CONSOLE.txt
java dp.comm.CommunityFit ../ polblogs 10 20 8 > ..\_console\polblogs_divisive_10_20_8-CONSOLE.txt
java dp.comm.CommunityFit ../ polblogs 10 20 32 > ..\_console\polblogs_divisive_10_20_32-CONSOLE.txt




java dp.comm.CommunityFit ../ ca-AstroPh 10 20 32 > ..\_console\ca-AstroPh_divisive_10_20_32-CONSOLE.txt


java dp.comm.CommunityFit ../ com_amazon_ungraph 10 20 64 > ..\_console\com_amazon_ungraph_divisive_10_20_64-CONSOLE.txt
java dp.comm.CommunityFit ../ com_amazon_ungraph 10 20 256 > ..\_console\com_amazon_ungraph_divisive_10_20_256-CONSOLE.txt
java dp.comm.CommunityFit ../ com_amazon_ungraph 10 20 8192 > ..\_console\com_amazon_ungraph_divisive_10_20_8192-CONSOLE.txt


java dp.comm.CommunityFit ../ com_youtube_ungraph 10 20 65536 > ..\_console\com_youtube_ungraph_divisive_10_20_65536-CONSOLE.txt
