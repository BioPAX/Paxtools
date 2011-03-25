package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * TODO:Class description
 * User: demir
 * Date: Jan 14, 2009
 * Time: 1:11:27 PM
 */
public class ModelCoverageTest
{
    @Test
      public void testCreationMethods() throws InvocationTargetException, IllegalAccessException {
          Method[] methods = MockFactory.class.getMethods();
          MockFactory factory = new MockFactory(BioPAXLevel.L3);
          for (Method method : methods) {
              if (method.getName().startsWith("create")) {
                  System.out.println("testing " + method);
                      method.invoke(factory);
                  }
              }
          
        //test reflectively create
        EditorMap map = new SimpleEditorMap(BioPAXLevel.L3);
        for (Class<? extends BioPAXElement> aClass : map.getKnownSubClassesOf(BioPAXElement.class))
        {
            int i = 0;
            if (!Modifier.isAbstract(aClass.getModifiers()))
            {

                factory.create(aClass, "mock://ModelCoverageTest/id/"+i++);
            }
        }

      }
}