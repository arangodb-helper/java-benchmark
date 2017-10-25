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
-batchSize <batchSize>   Number of documents processed in one batch (default: 1000)
-docSize <docSize>       Number of field in the documents (default: 20)
-ip <ip>                 Server address (default: 127.0.0.1)
-port <port>             Server port (default: 8529)
-protocol <protocol>     Network protocol (vst,http_json,http_vpack) (default: VST)
-threads <threads>       Number of client threads (default: 1)
```
