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
-hosts <hosts>                 comma separated host addresses (default: 127.0.0.1:8529)
-user <user>                   User (default: root)
-password <password>           Password (default: )
-protocol <protocol>           Network protocol (vst,http_json,http_vpack) (default: VST)
-threads <threads>             Number of client threads (default: 1)
-batchSize <batchSize>         Number of documents processed in one batch (default: 1000)
-docSize <docSize>             Number of field in the documents (default: 20)
-docFieldSize <docFieldSize>   The field size in the documents (default: 30)
```
