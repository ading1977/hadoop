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

package org.apache.hadoop.yarn.api.protocolrecords;

import java.util.List;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.yarn.api.ContainerManagementProtocol;
import org.apache.hadoop.yarn.api.records.NMToken;
import org.apache.hadoop.yarn.api.records.Token;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>The request sent by <code>Application Master</code> to the
 * <code>Node Manager</code> to change the resource quota of a container.</p>
 *
 * @see ContainerManagementProtocol#increaseContainersResource(IncreaseContainersResourceRequest)
 */
@Public
@Stable
public abstract class IncreaseContainersResourceRequest {
  @Public
  @Stable
  public static IncreaseContainersResourceRequest newInstance(
      List<Token> containersToIncrease) {
    IncreaseContainersResourceRequest request =
        Records.newRecord(IncreaseContainersResourceRequest.class);
    request.setContainersToIncrease(containersToIncrease);
    return request;
  }

  /**
   * Get a list of container tokens to be used for authorization during
   * container resource increase.
   * <p>
   * Note: {@link NMToken} will be used for authenticating communication with
   * {@code NodeManager}.
   * @return the list of container tokens to be used for authorization during
   * container resource increase.
   * @see NMToken
   */
  @Public
  @Stable
  public abstract List<Token> getContainersToIncrease();

  /**
   * Set container tokens to be used during container resource increase.
   * The token is acquired from
   * <code>AllocateResponse.getIncreasedContainers</code>.
   * The token contains the container id and resource capability required for
   * container resource increase.
   * @param containersToIncrease the list of container tokens to be used
   *                             for container resource increase.
   */
  @Public
  @Stable
  public abstract void setContainersToIncrease(
      List<Token> containersToIncrease);
}
