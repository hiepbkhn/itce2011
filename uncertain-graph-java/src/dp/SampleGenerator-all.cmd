
echo LOUVAIN
rem java dp.SampleGenerator ../ polbooks 10 polbooks_louvain_dendro_10 > ..\_console\polbooks_louvain_dendro_10_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ polbooks 10 polbooks_louvain_dendro_50 > ..\_console\polbooks_louvain_dendro_50_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ polblogs 10 polblogs_louvain_dendro_50 > ..\_console\polblogs_louvain_dendro_50_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ as20graph 10 as20graph_louvain_dendro_50 > ..\_console\as20graph_louvain_dendro_50_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ wiki-Vote 10 wiki-Vote_louvain_dendro_50 > ..\_console\wiki-Vote_louvain_dendro_50_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-HepPh 10 ca-HepPh_louvain_dendro_50 > ..\_console\ca-HepPh_louvain_dendro_50_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-AstroPh 10 ca-AstroPh_louvain_dendro_50 > ..\_console\ca-AstroPh_louvain_dendro_50_sample_10-CONSOLE.txt


echo HRG-FIT (WITHOUT LAPLACE in 2ND PHASE)
rem java dp.SampleGenerator ../ polbooks 10 polbooks_hrg_50_1000_105 > ..\_console\polbooks_hrg_50_1000_105_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ polblogs 10 polblogs_hrg_50_1000_1224 > ..\_console\polblogs_hrg_50_1000_1224_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ as20graph 10 as20graph_hrg_50_1000_6474 > ..\_console\as20graph_hrg_50_1000_6474_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ wiki-Vote 10 wiki-Vote_hrg_50_1000_7115 > ..\_console\wiki-Vote_hrg_50_1000_7115_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-HepPh 10 ca-HepPh_hrg_50_1000_1000 > ..\_console\ca-HepPh_hrg_50_1000_1000_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-AstroPh 10 ca-AstroPh_hrg_50_1000_1000 > ..\_console\ca-AstroPh_hrg_50_1000_1000_sample_10-CONSOLE.txt


echo HRG-MCMC (WITHOUT LAPLACE in 2ND PHASE)
rem java dp.SampleGenerator ../ polbooks 10 polbooks_dendro_50_1000_105 > ..\_console\polbooks_dendro_50_1000_105_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ polblogs 10 polblogs_dendro_50_1000_1224 > ..\_console\polblogs_dendro_50_1000_1224_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ as20graph 10 as20graph_dendro_50_1000_6474 > ..\_console\as20graph_dendro_50_1000_6474_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ wiki-Vote 10 wiki-Vote_dendro_50_1000_7115 > ..\_console\wiki-Vote_dendro_50_1000_7115_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-HepPh 10 ca-HepPh_dendro_50_1000_1000 > ..\_console\ca-HepPh_dendro_50_1000_1000_sample_10-CONSOLE.txt

rem java dp.SampleGenerator ../ ca-AstroPh 10 ca-AstroPh_dendro_50_1000_1000 > ..\_console\ca-AstroPh_dendro_50_1000_1000_sample_10-CONSOLE.txt




echo HRG-FIT (WITH LAPLACE)
java dp.SampleGenerator ../ polbooks 10 polbooks_hrg_50_1000_105 1.0 > ..\_console\polbooks_hrg_50_1000_105_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ polblogs 10 polblogs_hrg_50_1000_1224 1.0 > ..\_console\polblogs_hrg_50_1000_1224_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ as20graph 10 as20graph_hrg_50_1000_6474 1.0 > ..\_console\as20graph_hrg_50_1000_6474_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ wiki-Vote 10 wiki-Vote_hrg_50_1000_7115 1.0 > ..\_console\wiki-Vote_hrg_50_1000_7115_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ ca-HepPh 10 ca-HepPh_hrg_50_1000_1000 1.0 > ..\_console\ca-HepPh_hrg_50_1000_1000_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ ca-AstroPh 10 ca-AstroPh_hrg_50_1000_1000 1.0 > ..\_console\ca-AstroPh_hrg_50_1000_1000_1.0_sample_10-CONSOLE.txt


echo HRG-MCMC (WITH LAPLACE)
java dp.SampleGenerator ../ polbooks 10 polbooks_dendro_50_1000_105 1.0 > ..\_console\polbooks_dendro_50_1000_105_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ polblogs 10 polblogs_dendro_50_1000_1224 1.0 > ..\_console\polblogs_dendro_50_1000_1224_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ as20graph 10 as20graph_dendro_50_1000_6474 1.0 > ..\_console\as20graph_dendro_50_1000_6474_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ wiki-Vote 10 wiki-Vote_dendro_50_1000_7115 1.0 > ..\_console\wiki-Vote_dendro_50_1000_7115_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ ca-HepPh 10 ca-HepPh_dendro_50_1000_1000 1.0 > ..\_console\ca-HepPh_dendro_50_1000_1000_1.0_sample_10-CONSOLE.txt

java dp.SampleGenerator ../ ca-AstroPh 10 ca-AstroPh_dendro_50_1000_1000 1.0 > ..\_console\ca-AstroPh_dendro_50_1000_1000_1.0_sample_10-CONSOLE.txt


