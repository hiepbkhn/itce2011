python mesh_cloak_parallel.py oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 800 2 > ../out/parallel_5000_800_2_5_c2.txt;
echo "5000: 2 cpus - DONE"

python mesh_cloak_parallel.py oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 800 4 > ../out/parallel_5000_800_2_5_c4.txt;
echo "5000: 4 cpus - DONE"

python mesh_cloak_parallel.py oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 800 8 > ../out/parallel_5000_800_2_5_c8.txt;
echo "5000: 8 cpus - DONE"

#python mesh_cloak_parallel.py oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 600 2 > ../out/parallel_10000_600_2_5_c2.txt;
#echo "10000: 2 cpus - DONE"

#python mesh_cloak_parallel.py oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 600 4 > ../out/parallel_10000_600_2_5_c4.txt;
#echo "10000: 4 cpus - DONE"

#python mesh_cloak_parallel.py oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 600 8 > ../out/parallel_10000_600_2_5_c8.txt;
#echo "10000: 8 cpus - DONE"

#python mesh_cloak_parallel.py oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 500 2 > ../out/parallel_20000_500_2_5_c2.txt;
#echo "20000: 2 cpus - DONE"

python mesh_cloak_parallel.py oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 500 4 > ../out/parallel_20000_500_2_5_c4.txt;
#echo "20000: 4 cpus - DONE"

#python mesh_cloak_parallel.py oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt 3 500 8 > ../out/parallel_20000_500_2_5_c8.txt;
#echo "20000: 8 cpus - DONE"

echo
echo "Parallel Run - DONE !"