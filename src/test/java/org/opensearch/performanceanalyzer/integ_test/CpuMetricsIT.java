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

package org.opensearch.performanceanalyzer.integ_test;


import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.performanceanalyzer.http_action.config.RestConfig;
import org.opensearch.performanceanalyzer.integ_test.json.JsonResponseData;
import org.opensearch.performanceanalyzer.integ_test.json.JsonResponseField;
import org.opensearch.performanceanalyzer.integ_test.json.JsonResponseNode;
import org.opensearch.performanceanalyzer.metrics.AllMetrics.OSMetrics;

public class CpuMetricsIT extends MetricCollectorIntegTestBase {

    @Before
    public void init() throws Exception {
        initNodes();
    }

    @Test
    public void checkCPUUtilization() throws Exception {
        checkCPUUtilization(RestConfig.PA_BASE_URI);
    }

    @Test
    public void checkLegacyCPUUtilization() throws Exception {
        checkCPUUtilization(RestConfig.LEGACY_PA_BASE_URI);
    }

    public void checkCPUUtilization(String paBaseUri) throws Exception {
        // read metric from local node
        List<JsonResponseNode> responseNodeList =
                readMetric(paBaseUri + "/metrics/?metrics=CPU_Utilization&agg=sum");
        Assert.assertEquals(1, responseNodeList.size());
        validatePerNodeCPUMetric(responseNodeList.get(0));

        // read metric from all nodes in cluster
        responseNodeList =
                readMetric(paBaseUri + "/metrics/?metrics=CPU_Utilization&agg=sum&nodes=all");
        int nodeNum = getNodeIDs().size();
        Assert.assertEquals(nodeNum, responseNodeList.size());
        for (int i = 0; i < nodeNum; i++) {
            validatePerNodeCPUMetric(responseNodeList.get(i));
        }
    }

    /**
     * check if cpu usage is non zero { "JtlEoRowSI6iNpzpjlbp_Q": { "data": { "fields": [ { "name":
     * "CPU_Utilization", "type": "DOUBLE" } ], "records": [ [ 0.005275218803760752 ] ] },
     * "timestamp": 1606861740000 } }
     */
    private void validatePerNodeCPUMetric(JsonResponseNode responseNode) throws Exception {
        Assert.assertTrue(responseNode.getTimestamp() > 0);
        JsonResponseData responseData = responseNode.getData();
        Assert.assertEquals(1, responseData.getFieldDimensionSize());
        Assert.assertEquals(
                OSMetrics.CPU_UTILIZATION.toString(), responseData.getField(0).getName());
        Assert.assertEquals(
                JsonResponseField.Type.Constants.DOUBLE, responseData.getField(0).getType());
        Assert.assertEquals(1, responseData.getRecordSize());
        Assert.assertTrue(
                responseData.getRecordAsDouble(0, OSMetrics.CPU_UTILIZATION.toString()) > 0);
    }
}
