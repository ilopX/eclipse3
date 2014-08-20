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

package com.google.dart.engine.internal.type;

import com.google.dart.engine.EngineTestCase;
import com.google.dart.engine.element.ClassElement;
import com.google.dart.engine.type.InterfaceType;
import com.google.dart.engine.type.Type;
import com.google.dart.engine.type.UnionType;

import static com.google.dart.engine.element.ElementFactory.classElement;

import junit.framework.AssertionFailedError;

import java.util.Set;

public class UnionTypeImplTest extends EngineTestCase {

  private ClassElement classA = classElement("A");
  private InterfaceType typeA = classA.getType();
  private ClassElement classB = classElement("B", typeA);
  private InterfaceType typeB = classB.getType();

  private Type uA = UnionTypeImpl.union(typeA);
  private Type uB = UnionTypeImpl.union(typeB);
  private Type uAB = UnionTypeImpl.union(typeA, typeB);
  private Type uBA = UnionTypeImpl.union(typeB, typeA);
  private Type[] us = {uA, uB, uAB, uBA};

  public void fail_isSubtypeOf_element() {
    // Elements of union are sub types
    assertTrue(typeA.isSubtypeOf(uAB));
    assertTrue(typeB.isSubtypeOf(uAB));
  }

  public void fail_isSubtypeOf_reflexivity() {
    for (Type u : us) {
      assertTrue(u.isSubtypeOf(u));
    }
  }

  public void fail_toString_pair() {
    Type u = UnionTypeImpl.union(typeA, typeB);
    String s = u.toString();
    assertTrue(s.equals("{A,B}") || s.equals("{B,A}"));
  }

  public void test_emptyUnionsNotAllowed() {
    try {
      UnionTypeImpl.union();
    } catch (IllegalArgumentException e) {
      return;
    }
    throw new AssertionFailedError("Expected illegal argument exception.");
  }

  public void test_equality_beingASubtypeOfAnElementIsNotSufficient() {
    // Non-equal if some elements are different
    assertFalse(uAB.equals(uA));
  }

  public void test_equality_insertionOrderDoesntMatter() {
    // Insertion order doesn't matter, only sets of elements
    assertTrue(uAB.equals(uBA));
    assertTrue(uBA.equals(uAB));
  }

  public void test_equality_reflexivity() {
    for (Type u : us) {
      assertTrue(u.equals(u));
    }
  }

  public void test_equality_singletonsCollapse() {
    assertTrue(typeA.equals(uA));
    assertTrue(uA.equals(typeA));
  }

  public void test_isSubtypeOf_allElementsOnLHSAreSubtypesOfSomeElementOnRHS() {
    // Unions are subtypes when all elements are subtypes
    assertTrue(uAB.isSubtypeOf(uA));
    assertTrue(uAB.isSubtypeOf(typeA));
  }

  public void test_isSubtypeOf_notSubtypeOfAnyElement() {
    // Types that are not subtypes of elements are not subtypes
    assertFalse(typeA.isSubtypeOf(uB));
  }

  // This tests the more strict (less unsound) subtype semantics for union types.
  // It will break if we change to the less strict definition of subtyping.
  public void test_isSubtypeOf_someElementOnLHSIsNotASubtypeOfAnyElementOnRHS() {
    // Unions are not subtypes when some element is not a subtype
    assertFalse(uAB.isSubtypeOf(uB));
    assertFalse(uAB.isSubtypeOf(typeB));
  }

  public void test_isSubtypeOf_subtypeOfSomeElement() {
    // Subtypes of elements are sub types
    assertTrue(typeB.isSubtypeOf(uA));
  }

  public void test_nestedUnionsCollapse() {
    UnionType u = (UnionType) UnionTypeImpl.union(uAB, typeA);
    for (Type t : u.getElements()) {
      if (t instanceof UnionType) {
        throw new AssertionFailedError("Expected only non-union types but found " + t + "!");
      }
    }
  }

  public void test_noLossage() {
    UnionType u = (UnionType) UnionTypeImpl.union(typeA, typeB, typeB, typeA, typeB, typeB);
    Set<Type> elements = u.getElements();
    assertTrue(elements.contains(typeA));
    assertTrue(elements.contains(typeB));
    assertTrue(elements.size() == 2);
  }

  public void test_toString_singleton() {
    // Singleton unions collapse to the the single type.
    assertEquals("A", uA.toString());
  }
}
