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

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

/**
 * @author Mark Vollmary
 *
 */
public class CliOptions {

	@CliOptionInfo(description = "test case to use. possible values: \"write\", \"read\"", opt = "t", required = true)
	private String test;

	@CliOptionInfo(description = "run test n times", defaultValue = "1")
	private Integer runs;

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

	@CliOptionInfo(description = "number of field in the documents", defaultValue = "1")
	private Integer docSize;

	@CliOptionInfo(description = "the field size in the documents", defaultValue = "10")
	private Integer docFieldSize;

	@CliOptionInfo(description = "network protocol to use", defaultValue = "vst")
	private Protocol protocol;

	@CliOptionInfo(description = "load balancing strategy to use (for cluster setup)", defaultValue = "none")
	private LoadBalancingStrategy loadBalancing;

	@CliOptionInfo(description = "number of parallel connections per thread", defaultValue = "1")
	private Integer connections;

	@CliOptionInfo(description = "drop DB before run", defaultValue = "false")
	private Boolean dropDB;

	@CliOptionInfo(description = "database name to use in test", defaultValue = "ArangoJavaBenchmark")
	private String database;

	@CliOptionInfo(description = "collection name to use in test", defaultValue = "ArangoJavaBenchmark")
	private String collection;

	@CliOptionInfo(description = "number of shards of created collections", defaultValue = "1")
	private Integer numberOfShards;

	@CliOptionInfo(description = "replication factor of created collections", defaultValue = "1")
	private Integer replicationFactor;

	@CliOptionInfo(description = "document key prefix (necessary only when run multiple times)")
	private String keyPrefix;

	@CliOptionInfo(description = "automatic acquire list of endpoints to use for load balancing", defaultValue = "false")
	private Boolean acquireHostList;

	public CliOptions() {
		super();
	}

	public String getTest() {
		return test;
	}

	public void setTest(final String test) {
		this.test = test;
	}

	public Integer getRuns() {
		return runs;
	}

	public void setRuns(final Integer runs) {
		this.runs = runs;
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

	public Integer getDocSize() {
		return docSize;
	}

	public void setDocSize(final Integer docSize) {
		this.docSize = docSize;
	}

	public Integer getDocFieldSize() {
		return docFieldSize;
	}

	public void setDocFieldSize(final Integer docFieldSize) {
		this.docFieldSize = docFieldSize;
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

}
