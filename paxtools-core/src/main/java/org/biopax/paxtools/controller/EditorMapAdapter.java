package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.util.AbstractFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * This is the base adapter for all editor maps. A {@link PropertyEditor} is an object that can
 * manipulate a certain property. Editor maps are responsible for initializing these editors and
 * providing them. The default level is the latest official release of BioPAX.
 */
public abstract class EditorMapAdapter implements EditorMap
{

	protected final HashMap<String, Set<PropertyEditor>> propertyToEditorMap =
			new HashMap<String, Set<PropertyEditor>>();

	protected final HashMap<Class<? extends BioPAXElement>, HashMap<String, PropertyEditor>> classToEditorMap =

			new HashMap<Class<? extends BioPAXElement>, HashMap<String, PropertyEditor>>();

	protected final HashMap<Class<? extends BioPAXElement>, HashMap<String, ObjectPropertyEditor>>
			classToInverseEditorMap =
			new HashMap<Class<? extends BioPAXElement>, HashMap<String, ObjectPropertyEditor>>();


	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);


	public Set<PropertyEditor> getEditorsOf(BioPAXElement bpe)
	{
		return (Set<PropertyEditor>) this.classToEditorMap.get(bpe.getModelInterface()).values();
	}

	public Set<ObjectPropertyEditor> getInverseEditorsOf(BioPAXElement bpe)
	{
		return (Set<ObjectPropertyEditor>) this.classToInverseEditorMap.get(bpe.getModelInterface()).values();
	}

	public <D extends BioPAXElement> PropertyEditor<? super D, ?> getEditorForProperty(String property,
	                                                                           Class<D> javaClass)

	{
		PropertyEditor<? super D,?> result = this.classToEditorMap.get(javaClass).get(property);

		if (result == null)
		{
			if (log.isDebugEnabled()) log.debug("Could not locate controller for " + property + " | " + javaClass);
		}

		return result;
	}

	public <D extends BioPAXElement> Set<PropertyEditor<? extends D, ?>> getSubclassEditorsForProperty(
			String property, Class<D> domain)
	{
		return new SubDomainFilterSet<D>(this.getEditorsForProperty(property), domain);

	}

	public Set<PropertyEditor> getEditorsForProperty(String property)
	{
		return this.propertyToEditorMap.get(property);
	}

	public <E extends BioPAXElement> Set<Class<E>> getKnownSubClassesOf(Class<E> javaClass)
	{
		return new SubClassFilterSet(classToEditorMap.keySet(), javaClass);
	}


	protected boolean isInBioPAXNameSpace(String nameSpace)
	{
		return nameSpace != null && nameSpace.startsWith(BioPAXLevel.BP_PREFIX);
	}

	protected PropertyEditor createAndRegisterBeanEditor(String pName, Class javaClass)
	{
		PropertyEditor editor = PropertyEditor.createPropertyEditor(javaClass, pName);

		if (editor != null)
		{
			Set<PropertyEditor> beanEditorsForProperty = this.propertyToEditorMap.get(pName);
			if (beanEditorsForProperty == null)
			{
				beanEditorsForProperty = new HashSet<PropertyEditor>();
			}
			beanEditorsForProperty.add(editor);

			propertyToEditorMap.put(pName, beanEditorsForProperty);
			registerEditorsWithClasses(editor);
		} else
		{
			if (log.isWarnEnabled()) log.warn("property = " + pName + "\njavaClass = " + javaClass);
		}
		return editor;
	}

	protected void registerEditorsWithClasses(PropertyEditor editor)
	{
		for (Class<? extends BioPAXElement> c : classToEditorMap.keySet())
		{
			if (editor.getDomain().isAssignableFrom(c))
			{
				//workaround for participants - can be replaced w/ a general
				// annotation based system. For the time being, I am just handling it
				//as a special case
				if ((editor.getProperty().equals("PARTICIPANTS") && (conversion.class.isAssignableFrom(c) ||
				                                                     control.class.isAssignableFrom(c))) ||
				    (editor.getProperty().equals("participant") && (Conversion.class.isAssignableFrom(c) ||
				                                                    Control.class.isAssignableFrom(c))))
				{
					if (log.isDebugEnabled())
					{
						log.debug("skipping restricted participant property");
					}
				}
				else
				{
					classToEditorMap.get(c).put(editor.getProperty(),editor);
				}
			}

		}

		if (editor instanceof ObjectPropertyEditor && ((ObjectPropertyEditor) editor).hasInverseLink())
		{
			for (Class<? extends BioPAXElement> c : classToInverseEditorMap.keySet())
			{
				if (editor.getRange().isAssignableFrom(c))
				{
					HashMap<String, ObjectPropertyEditor> classMap =
							classToInverseEditorMap.get(c);
					if(classMap.get(editor.getProperty())== null)
					classMap.put(editor.getProperty(), (ObjectPropertyEditor) editor);
					else
						throw new RuntimeException();

				}
			}
		}
	}


	protected void registerModelClass(String localName)
	{
		try
		{
			Class<? extends BioPAXElement> modelInterface = getModelInterface(localName);

			classToEditorMap.put(modelInterface, new HashMap<String,PropertyEditor>());
			classToInverseEditorMap.put(modelInterface, new HashMap<String,ObjectPropertyEditor>());
		}
		catch (IllegalBioPAXArgumentException e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Skipping (" + e.getMessage() + ")");
			}
		}
	}


	private class SubClassFilterSet<E> extends AbstractFilterSet<Class<? extends BioPAXElement>, Class<E>>
	{
		private Class superClass = null;

		public SubClassFilterSet(Set<Class<? extends BioPAXElement>> baseSet, Class<E> superClass)
		{
			super(baseSet);
			this.superClass = superClass;
		}


		@Override public boolean filter(Class<? extends BioPAXElement> subClass)
		{
			return superClass.isAssignableFrom(subClass);
		}
	}
	private class SubDomainFilterSet<D extends BioPAXElement>
		extends AbstractFilterSet<PropertyEditor,PropertyEditor<? extends D,?>>
		{
			private Class<D> domain;

			public SubDomainFilterSet(Set<PropertyEditor> baseSet, Class<D> domain)
			{
				super(baseSet);
				this.domain = domain;
			}
			@Override public boolean filter(PropertyEditor editor)
			{
				return domain.isAssignableFrom(editor.getDomain());
			}
		}

	protected Class<? extends BioPAXElement> getModelInterface(String localName)
	{
		try
		{
			Class modelInterface = Class.forName(this.getLevel().getPackageName() + "." + localName);
			if (BioPAXElement.class.isAssignableFrom(modelInterface))
			{
				return modelInterface;
			} else
			{
				throw new IllegalBioPAXArgumentException(
						"BioPAXElement is not assignable from class:" + modelInterface.getSimpleName());
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalBioPAXArgumentException("Could not locate interface for:" + localName);
		}
	}

}
