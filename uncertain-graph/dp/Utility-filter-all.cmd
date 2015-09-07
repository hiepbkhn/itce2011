
echo FILTER eps=4.0
python utility_measure.py com_amazon_ungraph com_amazon_ungraph_filter_4.0 10 334863 > ../_console/com_amazon_ungraph_filter_4.0-UTIL.txt

python utility_measure.py com_youtube_ungraph com_youtube_ungraph_filter_4.0 10 1134890 > ../_console/com_youtube_ungraph_filter_4.0-UTIL.txt


echo FILTER eps=8.0
python utility_measure.py com_amazon_ungraph com_amazon_ungraph_filter_8.0 10 334863 > ../_console/com_amazon_ungraph_filter_8.0-UTIL.txt

python utility_measure.py com_youtube_ungraph com_youtube_ungraph_filter_8.0 10 1134890 > ../_console/com_youtube_ungraph_filter_8.0-UTIL.txt


echo FILTER eps=16.0
python utility_measure.py com_amazon_ungraph com_amazon_ungraph_filter_16.0 10 334863 > ../_console/com_amazon_ungraph_filter_16.0-UTIL.txt

python utility_measure.py com_youtube_ungraph com_youtube_ungraph_filter_16.0 10 1134890 > ../_console/com_youtube_ungraph_filter_16.0-UTIL.txt


echo FILTER eps=32.0
python utility_measure.py com_amazon_ungraph com_amazon_ungraph_filter_32.0 10 334863 > ../_console/com_amazon_ungraph_filter_32.0-UTIL.txt

python utility_measure.py com_youtube_ungraph com_youtube_ungraph_filter_32.0 10 1134890 > ../_console/com_youtube_ungraph_filter_32.0-UTIL.txt





echo FILTER eps=16.0
python utility_measure.py polbooks polbooks_filter_16.0 10 105 > ../_console/polbooks_filter_16.0-UTIL.txt


python utility_measure.py polblogs polblogs_filter_16.0 10 1224 > ../_console/polblogs_filter_16.0-UTIL.txt


python utility_measure.py as20graph as20graph_filter_16.0 10 6474 > ../_console/as20graph_filter_16.0-UTIL.txt


python utility_measure.py wiki-Vote wiki-Vote_filter_16.0 10 7115 > ../_console/wiki-Vote_filter_16.0-UTIL.txt


python utility_measure.py ca-HepPh ca-HepPh_filter_16.0 10 12006 > ../_console/ca-HepPh_filter_16.0-UTIL.txt


python utility_measure.py ca-AstroPh ca-AstroPh_filter_16.0 10 18771 > ../_console/ca-AstroPh_filter_16.0-UTIL.txt


echo FILTER eps=1.0
python utility_measure.py polbooks polbooks_filter_1.0 10 105 > ../_console/polbooks_filter_1.0-UTIL.txt


python utility_measure.py polblogs polblogs_filter_1.0 10 1224 > ../_console/polblogs_filter_1.0-UTIL.txt


python utility_measure.py as20graph as20graph_filter_1.0 10 6474 > ../_console/as20graph_filter_1.0-UTIL.txt


python utility_measure.py wiki-Vote wiki-Vote_filter_1.0 10 7115 > ../_console/wiki-Vote_filter_1.0-UTIL.txt


python utility_measure.py ca-HepPh ca-HepPh_filter_1.0 10 12006 > ../_console/ca-HepPh_filter_1.0-UTIL.txt


python utility_measure.py ca-AstroPh ca-AstroPh_filter_1.0 10 18771 > ../_console/ca-AstroPh_filter_1.0-UTIL.txt