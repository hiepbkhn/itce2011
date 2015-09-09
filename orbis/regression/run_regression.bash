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

# check inputs
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <dump_dir> <regression_file>"
    exit 1
fi

if [ ! -d "$1" ]; then
    echo "Cannot find dump dir $1"
    exit 1
fi

if [ ! -f "$2" ]; then
    echo "Could not find regression file $1"
fi

# setup configs, verify everything we need is there
dependencies=" dkDist  dkMetrics  dkRescale  dkRewire  dkTopoGen0k  \
    dkTopoGen1k  dkTopoGen2k scaleTopology.bash"
orbis_dir=`dirname $0`/..
topo_dir=$orbis_dir/topos
dump_dir=$1

for d in $dependencies; do
    if [ ! -f $orbis_dir/$d ]; then
	echo "Could not find file $orbis_dir/$d"
	exit 1
    fi
done

# Load configurations from regression file.  
# Should contain the following variables: HOT SIZES METRICS K and optionally R
source $2
#if [ -z $TOPO ]; then
if [ -z "$TOPO" ] || [ -z "$SIZES" ] || [ -z "$METRICS" ] || \
    [ -z "$K" ] || [ ! -f $topo_dir/$TOPO ]; then  # -o -z $METRICS ]; then
    echo "BAD PARAMETERS in regression file"
    exit 1
fi


# Run the tests
echo "Running regression on topology: $TOPO"
mkdir $dump_dir/$TOPO
for size in $SIZES; do
    echo "on size $size"
    for k in $K; do
	echo "on k $k"
	# scale topology
	$orbis_dir/scaleTopology.bash $topo_dir/$TOPO $k $size $R > \
	    $dump_dir/$TOPO/rescaledtopo.$k.$size 2> /dev/null
	# generate metrics
	for metric in $METRICS; do
	    echo "on metric $metric"
	    $orbis_dir/dkMetrics --$metric \
		-i $dump_dir/$TOPO/rescaledtopo.$k.$size > \
		$dump_dir/$TOPO/rescaledtopo.$k.metrics.$metric.$size 2> /dev/null
	done
    done
done

    
