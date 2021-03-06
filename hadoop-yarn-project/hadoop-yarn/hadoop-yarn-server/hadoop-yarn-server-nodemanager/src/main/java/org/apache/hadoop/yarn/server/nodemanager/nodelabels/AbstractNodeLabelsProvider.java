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
package org.apache.hadoop.yarn.server.nodemanager.nodelabels;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.NodeLabel;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.nodelabels.CommonNodeLabelsManager;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides base implementation of NodeLabelsProvider with Timer and expects
 * subclass to provide TimerTask which can fetch NodeLabels
 */
public abstract class AbstractNodeLabelsProvider extends NodeLabelsProvider {
  public static final long DISABLE_NODE_LABELS_PROVIDER_FETCH_TIMER = -1;

  // Delay after which timer task are triggered to fetch NodeLabels
  protected long intervalTime;

  // Timer used to schedule node labels fetching
  protected Timer nodeLabelsScheduler;

  public static final String NODE_LABELS_SEPRATOR = ",";

  protected Lock readLock = null;
  protected Lock writeLock = null;

  protected TimerTask timerTask;

  protected Set<NodeLabel> nodeLabels =
      CommonNodeLabelsManager.EMPTY_NODELABEL_SET;

  @VisibleForTesting
  long startTime = 0;

  public AbstractNodeLabelsProvider(String name) {
    super(name);
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    this.intervalTime =
        conf.getLong(YarnConfiguration.NM_NODE_LABELS_PROVIDER_FETCH_INTERVAL_MS,
            YarnConfiguration.DEFAULT_NM_NODE_LABELS_PROVIDER_FETCH_INTERVAL_MS);

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    readLock = readWriteLock.readLock();
    writeLock = readWriteLock.writeLock();
    super.serviceInit(conf);
  }

  @Override
  protected void serviceStart() throws Exception {
    timerTask = createTimerTask();
    if (intervalTime != DISABLE_NODE_LABELS_PROVIDER_FETCH_TIMER) {
      nodeLabelsScheduler =
          new Timer("DistributedNodeLabelsRunner-Timer", true);
      // Start the timer task and then periodically at the configured interval
      // time. Illegal values for intervalTime is handled by timer api
      nodeLabelsScheduler.scheduleAtFixedRate(timerTask, startTime,
          intervalTime);
    }
    super.serviceStart();
  }

  /**
   * terminate the timer
   * @throws Exception
   */
  @Override
  protected void serviceStop() throws Exception {
    if (nodeLabelsScheduler != null) {
      nodeLabelsScheduler.cancel();
    }
    super.serviceStop();
  }

  /**
   * @return Returns output from provider.
   */
  @Override
  public Set<NodeLabel> getNodeLabels() {
    readLock.lock();
    try {
      return nodeLabels;
    } finally {
      readLock.unlock();
    }
  }

  protected void setNodeLabels(Set<NodeLabel> nodeLabelsSet) {
    writeLock.lock();
    try {
      nodeLabels = nodeLabelsSet;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Used only by tests to access the timer task directly
   *
   * @return the timer task
   */
  TimerTask getTimerTask() {
    return timerTask;
  }

  static Set<NodeLabel> convertToNodeLabelSet(Set<String> nodeLabels) {
    if (null == nodeLabels) {
      return null;
    }
    Set<NodeLabel> labels = new HashSet<NodeLabel>();
    for (String label : nodeLabels) {
      labels.add(NodeLabel.newInstance(label));
    }
    return labels;
  }

  public abstract TimerTask createTimerTask();
}
