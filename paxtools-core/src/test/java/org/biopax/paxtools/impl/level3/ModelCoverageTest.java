package org.biopax.paxtools.impl.level3;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO:Class description
 * User: demir
 * Date: Jan 14, 2009
 * Time: 1:11:27 PM
 */
public class ModelCoverageTest
{
    @Test
      public void testCreationMethods()
      {
          Method[] methods = MockFactory.class.getMethods();
          MockFactory factory = new MockFactory();
          for (Method method : methods) {
              if (method.getName().startsWith("create")) {
                  System.out.println("testing " + method);
                  try {
                      method.invoke(factory);
                  } catch (IllegalAccessException e) {
                      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                  } catch (InvocationTargetException e) {
                      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                  }
              }
          }
      }

}