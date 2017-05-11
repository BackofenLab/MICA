# MICA - Multiple Interval-based Curve Alignment

MICA implements a heuristic landmark registration method in combination with a
progressive alignment scheme to generate multiple curve alignments and according
representative consensus data.

The input is a set of discrete time series of e.g. measured data. MICA assumes that
the time series are based on a common event such that start and end time are to be mapped
and a global alignment (of the whole time series) is to be computed. To this end,
MICA identifies prominent features of each curve (like minima, maxima, and inflection points)
that are considered as alignable landmarks. To reduce computational complexity and to
reduce noise, landmarks can be filtered. The filtered subset is than used in a greedy
local optimization scheme. Therein, for two curves a pair of landmarks identified that
(i) can be mapped (same type), (ii) their mapping (and according curve distortion) 
yields the best (local) score change possible for all such pairs (local optimal decision), 
and (iii) this score is lower than doing no mapping at all.
This local optimal landmark mapping is fixed and decomposes the two curves in two respective
sub-curves left and right of the mapped landmarks. For each such sub-problem the procedure
is repeated until an interval can not be decomposed any further. The mapping of landmarks,
which is a shift in according x/time-coordinates, is transfered the all other data points 
via linear interpolation.

To align multiple curves, a progressive scheme is applied that operates on groups of curves. 
Initially, each group consists of one of the input curves. A group is represented by a
derived consensus curves computed as the arithmetic mean of all enclosed curves. Iteratively,
the pair of groups with minimal score of their respective consensus curve alignments are
selected. The consensus curve alignment provides the information how the according groups' 
curves have to be warped in order to get fused into a new group (while the original groups
are discarded). This is repeated until only one group is left, which represents the alignment
of all curves. The according consensus curve yields thus the representative consensus curve
for the input.



