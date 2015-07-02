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

package org.apache.hadoop.yarn.api.records.impl.pb;

import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.DecreasedContainer;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.proto.YarnProtos.ContainerIdProto;
import org.apache.hadoop.yarn.proto.YarnProtos.DecreasedContainerProto;
import org.apache.hadoop.yarn.proto.YarnProtos.DecreasedContainerProtoOrBuilder;
import org.apache.hadoop.yarn.proto.YarnProtos.ResourceProto;

public class DecreasedContainerPBImpl extends DecreasedContainer {
  DecreasedContainerProto proto = DecreasedContainerProto
      .getDefaultInstance();
  DecreasedContainerProto.Builder builder = null;
  boolean viaProto = false;

  private ContainerId existingContainerId = null;
  private Resource targetCapability = null;

  public DecreasedContainerPBImpl() {
    builder = DecreasedContainerProto.newBuilder();
  }

  public DecreasedContainerPBImpl(DecreasedContainerProto proto) {
    this.proto = proto;
    viaProto = true;
  }

  public DecreasedContainerProto getProto() {
    mergeLocalToProto();
    proto = viaProto ? proto : builder.build();
    viaProto = true;
    return proto;
  }

  @Override
  public ContainerId getContainerId() {
    DecreasedContainerProtoOrBuilder p = viaProto ? proto : builder;
    if (this.existingContainerId != null) {
      return this.existingContainerId;
    }
    if (p.hasContainerId()) {
      this.existingContainerId = convertFromProtoFormat(p.getContainerId());
    }
    return this.existingContainerId;
  }

  @Override
  public void setContainerId(ContainerId existingContainerId) {
    maybeInitBuilder();
    if (existingContainerId == null) {
      builder.clearContainerId();
    }
    this.existingContainerId = existingContainerId;
  }

  @Override
  public Resource getCapability() {
    DecreasedContainerProtoOrBuilder p = viaProto ? proto : builder;
    if (this.targetCapability != null) {
      return this.targetCapability;
    }
    if (p.hasCapability()) {
      this.targetCapability = convertFromProtoFormat(p.getCapability());
    }
    return this.targetCapability;
  }

  @Override
  public void setCapability(Resource targetCapability) {
    maybeInitBuilder();
    if (targetCapability == null) {
      builder.clearCapability();
    }
    this.targetCapability = targetCapability;
  }

  @Override
  public int hashCode() {
    return getProto().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other.getClass().isAssignableFrom(this.getClass())) {
      return this.getProto().equals(this.getClass().cast(other).getProto());
    }
    return false;
  }

  private ContainerIdPBImpl convertFromProtoFormat(ContainerIdProto p) {
    return new ContainerIdPBImpl(p);
  }

  private ContainerIdProto convertToProtoFormat(ContainerId t) {
    return ((ContainerIdPBImpl) t).getProto();
  }

  private Resource convertFromProtoFormat(ResourceProto p) {
    return new ResourcePBImpl(p);
  }

  private ResourceProto convertToProtoFormat(Resource t) {
    return ((ResourcePBImpl) t).getProto();
  }

  private void mergeLocalToProto() {
    if (viaProto) {
      maybeInitBuilder();
    }
    mergeLocalToBuilder();
    proto = builder.build();
    viaProto = true;
  }

  private void maybeInitBuilder() {
    if (viaProto || builder == null) {
      builder = DecreasedContainerProto.newBuilder(proto);
    }
    viaProto = false;
  }

  private void mergeLocalToBuilder() {
    if (this.existingContainerId != null) {
      builder.setContainerId(convertToProtoFormat(this.existingContainerId));
    }
    if (this.targetCapability != null) {
      builder.setCapability(convertToProtoFormat(this.targetCapability));
    }
  }
}
