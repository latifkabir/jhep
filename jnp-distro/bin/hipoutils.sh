#!/bin/sh
#*********************************************************
#---------------------------------------------------------
# JHEP math CLI interface.
#---------------------------------------------------------
SCRIPT_DIR=`dirname $0`
#---------------------------------------------------------
# The MALLOC_ARENA_MAX is GLIB flag that controls
# how much VIRTUAL memory will be claimed by JVM
#---------------------------------------------------------
MALLOC_ARENA_MAX=1; export MALLOC_ARENA_MAX
#---------------------------------------------------------
# SET UP JAVA_OPTIONS With the max memory and starting
# memory
#---------------------------------------------------------
JAVA_OPTIONS="-Xmx1024m -Xms1024m"
java $JAVA_OPTIONS -cp "$SCRIPT_DIR/../lib/jaw-0.8.jar" org.jlab.jnp.hipo.utils.HipoUtilities $*
