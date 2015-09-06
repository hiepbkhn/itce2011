
file name conventions:
----------------------

- .gr : generated graph file (er: Erdos-Renyi, ba: Babarasi-Albert, sm: Small world, pl: Power Law, ff: Forest Fire)

- _uncertain.gr: representative_graph.py
- _uncertain_exp_1.0.gr: representative_graph.py

- er_trust_...:	auto_sharing.py

- _dp2_x.x.out: dp_aggregate.py

- en_x.x.out: entropy_aggregate.py
- en_x.x_filtered.txt: MATLAB (filter_graph.m)


----------------------------------------------------------------------
_post		:	post-processing, rounding (deterministic)
----------------------

- _entropy_(sigma)_(c)_(q).out: entropy_obfuscation.py
- _entropy_post_(sigma)_(c)_(q).out: entropy_obfuscation.py

----------------------------------------------------------------------
_rep		: 	representative (deterministic)
_missing	:	not distribute missing edges
_mst		: 	spanning tree-based partition
_nb			: 	nearby new edges 
----------------------

- ...cvxopt_(newedges).out: uncertain_convex_opt.py
- ...cvxopt_rep_(newedges).out: uncertain_convex_opt.py				(representative, deterministic)
- ...cvxopt_(newedges)_nb.out: uncertain_convex_opt.py
- ...cvxopt_rep_(newedges)_nb.out: uncertain_convex_opt.py			(representative, deterministic)

- ...cvxopt_(newedges)_missing.out: uncertain_convex_opt.py			(not distribute missing edges, uncertain)			
- ..._mst_cvxopt_(newedges)_missing.out: uncertain_convex_opt.py	(spanning tree-based partition, not distribute missing edges, uncertain)

----------------------------------------------------------------------
_neg		: 	(weighting by node degrees) average + negative for new_edges
_inv		:	(weighting by node degrees) inverse of average
----------------------

----------------------------------------------------------------------
.gp.rep		: 	representative by GP (greedy probability)
.adr.rep	: 	representative by ADR
.abm.rep	: 	representative by ABM
----------------------

----------------------------------------------------------------------
_randwalk_(t)_(M)	: random walk (NDSS'13)
----------------------


