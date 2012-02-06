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
package com.google.dart.tools.ui.swtbot.action;

import com.google.dart.tools.ui.swtbot.DartLib;
import com.google.dart.tools.ui.swtbot.Performance;
import com.google.dart.tools.ui.swtbot.Performance.Metric;
import com.google.dart.tools.ui.swtbot.views.ProblemsViewHelper;

import static com.google.dart.tools.ui.swtbot.util.SWTBotUtil.activeShell;
import static com.google.dart.tools.ui.swtbot.util.SWTBotUtil.toolbarDropDownButton;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for launching a Dart application in a browser
 */
public class LaunchBrowserHelper {

  private final SWTWorkbenchBot bot;

  public LaunchBrowserHelper(SWTWorkbenchBot bot) {
    this.bot = bot;
  }

  /**
   * Click the toolbar button to launch a browser based application and wait for the operation to
   * complete.
   * 
   * @param title the name of the application being launched (not <code>null</code>)
   */
  public void launch(final DartLib lib) {
    Performance.waitForResults(bot);
    new ProblemsViewHelper(bot).assertNoProblems();

    final SWTBotShell mainShell = bot.activeShell();

    SWTBotToolbarDropDownButton launchButton = toolbarDropDownButton(bot, "Run.*");
    long start = System.currentTimeMillis();
    launchButton.click();
    Metric metric = Performance.LAUNCH_APP;

    List<String> comments = new ArrayList<String>();
    comments.add(lib.name);
    try {

      // Wait for the main shell to loose focus
      bot.waitUntil(new ICondition() {

        @Override
        public String getFailureMessage() {
          return "Failed to detect launch of " + lib.name;
        }

        @Override
        public void init(SWTBot bot) {
        }

        @Override
        public boolean test() throws Exception {
          return !mainShell.isActive();
        }
      }, Performance.DEFAULT_TIMEOUT_MS);
      SWTBotShell activeShell = activeShell(bot);

      // If progress dialog, then wait for it to close
      if (activeShell != null && activeShell.getText().startsWith("Launching ")) {
        comments.add("progress dialog");
        bot.waitUntil(shellCloses(activeShell), Performance.DEFAULT_TIMEOUT_MS);
        activeShell = activeShell(bot);
      }

      // If a dialog appears, assume error condition
      if (activeShell != null && activeShell != mainShell) {
        String errMsg = "Unexpected dialog " + activeShell.getText();
        comments.add(errMsg);
        throw new RuntimeException(errMsg);
      }

    } finally {
      metric.log(start, comments.toArray(new String[comments.size()]));
    }

    // Ensure main shell has focus
    bot.sleep(100);
    if (!mainShell.isActive()) {
      mainShell.setFocus();
      bot.sleep(100);
    }
  }

}
