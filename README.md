# arangodb-java-benchmark

## usage

First the java code has to be compiled:

```
mvn package
```

The script `run.sh` can then be used to run a series of tests, currently:

 * a full suite of document tests as baseline using 1 thread and 3 shards
 * document, vertex, edge, and AQL tests with 8, 32, and 64 threads and 3 shards, and
 * document, vertex, edge, and AQL tests with 1 thread and 3, 9, 27, and 81 shards.

the results are output as CSVs into a directory with name `javabench-$(date --iso-8601=minutes)`
(so for example `javabench-2019-06-26T13:12+00:00`).

The following parameters are accepted:

```
 -e|--endpoint               The ArangoDB endpoint to connect to for the test (directly passed to the arangodb-java-benchmark.jar)
 -r|--requests <Integer>     Number of requests (default 1000000)
```

For the 3.5 release candidates the script was run as follows

```
 # ./run.sh -e sup4.arangodb.org:8529 -r 1000000
```

Where `sup4.arangodb.org` was one computer in a ArangoDB cluster started (using
the arangodb starter) on set of 3 scaleway machines.

These tests run for more than a day. To have a more concise testing run
you can decrease the number of requests.

### Some internals

The runscript runs `arangodb-java-benchmark.jar`.

Note that you have to run an `insert` benchmark before you can run any of the others, i.e. to run
`document_update` you first have to run `document_insert`.

```
java -jar arangodb-java-benchmark.jar --test document_insert
```
It is not necessary to run `vertex_insert` for the `edge` tests.

#### additional options for the arangodb-java-benchmark jar

```
    --acquireHostList <Boolean>               automatic acquire list of endpoints to use for load balancing
                                              (default: false)
    --batchSize <Integer>                     number of operations in one batch
                                              (necessary only when API supports batching) (default: 1)
    --collection <String>                     collection name to use in test (default: ArangoJavaBenchmark)
    --connections <Integer>                   number of parallel connections per thread (default: 1)
    --database <String>                       database name to use in test (default: ArangoJavaBenchmark)
    --delay <Integer>                         delay (in seconds) to use between runs
                                              (necessary only when --runs > 1) (default: 0)
    --docArraysSize <Integer>                 size of array fields in the documents (default: 10)
    --docIndexArrays <Collection>             comma separated list of types of indexes on array
                                              fields. possible values: "hash", "skiplist",
                                              "persistent", "geo", "fulltext"
    --docIndexLargeSimple <Collection>        comma separated list of types of indexes on large
                                              String fields. possible values: "hash", "skiplist",
                                              "persistent", "geo", "fulltext"
    --docIndexObjects <Collection>            comma separated list of types of indexes on nested
                                              objects. possible values: "hash", "skiplist",
                                              "persistent", "geo", "fulltext"
    --docIndexSimple <Collection>             comma separated list of types of indexes on String
                                              fields. possible values: "hash", "skiplist",
                                              "persistent", "geo", "fulltext"
    --docLargeSimpleSize <Integer>            size of large String fields in the documents (default: 100)
    --docNestingDepth <Integer>               max depth of nested objects in the documents (default: 1)
    --docNumArrays <Integer>                  number of array fields in the documents (default: 0)
    --docNumIndexArrays <Integer>             number of array fields to be indexed (default: 1)
    --docNumIndexLargeSimple <Integer>        number of large String fields to be indexed (default: 1)
    --docNumIndexObjects <Integer>            number of nexted objects to be indexed (default: 1)
    --docNumIndexSimple <Integer>             number of String fields to be indexed (default: 1)
    --docNumLargeSimple <Integer>             number of large String fields in the documents (default: 0)
    --docNumObjects <Integer>                 number of nested objects in the documents (default: 0)
    --docNumSimple <Integer>                  number of String fields in the documents (default: 5)
    --docSimpleSize <Integer>                 size of String fields in the documents (default: 20)
    --dropDB <Boolean>                        drop DB before run (default: false)
    --duration <Integer>                      number of seconds the test should run
                                              (if > 0 the option 'requests' is ignored) (default: 0)
 -e,--endpoints <String>                      comma separated list of endpoints to connect to
                                              (default: 127.0.0.1:8529)
    --edgeCollection <String>                 edge collection name to use in graph (default:
                                              ArangoJavaBenchmarkEdge)
    --graph <String>                          graph name to use in test (default: ArangoJavaBenchmarkGraph)
    --keyPrefix <String>                      document key prefix (necessary only when run multiple times)
    --loadBalancing <LoadBalancingStrategy>   load balancing strategy to use (for cluster setup).
                                              possible values: "none", "round_robin", "one_random"
                                              (default: none)
    --numberOfShards <Integer>                number of shards of created collections (default: 1)
    --outputInterval <Integer>                output interval in seconds (default: 1)
 -p,--password <String>                       password to use when connecting.
    --protocol <Protocol>                     network protocol to use. possible values: "vst",
                                              "http_json", "http_vpack" (default: vst)
    --query <String>                          Custom AQL query (supported bind params: @@collection,
                                              @@vertex, @@edge, @graph, @doc, @docs, @key, @@keys)
    --replicationFactor <Integer>             replication factor of created collections (default: 1)
    --requests <Integer>                      number of operations per thread (default: 1000)
    --runs <Integer>                          run test n times. drop database between runs (default: 1)
 -t,--test <Collection>                       comma separeted list of test cases to use.
                                              possible values: "version", "document_get",
                                              "document_insert", "document_import",
                                              "document_update", "document_replace",
                                              "aql_custom", "aql_get", "aql_insert", "aql_replace", 
                                              "vertex_get", "vertex_insert", "vertex_update",
                                              "vertex_replace", "edge_get", "edge_insert",
                                              "edge_update", "edge_replace"
    --threads <Integer>                       number of parallel client threads (default: 1)
 -u,--user <String>                           username to use when connecting (default: root)
    --vertexCollection <String>               vertex collection name to use in graph (default:
                                              ArangoJavaBenchmarkVertex)
    --waitForSync <Boolean>                   use waitForSync for created collections (default: false)
```
