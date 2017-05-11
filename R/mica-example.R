
#########################################################
#1# install on your machine JRE or JDK version 8 or higher (Java SE Development Kit)
### http://www.oracle.com/technetwork/java/javase/downloads/index.html

#########################################################
#2# add the path to file jvm.dll to your PATH environment variable
#### Windows -> Systemsteuerung -> System -> Erweiterte Systemeinstellungen -> Umgebungsvariablen -> Path
#### for JRE : add ;C:\Program Files (x86)\Java\jre1.8.0_91\bin\server
#### for JDK : add ;C:\Program Files\Java\jdk1.8.0_92\jre\bin\server 
####   using a ; as separator to existing pathes (adapt path!!!)

#########################################################
#3# install the rJava package - http://www.rforge.net/rJava/
#### either run : install.packages('rJava')
#### or manually download and install recent version (zip file for Windows) from https://cran.r-project.org/web/packages/rJava/index.html


# include the MICA R interface utility function script
source("./mica-functions.R")

# TEMP - REMOVE
# initMica("C:\\Research\\Java\\MICA\\versions\\dev")



#########################################################
#############  EXAMPLE WITH X & Y DATA   ################
#########################################################

# define individual curves
x1 <- c(2,4,6,8,10,11,12,13);
y1 <- c(1, 1.5, 3, 4, 5, 3, 1.5, 1);

x2 <- c(0,1,2,3,4,5,6,7);
y2 <- y1 + 1;

x3 <- c(0,0.3,0.6,1,1.4,3,5,7);
y3 <- y1 * 1.1;

# compile matrices of x- and y-coordinates
x <- cbind(x1,x2,x3); 
colnames(x) <- as.character(1:ncol(x));
y <- cbind(y1,y2,y3);
colnames(y) <- colnames(x);

# compute annotations
annotations <- getAnnotations(  x=x, y=y, minRelMinMaxDist=1, minRelSlopeHeight=0.5);

# run mica and store alignment data 
alignment <- alignCurves( x=x, y=y, maxRelXShift=0.5, outSlope=TRUE );

# x plot of alignment with distance in title
matplot( alignment$x, y, xlim=c(min(alignment$x,x),max(alignment$x,x)), type="b", lwd=3, main=paste("mean distance =",mean(na.omit(as.vector(alignment$pairDist$warped)))) );
# add x-normalized plot of original data
matlines( x, y, type="l", lwd=1 );
# add x-normalized consensus
lines( alignment$consensus$x, alignment$consensus$y, lwd=3, col="blue", type="b");


# x-normalized plot of alignment with distance in title
matplot( getRelCoord(alignment$x), y, type="b", lwd=3, main=paste("mean distance =",mean(na.omit(as.vector(alignment$pairDist$warped)))) );
# add x-normalized plot of original data
matlines( getRelCoord(x), y, type="l", lwd=1 );
# add x-normalized consensus
lines( getRelCoord(alignment$consensus$x), alignment$consensus$y, lwd=3, col="blue", type="b");

# x-normalized plot of slopes 
matplot( getRelCoord(alignment$x), alignment$slope$warped, type="b", lwd=3, main=paste("slope") );
matlines( getRelCoord(x), alignment$slope$orig, type="l", lwd=1 );



#########################################################
#############  EXAMPLE WITH Y DATA ONLY  ################
#########################################################

newY <- interpolateCurves(x,y,20)$y;
colnames(newY) <- colnames(y);

alignment <- alignCurves( newY, maxRelXShift=0.6 );

# plot of alignment with distance in title
matplot( alignment$x, newY, type="b", lwd=3, main=paste("mean distance =",mean(na.omit(as.vector(alignment$pairDist$warped)))) );
# add original data (equidistant x data)
matlines( getEquiX(newY), newY, type="l", lwd=1 );
# add consensus
lines( alignment$consensus$x, alignment$consensus$y, lwd=3, col="blue");



