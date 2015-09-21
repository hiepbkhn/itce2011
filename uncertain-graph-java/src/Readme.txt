
[algs4] http://algs4.cs.princeton.edu/
- EdgeWeightedGraph.java: used in Louvain

[dp]
- DPUtil.java: Laplace, geometric mechanisms
- SampleGenerator.java: 
- UtilityMeasure.java --> see uncertain-graph/dp/utility_measure.py

[dp.mcmc] private HRG
- Dendrogram.java
- MCMCInference.java: use Dendrogram.java 

[dp.hrg] non-private HRG
- HRG.java (subclass of Dendrogram)
- MCMCFit.java: use HRG.java

[dp.comm] HRG-Divisive
- CommunityFit.java: non-private, use NodeSet.java
- CommunityPriv.java: private, use NodePriv.java (extends NodeSet.java, non-use)
- CommunityFit2.java: non-private, use NodeSet2.java

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
- AutoPart.java: AutoPart algorithm

- NodeSetDiv.java: private (copied from NodeSet.java and NodePriv.java)
- DivisiveTmF.java: use NodeSetDiv.java

- Louvain.java: converted from Python 'community'
- LouvainDP.java: e-DP by input perturbation

- Scan.java: SCAN algorithm

- TmFPart.java: apply TmF to subgraphs, sub-bigraphs

- HRGCut.java: find the good cuts of HRG (high modularity)

- HRGDivisiveGreedy.java: combine HRG-Divisive and modularity
- NodeSetDivGreedy.java

- NodeDivisiveDP.java: exponential mechanism with modularity Q
- NodeSetMod.java

- NodeDivisiveDP2.java: allow to fix nodes to groups (reduce MCMC space)
- NodeSetMod2.java
