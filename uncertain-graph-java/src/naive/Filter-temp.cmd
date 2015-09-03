

echo AMAZON
java naive.GreedyReconstruct ../ com_amazon_ungraph 10 4.0 > ..\_console\com_amazon_ungraph_filter_10_4.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_amazon_ungraph 10 8.0 > ..\_console\com_amazon_ungraph_filter_10_8.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_amazon_ungraph 10 16.0 > ..\_console\com_amazon_ungraph_filter_10_16.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_amazon_ungraph 10 32.0 > ..\_console\com_amazon_ungraph_filter_10_32.0-CONSOLE.txt

echo YOUTUBE
java naive.GreedyReconstruct ../ com_youtube_ungraph 10 4.0 > ..\_console\com_youtube_ungraph_filter_10_4.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_youtube_ungraph 10 8.0 > ..\_console\com_youtube_ungraph_filter_10_8.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_youtube_ungraph 10 16.0 > ..\_console\com_youtube_ungraph_filter_10_16.0-CONSOLE.txt
java naive.GreedyReconstruct ../ com_youtube_ungraph 10 32.0 > ..\_console\com_youtube_ungraph_filter_10_32.0-CONSOLE.txt


