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

    /**
     * This method will create and return a new instance 
     * of the given class (rdfid is not set!).
     * If the class is not defined in the API, it will throw an
     * exception.
     * @param aClass a BioPAX model interface
     * @return a new instance of the class as defined by BioPAX ontology
     */
	@SuppressWarnings("unchecked")
	protected <T extends BioPAXElement> T reflectivelyCreate(Class<T> c)
	{
		Method method = helper.methodsByClass.get(c);
		if (method == null)
		{
			throw new IllegalBioPAXArgumentException(
				"No creation methods for class" + c);
		}
		T bpe = (T)invokeCreation(method);
		return bpe;
	}

	
    /**
     * Factory method to set the element's RDFId via reflection.
     * 
     * Caution: for most "normal" uses, RDFId should not change once it's set! 
     * Consider using, e.g., {@link Model#addNew(Class, String)} or 
     * {@link Model#reflectivelyCreate(String, String)} method instead
     * to create new elements, then - copy/replace an element with another one
     * in the model if required, etc. (not modifying original IDs). A BioPAX 
     * element can be persistent, member of more than one models, attached to 
     * other (parent) elements' property, etc., 
     * so be warned and use this method with care!
     * 
     * @param bpe
     * @param uri
     */
	protected void setId(BioPAXElement bpe, String uri) 
	{
		try {
			Method method = BioPAXElementImpl.class
				.getDeclaredMethod("setRDFId", String.class);
			method.setAccessible(true);
			method.invoke(bpe, uri);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public boolean canInstantiate(String name)
	{
		return helper.methodsByName.get(name) != null;
	}

	public BioPAXElement reflectivelyCreate(String name, String id)
	{
		Method method = helper.methodsByName.get(name);

		if (method == null)
		{
			throw new IllegalBioPAXArgumentException(
				"No creation methods for name: " + name);
		}

		BioPAXElement e =  invokeCreation(method);
		setId(e, id);
		return e;
	}

	
	public <T extends BioPAXElement> T reflectivelyCreate(Class<T> aClass,
			String uri) 
	{
		T bpe = reflectivelyCreate(aClass);
		setId(bpe, uri);
		return bpe;
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
					methodsByName.put(s.substring(s.lastIndexOf('.') + 1), method);
				}
			}
		}
	}
	
	public Model createModel()
	{
		return new ModelImpl(this);
	}
}
