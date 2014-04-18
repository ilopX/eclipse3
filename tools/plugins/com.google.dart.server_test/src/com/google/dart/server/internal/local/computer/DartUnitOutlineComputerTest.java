/*
 * Copyright (c) 2014, the Dart project authors.
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

package com.google.dart.server.internal.local.computer;

import com.google.common.collect.ImmutableMap;
import com.google.dart.engine.source.Source;
import com.google.dart.server.NotificationKind;
import com.google.dart.server.Outline;
import com.google.dart.server.OutlineKind;
import com.google.dart.server.internal.local.AbstractLocalServerTest;

import static org.fest.assertions.Assertions.assertThat;

public class DartUnitOutlineComputerTest extends AbstractLocalServerTest {
  public void test_class() throws Exception {
    String contextId = createContext("test");
    String code = makeSource(//
        "class A {",
        "  int fa, fb;",
        "  String fc;",
        "  A(int i, String s);",
        "  A.name(num p);",
        "  String ma(int pa) => null;",
        "  mb(int pb) => null;",
        "  String get propA => null;",
        "  set propB(int v) {}",
        "}",
        "class B {",
        "  B(int p);",
        "}");
    Source source = addSource(contextId, "/test.dart", code);
    server.subscribe(
        contextId,
        ImmutableMap.of(NotificationKind.OUTLINE, TestListSourceSet.create(source)));
    server.test_waitForWorkerComplete();
    // validate
    Outline unitOutline = serverListener.getOutline(contextId, source);
    Outline[] topOutlines = unitOutline.getChildren();
    assertThat(topOutlines).hasSize(2);
    // A
    {
      Outline outline_A = topOutlines[0];
      assertSame(unitOutline, outline_A.getParent());
      assertSame(OutlineKind.CLASS, outline_A.getKind());
      assertEquals("A", outline_A.getName());
      assertEquals(code.indexOf("A {"), outline_A.getOffset());
      assertEquals(1, outline_A.getLength());
      assertSame(null, outline_A.getArguments());
      assertSame(null, outline_A.getReturnType());
      // A children
      Outline[] outlines_A = outline_A.getChildren();
      assertThat(outlines_A).hasSize(9);
      {
        Outline outline = outlines_A[0];
        assertSame(OutlineKind.FIELD, outline.getKind());
        assertEquals("fa", outline.getName());
        assertNull(outline.getArguments());
        assertEquals("int", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[1];
        assertSame(OutlineKind.FIELD, outline.getKind());
        assertEquals("fb", outline.getName());
        assertNull(outline.getArguments());
        assertEquals("int", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[2];
        assertSame(OutlineKind.FIELD, outline.getKind());
        assertEquals("fc", outline.getName());
        assertNull(outline.getArguments());
        assertEquals("String", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[3];
        assertSame(OutlineKind.CONSTRUCTOR, outline.getKind());
        assertEquals("A", outline.getName());
        assertEquals(code.indexOf("A(int i, String s);"), outline.getOffset());
        assertEquals("A".length(), outline.getLength());
        assertEquals("(int i, String s)", outline.getArguments());
        assertNull(outline.getReturnType());
      }
      {
        Outline outline = outlines_A[4];
        assertSame(OutlineKind.CONSTRUCTOR, outline.getKind());
        assertEquals("A.name", outline.getName());
        assertEquals(code.indexOf("name(num p);"), outline.getOffset());
        assertEquals("name".length(), outline.getLength());
        assertEquals("(num p)", outline.getArguments());
        assertNull(outline.getReturnType());
      }
      {
        Outline outline = outlines_A[5];
        assertSame(OutlineKind.METHOD, outline.getKind());
        assertEquals("ma", outline.getName());
        assertEquals(code.indexOf("ma(int pa) => null;"), outline.getOffset());
        assertEquals("ma".length(), outline.getLength());
        assertEquals("(int pa)", outline.getArguments());
        assertEquals("String", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[6];
        assertSame(OutlineKind.METHOD, outline.getKind());
        assertEquals("mb", outline.getName());
        assertEquals(code.indexOf("mb(int pb) => null;"), outline.getOffset());
        assertEquals("mb".length(), outline.getLength());
        assertEquals("(int pb)", outline.getArguments());
        assertEquals("", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[7];
        assertSame(OutlineKind.GETTER, outline.getKind());
        assertEquals("propA", outline.getName());
        assertEquals(code.indexOf("propA => null;"), outline.getOffset());
        assertEquals("propA".length(), outline.getLength());
        assertEquals("", outline.getArguments());
        assertEquals("String", outline.getReturnType());
      }
      {
        Outline outline = outlines_A[8];
        assertSame(OutlineKind.SETTER, outline.getKind());
        assertEquals("propB", outline.getName());
        assertEquals(code.indexOf("propB(int v) {}"), outline.getOffset());
        assertEquals("propB".length(), outline.getLength());
        assertEquals("(int v)", outline.getArguments());
        assertEquals("", outline.getReturnType());
      }
    }
    // B
    {
      Outline outline_B = topOutlines[1];
      assertSame(unitOutline, outline_B.getParent());
      assertSame(OutlineKind.CLASS, outline_B.getKind());
      assertEquals("B", outline_B.getName());
      assertEquals(code.indexOf("B {"), outline_B.getOffset());
      assertEquals(1, outline_B.getLength());
      assertSame(null, outline_B.getArguments());
      assertSame(null, outline_B.getReturnType());
      // B children
      Outline[] outlines_B = outline_B.getChildren();
      assertThat(outlines_B).hasSize(1);
      {
        Outline outline = outlines_B[0];
        assertSame(OutlineKind.CONSTRUCTOR, outline.getKind());
        assertEquals("B", outline.getName());
        assertEquals(code.indexOf("B(int p);"), outline.getOffset());
        assertEquals("B".length(), outline.getLength());
        assertEquals("(int p)", outline.getArguments());
        assertNull(outline.getReturnType());
      }
    }
  }

  public void test_topLevel() throws Exception {
    String contextId = createContext("test");
    String code = makeSource(//
        "typedef String FTA(int i, String s);",
        "typedef FTB(int p);",
        "class A {}",
        "class B {}",
        "class CTA = A with B;",
        "String fA(int i, String s) => null;",
        "fB(int p) => null;",
        "String get propA => null;",
        "set propB(int v) {}",
        "");
    Source source = addSource(contextId, "/test.dart", code);
    server.subscribe(
        contextId,
        ImmutableMap.of(NotificationKind.OUTLINE, TestListSourceSet.create(source)));
    server.test_waitForWorkerComplete();
    // validate
    Outline unitOutline = serverListener.getOutline(contextId, source);
    Outline[] topOutlines = unitOutline.getChildren();
    assertThat(topOutlines).hasSize(9);
    // FTA
    {
      Outline outline = topOutlines[0];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.FUNCTION_TYPE_ALIAS, outline.getKind());
      assertEquals("FTA", outline.getName());
      assertEquals(code.indexOf("FTA("), outline.getOffset());
      assertEquals("FTA".length(), outline.getLength());
      assertEquals("(int i, String s)", outline.getArguments());
      assertEquals("String", outline.getReturnType());
    }
    // FTB
    {
      Outline outline = topOutlines[1];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.FUNCTION_TYPE_ALIAS, outline.getKind());
      assertEquals("FTB", outline.getName());
      assertEquals(code.indexOf("FTB("), outline.getOffset());
      assertEquals("FTB".length(), outline.getLength());
      assertEquals("(int p)", outline.getArguments());
      assertEquals("", outline.getReturnType());
    }
    // CTA
    {
      Outline outline = topOutlines[4];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.CLASS_TYPE_ALIAS, outline.getKind());
      assertEquals("CTA", outline.getName());
      assertEquals(code.indexOf("CTA ="), outline.getOffset());
      assertEquals("CTA".length(), outline.getLength());
      assertNull(outline.getArguments());
      assertNull(outline.getReturnType());
    }
    // fA
    {
      Outline outline = topOutlines[5];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.FUNCTION, outline.getKind());
      assertEquals("fA", outline.getName());
      assertEquals(code.indexOf("fA("), outline.getOffset());
      assertEquals("fA".length(), outline.getLength());
      assertEquals("(int i, String s)", outline.getArguments());
      assertEquals("String", outline.getReturnType());
    }
    // fB
    {
      Outline outline = topOutlines[6];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.FUNCTION, outline.getKind());
      assertEquals("fB", outline.getName());
      assertEquals(code.indexOf("fB("), outline.getOffset());
      assertEquals("fB".length(), outline.getLength());
      assertEquals("(int p)", outline.getArguments());
      assertEquals("", outline.getReturnType());
    }
    // propA
    {
      Outline outline = topOutlines[7];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.GETTER, outline.getKind());
      assertEquals("propA", outline.getName());
      assertEquals(code.indexOf("propA => null;"), outline.getOffset());
      assertEquals("propA".length(), outline.getLength());
      assertEquals("", outline.getArguments());
      assertEquals("String", outline.getReturnType());
    }
    // propB
    {
      Outline outline = topOutlines[8];
      assertSame(unitOutline, outline.getParent());
      assertSame(OutlineKind.SETTER, outline.getKind());
      assertEquals("propB", outline.getName());
      assertEquals(code.indexOf("propB(int v) {}"), outline.getOffset());
      assertEquals("propB".length(), outline.getLength());
      assertEquals("(int v)", outline.getArguments());
      assertEquals("", outline.getReturnType());
    }
  }
}