package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * User: demir Date: Oct 13, 2007 Time: 10:15:55 PM
 */
public abstract class BioPAXFactoryImpl implements BioPAXFactory
{
	
    private final MethodMapHelper helper;

	protected BioPAXFactoryImpl()
	{
			helper = new MethodMapHelper(this.getClass().getMethods());
	}


	@SuppressWarnings("unchecked")
	public <T extends BioPAXElement> T reflectivelyCreate(Class<T> c)
	{
		Method method = helper.methodsByClass.get(c);
		if (method == null)
		{
			throw new IllegalBioPAXArgumentException(
				"No creation methods for class" + c);
		}
		T invokeCreation = (T)invokeCreation(method);
		return invokeCreation;
	}

	public boolean canInstantiate(String name)
	{
		return helper.methodsByName.get(name) != null;
	}

	public BioPAXElement reflectivelyCreate(String name)
	{
		Method method = helper.methodsByName.get(name);

		if (method == null)
		{
			throw new IllegalBioPAXArgumentException(
				"No creation methods for name: " + name);
		}

		return invokeCreation(method);
	}

	private BioPAXElement invokeCreation(Method method)
	{
		try
		{
			return ((BioPAXElement) method.invoke(this));
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalBioPAXArgumentException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalBioPAXArgumentException(e);
		}
	}

	private class MethodMapHelper
	{
		private final HashMap<Class<? extends BioPAXElement>, Method> methodsByClass;
		private final HashMap<String, Method> methodsByName;


		public MethodMapHelper(Method[] methods)
		{
			methodsByClass =
				new HashMap<Class<? extends BioPAXElement>, Method>();
			methodsByName = new HashMap<String, Method>();

			for (Method method : methods)
			{
				if (method.getName().startsWith("create"))
				{
					Class<? extends BioPAXElement> clazz =
						(Class<? extends BioPAXElement>) method.getReturnType();
					methodsByClass.put(clazz, method);
					String s = clazz.getName();
					methodsByName
						.put(s.substring(s.lastIndexOf('.') + 1), method);
				}
			}
		}
	}
	public Model createModel()
	{
		return new ModelImpl(this);
	}
}
