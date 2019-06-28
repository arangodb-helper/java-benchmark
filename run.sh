#!/usr/bin/env bash
set +ex

trap ctrl_c INT

function ctrl_c()
{
    echo "CTRL+C received, quitting"
    exit
}

#
# Written by Markus Pfeiffer <markus@arangodb.com>
#
# This script is supposed to run the java benchmarks in pre-release testing.
#
# This test is supposed to be run in a cluster of machines, so maybe in the
# future we should add starting of that cluster into the script. (or plug a
# script in front that starts a cluster)
#
# TODO:
#  * Add some results, or a place where past results can be found for
#    future comparison
#  * Add a function to plot the results
#  * Document what the results mean
#
# The *create* tests have to be run before other tests because they create
# the collections required for the subsequent tests.
#
# Some parameters
REQUESTS=1000000
THREADS="8,32,64"
SHARDS="3,9,27,81"
ARANGO_ENDPOINT="localhost:8529"

POSITIONAL=()
while [[ $# -gt 0 ]]
do
    key="$1"

    case $key in
        -e|--endpoint)
            ARANGO_ENDPOINT="$2"
            shift
            shift
            ;;
        -r|--requests)
            REQUESTS="$2"
            shift
            shift
            ;;
        -s|--shards)
            SHARDS="$2"
            shift
            shift
            ;;
        -t|--threads)
            THREADS="$2"
            shift
            shift
            ;;
        -h|--help)
            echo "Usage:"
            echo "  -e|--endpoint   ArangoDB endpoint"
            echo "  -r|--requests   number of requests"
            echo "  -s|--shards     comma-separated list of shard numbers to test"
            echo "  -t|--threads    comma-separated list of thread numbers to test"
            exit 0
            ;;
        *)  # unknown option
            POSITIONAL+=("$1") # save it in an array for later
            shift # past argument
            ;;
    esac
done

# When the test was started; this is used in filenames for outputs.
WHEN=$(date --iso-8601=minutes)
RESULT_DIR="javabench-$WHEN/"

echo "Using"
echo "   endpoint $ARANGO_ENDPOINT"
echo "   for      $REQUESTS requests"
echo "   testing  $SHARDS shards (with 32 threads)"
echo "   testing  $THREADS threads (with 3 shards)"
echo ""

#
# runs the java benchmark, parameters as follows
#
# 1st -- output prefix
# 2nd -- which test to run. To get a list of tests refer to the java-benchmark help
# 3rd -- number of threads to use
# 4th -- number of shards to use
#
# Currently replication factor is always 1
#
run_bench() {
    local PREFIX=$1
    local TEST=$2
    local THREADS=$3
    local SHARDS=$4

    echo "Starting $TEST at $(date --iso-8601=minutes)"
    if [[ $TEST == *"insert"* ]]; then
        DROP=true
        echo " WARNING -- Dropping database in advance"
    else
        DROP=false
    fi;

    java -jar target/arangodb-java-benchmark.jar \
         -e $ARANGO_ENDPOINT \
         --numberOfShards $SHARDS --replicationFactor 1 \
         --docIndexLargeSimple skiplist \
         --docNumLargeSimple 5 \
         -t $TEST \
         --dropDB $DROP \
         --requests $REQUESTS \
         --threads $THREADS \
         --keyPrefix "baseline" \
         --outputInterval 1 \
         --outputFile $RESULT_DIR/output-$PREFIX-$TEST-$THREADS-threads-$SHARDS-shards.csv
}


plot_bench() {
    # TODO: this is super ugly
    local GNUPLOT=$(which gnuplot)
    if [[ -f "$GNUPLOT" ]]; then
        $GNUPLOT -c plot.gp $RESULT_DIR \
                 $1-$2-threads-$3-shards \
                 $RESULT_DIR/output-$1-$4-$2-threads-$3-shards \
                 $RESULT_DIR/output-$1-$5-$2-threads-$3-shards \
                 $RESULT_DIR/output-$1-$6-$2-threads-$3-shards \
                 $RESULT_DIR/output-$1-$7-$2-threads-$3-shards
    else
        echo "gnuplot not found, not plotting results"
    fi;
}

# Run document benchmarks
run_document_bench() {
    run_bench $1 "document_insert" $2 $3
    run_bench $1 "document_update" $2 $3
    run_bench $1 "document_replace" $2 $3
    run_bench $1 "document_get" $2 $3
    plot_bench $1 $2 $3 "document_insert" "document_update" "document_replace" "document_get"
}

# Run vertex benchmarks
run_vertex_bench() {
    run_bench $1 "vertex_insert" $2 $3
    run_bench $1 "vertex_update" $2 $3
    run_bench $1 "vertex_replace" $2 $3
    run_bench $1 "vertex_get" $2 $3
    plot_bench $1 $2 $3 "vertex_insert" "vertex_update" "vertex_replace" "vertex_get"
}

# Run edge benchmarks
run_edge_bench() {
    run_bench $1 "edge_insert" $2 $3
    run_bench $1 "edge_update" $2 $3
    run_bench $1 "edge_replace" $2 $3
    run_bench $1 "edge_get" $2 $3
    plot_bench $1 $2 $3 "edge_insert" "edge_update" "edge_replace" "edge_get"
}

# Run AQL benchmarks
run_aql_bench() {
    run_bench $1 "aql_insert" $2 $3
    run_bench $1 "aql_get" $2 $3
    run_bench $1 "aql_replace" $2 $3
#    run_bench $1 "aql_custom" $2 $3
    # TODO: using aql_insert twice is a hack
    plot_bench $1 $2 $3 "aql_insert" "aql_insert" "aql_replace" "aql_get"
}

run() {
    echo "Results will be stored in $RESULT_DIR"
    mkdir -p $RESULT_DIR
    echo ""

    echo "Running java-bench for baseline (1 thread, 3 shards)"
    run_document_bench "baseline" 1 3
    echo ""

    echo "Running java-bench for thread scaling ($THREADS threads, 3 shards)"
    for nthreads in $(echo $THREADS | tr "," "\n")
    do
        echo $nthreads
        run_document_bench "document-threadscale" $nthreads 3
        run_vertex_bench "vertex-threadscale" $nthreads 3
        run_edge_bench "edge-threadscale" $nthreads 3
        run_aql_bench "aql-threadscale" $nthreads 3
    done
    echo ""

    echo "Running java-bench for shard scaling (32 threads, $SHARDS shards)"
    for nshards in $(echo $SHARDS | tr "," "\n")
    do
        echo $nshards
        run_document_bench "document-shardscale" 32 $nshards
        run_vertex_bench "vertex-shardscale" 32 $nshards
        run_edge_bench "edge-shardscale" 32 $nshards
        run_aql_bench "aql-shardscale" 32 $nshards
    done
}

run
