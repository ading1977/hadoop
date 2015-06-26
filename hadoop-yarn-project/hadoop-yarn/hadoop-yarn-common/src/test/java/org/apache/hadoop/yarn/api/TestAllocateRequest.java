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

import org.apache.hadoop.yarn.api.records.ContainerResourceChangeRequest;
import org.junit.Assert;

import org.apache.hadoop.yarn.api.protocolrecords.AllocateRequest;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.AllocateRequestPBImpl;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.proto.YarnServiceProtos.AllocateRequestProto;
import org.junit.Test;

public class TestAllocateRequest {
  @Test
  public void testAllcoateRequestWithIncreaseAndDecrease() {
    List<ContainerResourceChangeRequest> incRequests =
        new ArrayList<ContainerResourceChangeRequest>();
    List<ContainerResourceChangeRequest> decRequests =
        new ArrayList<ContainerResourceChangeRequest>();
    for (int i = 0; i < 3; i++) {
      incRequests.add(ContainerResourceChangeRequest.newInstance(null,
              Resource.newInstance(0, i)));
    }
    for (int i = 0; i < 3; i++) {
      decRequests.add(ContainerResourceChangeRequest.newInstance(null,
              Resource.newInstance(0, i)));
    }
    AllocateRequest r =
        AllocateRequest.newInstance(123, 0f, null, null, null,
                incRequests, decRequests);

    // serde
    AllocateRequestProto p = ((AllocateRequestPBImpl) r).getProto();
    r = new AllocateRequestPBImpl(p);

    // check value
    Assert.assertEquals(123, r.getResponseId());
    Assert.assertEquals(incRequests.size(), r.getIncreaseRequests().size());
    Assert.assertEquals(decRequests.size(), r.getDecreaseRequests().size());

    for (int i = 0; i < incRequests.size(); i++) {
      Assert.assertEquals(r.getIncreaseRequests().get(i).getCapability()
          .getVirtualCores(), incRequests.get(i).getCapability()
          .getVirtualCores());
    }
    for (int i = 0; i < decRequests.size(); i++) {
      Assert.assertEquals(r.getDecreaseRequests().get(i).getCapability()
              .getVirtualCores(), decRequests.get(i).getCapability()
              .getVirtualCores());
    }
  }

  @Test
  public void testAllcoateRequestWithoutIncreaseAndDecrease() {
    AllocateRequest r =
        AllocateRequest.newInstance(123, 0f, null, null, null, null, null);

    // serde
    AllocateRequestProto p = ((AllocateRequestPBImpl) r).getProto();
    r = new AllocateRequestPBImpl(p);

    // check value
    Assert.assertEquals(123, r.getResponseId());
    Assert.assertEquals(0, r.getIncreaseRequests().size());
  }
}
