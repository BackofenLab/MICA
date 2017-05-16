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


## Dependencies

- MICA tool :
  - Java-8
- MICA R-interface :
  - Java-8
  - `R` (tested with version 3.3.0)
  - [`rJava`](https://www.rforge.net/rJava/) - R package to interface Java runtime (tested with version 0.9-8)
  
Required non-standard Java libraries are either included within the JAR file or part of the provided packages.



## R-interface

To use MICA from within R, the following steps are necessary:
- install on your machine JRE or JDK version 8 or higher (Java SE Development Kit) e.g. from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- install `rJava` (e.g. using `install.packages("rJava"` within `R`)
- note: `rJava-0.9-8` does not identify the installed Java runtime correctly/automatically on Windows! Here the following additional steps are needed:
   - add the path to file `jvm.dll` to your `PATH` environment variable
     - edit the `PATH` system variable (see [help](https://www.java.com/en/download/help/path.xml))
     - for JRE : add e.g. `;C:\Program Files (x86)\Java\jre1.8.0_91\bin\server` at the end of the value
     - for JDK : add e.g. `;C:\Program Files\Java\jdk1.8.0_92\jre\bin\server` at the end of the value
     - note: use `;` as a path separator and adapt the pathes from above! :-)
- download and extract the MICA R-package from the [release page](https://github.com/BackofenLab/MICA/releases)
- load the MICA R functions using `source("PATH_TO_MICA_R_PACKAGE/mica-functions.R");` from within `R`


