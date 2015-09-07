
rem python recursive_partition.py polbooks 10 10 > ../_console/polbooks_louvain_10_10-CONSOLE.txt

python recursive_partition.py polbooks 10 50 > ../_console/polbooks_louvain_10_50-CONSOLE.txt

python recursive_partition.py polblogs 10 50 > ../_console/polblogs_louvain_10_50-CONSOLE.txt

python recursive_partition.py as20graph 10 50 > ../_console/as20graph_louvain_10_50-CONSOLE.txt

python recursive_partition.py wiki-Vote 10 50 > ../_console/wiki-Vote_louvain_10_50-CONSOLE.txt

rem python recursive_partition.py ca-HepPh 10 50 > ../_console/ca-HepPh_louvain_10_50-CONSOLE.txt

rem python recursive_partition.py ca-AstroPh 10 50 > ../_console/ca-AstroPh_louvain_10_50-CONSOLE.txt


python recursive_partition.py com_amazon_ungraph 1 50 > ../_console/com_amazon_ungraph_louvain_1_50-CONSOLE.txt
python recursive_partition.py com_amazon_ungraph 1 100 > ../_console/com_amazon_ungraph_louvain_1_100-CONSOLE.txt

python recursive_partition.py com_youtube_ungraph 1 50 > ../_console/com_youtube_ungraph_louvain_1_50-CONSOLE.txt
python recursive_partition.py com_youtube_ungraph 1 100 > ../_console/com_youtube_ungraph_louvain_1_100-CONSOLE.txt