
#########################################################
# load java-interface-library
#########################################################
require(rJava)

#########################################################
#' central data structure that stores the MICA setup data
#' and avoid duplicated runs of initMica()
#' @export
micaSetup <<- list( wasInitialized=FALSE, micaJavaPath=NA );


#########################################################
#' Initializes the Java interface for the MICA R package
#' 
#' @param micaJavaPath the absolute path to the MICA jar files (string)
#' 
#' @export
initMica <- function( micaJavaPath ) {

	# ensure it is a string and thus maybe a path
	if (!is.character(micaJavaPath)) stop("given path is no string");

	# check path
	if (sum(file.access(micaJavaPath,4)) != 0 ) stop(paste("given micaJavaPath='",micaJavaPath,"' can not be accessed for reading",sep=""));

	# check if all-in-one mica.jar available
	allInOneJarAvailable = FALSE;
	if (sum(file.access(paste(micaJavaPath ,"\\mica.jar",sep=""),4)) == 0 )  {
		allInOneJarAvailable = TRUE;
	}
	# check if dependencies are available
	depJars = c("commons-lang3-3.4.jar","commons-math3-3.6.1.jar","java-hamcrest-2.0.0.0.jar");
	for( depJar in depJars ) {
		if (sum(file.access(paste(micaJavaPath,depJar,sep="\\"),4)) != 0 ) stop(paste("given micaJavaPath='",micaJavaPath,"' does not contain needed jar file '",depJar,"'",sep=""));
	}

	# initialize java interface
	rJava::.jinit()

	# store MICA Java path
	micaSetup$micaJavaPath <<- micaJavaPath;

	# extend classpath accordingly
	if (allInOneJarAvailable) {
		rJava::.jaddClassPath(paste(micaJavaPath ,"\\mica.jar",sep=""));
	} else {
		rJava::.jaddClassPath(micaJavaPath)
	}
	for( depJar in depJars ) {
		rJava::.jaddClassPath(paste(micaJavaPath,depJar,sep="\\"));
	}

	micaSetup$wasInitialized <<- TRUE;

}


#########################################################
#' Generates for each column of y-coordinates according
#' equidistant x-coordinates in the interval [0,1]
#' 
#' @param y the y-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3)
#' 
#' @return the equidistance x coordinates, each in interval [0,1]
#' 
#' @export
getEquiX <- function( y ) {

	if (is.matrix(y) || is.data.frame(y)) {

		# check y data
		for (c in 1:ncol(y)) {
			yC <- as.vector(na.omit(y[,c]));
			# ignore pure NA columns
			if (length(yC) == 0) next;
			# check column
			if (!is.double(yC)) stop(paste("column",c,":","y has to be double data"));
			if (length(yC)<3) stop(paste("column",c,":","min column length of y is 3"));
		}
	
		# create x values
		x <- y;
		for (c in 1:ncol(x)) {
			# get number of coordinates to generate for this column
			samples <- length(as.vector(na.omit(y[,c])));
			# skip empty columns
			if (samples==0) next;
			# generate coordinates and feed to according positions
			x[ !is.na(y[,c]), c ] <- (0:(samples-1))/(samples-1);
		}
	
		# return x-coordinates
		return(x);
	} else {
	if (is.vector(y)) {
		if (is.list(y)) stop("y is a list, not a vector, matrix or data.frame");
		# get number of coordinates to generate for this column
		yN <- as.vector(na.omit(y));
		samples <- length(yN);
		if (samples==0) stop("only NA given");
		if (!is.double(yN)) stop("y has to be double data");
		return( (0:(samples-1))/(samples-1) );
	} else {
		stop("y is neither a matrix/data.frame nor a vector");
	}
	}
	# will never happen
	return(NA);
}


#########################################################
#' Computes the relative coordinates in the interval [0,1]
#' for the given data by applying to each coordinate d[i]
#'    dNew[i] = (d[i]-min(d[i]))/(max(d[i])-min(d[i]))
#' NA-entries are omitted
#' 
#' @param d coordinate data to normalize (vector(double)||matrix(double)||data.frame(vector(double)))
#' 
#' @return the normalized data
#' 
#' @export
getRelCoord <- function( d ) {

	if (is.matrix(d) || is.data.frame(d)) {
		# copy data for overwrite
		dNew <- d;
		for (c in 1:ncol(d)) {
			dNoNa <- as.vector(na.omit(d[,c]));
			# ignore pure NA columns
			if (length(dNoNa) == 0) next;
			# apply checks
			if (!is.double(dNoNa )) stop(paste("column",c,":","has to be double data"));
			if (length(dNoNa)<1) stop(paste("column",c,":","contains no double data"));
			# get minimal value and coordinate range for normalization
			dMin <- min(dNoNa);
			dRange <- max(dNoNa)-dMin;
			# compute relative coordinates
			dNew[!is.na(d[,c]),c] <- (dNoNa-dMin)/dRange ;
		}
		return(dNew);
	} else {
	if (is.vector(d) && !is.list(d)) {
		dNoNa <- as.vector(na.omit(d));
		# ignore pure NA columns
		if (length(dNoNa) == 0) return(d);
		# apply checks
		if (!is.double(d)) stop("d has to be double data");
		if (length(dNoNa)<1) stop("d contains no double data");
		# get minimal value and coordinate range for normalization
		dMin <- min(dNoNa);
		dRange <- max(dNoNa)-dMin;
		# compute relative coordinates
		return ( (d-dMin)/dRange );
	} else {
		stop("d is neither a matrix nor a vector");
	}
	}
	# will never happen
	return(NA);
}




#########################################################
#' Normalizes the given input by applying to each coordinate d[i]
#'    dNew[i] = (d[i]-mean(d))/sd(d))
#' NA-entries are omitted
#' 
#' @param d coordinate data to normalize (vector(double)||matrix(double)||data.frame(vector(double)))
#' 
#' @return the normalized data
#' 
#' @export
getNormData <- function( d ) {
	
	if (is.matrix(d) || is.data.frame(d)) {
		# copy data for overwrite
		dNew <- d;
		for (c in 1:ncol(d)) {
			dNoNa <- as.vector(na.omit(d[,c]));
			# ignore pure NA columns
			if (length(dNoNa) == 0) next;
			# apply checks
			if (!is.double(dNoNa )) stop(paste("column",c,":","has to be double data"));
			if (length(dNoNa)<1) stop(paste("column",c,":","contains no double data"));
			# get minimal value and coordinate range for normalization
			dMean <- mean(dNoNa);
			dSd <- sd(dNoNa);
			# compute relative coordinates
			dNew[!is.na(d[,c]),c] <- (dNoNa-dMean)/dSd ;
		}
		return(dNew);
	} else {
		if (is.vector(d) && !is.list(d)) {
			dNoNa <- as.vector(na.omit(d));
			# ignore pure NA columns
			if (length(dNoNa) == 0) return(d);
			# apply checks
			if (!is.double(d)) stop("d has to be double data");
			if (length(dNoNa)<1) stop("d contains no double data");
			# get minimal value and coordinate range for normalization
			dMean <- mean(dNoNa);
			dSd <- sd(dNoNa);
			# compute relative coordinates
			return ( (d-dMean)/dSd );
		} else {
			stop("d is neither a matrix nor a vector");
		}
	}
	# will never happen
	return(NA);
}



#########################################################
#' Computes the mean curve for the given curves for a given
#' number of sample points.
#' 
#' NA-entries are omitted
#' 
#' @param x the x-coordinate data of the curves (matrix(double)||data.frame(vector(double)))
#' @param y the y-coordinate data of the curves (matrix(double)||data.frame(vector(double)))
#' @param samples the number of equidistant samples to be taken for the mean computation
#' 
#' @return list(x,y) the x- and y-coordinates of the consensus curve
#' 
#' @export
getMeanCurve <- function( x, y, samples ) {
	
	# get interpolated data
	tmp <- interpolateCurves( x=x,y=y,samples=samples );
	
	# get row means of the interpolated curves' data
	return( list( x=rowMeans(tmp$x), y=rowMeans(tmp$y) ) );
}


#########################################################
#' Computes the linearly interpolated values of the given curve.
#' NOTE: you have to call initMica() before calling this function
#' 
#' @param x the x-values of the curve's points (vector(double), length >= 3)
#' @param y the y-values of the curve's points (vector(double), length >= 3)
#' @param samples the number of equidistant points to be interpolated (integer > 3)
#' 
#' @return list(x,y) = the sampled x and y coordinates
#' 
#' @export
interpolateCurve <- function( x, y, samples ) {

	if ( ! micaSetup$wasInitialized ) stop("MICA was not initialized, run 'initMica' first");

	# check samples
	if( !(is.double(samples)||is.integer(samples)) || (as.integer(samples)!=samples) ) stop("samples has to be an integer");
	if( samples < 3) stop("samples has to be >= 3");

	# check x data
	if (!is.vector(x)) stop("x is no vector");
	if (is.list(x)) stop("x is no vector but a list");
	if (!is.double(x)) stop("x has to be double data");
	if (length(x)!=length(y)) stop("lengths of x and y differ");
	if (is.unsorted(x)) stop("x data is not sorted ascendingly");

	# check y data
	if (!is.vector(y)) stop("y is no vector");
	if (is.list(y)) stop("y is no vector but a list");
	if (!is.double(y)) stop("y has to be double data");
	if (length(y)<3) stop("min length of y is 3");

	# create x data to be interpolated
	xNew <- x[1] + ((x[length(x)-x[1]])*((0:(samples-1))/(samples-1)));

	# create curve object
	jCurve <- rJava::.jnew("de/uni_freiburg/bioinf/mica/algorithm/Curve","newCurve", x, y );

	# get interpolated y-data
	yNew <- xNew;
	for (i in 1:length(yNew)) {
		yNew[i] <- rJava::.jcall( jCurve, "D", "getY", xNew[i]);
	}

	return(list(x=xNew,y=yNew));
}


#########################################################
#' Computes the linearly interpolated values of the given curves.
#' NOTE: you have to call initMica() before calling this function
#' 
#' @param x the x-values of the curves' points (data.frame(vector(double)) or matrix, nrow >= 3)
#' @param y the y-values of the curves' points (data.fram(vector(double)) or matrix, nrow >= 3)
#' @param samples the number of equidistant points to be interpolated (integer > 3)
#' 
#' @return list(x,y) = the sampled x and y coordinates (each a matrix of dim(samples,ncol(x)))
#' 
#' @export
interpolateCurves <- function( x, y, samples ) {

	if ( ! micaSetup$wasInitialized ) stop("MICA was not initialized, run 'initMica' first");

	if (!is.data.frame(x) && !is.matrix(x)) stop("x is no data.frame or matrix");
	if (!is.data.frame(y) && !is.matrix(y)) stop("y is no data.frame or matrix");
	if (ncol(x)!=ncol(y)) stop("x and y differ in column number");
	for (c in 1:ncol(x)) {
		if (length(as.vector(na.omit(x[,c]))) != length(as.vector(na.omit(y[,c])))) stop(paste("column",c,"differs in length for x and y (na.omitted)"));
	}
	
	xNew <- matrix(NA, ncol=ncol(x), nrow=samples);
	yNew <- matrix(NA, ncol=ncol(x), nrow=samples);

	for (c in 1:ncol(x)) {
	  xC <- as.vector(na.omit(x[,c]));
	  # ignore pure NA columns
	  if (length(xC) == 0) next;
	  yC <- as.vector(na.omit(y[,c]));
	  sampled <- interpolateCurve( xC, yC, samples );
		xNew[,c] <- sampled$x;
		yNew[,c] <- sampled$y;
	}

	return( list(x=xNew,y=yNew) )
}


#########################################################
#' Utility function for data pre-processing: You can 
#' (a) interpolate your data to a given number of dataPoints and/or
#' (b) apply a smoothing loess function with the given span value.
#' 
#' Note, (b) if the number of data points is below a given threshold, 
#' the data is interpolated before loess is applied (loessMinPoints). 
#' Subsequently, the loess coordinates of the original number of data
#' points are reported. This procedure might be needed if only a low
#' number of points is available (<20), which might cause the loess 
#' function to fail.
#' 
#' @param y the data to smooth (matrix(double)||data.frame(vector(double)))
#' @param dataPoints number of data points to interpolate per curve; if <=0 the original data point number is preserved.
#' @param loessSpan span for the loess smoothing (double); if <= 0 no loess smoothing is applied
#' @param loessMinPoints minimal number of data points for loess smoothing (integer); if <= 0 no data point interpolation is applied
#' 
#' @return the smoothed data
#' 
#' @export
smoothData <- function( y, dataPoints=0, loessSpan=0, loessMinPoints=0) {

	# check dataPoints
	if( !(is.double(dataPoints)||is.integer(dataPoints)) || (as.integer(dataPoints)!=dataPoints) ) stop("dataPoints has to be an integer");

	# check loessSpan
	if( !(is.double(loessSpan)) ) stop("loessSpan has to be a double");

	# check y
	if (!is.data.frame(y) && !is.matrix(y)) stop("y is no data.frame or matrix");
	for (c in 1:ncol(y)) {
		yC <- as.vector(na.omit(y[,c]));
		# ignore pure NA columns
		if (length(yC) == 0) next;
		# check column
		if (!is.double(yC)) stop(paste("column",c,":","y has to be double data"));
	}

	# copy data
	yNew <- y;

	# check if number of data points to be normalized
	if (dataPoints > 0) {
	  yNew <- interpolateCurves(x=getEquiX(y), y=y, samples=dataPoints)$y;
	}

	# check if loess is to be applied
	if (loessSpan > 0) {
		# apply per column
		for( c in 1:ncol(yNew) ) {
			colNoNa <- as.vector(na.omit(yNew[,c]));
			# ignore empty col
			if (length(colNoNa) == 0) next;
			# interpolate if needed
			if (loessMinPoints > 0 && length(colNoNa) < loessMinPoints) {
				colNoNa <- interpolateCurve(x=getEquiX(colNoNa), y=colNoNa, samples=loessMinPoints)$y;
			}
			# simulate x values
			tmp_X <- 1:length(colNoNa);
			# get loess prediction of data
			tmp <- tryCatch( {predict( loess(colNoNa~tmp_X, span=loessSpan), data.frame(tmp_X=tmp_X))}, error = function(e){NULL}, warning = function(e){NULL});
			# check if loess prediction failed
			if (is.null(tmp)) {
				# fall back to original data and skip further processing
				next;
			}
			# keep first and last point of original data
			tmp[1] <- colNoNa[1];
			tmp[length(tmp)] <- colNoNa[length(colNoNa)];
			# get indices in original data to be replaced
			toSet <- !is.na(yNew[,c]);
			# reduce to original number of points if needed
			if (loessMinPoints > 0 && sum(toSet) < loessMinPoints) {
				tmp <- interpolateCurve(x=getEquiX(tmp), y=tmp, samples=sum(toSet))$y;
			}
			# replace data with loess interpolation
			yNew[toSet,c] <- tmp;
		}
	}
	colnames(yNew) <- colnames(y);
	return(yNew);
}



#########################################################
#' Computes the curve annotations that would be used for alignment.
#' 
#' The types of annotation and their according type value are
#' 
#'  0 = normal point
#' -1 = slope minimum
#' -2 = slope maximum
#' -3 = inflection point in ascent (slope maximum with pos. slope value)
#' -4 = inflection point in descent (slope minimum with neg. slope value)
#' -5 = curve minimum
#' -6 = curve maximum
#' -7 = beginning of curve
#' -8 = end of curve
#' 
#' @param y the y-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3)
#' @param x the x-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3)
#' @param minRelMinMaxDist minimal distance between identified neighbored minima and maxima to be kept for alignment
#'        This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
#' @param minRelSlopeHeight the minimal relative slope value of an inflection point to be kept by the filtering.
#'        This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
#'        NOTE: slope values do change during alignment, such that the filtering shows dynamic effects during alignment.
#' 
#' @return annotationType=data.frame(vector(double)) 
#'         i.e. the annotation type for each coordinate of the curve
#' 
#' @export
getAnnotations <- function( y, x=getEquiX(y), minRelMinMaxDist=0.01, minRelSlopeHeight=0.01 ) {
	
	# ensure mica was loaded
	if ( ! micaSetup$wasInitialized ) stop("MICA was not initialized, run 'initMica' first");
	
	# check minRelMinMaxDist
	if( !(is.double(minRelMinMaxDist)) ) stop("minRelMinMaxDist has to be a double value");
	if( minRelMinMaxDist<0 || minRelMinMaxDist>1 ) stop("minRelMinMaxDist has to be in the interval [0,1]");
	
	# check minRelSlopeHeight
	if( !(is.double(minRelSlopeHeight)) ) stop("minRelSlopeHeight has to be a double value");
	if( minRelSlopeHeight<0 || minRelSlopeHeight>1 ) stop("minRelSlopeHeight has to be in the interval [0,1]");
	
	# check x and y data
	if (!is.data.frame(x) && !is.matrix(x)) stop("x is no data.frame or matrix");
	if (!is.data.frame(y) && !is.matrix(y)) stop("y is no data.frame or matrix");
	if (ncol(x)!=ncol(y)) stop("x and y differ in column number");
	curCurveNumber <- 0;
	for (c in 1:ncol(x)) {
		#if (!is.vector(x[,c])) stop(paste("column",c,":","x is no vector"));
		#if (is.list(x[,c])) stop(paste("column",c,":","x is no vector but a list"));
		#if (!is.vector(y[,c])) stop(paste("column",c,":","y is no vector"));
		# if (is.list(y[,c])) stop(paste("column",c,":","y is no vector but a list"));
		xC <- as.vector(na.omit(x[,c]));
		# ignore pure NA columns
		if (length(xC) == 0) {
			# skip this curve for testing
			next;
		}
		# check column
		yC <- as.vector(na.omit(y[,c]));
		if (length(yC)<3) stop(paste("column",c,":","min column length of y is 3"));
		if (length(xC) != length(yC)) stop(paste("column",c,":","length for x and y differ (na.omitted)"));
		if (!is.double(xC)) stop(paste("column",c,":","x has to be double data"));
		if (!is.double(yC)) stop(paste("column",c,":","y has to be double data"));
		if (is.unsorted(xC)) stop(paste("column",c,":","x data is not sorted ascendingly"));
		curCurveNumber <- curCurveNumber+1;
	}
	if (curCurveNumber == 0) stop("all columns are NA only");
	
	# instantiate MICA's R controller
	micaR <- rJava::.jnew("de/uni_freiburg/bioinf/mica/controller/MicaR"
					, as.integer(0)
					, as.integer(10)
					, as.double(1)
					, as.double(0)
					, as.double(0)
					, as.double(minRelMinMaxDist)
					, as.double(0)
					, as.double(minRelSlopeHeight)
				); 
	# fill annotations
	a <- x;
	for (c in 1:ncol(x)) {
		# get NA free data
		xC <- as.vector(na.omit(x[,c]));
		# ignore pure NA columns
		if (length(xC) == 0) next;
		yC <- as.vector(na.omit(y[,c]));
		
		# get annotations
		aC <- rJava::.jcall(micaR,"[I","getAnnotations", as.double(xC), as.double(yC) );
		
		# copy annotations back to final array
		a[!is.na(x[,c]),c] <- aC;
		
	}

	# check for debug output within MICA
	debugOutput <- rJava::.jcall(micaR,"S","getDebugOutput" );
	if (nchar(debugOutput) > 0) {
		write( debugOutput, stderr());
	}
	
	# return final annotations
	return(a);
}



#########################################################
#' Computes a MICA  alignment for the given curves.
#' 
#' @param y the y-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3)
#' @param x the x-values of the curves' points (data.frame(vector(double)) or matrix(double), nrow >= 3)
#' @param distFunc selects the distance function (0 = curve RMSD, 1 = slope RMSD, 2 = curve mean absolute distance, 3 = slope mean absolute distance) (integer)
#' @param distSample number of equidistant samples to be used for the distance calculation (integer > 0)
#' @param distWarpScaling if >0, the distance is multiplied with the warping factor and the given distWarpScaling value in order to compute the final distance. 
#'            That is, if >1 the warping is more penalized than for values <1.
#' @param maxWarpingFactor maximally allowed length distortion of intervals per alignment (double >= 1)
#' @param maxRelXShift maximally allowed relative shift of x-coordinates within the curves per alignment (double [0,1])
#' @param minRelIntervalLength the minimal relative interval length to be considered for further decomposition (double in [0,1])
#' @param minRelMinMaxDist minimal distance between identified neighbored minima and maxima to be kept for alignment (double in [0,1])
#'        This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
#' @param minRelSlopeHeight the minimal relative slope value of an inflection point to be kept by the filtering.
#'        This basically implements a kind of noise filtering: 0 no filtering, 1 almost everything filtered.
#'        NOTE: slope values do change during alignment, such that the filtering shows dynamic effects during alignment.
#' @param reference index of the reference curve (column) within the x/y data; 0 if no reference-based alignment is to be done
#' @param outSlope whether or not to add the slope (original and warped) to the return list
#' 
#' @return list( xWarped=data.frame(vector(double)), consensus=list(x,y), pairDist=list(orig=matrix(double),warped=matrix(double)), guideTree=character, slope=list(orig,warped) ) 
#'         i.e. the warped x coordinates for each curve, the consensus curve, 
#'              the matrices of pairwise distances between all curves before and after alignment,
#'              a NEWICK string representation of the alignment guide tree,
#' 				if (outSlope==TRUE) the list of slopes before and after warping otherwise NA
#' 
#' @export
alignCurves <- function( 
			y, 
			x=getEquiX(y), 
			distFunc=3, 
			distSample=100, 
			distWarpScaling=0, 
			maxWarpingFactor=2, 
			maxRelXShift=0.2, 
			minRelIntervalLength=0.05, 
			minRelMinMaxDist=0.01, 
			minRelSlopeHeight=0.01, 
			reference=0,
			outSlope=FALSE
	) 
{

	# ensure mica was loaded
	if ( ! micaSetup$wasInitialized ) stop("MICA was not initialized, run 'initMica' first");

	# check distFunc
	if( !(is.double(distFunc)||is.integer(distFunc)) || (as.integer(distFunc)!=distFunc) ) stop("distFunc has to be an integer");
	if( distFunc < 0 || distFunc > 3 ) stop("distFunc has to be one of {0..3}");

	# check distSample
	if( !(is.double(distSample)||is.integer(distSample)) || (as.integer(distSample)!=distSample) ) stop("distSample has to be an integer");
	if( distSample <= 0 ) stop("distSample has to be > 0");

	# check distWarpScaling 
	if( !(is.double(distWarpScaling)) ) stop("distWarpScaling has to be a double value");
	if( distWarpScaling < 0 ) stop("distWarpScaling has to be >= 0");

	# check maxWarpingFactor 
	if( !(is.double(maxWarpingFactor)) ) stop("maxWarpingFactor has to be a double value");
	if( maxWarpingFactor < 1 ) stop("maxWarpingFactor has to be >= 1");

	# check maxRelXShift 
	if( !(is.double(maxRelXShift)) ) stop("maxRelXShift has to be a double value");
	if( maxRelXShift < 0 || maxRelXShift > 1 ) stop("maxRelXShift has to be in [0,1]");

	# check minRelIntervalLength 
	if( !(is.double(minRelIntervalLength)) ) stop("minRelIntervalLength has to be a double value");
	if( minRelIntervalLength < 0 || minRelIntervalLength > 1 ) stop("minRelIntervalLength has to be in [0,1]");

	# check minRelMinMaxDist
	if( !(is.double(minRelMinMaxDist)) ) stop("minRelMinMaxDist has to be a double value");
	if( minRelMinMaxDist<0 || minRelMinMaxDist>1 ) stop("minRelMinMaxDist has to be in the interval [0,1]");
	
	# check minRelSlopeHeight
	if( !(is.double(minRelSlopeHeight)) ) stop("minRelSlopeHeight has to be a double value");
	if( minRelSlopeHeight<0 || minRelSlopeHeight>1 ) stop("minRelSlopeHeight has to be in the interval [0,1]");

	# check reference
	if( !((is.double(reference))||is.integer(reference)) || (as.integer(reference)!=reference) ) stop("reference has to be an integer");
	if( reference < 0 || reference > ncol(y) ) stop("reference has to be either 0 or within 1:ncol(y)");

	# check x and y data
	if (!is.data.frame(x) && !is.matrix(x)) stop("x is no data.frame or matrix");
	if (!is.data.frame(y) && !is.matrix(y)) stop("y is no data.frame or matrix");
	if (ncol(x)!=ncol(y)) stop("x and y differ in column number");
	curveNumber <- 1:ncol(x);
	curCurveNumber <- 0;
	for (c in 1:ncol(x)) {
		#if (!is.vector(x[,c])) stop(paste("column",c,":","x is no vector"));
		#if (is.list(x[,c])) stop(paste("column",c,":","x is no vector but a list"));
		#if (!is.vector(y[,c])) stop(paste("column",c,":","y is no vector"));
		# if (is.list(y[,c])) stop(paste("column",c,":","y is no vector but a list"));
		xC <- as.vector(na.omit(x[,c]));
		# ignore pure NA columns
		if (length(xC) == 0) {
			# store that this curve is omitted
			curveNumber[c] <- NA;
			# skip this curve for testing
			next;
		}
		# check column
		yC <- as.vector(na.omit(y[,c]));
		if (length(yC)<3) stop(paste("column",c,":","min column length of y is 3"));
		if (length(xC) != length(yC)) stop(paste("column",c,":","length for x and y differ (na.omitted)"));
		if (!is.double(xC)) stop(paste("column",c,":","x has to be double data"));
		if (!is.double(yC)) stop(paste("column",c,":","y has to be double data"));
		if (is.unsorted(xC)) stop(paste("column",c,":","x data is not sorted ascendingly"));
		# store curve number
		curveNumber[c] <- curCurveNumber;
		curCurveNumber <- curCurveNumber+1;
	}
	if (curCurveNumber == 0) stop("all columns are NA only");
	
	# instantiate MICA's R controller
	micaR <- rJava::.jnew("de/uni_freiburg/bioinf/mica/controller/MicaR"
					, as.integer(distFunc)
					, as.integer(distSample)
					, as.double(maxWarpingFactor)
					, as.double(maxRelXShift)
					, as.double(minRelIntervalLength)
					, as.double(minRelMinMaxDist)
					, as.double(distWarpScaling)
					, as.double(minRelSlopeHeight)
			); 

	# add curves
	for (i in 1:ncol(x)) {
		if (is.na(curveNumber[i])) next;
		# add only non-NA curves
		if (i == reference) {
			# add reference curve
			rJava::.jcall(micaR, method="addReferenceCurve", as.double(as.vector(na.omit(x[,i]))), as.double(as.vector(na.omit(y[,i]))), returnSig="V");
		} else {
			# add normal curve
			rJava::.jcall(micaR, method="addCurve", as.double(as.vector(na.omit(x[,i]))), as.double(as.vector(na.omit(y[,i]))), returnSig="V");
		}
	}


	# run alignment
	rJava::.jcall(micaR, method="align", returnSig="V");

	# get warped x data
	xWarped <- x;
	for (i in 1:ncol(x)) {
		if (is.na(curveNumber[i])) next;
		# store only non-NA curves
		xWarped[!is.na(x[,i]),i] <- rJava::.jcall(micaR,"[D","getAlignedX", as.integer(curveNumber[i]) );
	}
	
	# get distance

	# get slope data original and warped if requested
	slopeOrig <- NA;
	slopeWarp <- NA;
	if (outSlope) {
		slopeOrig <- x;
		slopeWarp <- x;
		for (i in 1:ncol(x)) {
			if (is.na(curveNumber[i])) next;
			# store only non-NA curves
			xWarped[!is.na(x[,i]),i] <- rJava::.jcall(micaR,"[D","getAlignedX", as.integer(curveNumber[i]) );
			slopeOrig[!is.na(x[,i]),i] <- rJava::.jcall(micaR,"[D","getCurveSlope", as.integer(curveNumber[i]) );
			slopeWarp[!is.na(x[,i]),i] <- rJava::.jcall(micaR,"[D","getAlignedSlope", as.integer(curveNumber[i]) );
		}
	}
	
	# get consensus of aligned data
	xC <- .jcall(micaR,"[D","getConsensusX" );
	yC <- .jcall(micaR,"[D","getConsensusY" );

	
	pairDistOrig <- NA;
	pairDistWarped <- NA;
	
	# get pairwise curve distances for no-NA-curves before alignment
	jPairDistOrig <- .jcall(micaR,"[[D","getOriginalPairwiseDistances", evalArray=FALSE );
	# compile rJava object into R-array
	noNaPairDistOrig <- do.call(rbind, lapply(jPairDistOrig, .jevalArray));
	# get final pairwise distances for all curves
	pairDistOrig <- matrix(data=NA, ncol=ncol(y), nrow=ncol(y), dimnames=list(colnames(y),colnames(y)));
	for (i in 1:(ncol(pairDistOrig)-1)) {
		if (is.na(curveNumber[i])) next;
		for (j in (i+1):ncol(pairDistOrig)) {
			if (is.na(curveNumber[j])) next;
			# copy data from no-NA-curves-data
			pairDistOrig[i,j] <- noNaPairDistOrig[1+curveNumber[i],1+curveNumber[j]];
		}
	}

	# get pairwise curve distances for no-NA-curves after alignment
	jPairDistWarped <- .jcall(micaR,"[[D","getAlignedPairwiseDistances", evalArray=FALSE );
	# compile rJava object into R-array
	noNaPairDistWarped <- do.call(rbind, lapply(jPairDistWarped, .jevalArray));
	# get final pairwise distances for all curves
	pairDistWarped <- matrix(data=NA, ncol=ncol(y), nrow=ncol(y), dimnames=list(colnames(y),colnames(y)));
	for (i in 1:(ncol(pairDistWarped)-1)) {
		if (is.na(curveNumber[i])) next;
		for (j in (i+1):ncol(pairDistWarped)) {
			if (is.na(curveNumber[j])) next;
			# copy data from no-NA-curves-data
			pairDistWarped[i,j] <- noNaPairDistWarped[1+curveNumber[i],1+curveNumber[j]];
		}
	}

	guideTree <- .jcall(micaR,"S","getGuideTree" );

	# update guide tree with column headers
	treeNames <- colnames(y)[!is.na(curveNumber)];
	for (i in length(treeNames ):1) {
		guideTree <- gsub(paste("(\\D)",as.character(i-1),"(\\D)",sep=""),paste("\\1",treeNames[i],"\\2",sep=""), guideTree);
	}
	
	# check for debug output within MICA
	debugOutput <- rJava::.jcall(micaR,"S","getDebugOutput" );
	if (nchar(debugOutput) > 0) {
		write( debugOutput, stderr());
	}
	
	# final data
	return( list( xWarped=xWarped, consensus=list(x=xC,y=yC), pairDist=list(orig=pairDistOrig,warped=pairDistWarped), guideTree=guideTree, slope=list(orig=slopeOrig, warped=slopeWarp) ) );
}



#########################################################
# try to initialize with script directory
#########################################################
initMica(normalizePath(dirname(sys.frame(1)$ofile)));

