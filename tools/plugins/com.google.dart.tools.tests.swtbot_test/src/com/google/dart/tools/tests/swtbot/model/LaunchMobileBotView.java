/*
 * Copyright 2014 Dart project authors.
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
package com.google.dart.tools.tests.swtbot.model;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;

public class LaunchMobileBotView extends LaunchBrowserBasedBotView {

  public LaunchMobileBotView(SWTWorkbenchBot bot, SWTBot shellBot) {
    super(bot, shellBot);
  }

  /**
   * Select USB debugging.
   */
  public void useUSB() {
    shellBot.comboBox().setSelection(1);
  }

  /**
   * Select WIFI debugging.
   */
  public void useWifi() {
    shellBot.comboBox().setSelection(0);
  }

  @Override
  protected String viewName() {
    return "Launch Mobile";
  }
}
