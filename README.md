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
    --acquireHostList <Boolean>               automatic acquire list of endpoints to use for load balancing (default: false)
    --batchSize <Integer>                     number of operations in one batch (necessary only when API supports batching) (default: 1)
    --collection <String>                     collection name to use in test (default: ArangoJavaBenchmark) 
    --connections <Integer>                   number of parallel connections per thread (default: 1)
    --database <String>                       database name to use in test (default: ArangoJavaBenchmark) 
    --delay <Integer>                         delay (in seconds) to use between runs (necessary only when --runs > 1) (default: 0)
    --docFieldSize <Integer>                  the field size in the documents (default: 10) 
    --docSize <Integer>                       number of field in the documents (default: 1)
    --dropDB <Boolean>                        drop DB before run (default: false)
 -e,--endpoints <String>                      comma separated list of endpoints to connect to (default: 127.0.0.1:8529)
    --keyPrefix <String>                      document key prefix (necessary only when run multiple times)
    --loadBalancing <LoadBalancingStrategy>   load balancing strategy to use (for cluster setup). possible values: "none", "round_robin", "one_random" (default: none)
    --numberOfShards <Integer>                number of shards of created collections (default: 1)
    --outputInterval <Integer>                output interval in seconds (default: 1)
 -p,--password <String>                       password to use when connecting.
    --protocol <Protocol>                     network protocol to use. possible values: "vst", "http_json", "http_vpack" (default: vst)
    --replicationFactor <Integer>             replication factor of created collections (default: 1)
    --requests <Integer>                      number of operations per thread (default: 1000)
    --runs <Integer>                          run test n times. drop database between runs (default: 1)
 -t,--test <String>                           comma separeted list of test cases to use. possible values: "document_get", "document_insert", "version"
    --threads <Integer>                       number of parallel client threads (default: 1)
 -u,--user <String>                           username to use when connecting (default: root)
    --waitForSync <Boolean>                   use waitForSync for created collections (default: false)
```
