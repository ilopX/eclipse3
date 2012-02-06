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
package com.google.dart.tools.ui.swtbot;

import com.google.dart.tools.core.test.util.FileUtilities;
import com.google.dart.tools.ui.swtbot.action.LaunchBrowserHelper;
import com.google.dart.tools.ui.swtbot.conditions.BuildLibCondition;
import com.google.dart.tools.ui.swtbot.dialog.OpenLibraryHelper;
import com.google.dart.tools.ui.swtbot.util.AntRunner;

import static com.google.dart.tools.ui.swtbot.Performance.prepend;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

/**
 * Represents a Dart library
 */
public class DartLib {
  public static final DartLib CLOCK_SAMPLE = new DartLib("clock", "Clock");
  public static final DartLib ISOLATE_SAMPLE = new DartLib("isolate", "isolate_sample");
  public static final DartLib SLIDER_SAMPLE = new DartLib("slider", "slider_sample");
  public static final DartLib SUNFLOWER_SAMPLE = new DartLib("sunflower", "Sunflower");
  public static final DartLib TIME_SERVER_SAMPLE = new DartLib("time", "time_server");
  public static final DartLib TOTAL_SAMPLE = new DartLib("total", "client/Total".replace('/',
      File.separatorChar));
  public static DartLib[] allSamples;

  private static IPath dartTrunk;
  private static File samplesDir;

  /**
   * Call the Ant script to build the editor "samples" from content in the SVN tree
   */
  public static void buildSamples() throws IOException {
    if (getSamplesDir().exists()) {
      System.out.println("Deleting " + getSamplesDir());
      FileUtilities.delete(getSamplesDir());
    }
    System.out.println("Building " + getSamplesDir());
    AntRunner runner = AntRunner.buildTarget("buildSamples");
    runner.setProperty("build.source.root", getDartTrunk().toOSString());
    runner.setProperty("samples.out.dir", getSamplesDir().getAbsolutePath());
    runner.run();
  }

  /**
   * Answer the samples that ship with Dart Editor
   */
  public static DartLib[] getAllSamples() {
    if (allSamples == null) {
      allSamples = new DartLib[] {
          CLOCK_SAMPLE, ISOLATE_SAMPLE, SLIDER_SAMPLE, SUNFLOWER_SAMPLE, TIME_SERVER_SAMPLE,
          TOTAL_SAMPLE};

      // Assert that all samples are included
      StringBuilder missing = new StringBuilder();
      for (File dir : samplesDir.listFiles()) {
        if (!dir.isDirectory() || dir.getName().equals("libraries")) {
          continue;
        }
        boolean found = false;
        for (DartLib lib : allSamples) {
          if (lib.dir.equals(dir)) {
            found = true;
            break;
          }
        }
        if (!found) {
          missing.append(dir.getName());
          missing.append(", ");
        }
      }
      if (missing.length() > 0) {
        missing.insert(0, "Missing samples: ");
        fail(missing.toString());
      }
    }
    return allSamples;
  }

  /**
   * Answer the SVN root directory
   */
  public static IPath getDartTrunk() {
    if (dartTrunk != null) {
      return dartTrunk;
    }
    String key = "DART_TRUNK";
    String value = System.getenv(key);
    if (value == null) {
      throw new RuntimeException("Please define the environment variable \"" + key
          + "\" that points to the SVN root directory (e.g. /users/me/trunk/dart)");
    }
    System.out.println(key + " = " + value);
    IPath path = new Path(value);
    if (!path.append("editor/docs/README.txt").toFile().exists()) {
      throw new RuntimeException("The " + key
          + " environment variable must point to [svnroot]/dart "
          + "(the parent directory of the Dart Editor source tree)"
          + " as obtained from http://code.google.com/p/dart/wiki/GettingTheSource?tm=4");
    }
    dartTrunk = path;
    return dartTrunk;

  }

  /**
   * Answer the Dart Editor samples directory
   */
  private static File getSamplesDir() {
    if (samplesDir == null) {
      File homeDir = new File(System.getProperty("user.home"));
      File downloadsDir = new File(homeDir, "Downloads");
      File editorDir = new File(downloadsDir, "dart");
      samplesDir = new File(editorDir, "samples");
    }
    return samplesDir;
  }

  public final String name;

  public final File dir;

  public final File dartFile;

  public final File jsFile;

  public SWTBotEclipseEditor editor;

  public DartLib(File dir, String name) {
    this.dir = dir;
    this.name = name;
    this.dartFile = new File(dir, name + ".dart");
    this.jsFile = new File(dir, name + ".dart.js");
  }

  private DartLib(String dirName, String fileName) {
    this(new File(getSamplesDir(), dirName), fileName);
  }

  /**
   * If the library dart file exists, then delete the directory containing it. This is used to
   * ensure a clean test when creating new Dart applications.
   */
  public void deleteDir() {
    if (dartFile.exists()) {
      FileUtilities.delete(dir);
    }
  }

  /**
   * Delete the Javascript file. This is used to ensure a clean test before opening an existing Dart
   * library.
   */
  public void deleteJsFile() {
    if (jsFile.exists()) {
      jsFile.delete();
    }
  }

  /**
   * Wait then log the time for the JS file to be generated, without blocking the current thread.
   */
  public void logFullCompileTime(String... comments) {
    Performance.COMPILE_FULL.logInBackground(new BuildLibCondition(this), prepend(name, comments));
  }

  /**
   * Wait then log the time for the JS file to be generated, without blocking the current thread.
   */
  public void logIncrementalCompileTime(String... comments) {
    Performance.COMPILE_INCREMENTAL.logInBackground(new BuildLibCondition(this),
        prepend(name, comments));
  }

  /**
   * Open the specified sample in the editor then click the launch toolbar button.
   */
  public void openAndLaunch(SWTWorkbenchBot bot) {
    deleteJsFile();
    new OpenLibraryHelper(bot).open(this);
    if (this == TIME_SERVER_SAMPLE) {
      Performance.waitForResults(bot);
    } else {
      new LaunchBrowserHelper(bot).launch(this);
    }
  }
}
