#!/usr/bin/env bash
set +ex

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
ARANGO_ENDPOINT="localhost:8529"

POSITIONAL=()
while [[ $# -gt 0 ]]
do
    key="$1"

    case $key in
        -e|--endpoint)
            ARANGO_ENDPOINT="$2"
            shift # past argument
            shift # past value
            ;;
        -r|--requests)
            REQUESTS="$2"
            shift # past argument
            shift # past value
            ;;
        -h|--help)
            echo "Usage:"
            echo "  -e|--endpoint   ArangoDB endpoint"
            echo "  -r|--requests   Number of requests"
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
    PREFIX=$1
    TEST=$2
    THREADS=$3
    SHARDS=$4
    if [[ $TEST == *"insert"* ]]; then
        DROP=true
        echo " WARNING -- Dropping database in advance"
    else
        DROP=false
    fi;
    echo "Starting $TEST at $(date --iso-8601=minutes)"

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

# Run document benchmarks
run_document_bench() {
    run_bench $1 "document_insert" $2 $3
    run_bench $1 "document_update" $2 $3
    run_bench $1 "document_replace" $2 $3
    run_bench $1 "document_get" $2 $3
}

# Run vertex benchmarks
run_vertex_bench() {
    run_bench $1 "vertex_insert" $2 $3
    run_bench $1 "vertex_update" $2 $3
    run_bench $1 "vertex_replace" $2 $3
    run_bench $1 "vertex_get" $2 $3
}

# Run edge benchmarks
run_edge_bench() {
    run_bench $1 "edge_insert" $2 $3
    run_bench $1 "edge_update" $2 $3
    run_bench $1 "edge_replace" $2 $3
    run_bench $1 "edge_get" $2 $3
}

# Run AQL benchmarks
run_aql_bench() {
    run_bench $1 "aql_insert" $2 $3
    run_bench $1 "aql_get" $2 $3
    run_bench $1 "aql_replace" $2 $3
#    run_bench $1 "aql_custom" $2 $3
}

run() {
    echo "Results will be stored in $RESULT_DIR"
    mkdir -p $RESULT_DIR

    echo "Running java-bench for baseline (1 thread, 3 shards)"
    run_document_bench "baseline" 1 3

    echo "Running java-bench for thread scaling (8, 32, 64 threads, 3 shards)"
    for nthreads in 8 32 64
    do
        run_document_bench "document-threadscale" $nthreads 3
        run_vertex_bench "vertex-threadscale" $nthreads 3
        run_edge_bench "edge-threadscale" $nthreads 3
        run_aql_bench "aql-threadscale" $nthreads 3
    done

    echo "Running java-bench for shard scaling (32 threads, 3, 9, 27, 81 shards)"
    for nshards in 3 9 27 81
    do
        run_document_bench "document-shardscale" 32 $nshards
        run_vertex_bench "vertex-shardscale" 32 $nshards
        run_edge_bench "edge-shardscale" 32 $nshards
        run_aql_bench "aql-shardscale" 32 $nshards
    done
}

run
