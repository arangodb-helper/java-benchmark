# arangodb-java-benchmark


## compile

```
mvn package
```

## usage

```
java -jar arangodb-java-benchmark.jar --test document_insert
```

## additional options

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
    --replicationFactor <Integer>             replication factor of created collections (default: 1)
    --requests <Integer>                      number of operations per thread (default: 1000)
    --runs <Integer>                          run test n times. drop database between runs (default: 1)
 -t,--test <Collection>                       comma separeted list of test cases to use.
                                              possible values: "version", "document_get",
                                              "document_insert", "document_import",
                                              "document_update", "document_replace"
    --threads <Integer>                       number of parallel client threads (default: 1)
 -u,--user <String>                           username to use when connecting (default: root)
    --vertexCollection <String>               vertex collection name to use in graph (default:
                                              ArangoJavaBenchmarkVertex)
    --waitForSync <Boolean>                   use waitForSync for created collections (default: false)
```
