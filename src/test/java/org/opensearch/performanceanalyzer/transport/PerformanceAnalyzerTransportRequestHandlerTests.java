/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020-2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.opensearch.performanceanalyzer.transport;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opensearch.action.bulk.BulkItemRequest;
import org.opensearch.action.bulk.BulkShardRequest;
import org.opensearch.action.support.replication.TransportReplicationAction.ConcreteShardRequest;
import org.opensearch.index.shard.ShardId;
import org.opensearch.performanceanalyzer.config.PerformanceAnalyzerController;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportChannel;
import org.opensearch.transport.TransportRequest;
import org.opensearch.transport.TransportRequestHandler;

@SuppressWarnings("unchecked")
public class PerformanceAnalyzerTransportRequestHandlerTests {
    private PerformanceAnalyzerTransportRequestHandler handler;
    private ConcreteShardRequest concreteShardRequest;

    @Mock private TransportRequestHandler transportRequestHandler;
    @Mock private PerformanceAnalyzerController controller;
    @Mock private TransportChannel channel;
    @Mock private TransportRequest request;
    @Mock private BulkShardRequest bulkShardRequest;
    @Mock private Task task;
    @Mock private ShardId shardId;

    @Before
    public void init() {
        // this test only runs in Linux system
        // as some of the static members of the ThreadList class are specific to Linux
        org.junit.Assume.assumeTrue(SystemUtils.IS_OS_LINUX);

        initMocks(this);
        handler =
                new PerformanceAnalyzerTransportRequestHandler(transportRequestHandler, controller);
        handler.set(transportRequestHandler);
        Mockito.when(controller.isPerformanceAnalyzerEnabled()).thenReturn(true);
    }

    @Test
    public void testMessageReceived() throws Exception {
        handler.messageReceived(request, channel, task);
        verify(transportRequestHandler).messageReceived(request, channel, task);
    }

    @Test
    public void testGetChannel() {
        concreteShardRequest = new ConcreteShardRequest(bulkShardRequest, "id", 1);
        handler.getChannel(concreteShardRequest, channel, task);

        Mockito.when(bulkShardRequest.shardId()).thenReturn(shardId);
        Mockito.when(bulkShardRequest.items()).thenReturn(new BulkItemRequest[1]);
        TransportChannel actualChannel = handler.getChannel(concreteShardRequest, channel, task);
        assertTrue(actualChannel instanceof PerformanceAnalyzerTransportChannel);
    }
}
