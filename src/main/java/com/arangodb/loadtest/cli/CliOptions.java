/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.loadtest.cli;

import java.util.Collection;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.loadtest.testcase.TestCase;

/**
 * @author Mark Vollmary
 *
 */
public class CliOptions {

	@CliOptionInfo(description = "comma separeted list of test cases to use", opt = "t", required = true, componentType = TestCase.class)
	private Collection<TestCase> test;

	@CliOptionInfo(description = "number of operations per thread", defaultValue = "1000")
	private Integer requests;

	@CliOptionInfo(description = "number of seconds the test should run (if > 0 the option 'requests' is ignored)", defaultValue = "0")
	private Integer duration;

	@CliOptionInfo(description = "run test n times. drop database between runs", defaultValue = "1")
	private Integer runs;

	@CliOptionInfo(description = "delay (in seconds) to use between runs (necessary only when --runs > 1)", defaultValue = "0")
	private Integer delay;

	@CliOptionInfo(description = "comma separated list of endpoints to connect to", opt = "e", defaultValue = "127.0.0.1:8529")
	private String endpoints;

	@CliOptionInfo(description = "username to use when connecting", opt = "u", defaultValue = "root")
	private String user;

	@CliOptionInfo(description = "password to use when connecting.", opt = "p")
	private String password;

	@CliOptionInfo(description = "number of operations in one batch (necessary only when API supports batching)", defaultValue = "1")
	private Integer batchSize;

	@CliOptionInfo(description = "number of parallel client threads", defaultValue = "1")
	private Integer threads;

	@CliOptionInfo(description = "network protocol to use", defaultValue = "vst")
	private Protocol protocol;

	@CliOptionInfo(description = "use SSL", defaultValue = "false")
	private Boolean ssl;

	@CliOptionInfo(description = "load balancing strategy to use (for cluster setup)", defaultValue = "none")
	private LoadBalancingStrategy loadBalancing;

	@CliOptionInfo(description = "number of parallel connections per thread", defaultValue = "1")
	private Integer connections;

	@CliOptionInfo(description = "drop DB before run", defaultValue = "false")
	private Boolean dropDB;

	@CliOptionInfo(description = "database name to use in test", defaultValue = "ArangoJavaBenchmarkDB")
	private String database;

	@CliOptionInfo(description = "collection name to use in test", defaultValue = "ArangoJavaBenchmarkCollection")
	private String collection;

	@CliOptionInfo(description = "graph name to use in test", defaultValue = "ArangoJavaBenchmarkGraph")
	private String graph;

	@CliOptionInfo(description = "vertex collection name to use in graph", defaultValue = "ArangoJavaBenchmarkVertex")
	private String vertexCollection;

	@CliOptionInfo(description = "edge collection name to use in graph", defaultValue = "ArangoJavaBenchmarkEdge")
	private String edgeCollection;

	@CliOptionInfo(description = "number of shards of created collections", defaultValue = "1")
	private Integer numberOfShards;

	@CliOptionInfo(description = "replication factor of created collections", defaultValue = "1")
	private Integer replicationFactor;

	@CliOptionInfo(description = "use waitForSync for created collections", defaultValue = "false")
	private Boolean waitForSync;

	@CliOptionInfo(description = "document key prefix (necessary only when run multiple times)")
	private String keyPrefix;

	@CliOptionInfo(description = "automatic acquire list of endpoints to use for load balancing", defaultValue = "false")
	private Boolean acquireHostList;

	@CliOptionInfo(description = "output interval in seconds", defaultValue = "1")
	private Integer outputInterval;

	@CliOptionInfo(description = "number of String fields in the documents", defaultValue = "5")
	private Integer docNumSimple;

	@CliOptionInfo(description = "number of large String fields in the documents", defaultValue = "0")
	private Integer docNumLargeSimple;

	@CliOptionInfo(description = "number of nested objects in the documents", defaultValue = "0")
	private Integer docNumObjects;

	@CliOptionInfo(description = "number of array fields in the documents", defaultValue = "0")
	private Integer docNumArrays;

	@CliOptionInfo(description = "size of String fields in the documents", defaultValue = "20")
	private Integer docSimpleSize;

	@CliOptionInfo(description = "size of large String fields in the documents", defaultValue = "100")
	private Integer docLargeSimpleSize;

	@CliOptionInfo(description = "size of array fields in the documents", defaultValue = "10")
	private Integer docArraysSize;

	@CliOptionInfo(description = "max depth of nested objects in the documents", defaultValue = "1")
	private Integer docNestingDepth;

	@CliOptionInfo(description = "comma separated list of types of indexes on String fields", componentType = Index.class)
	private Collection<Index> docIndexSimple;

	@CliOptionInfo(description = "number of String fields to be indexed", defaultValue = "1")
	private Integer docNumIndexSimple;

	@CliOptionInfo(description = "comma separated list of types of indexes on large String fields", componentType = Index.class)
	private Collection<Index> docIndexLargeSimple;

	@CliOptionInfo(description = "number of large String fields to be indexed", defaultValue = "1")
	private Integer docNumIndexLargeSimple;

	@CliOptionInfo(description = "comma separated list of types of indexes on nested objects", componentType = Index.class)
	private Collection<Index> docIndexObjects;


	@CliOptionInfo(description = "if you want to create a view", defaultValue = "false")
	private Boolean docView;

	@CliOptionInfo(description = "number of nexted objects to be indexed", defaultValue = "1")
	private Integer docNumIndexObjects;

	@CliOptionInfo(description = "comma separated list of types of indexes on array fields", componentType = Index.class)
	private Collection<Index> docIndexArrays;

	@CliOptionInfo(description = "number of array fields to be indexed", defaultValue = "1")
	private Integer docNumIndexArrays;

	@CliOptionInfo(description = "verbose log output", defaultValue = "true")
	private Boolean verbose;

	@CliOptionInfo(description = "File path of output file", defaultValue = "")
	private String outputFile;

	@CliOptionInfo(description = "AQL cursor batch size", defaultValue = "1000")
	private Integer cursorBatchSize;

	@CliOptionInfo(description = "AQL cursor stream", defaultValue = "false")
	private Boolean cursorStream;

	@CliOptionInfo(description = "Custom AQL query (supported bind params: @@collection, @@vertex, @@edge, @graph, @doc, @docs, @key, @@keys)", defaultValue = "")
	private String query;

	public CliOptions() {
		super();
	}

	public Collection<TestCase> getTest() {
		return test;
	}

	public void setTest(final Collection<TestCase> test) {
		this.test = test;
	}

	public Integer getRequests() {
		return requests;
	}

	public void setRequests(final Integer requests) {
		this.requests = requests;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(final Integer duration) {
		this.duration = duration;
	}

	public Integer getRuns() {
		return runs;
	}

	public void setRuns(final Integer runs) {
		this.runs = runs;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(final Integer delay) {
		this.delay = delay;
	}

	public String getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(final String endpoints) {
		this.endpoints = endpoints;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final Integer batchSize) {
		this.batchSize = batchSize;
	}

	public Integer getThreads() {
		return threads;
	}

	public void setThreads(final Integer threads) {
		this.threads = threads;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(final Boolean ssl) {
		this.ssl = ssl;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(final Protocol protocol) {
		this.protocol = protocol;
	}

	public LoadBalancingStrategy getLoadBalancing() {
		return loadBalancing;
	}

	public void setLoadBalancing(final LoadBalancingStrategy loadBalancing) {
		this.loadBalancing = loadBalancing;
	}

	public Integer getConnections() {
		return connections;
	}

	public void setConnections(final Integer connections) {
		this.connections = connections;
	}

	public Boolean getDropDB() {
		return dropDB;
	}

	public void setDropDB(final Boolean dropDB) {
		this.dropDB = dropDB;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(final String database) {
		this.database = database;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(final String graph) {
		this.graph = graph;
	}

	public String getVertexCollection() {
		return vertexCollection;
	}

	public void setVertexCollection(final String vertexCollection) {
		this.vertexCollection = vertexCollection;
	}

	public String getEdgeCollection() {
		return edgeCollection;
	}

	public void setEdgeCollection(final String edgeCollection) {
		this.edgeCollection = edgeCollection;
	}

	public Integer getNumberOfShards() {
		return numberOfShards;
	}

	public void setNumberOfShards(final Integer numberOfShards) {
		this.numberOfShards = numberOfShards;
	}

	public Integer getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(final Integer replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public void setWaitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(final String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public Boolean getAcquireHostList() {
		return acquireHostList;
	}

	public void setAcquireHostList(final Boolean acquireHostList) {
		this.acquireHostList = acquireHostList;
	}

	public Integer getOutputInterval() {
		return outputInterval;
	}

	public void setOutputInterval(final Integer outputInterval) {
		this.outputInterval = outputInterval;
	}

	public Integer getDocNumSimple() {
		return docNumSimple;
	}

	public void setDocNumSimple(final Integer docNumSimple) {
		this.docNumSimple = docNumSimple;
	}

	public Integer getDocNumLargeSimple() {
		return docNumLargeSimple;
	}

	public void setDocNumLargeSimple(final Integer docNumLargeSimple) {
		this.docNumLargeSimple = docNumLargeSimple;
	}

	public Integer getDocNumObjects() {
		return docNumObjects;
	}

	public void setDocNumObjects(final Integer docNumObjects) {
		this.docNumObjects = docNumObjects;
	}

	public Integer getDocNumArrays() {
		return docNumArrays;
	}

	public void setDocNumArrays(final Integer docNumArrays) {
		this.docNumArrays = docNumArrays;
	}

	public Integer getDocSimpleSize() {
		return docSimpleSize;
	}

	public void setDocSimpleSize(final Integer docSimpleSize) {
		this.docSimpleSize = docSimpleSize;
	}

	public Integer getDocLargeSimpleSize() {
		return docLargeSimpleSize;
	}

	public void setDocLargeSimpleSize(final Integer docLargeSimpleSize) {
		this.docLargeSimpleSize = docLargeSimpleSize;
	}

	public Integer getDocArraysSize() {
		return docArraysSize;
	}

	public void setDocArraysSize(final Integer docArraysSize) {
		this.docArraysSize = docArraysSize;
	}

	public Integer getDocNestingDepth() {
		return docNestingDepth;
	}

	public void setDocNestingDepth(final Integer docNestingDepth) {
		this.docNestingDepth = docNestingDepth;
	}

	public Collection<Index> getDocIndexSimple() {
		return docIndexSimple;
	}

	public void setDocIndexSimple(final Collection<Index> docIndexSimple) {
		this.docIndexSimple = docIndexSimple;
	}

	public Integer getDocNumIndexSimple() {
		return docNumIndexSimple;
	}

	public void setDocNumIndexSimple(final Integer docNumIndexSimple) {
		this.docNumIndexSimple = docNumIndexSimple;
	}

	public Collection<Index> getDocIndexLargeSimple() {
		return docIndexLargeSimple;
	}

	public void setDocIndexLargeSimple(final Collection<Index> docIndexLargeSimple) {
		this.docIndexLargeSimple = docIndexLargeSimple;
	}

	public Integer getDocNumIndexLargeSimple() {
		return docNumIndexLargeSimple;
	}

	public void setDocNumIndexLargeSimple(final Integer docNumIndexLargeSimple) {
		this.docNumIndexLargeSimple = docNumIndexLargeSimple;
	}

	public Collection<Index> getDocIndexObjects() {
		return docIndexObjects;
	}

  public boolean getDocView() {
    return docView;
  }

	public void setDocView(final Boolean docView) {
		this.docView = docView;
	}

	public void setDocIndexObjects(final Collection<Index> docIndexObjects) {
		this.docIndexObjects = docIndexObjects;
	}

	public Integer getDocNumIndexObjects() {
		return docNumIndexObjects;
	}

	public void setDocNumIndexObjects(final Integer docNumIndexObjects) {
		this.docNumIndexObjects = docNumIndexObjects;
	}

	public Collection<Index> getDocIndexArrays() {
		return docIndexArrays;
	}

	public void setDocIndexArrays(final Collection<Index> docIndexArrays) {
		this.docIndexArrays = docIndexArrays;
	}

	public Integer getDocNumIndexArrays() {
		return docNumIndexArrays;
	}

	public void setDocNumIndexArrays(final Integer docNumIndexArrays) {
		this.docNumIndexArrays = docNumIndexArrays;
	}

	public Boolean getVerbose() {
		return verbose;
	}

	public void setVerbose(final Boolean verbose) {
		this.verbose = verbose;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(final String outputFile) {
		this.outputFile = outputFile;
	}

	public Integer getCursorBatchSize() {
		return cursorBatchSize;
	}

	public void setCursorBatchSize(final Integer cursorBatchSize) {
		this.cursorBatchSize = cursorBatchSize;
	}

	public Boolean getCursorStream() {
		return cursorStream;
	}

	public void setCursorStream(final Boolean cursorStream) {
		this.cursorStream = cursorStream;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

}
