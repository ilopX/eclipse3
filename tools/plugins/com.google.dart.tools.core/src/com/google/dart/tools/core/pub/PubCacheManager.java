/*
 * Copyright (c) 2013, the Dart project authors.
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
package com.google.dart.tools.core.pub;

import com.google.dart.tools.core.DartCore;
import com.google.dart.tools.core.utilities.yaml.PubYamlUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a list of the latest versions for all packages installed in the local pub cache.
 * 
 * @coverage dart.tools.core.pub
 */
public class PubCacheManager {

  protected class FillPubCacheList extends Job {

    // Map of <packageName, version no> for packages added
    Map<String, String> packages = null;

    public FillPubCacheList(String name, Map<String, String> packages) {
      super(name);
      this.packages = packages;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IStatus run(IProgressMonitor monitor) {

      String message = getPubCacheList();
      // no errors
      if (message.startsWith("{\"packages")) {
        Map<String, Object> object = null;
        try {
          object = PubYamlUtils.parsePubspecYamlToMap(message);
          synchronized (pubUsedPackages) {
            pubCachePackages = (HashMap<String, Object>) object.get(PubspecConstants.PACKAGES);
          }
        } catch (Exception e) {
          DartCore.logError("Error while parsing pub cache list", e);
        }

        if (packages == null) {
          initializeList();
          if (!pubUsedPackages.isEmpty()) {
            PubManager.getInstance().notifyListeners(getLocalPackages());
          }
        } else {
          Map<String, Object> added = processPackages(packages);
          if (!added.isEmpty()) {
            PubManager.getInstance().notifyListeners(added);
          }
        }
      } else {
        DartCore.logError(message);
      }
      return Status.OK_STATUS;
    }

  }

  /**
   * A map to store the list of the installed packages and their locations, synchronize on
   * pubUsedPackages
   */
  protected HashMap<String, Object> pubCachePackages = new HashMap<String, Object>();

  /**
   * A map of packages & locations used in the open folders in the editor, access should be
   * synchronized against itself
   */
  protected final HashMap<String, Object> pubUsedPackages = new HashMap<String, Object>();

  private static final PubCacheManager INSTANCE = new PubCacheManager();

  public static final PubCacheManager getInstance() {
    return INSTANCE;
  }

  // keeps track of packages last updated
  private Map<String, String> currentPackages;

  public HashMap<String, Object> getLocalPackages() {
    synchronized (pubUsedPackages) {
      HashMap<String, Object> copy = new HashMap<String, Object>(pubUsedPackages);
      return copy;
    }
  }

  public void updatePackagesList(int delay) {
    updatePackagesList(delay, null);
  }

  public void updatePackagesList(int delay, Map<String, String> packages) {
    if (currentPackages == null || !currentPackages.equals(packages)) {
      currentPackages = packages;
      new FillPubCacheList("update installed packages", packages).schedule(delay);
    }
  }

  protected IProject[] getProjects() {
    return ResourcesPlugin.getWorkspace().getRoot().getProjects();
  }

  protected String getPubCacheList() {
    RunPubCacheListJob job = new RunPubCacheListJob();
    return job.run(new NullProgressMonitor()).getMessage();
  }

  protected void processLockFileContents(IResource resource) {
    // Map<packageName,versionNo>
    Map<String, String> versionMap = PubYamlUtils.getPackageVersionMap(resource);
    if (versionMap != null && !versionMap.isEmpty()) {
      synchronized (pubUsedPackages) {
        for (String packageName : versionMap.keySet()) {
          String version = versionMap.get(packageName);
          Map<String, String> versionInfo = getVersionInfo(packageName, version);
          if (versionInfo != null) {
            versionInfo.put(PubspecConstants.VERSION, version);
            pubUsedPackages.put(packageName, versionInfo);
          }
        }
      }
    }
  }

  protected Map<String, Object> processPackages(Map<String, String> packages) {
    // Map<packageName, {{"version",versionNo}{"location",locationPath}}>
    Map<String, Object> added = new HashMap<String, Object>();
    synchronized (pubUsedPackages) {
      Set<String> usedKeySet = pubUsedPackages.keySet();
      for (String packageName : packages.keySet()) {
        if (!usedKeySet.contains(packageName)) {
          String version = packages.get(packageName);
          Map<String, String> versionInfo = getVersionInfo(packageName, version);
          if (versionInfo != null) {
            versionInfo.put(PubspecConstants.VERSION, version);
            pubUsedPackages.put(packageName, versionInfo);
            added.put(packageName, versionInfo);
          }
        }
      }
    }
    return added;
  }

  /**
   * Checks for the location information in the pub cache information for the given package and
   * version
   * 
   * @param packageName - the name of the pub package
   * @param version - the version no of the package
   * @return Map<"location",locationPath> for the package-version
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> getVersionInfo(String packageName, String version) {
    Map<String, Map<String, String>> object = (Map<String, Map<String, String>>) pubCachePackages.get(packageName);
    if (object != null) {
      Map<String, String> versionInfo = object.get(version);
      return versionInfo;
    }
    return null;
  }

  private void initializeList() {
    IProject[] projects = getProjects();
    for (IProject project : projects) {
      try {
        project.accept(new IResourceVisitor() {

          @Override
          public boolean visit(IResource resource) throws CoreException {

            if (resource.getType() != IResource.FILE) {
              return true;
            }
            if (resource.getName().equals(DartCore.PUBSPEC_LOCK_FILE_NAME)) {
              processLockFileContents(resource);
            }
            return false;
          }
        });
      } catch (CoreException e) {
        // do nothing
      }
    }

  }
}
