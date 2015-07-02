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

package org.apache.hadoop.yarn.api.records;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ApplicationMasterProtocol;
import org.apache.hadoop.yarn.api.ContainerManagementProtocol;
import org.apache.hadoop.yarn.util.Records;

/**
 * {@code DecreasedContainer} represents a running container whose
 * allocated resource has been decreased.
 * <p>
 * The {@code ResourceManager} is the sole authority to decrease
 * resource to any running {@code Container} in an application.
 * <p>
 * It includes details such as:
 * <ul>
 *   <li>{@link ContainerId} for the container.</li>
 *   <li>{@link Resource} allocated to the container.</li>
 * </ul>
 * <p>
 * An {@code ApplicationMaster} receives the {@code DecreasedContainer}
 * from the {@code ResourceManager} during resource-negotiation.
 * After this, the {@code ApplicationMaster} is expected to call
 * {@code ContainerManager.getContainerStatuses} to confirm whether a container
 * resource decrease has been completed in {@code NodeManager}.
 *
 * @see ApplicationMasterProtocol#allocate(AllocateRequest)
 * @see ContainerManagementProtocol#getContainerStatuses(GetContainerStatusesRequest)
 */
public abstract class DecreasedContainer {

  @Private
  @Unstable
  public static DecreasedContainer newInstance(
      ContainerId existingContainerId, Resource targetCapability) {
    DecreasedContainer context = Records
        .newRecord(DecreasedContainer.class);
    context.setContainerId(existingContainerId);
    context.setCapability(targetCapability);
    return context;
  }

  /**
   * Get the <code>ContainerId</code> of the container.
   * @return <code>ContainerId</code> of the container
   */
  @Public
  @Stable
  public abstract ContainerId getContainerId();

  @Private
  @Unstable
  public abstract void setContainerId(ContainerId containerId);

  /**
   * Get the <code>Resource</code> allocated to the container.
   * @return <code>Resource</code> allocated to the container
   */
  @Public
  @Stable
  public abstract Resource getCapability();

  @Private
  @Unstable
  public abstract void setCapability(Resource capability);
}
