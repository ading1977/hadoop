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

import org.apache.hadoop.security.proto.SecurityProtos.TokenProto;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.IncreasedContainer;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.Token;
import org.apache.hadoop.yarn.proto.YarnProtos.ContainerIdProto;
import org.apache.hadoop.yarn.proto.YarnProtos.IncreasedContainerProto;
import org.apache.hadoop.yarn.proto.YarnProtos.IncreasedContainerProtoOrBuilder;
import org.apache.hadoop.yarn.proto.YarnProtos.ResourceProto;

public class IncreasedContainerPBImpl extends IncreasedContainer {
  IncreasedContainerProto proto = IncreasedContainerProto
      .getDefaultInstance();
  IncreasedContainerProto.Builder builder = null;
  boolean viaProto = false;

  private ContainerId existingContainerId = null;
  private Resource targetCapability = null;
  private Token token = null;

  public IncreasedContainerPBImpl() {
    builder = IncreasedContainerProto.newBuilder();
  }

  public IncreasedContainerPBImpl(IncreasedContainerProto proto) {
    this.proto = proto;
    viaProto = true;
  }

  public IncreasedContainerProto getProto() {
    mergeLocalToProto();
    proto = viaProto ? proto : builder.build();
    viaProto = true;
    return proto;
  }

  @Override
  public ContainerId getContainerId() {
    IncreasedContainerProtoOrBuilder p = viaProto ? proto : builder;
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
    IncreasedContainerProtoOrBuilder p = viaProto ? proto : builder;
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
  public Token getContainerToken() {
    IncreasedContainerProtoOrBuilder p = viaProto ? proto : builder;
    if (this.token != null) {
      return this.token;
    }
    if (p.hasContainerToken()) {
      this.token = convertFromProtoFormat(p.getContainerToken());
    }
    return this.token;
  }

  @Override
  public void setContainerToken(Token token) {
    maybeInitBuilder();
    if (token == null) {
      builder.clearContainerToken();
    }
    this.token = token;
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

  private Token convertFromProtoFormat(TokenProto p) {
    return new TokenPBImpl(p);
  }

  private TokenProto convertToProtoFormat(Token t) {
    return ((TokenPBImpl) t).getProto();
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
      builder = IncreasedContainerProto.newBuilder(proto);
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
    if (this.token != null) {
      builder.setContainerToken(convertToProtoFormat(this.token));
    }
  }
}
