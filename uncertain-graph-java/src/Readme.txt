CLASSPATH: D:\git\itce2011\uncertain-graph-java\bin;E:\caidat\Java\grph-1.6.29-selfcontained\*;E:\Tailieu\Paper-code\Social Network\webgraph\webgraph-deps\*
(copy powerlaws-0.0.1-SNAPSHOT.jar into \grph-1.6.29-selfcontained)

\_cmd	: for CommunityDP
\_cmd2	: for GraphDP + SNAM

[algs4] http://algs4.cs.princeton.edu/
- EdgeWeightedGraph.java: used in Louvain

[dp]
- DPUtil.java: Laplace, geometric mechanisms
- SampleGenerator.java: 
- UtilityMeasure.java --> see uncertain-graph/dp/utility_measure.py

[dp.mcmc] private HRG
- Dendrogram.java
- MCMCInference.java: use Dendrogram.java 

- DendrogramFixed.java: fixed balance binary tree
- MCMCInferenceFixed.java: use DendrogramFixed.java

- SampleGenerator.java: read tree in \out folder (_dendro_, _hrgdiv_, _fixed_) and generate samples

[dp.hrg] non-private HRG
- HRG.java (subclass of Dendrogram)
- MCMCFit.java: use HRG.java

[dp.comm] HRG-Divisive
- CommunityFit.java: non-private, use NodeSet.java
- CommunityPriv.java: private, use NodePriv.java (extends NodeSet.java, non-use)
- CommunityFit2.java: non-private, use NodeSet2.java

- HRGDivisiveFit.java: copied from HRGDivisiveGreedy.java, use NodeSetDivGreedy.java

[dp.der] DER
- DensityExploreReconstruct.java

[dp.naive] TmF
- GreedyReconstruct.java

[dp.hist] MCMC by edge switching
- DegreeSeqHist.java
- DegreeIntSet.java: improve DegreeSeqHist by using GraphIntSet.java

[dp.mixture] 
- MixtureModel.java: converted from C

[dp.combined]
- NodeSetDiv.java: private (copied from NodeSet.java and NodePriv.java), use log-likelihood
- DivisiveTmF.java: use NodeSetDiv.java

- Louvain.java: converted from Python 'community'
- LouvainInt.java: use EdgeInt, EdgeIntGraph

- LouvainDP.java: e-DP by input perturbation (equal-sized supernodes)

- LouvainModDiv.java: call NodeSetLouvain
- NodeSetLouvain.java:  MCMC at the first level, used in LouvainDP (k-ary tree)

- Scan.java: SCAN algorithm

- TmFPart.java: apply TmF to subgraphs, sub-bigraphs

- HRGCut.java: find the good cuts of HRG (high modularity)

- HRGDivisiveGreedy.java: combine HRG-Divisive and modularity
- NodeSetDivGreedy.java: use log-likelihood (binary tree)

- ModDivisiveDP.java: exponential mechanism with modularity Q
- NodeSetMod.java: used in ModDivisiveDP (binary tree)
- CutNode.java: used in NodeSetMod.bestCut()

- NodeDivisiveDP2.java: allow to fix nodes to groups (reduce MCMC space)
- NodeSetMod2.java

- NodeSetModOpt.java
- NodeSetModOpt2.java: do not use IntSet !

- LouvainOpt.java
- NodeSetLouvainOpt.java: k-ary tree, copy from NodeSetLouvain, used in LouvainOpt

- EdgeFlip: implement paper EDBTw'15

- CommunityMeasure: clustering metrics

- BatchGenerator: generate .cmd files


[dp.generator]
- AutoPart.java: AutoPart algorithm
- Orbis.java: equivalent to C++ orbis
- RMAT.java: R-MAT algorithm

- DendrogramSampler.java: read _tree files (MCMCInference, MCMCInferenceFixed, HRGDivisiveFit) and generate sample graphs	
 





