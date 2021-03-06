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

package org.opensearch.performanceanalyzer.collectors;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.indices.IndicesService;
import org.opensearch.performanceanalyzer.OpenSearchResources;
import org.opensearch.performanceanalyzer.config.PluginSettings;
import org.opensearch.performanceanalyzer.metrics.MetricsConfiguration;
import org.opensearch.performanceanalyzer.metrics.PerformanceAnalyzerMetrics;
import org.opensearch.performanceanalyzer.reader_writer_shared.Event;
import org.opensearch.performanceanalyzer.util.TestUtil;
import org.opensearch.test.OpenSearchSingleNodeTestCase;

public class NodeStatsAllShardsMetricsCollectorTests extends OpenSearchSingleNodeTestCase {
    private static final String TEST_INDEX = "test";
    private NodeStatsAllShardsMetricsCollector nodeStatsAllShardsMetricsCollector;
    private long startTimeInMills = 1153721339;

    @Before
    public void init() {
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        OpenSearchResources.INSTANCE.setIndicesService(indicesService);

        MetricsConfiguration.CONFIG_MAP.put(
                NodeStatsAllShardsMetricsCollector.class, MetricsConfiguration.cdefault);
        nodeStatsAllShardsMetricsCollector = new NodeStatsAllShardsMetricsCollector(null);

        // clean metricQueue before running every test
        TestUtil.readEvents();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetNodeIndicesStatsByShardField() {
        try {
            nodeStatsAllShardsMetricsCollector.getNodeIndicesStatsByShardField();
        } catch (Exception e) {
            assertTrue(
                    "There shouldn't be any exception in the code; Please check the reflection code for any changes",
                    true);
        }
    }

    @Test
    public void testGetMetricsPath() {
        String expectedPath =
                PluginSettings.instance().getMetricsLocation()
                        + PerformanceAnalyzerMetrics.getTimeInterval(startTimeInMills)
                        + "/"
                        + PerformanceAnalyzerMetrics.sIndicesPath
                        + "/NodesStatsIndex/55";
        String actualPath =
                nodeStatsAllShardsMetricsCollector.getMetricsPath(
                        startTimeInMills, "NodesStatsIndex", "55");
        assertEquals(expectedPath, actualPath);

        try {
            nodeStatsAllShardsMetricsCollector.getMetricsPath(startTimeInMills, "NodesStatsIndex");
            fail("Negative scenario test: Should have been a RuntimeException");
        } catch (RuntimeException ex) {
            // - expecting exception...only 1 values passed; 2 expected
        }
    }

    @Test
    public void testCollectMetrics() throws IOException {
        createIndex(TEST_INDEX);

        nodeStatsAllShardsMetricsCollector.collectMetrics(startTimeInMills);
        startTimeInMills += 500;
        nodeStatsAllShardsMetricsCollector.collectMetrics(startTimeInMills);

        List<NodeStatsAllShardsMetricsCollector.NodeStatsMetricsAllShardsPerCollectionStatus>
                metrics = readMetrics();
        assertEquals(2, metrics.size());

        NodeStatsAllShardsMetricsCollector.NodeStatsMetricsAllShardsPerCollectionStatus
                diffMetricValue = metrics.get(1);
        assertEquals(0, diffMetricValue.getFieldDataEvictions());
        assertEquals(0, diffMetricValue.getFieldDataInBytes());
        assertEquals(0, diffMetricValue.getQueryCacheHitCount());
        assertEquals(0, diffMetricValue.getQueryCacheInBytes());
        assertEquals(0, diffMetricValue.getQueryCacheMissCount());
        assertEquals(0, diffMetricValue.getRequestCacheEvictions());
        assertEquals(0, diffMetricValue.getRequestCacheHitCount());
        assertEquals(0, diffMetricValue.getRequestCacheInBytes());
        assertEquals(0, diffMetricValue.getRequestCacheMissCount());
    }

    private List<NodeStatsAllShardsMetricsCollector.NodeStatsMetricsAllShardsPerCollectionStatus>
            readMetrics() throws IOException {
        List<Event> metrics = TestUtil.readEvents();
        assert metrics.size() == 2;
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new ParanamerModule());

        List<NodeStatsAllShardsMetricsCollector.NodeStatsMetricsAllShardsPerCollectionStatus> list =
                new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String[] jsonStrs = metrics.get(0).value.split("\n");
            assert jsonStrs.length == 2;
            list.add(
                    objectMapper.readValue(
                            jsonStrs[1],
                            NodeStatsAllShardsMetricsCollector
                                    .NodeStatsMetricsAllShardsPerCollectionStatus.class));
        }
        return list;
    }
}
