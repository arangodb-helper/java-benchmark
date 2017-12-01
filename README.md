# load-test

Creates load on ArangoDB using the Java driver


## compile

```
mvn package
```

## usage write

```
java -jar target/arangodb-load-test.jar -case write
```

## usage read

```
java -jar arangodb-load-test.jar -case read
```

## additional options

```
-hosts <hosts>                         Comma separated host addresses (default: 127.0.0.1:8529)
-user <user>                           User (default: root)
-password <password>                   Password (default: )
-threads <threads>                     Number of client threads (default: 1)
-protocol <protocol>                   Network protocol (vst,http_json,http_vpack) (default: VST)
-loadBalancing <loadBalancing>         Load balancing strategy (none,round_robin,one_random) (default: NONE)
-dropDB <dropDB>                       Drop DB before run (default: false)
-batchSize <batchSize>                 Number of documents processed in one batch (default: 1000)
-connections <connections>             Connections per thread (default for vst: 0, http: 20)
-docSize <docSize>                     Number of field in the documents (default: 20)
-docFieldSize <docFieldSize>           The field size in the documents (default: 30)
-keyPrefix <keyPrefix>                 Document key prefix (when running on multiple clients) (default: )
-printRequestTime <printRequestTime>   Print time for every request (default: false)
```
