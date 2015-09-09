#!/bin/bash

#########################################################################
## Copyright 2007
## The Regents of the University of California
## All Rights Reserved
##
## Permission to use, copy, modify and distribute any part of
## this software package for educational, research and non-profit
## purposes, without fee, and without a written agreement is hereby
## granted, provided that the above copyright notice, this paragraph
## and the following paragraphs appear in all copies.
##
## Those desiring to incorporate this into commercial products or
## use for commercial purposes should contact the Technology Transfer
## Office, University of California, San Diego, 9500 Gilman Drive,
## La Jolla, CA 92093-0910, Ph: (858) 534-5815, FAX: (858) 534-7345.
##
## IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
## PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
## DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS
## SOFTWARE, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED
## OF THE POSSIBILITY OF SUCH DAMAGE.
##
## THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE
## UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE,
## SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS. THE UNIVERSITY
## OF CALIFORNIA MAKES NO REPRESENTATIONS AND EXTENDS NO WARRANTIES
## OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT LIMITED
## TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
## PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT
## INFRINGE ANY PATENT, TRADEMARK OR OTHER RIGHTS.
##
##
## Written by Priya Mahadevan <pmahadevan@cs.ucsd.edu>  and
##            Calvin Hubble <chubble@cs.ucsd.edu
##
#########################################################################

# Scales a given a topology using the following steps
#   * Extract 1k or 2k distribution (specified by user) using dkDist
#   * Scale this distribution using dkRescale
#   * Generate a topology from the scaled distribution using dkTopoGen
#
# Imposes a maximum degree of 330 of a 4th parameter is specified (for scaling router topologies)


## Check dependencies
DKDIST=`dirname $0`/dkDist
DKRESCALE=`dirname $0`/dkRescale
DKTOPOGEN2K=`dirname $0`/dkTopoGen2k
DKTOPOGEN1K=`dirname $0`/dkTopoGen1k

if [ ! -x $DKDIST ]; then
    echo "Could not find $DKDIST"
    exit 1
elif [ ! -x $DKRESCALE ]; then
    echo "Could not find $DKRESCALE"
    exit 1
elif [ ! -x $DKTOPOGEN2K ]; then
    echo "Could not find $DKTOPOGEN2K"
    exit 1
elif [ ! -x $DKTOPOGEN1K ]; then
    echo "Could not find $DKTOPOGEN1K"
    exit 1
fi

## check command line parameters
if [ "$#" -lt 3 ]; then
    echo "Usage $0 <input graph> <{1,2}> <target # nodes> <r?>"
    echo "    Second parameter specifies 1k or 2k rescaling"
    echo "    Last parameter r specifies router topologies and will impose a maximum degree of 330"
    exit 1
fi

if [ ! -f "$1" ]; then
    echo "Could not find input graph $1"  >&2
    exit 1
fi

inputGraph=$1
k=$2
numTargetNodes=$3
if [ ! -z $4 ]; then
    doRouter=1
else
    doRouter=0
fi

if [ $k != 1 -a $k != 2 ]; then
    echo "Must specify k as either 1 or 2" >&2
    exit 1
fi

## generate distribution from input file and make sure all is well
tmpDist=`mktemp tmp-dist1-XXXXXX`
tmpDistRescaled=`mktemp tmp-dist2-XXXXXX`

$DKDIST -i $inputGraph -k $k > $tmpDist
if [ $? -ne 0 ]; then
    echo "Bad return value for $DKDIST" >&2
    rm $tmpDist
    rm $tmpDistRescaled
    exit 1
fi

## rescale the topology
if [ $doRouter -eq 1 ]; then
    $DKRESCALE -i $tmpDist -k $k -n $numTargetNodes -m 330 > $tmpDistRescaled
else
    $DKRESCALE -i $tmpDist -k $k -n $numTargetNodes > $tmpDistRescaled    
fi

if [ $? -ne 0 ]; then
    echo "Bad return value for $DKDIST" >&2
    rm $tmpDist
    rm $tmpDistRescaled
    exit 1
fi


## generate topology from rescaled distribution
if [ $k -eq 1 ]; then
    $DKTOPOGEN1K -i $tmpDistRescaled
else
    $DKTOPOGEN2K -i $tmpDistRescaled
fi

if [ $? -ne 0 ]; then
    echo "There was a problem with topology generation" >&2
fi

rm $tmpDistRescaled
rm $tmpDist
