/*
 * Copyright 2012 Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.dart.tools.debug.core.server;

import java.util.HashMap;
import java.util.Map;

/**
 * The representation of a VM isolate.
 */
public class VmIsolate {
  private int id;
  private boolean paused = true;

  private Map<Integer, VmClass> classInfoMap = new HashMap<Integer, VmClass>();

  protected VmIsolate(int isolateId) {
    this.id = isolateId;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof VmIsolate) {
      VmIsolate isolate = (VmIsolate) other;

      return getId() == isolate.getId();
    }

    return false;
  }

  public VmClass getClassInfo(int classId) {
    return classInfoMap.get(classId);
  }

  public String getClassName(int classId) {
    VmClass vmClass = classInfoMap.get(classId);

    return vmClass == null ? null : vmClass.getName();
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return "isolate-" + getId();
  }

  public boolean hasClassInfo(int classId) {
    return classInfoMap.containsKey(classId);
  }

  @Override
  public int hashCode() {
    return getId();
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean value) {
    paused = value;
  }

  @Override
  public String toString() {
    return "VmIsolate " + getId();
  }

  protected void clearClassInfoMap() {
    classInfoMap.clear();
  }

  protected void setClassInfo(int classId, VmClass vmClass) {
    classInfoMap.put(classId, vmClass);
  }

}
