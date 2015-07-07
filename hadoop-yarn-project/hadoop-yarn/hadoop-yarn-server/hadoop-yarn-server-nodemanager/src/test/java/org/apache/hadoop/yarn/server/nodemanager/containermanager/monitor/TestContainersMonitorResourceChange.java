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

package org.apache.hadoop.yarn.server.nodemanager.containermanager.monitor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.server.nodemanager.ContainerExecutor;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.ContainerEvent;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.ContainerEventType;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.monitor.ContainersMonitorImpl.ProcessTreeInfo;
import org.apache.hadoop.yarn.server.nodemanager.executor.ContainerLivenessContext;
import org.apache.hadoop.yarn.server.nodemanager.executor.ContainerSignalContext;
import org.apache.hadoop.yarn.server.nodemanager.executor.ContainerStartContext;
import org.apache.hadoop.yarn.server.nodemanager.executor.DeletionAsUserContext;
import org.apache.hadoop.yarn.server.nodemanager.executor.LocalizerStartContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestContainersMonitorResourceChange {

  private ContainersMonitorImpl containersMonitor;
  private MockExecutor executor;
  private Configuration conf;
  private AsyncDispatcher dispatcher;
  private MockContainerEventHandler containerEventHandler;

  private static class MockExecutor extends ContainerExecutor {
    @Override
    public void init() throws IOException {
    }
    @Override
    public void startLocalizer(LocalizerStartContext ctx)
        throws IOException, InterruptedException {
    }
    @Override
    public int launchContainer(ContainerStartContext ctx) throws
        IOException {
      return 0;
    }
    @Override
    public boolean signalContainer(ContainerSignalContext ctx)
        throws IOException {
      return true;
    }
    @Override
    public void deleteAsUser(DeletionAsUserContext ctx)
        throws IOException, InterruptedException {
    }
    @Override
    public String getProcessId(ContainerId containerId) {
      return String.valueOf(containerId.getContainerId());
    }
    @Override
    public boolean isContainerProcessAlive(ContainerLivenessContext ctx)
        throws IOException {
      return true;
    }
  }

  private static class MockContainerEventHandler implements
      EventHandler<ContainerEvent> {
    final private Set<ContainerId> killedContainer
        = new HashSet<>();
    @Override
    public void handle(ContainerEvent event) {
      if (event.getType() == ContainerEventType.KILL_CONTAINER) {
        synchronized (killedContainer) {
          killedContainer.add(event.getContainerID());
        }
      }
    }
    public boolean isContainerKilled(ContainerId containerId) {
      synchronized (killedContainer) {
        return killedContainer.contains(containerId);
      }
    }
  }

  @Before
  public void setup() {
    executor = new MockExecutor();
    dispatcher = new AsyncDispatcher();
    conf = new Configuration();
    conf.set(
        YarnConfiguration.NM_CONTAINER_MON_RESOURCE_CALCULATOR,
        MockResourceCalculatorPlugin.class.getCanonicalName());
    conf.set(
        YarnConfiguration.NM_CONTAINER_MON_PROCESS_TREE,
        MockResourceCalculatorProcessTree.class.getCanonicalName());
    dispatcher.init(conf);
    dispatcher.start();
    containerEventHandler = new MockContainerEventHandler();
    dispatcher.register(ContainerEventType.class, containerEventHandler);
  }

  @After
  public void tearDown() throws Exception {
    if (containersMonitor != null) {
      containersMonitor.stop();
    }
    if (dispatcher != null) {
      dispatcher.stop();
    }
  }

  @Test
  public void testContainersResourceChange() throws Exception {
    // set container monitor interval to be 20ms
    conf.setLong(YarnConfiguration.NM_CONTAINER_MON_INTERVAL_MS, 20L);
    containersMonitor = createContainersMonitor(executor, dispatcher);
    containersMonitor.init(conf);
    containersMonitor.start();
    // create container 1
    containersMonitor.handle(new ContainerStartMonitoringEvent(
        getContainerId(1), 2100L, 1000L, 1, 0, 0));
    // verify that this container is properly tracked
    Thread.sleep(200);
    assertNotNull(getProcessTreeInfo(getContainerId(1)));
    assertEquals(1000L, getProcessTreeInfo(getContainerId(1))
        .getPmemLimit());
    assertEquals(2100L, getProcessTreeInfo(getContainerId(1))
        .getVmemLimit());
    // increase pmem usage, the container should be killed
    MockResourceCalculatorProcessTree mockTree =
        (MockResourceCalculatorProcessTree) getProcessTreeInfo(
            getContainerId(1)).getProcessTree();
    mockTree.setRssMemorySize(2500L);
    // verify that this container is killed
    Thread.sleep(200);
    assertTrue(containerEventHandler
        .isContainerKilled(getContainerId(1)));
    // create container 2
    containersMonitor.handle(new ContainerStartMonitoringEvent(
        getContainerId(2), 2100L, 1000L, 1, 0, 0));
    // verify that this container is properly tracked
    Thread.sleep(200);
    assertNotNull(getProcessTreeInfo(getContainerId(2)));
    assertEquals(1000L, getProcessTreeInfo(getContainerId(2))
        .getPmemLimit());
    assertEquals(2100L, getProcessTreeInfo(getContainerId(2))
        .getVmemLimit());
    // trigger a change resource event, check limit after change
    containersMonitor.handle(new ChangeMonitoringContainerResourceEvent(
        getContainerId(2), 4200L, 2000L, 1));
    Thread.sleep(200);
    assertNotNull(getProcessTreeInfo(getContainerId(2)));
    assertEquals(2000L, getProcessTreeInfo(getContainerId(2))
        .getPmemLimit());
    assertEquals(4200L, getProcessTreeInfo(getContainerId(2))
        .getVmemLimit());
    // increase pmem usage, the container should NOT be killed
    mockTree =
        (MockResourceCalculatorProcessTree) getProcessTreeInfo(
            getContainerId(2)).getProcessTree();
    mockTree.setRssMemorySize(2500L);
    // verify that this container is not killed
    Thread.sleep(200);
    assertFalse(containerEventHandler
        .isContainerKilled(getContainerId(2)));
    containersMonitor.stop();
  }

  @Test
  public void testContainersResourceChangeIsTriggeredImmediately()
      throws Exception {
    // set container monitor interval to be 20s
    conf.setLong(YarnConfiguration.NM_CONTAINER_MON_INTERVAL_MS, 20000L);
    containersMonitor = createContainersMonitor(executor, dispatcher);
    containersMonitor.init(conf);
    containersMonitor.start();
    // sleep 1 second to make sure the container monitor thread is
    // now waiting for the next monitor cycle
    Thread.sleep(1000);
    // create a container with id 3
    containersMonitor.handle(new ContainerStartMonitoringEvent(
        getContainerId(3), 2100L, 1000L, 1, 0, 0));
    // sleep another second, and verify that this container still has not
    // been tracked because the container monitor thread is still waiting
    Thread.sleep(1000);
    assertNull(getProcessTreeInfo(getContainerId(3)));
    // trigger a change resource event, check limit after change
    containersMonitor.handle(new ChangeMonitoringContainerResourceEvent(
        getContainerId(3), 4200L, 2000L, 1));
    Thread.sleep(200);
    // verify that this container has been properly tracked with the
    // correct size
    assertNotNull(getProcessTreeInfo(getContainerId(3)));
    assertEquals(2000L, getProcessTreeInfo(getContainerId(3))
        .getPmemLimit());
    assertEquals(4200L, getProcessTreeInfo(getContainerId(3))
        .getVmemLimit());
    containersMonitor.stop();
  }

  private ContainersMonitorImpl createContainersMonitor(
      ContainerExecutor containerExecutor, AsyncDispatcher dispatcher) {
    return new ContainersMonitorImpl(containerExecutor, dispatcher, null);
  }

  private ContainerId getContainerId(int id) {
    return ContainerId.newContainerId(ApplicationAttemptId.newInstance(
        ApplicationId.newInstance(123456L, 1), 1), id);
  }

  private ProcessTreeInfo getProcessTreeInfo(ContainerId id) {
    return containersMonitor.trackingContainers.get(id);
  }
}
