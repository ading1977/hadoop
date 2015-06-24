/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.api;

import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.yarn.api.protocolrecords.IncreaseContainersResourceRequest;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.IncreaseContainersResourceRequestPBImpl;
import org.apache.hadoop.yarn.api.records.Token;
import org.apache.hadoop.yarn.proto.YarnServiceProtos.IncreaseContainersResourceRequestProto;
import org.junit.Assert;
import org.junit.Test;

public class TestIncreaseContainersResourceRequest {

  @Test
  public void testIncreaseContainersResourceRequest() {
    List<Token> containerToIncrease = new ArrayList<Token>();
    for (int i = 0; i < 10; i++) {
      Token ctx =
              Token.newInstance("identifier".getBytes(), "simple",
                      "passwd".getBytes(), "service");
      containerToIncrease.add(ctx);
    }
    IncreaseContainersResourceRequest request =
            IncreaseContainersResourceRequest.newInstance(containerToIncrease
            );
    IncreaseContainersResourceRequestProto proto =
            ((IncreaseContainersResourceRequestPBImpl) request).getProto();
    IncreaseContainersResourceRequest requestFromProto =
            new IncreaseContainersResourceRequestPBImpl(proto);
    // verify the whole record equals with original record
    Assert.assertEquals(requestFromProto, request);
    Assert.assertEquals(requestFromProto.getContainersToIncrease().size(),
            containerToIncrease.size());
  }

  @Test
  public void testIncreaseContainersResourceRequestWithNull() {
    IncreaseContainersResourceRequest request =
            IncreaseContainersResourceRequest.newInstance(null);
    IncreaseContainersResourceRequestProto proto =
            ((IncreaseContainersResourceRequestPBImpl) request).getProto();
    request = new IncreaseContainersResourceRequestPBImpl(proto);
    // check value
    Assert.assertEquals(0, request.getContainersToIncrease().size());
  }
}
