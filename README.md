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


<br /><br /><br /><br />
## R-interface

To use MICA from within R, the following steps are necessary:

- install on your machine JRE or JDK version 8 or higher (Java SE Development Kit) e.g. from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- install [`rJava`](https://www.rforge.net/rJava/) (e.g. using `install.packages("rJava");` within `R`)
- note: `rJava-0.9-8` does not identify the installed Java runtime correctly/automatically on Windows! Here the following additional steps are needed:
   - add the path to file `jvm.dll` to your `PATH` environment variable
     - edit the `PATH` system variable (see [help](https://www.java.com/en/download/help/path.xml))
     - for JRE : add e.g. `C:\Program Files (x86)\Java\jre1.8.0_91\bin\server` at the end of the value
     - for JDK : add e.g. `C:\Program Files\Java\jdk1.8.0_92\jre\bin\server` at the end of the value
     - note: use `;` as a path separator and adapt the pathes from above! :-)
- download and extract the MICA R-package from the [release page](https://github.com/BackofenLab/MICA/releases)
- load the MICA R functions using `source("PATH_TO_MICA_R_PACKAGE/mica-functions.R");` from within `R`

### Quick Start

```[R]
# fill matrix/dataframe of (equidistant) data to be aligned (columnwise)
curves <- read.csv(...);

# include the MICA R interface utility function script
source("PATH_TO_MICA_R_PACKAGE/mica-functions.R")

# align curves using MICA
alignment <- alignCurves( y=curves );

# plot aligned data
matplot( x=alignment$x, y=curves, type="l" );

```

As you can see from the example from above, in order to use MICA within `R` you only have to source the function
definitions followed by a call of the alignment function `alignCurves()`. In the following, the provided functions 
and their parameters detailed.

### Provided functions

- [`alignCurves(..)`](#alignCurves) : computes a curve alignment
- [`getAnnotations(..)`](#getAnnotations) : get the curve annotations used for alignment


- [`getEquiX(..)`](#getEquiX) : equidistant x-coordinates generation in interval [0,1]
- [`getRelCoord(..)`](#getRelCoord) : transforms coordinates into relative coordinates in the interval [0,1]
- [`interpolateCurve(..)`](#interpolateCurve) : computes equidistant curve coordinates using linear interpolation
- [`interpolateCurves(..)`](#interpolateCurves) : computes equidistant curve coordinates for a set of curves
- [`getMeanCurve(..)`](#getMeanCurve) : computes the mean curve for a given number of equidistant x coordinates


- [`initMica(..)`](#initMica) : initializes the MICA R interface


<a name="alignCurves" />

#### `alignCurves(..)`

`alignCurves()` computes a multiple curve alignment using MICA for a given set of curves. It automatically identifies
landmarks within the curves that can be aligned, filters them according to the user defined settings and performs a
progressive alignment to join all curves in a global alignment. 

As a result, it returns the warped x coordinates of the input curves as well as a representative consensus curve derived
by the mean values at all warped x coordinates. Further auxiliary information is provided too.

*Input parameters:*

- `y` : the y-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3); NA entries are omitted
- `x` : (optional) the x-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3); NA entries are omitted.
If not provided, equidistant x coordinates are computed using `getEquiX(..)` 
- `distFunc` : (def=3) selects the distance function (0 = curve RMSD, 1 = slope RMSD, 2 = curve mean absolute distance, 3 = slope mean absolute distance) (integer)
- `distSample` : (def=100) number of equidistant samples to be used for the distance calculation (integer > 0).
Note, this has to be set according to the length of the curves.
- `distWarpScaling` : (def=0) if >0, the distance is multiplied with the warping factor and the given distWarpScaling value in order to compute the final distance. 
That is, if >1 the warping is more penalized than for values <1.
- `maxWarpingFactor` : (def=2) maximally allowed length distortion of intervals per alignment (double >= 1)
- `maxRelXShift` : (def=0.2) maximally allowed relative shift of x-coordinates within the curves per alignment (double [0,1])
- `minRelIntervalLength` : (def=0.05) the minimal relative interval length to be considered for further decomposition (double in [0,1])
- `minRelMinMaxDist` : (def=0.01) minimal distance between identified neighbored minima and maxima to be kept for alignment (double in [0,1])
This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
- `minRelSlopeHeight` : (def=0.01) the minimal relative slope value of an inflection point to be kept by the filtering.
This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
NOTE: slope values do change during alignment, such that the filtering shows dynamic effects during alignment.
- `reference` : (def=0) index of the reference curve (column) within the x/y data; 0 if no reference-based alignment is to be done
- `outSlope` : (def=FALSE) whether or not to add the computed slope values for the curves (original and warped) to the returned list

*Output* is a `list` containing:

- `xWarped` = data.frame(vector(double)) : the warped x coordinates for each curve
- `consensus` = list(x,y) : coordinates of the representative consensus curve
- `pairDist` = list(orig=matrix(double),warped=matrix(double)) : matrices of pairwise distances between all curves before and after alignment
- `guideTree` = character : NEWICK string representation of the alignment guide tree (order of fusions)
- `slope` = list(orig,warped) : if input paramter `outSlope==TRUE`, the list of slopes before and after warping; otherwise NA




<a name="getAnnotations" />

#### `getAnnotations(..)`

`getAnnotations(..)` computes the curve annotations that would be used for alignment.

The types of annotation and their according type value are:

-  0 = normal point
- -1 = slope minimum
- -2 = slope maximum
- -3 = inflection point in ascent (slope maximum with pos. slope value)
- -4 = inflection point in descent (slope minimum with neg. slope value)
- -5 = curve minimum
- -6 = curve maximum
- -7 = beginning of curve
- -8 = end of curve


*Input parameters:*

- `y` : the y-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3); NA entries are omitted
- `x` : (optional) the x-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3); NA entries are omitted.
If not provided, equidistant x coordinates are computed using `getEquiX(..)` 
- `minRelMinMaxDist` : (def=0.01) minimal distance between identified neighbored minima and maxima to be kept for alignment (double in [0,1])
This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
- `minRelSlopeHeight` : (def=0.01) the minimal relative slope value of an inflection point to be kept by the filtering.
This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
NOTE: slope values do change during alignment, such that the filtering shows dynamic effects during alignment.

*Output:*

- the annotations for each coordinate (see above) in the format of `x`




<a name="getEquiX" />

#### `getEquiX(..)`

`getEquiX(..)` generates for a given set of curves for their respective y coordinates equidistant x coordinates in the
range [0,1]. That is, for each curve (column in input) the number of non-NA y coordinates is identified and the x coordinate 
of the i-th y coordinate is given by (i-1)/(overallNumber-1). An according matrix (same layout as input) of the generated 
x coordinates is returned.

*Input parameters:* 

- `y` : the y-values of the curves' coordinates (data.frame(vector(double)) or matrix(double), nrow >= 3)

*Output:*

- the equidistance x coordinates, each in interval [0,1], in the same format as `y`


<a name="getRelCoord" />

#### `getRelCoord(..)`

`getRelCoord(..)` computes the relative coordinates in the interval [0,1]
for the given data by applying to each coordinate d[i]
```[R]
dNew[i] = (d[i]-min(d[i]))/(max(d[i])-min(d[i]))
```
The function can be applied to single data vectors or multiple curve data at once. NA entries are omitted.

*Input parameters:*

- `d` : coordinate data to normalize (vector(double)||matrix(double)||data.frame(vector(double)))

*Output:*

- the normalized data in the same format as `d`



<a name="interpolateCurve" />

#### `interpolateCurve(..)`

`interpolateCurve(..)` computes a linear interpolation of a curve for a given number of equidistant
x coordinates.

*Input parameters:* 

- `x` : the x-values of the curve's points (vector(double), length >= 3)
- `y` : the y-values of the curve's points (vector(double), length >= 3)
- `samples` : the number of equidistant points to be interpolated (integer > 3)

*Output:*

- `list(x,y)` : the sampled x and y coordinates of the input curve



<a name="interpolateCurves" />

#### `interpolateCurves(..)`

`interpolateCurves(..)` computes the linearly interpolated values of the given curves by calling
`interpolateCurve()` for each column of the input.

*Input parameters:*

- `x` : the x-values of the curves' points (data.frame(vector(double)) or matrix, nrow >= 3)
- `y` : the y-values of the curves' points (data.fram(vector(double)) or matrix, nrow >= 3)
- `samples` : the number of equidistant points to be interpolated (integer > 3)

*Output:*

- `list(x,y)` : the sampled x and y coordinates (each a matrix of dim(samples,ncol(x)))



<a name="getMeanCurve" />

#### `getMeanCurve(..)`

`getMeanCurve(..)` computes the mean curve for the given curves for a given
number of equidistant x coordinates. To this end, each curve if interpolated using `interpolateCurves()`
and than the mean per row (x coordinate) is computed.

*Input parameters:*

- `x` : the x-coordinate data of the curves (matrix(double)||data.frame(vector(double)))
- `y` : the y-coordinate data of the curves (matrix(double)||data.frame(vector(double)))
- `samples` : the number of equidistant samples to be taken for the mean computation

*Output:*

- `list(x,y)` : the x- and y-coordinates of the consensus curve




<a name="initMica" />

#### `initMica(..)`

`initMica(..)` initializes the MICA `R` interface by initializing `rJava` and setting the needed class path directives
to properly use MICA's Java implementation from within `R`. This function is typically automatically loaded, when the 
file `mica-functions.R` is loaded using `source("PATH_TO_MICA_R_PACKAGE/mica-functions.R")`. If this fails or the
MICA jar files are not stored in the same folder (here `PATH_TO_MICA_R_PACKAGE`), you can explicitly run this function
with the according MICA jar file path to enable the correct `rJava` initialization.

*Input parameters:* 

- `micaJavaPath` : the absolute path to the MICA jar files (string)



