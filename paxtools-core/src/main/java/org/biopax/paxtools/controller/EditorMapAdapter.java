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

	protected final HashMap<Class<? extends BioPAXElement>, Set<PropertyEditor>> classToEditorMap =
			new HashMap<Class<? extends BioPAXElement>, Set<PropertyEditor>>();

	protected final BioPAXLevel level;

	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);

	public EditorMapAdapter(BioPAXLevel level)  //todo : change default to level 3
	{
		this.level = level != null ? level : BioPAXLevel.L2;
	}

	public EditorMapAdapter()
	{
		this(null);
	}

	public Set<PropertyEditor> getEditorsOf(BioPAXElement bpe)
	{
		return this.classToEditorMap.get(bpe.getModelInterface());
	}

	public PropertyEditor getEditorForProperty(String property, Class javaClass)

	{
		PropertyEditor result = this.ifExistsGetEditorForProperty(property, javaClass);
		if (result == null)
		{
			log.error(
					"Could not locate controller for " + property + " | " +
					javaClass);
		}

		return result;
	}

	protected PropertyEditor ifExistsGetEditorForProperty(String property, Class javaClass)
	{
		PropertyEditor result = null;
		Set<PropertyEditor> editors = this.getEditorsForProperty(property);
		if (editors != null)
		{
			for (PropertyEditor editor : editors)
			{
				if (editor.getDomain().isAssignableFrom(javaClass))
				{
					if (result == null)
					{
						result = editor;
					}
					else if (editor.getDomain().isAssignableFrom(result.getDomain()))
					{
						result = editor;
					}
					else
					{
						assert result.getDomain().isAssignableFrom(editor.getDomain());
					}
				}
			}
		}
		return result;

	}

	public Set<PropertyEditor> getEditorsForProperty(String property)
	{
		return this.propertyToEditorMap.get(property);
	}

	public Set<Class> getKnownSubClassesOf(Class javaClass)
	{
		return new SubClassFilterSet(classToEditorMap.keySet(), javaClass);
	}

	public BioPAXLevel getLevel()
	{
		return level;
	}

	protected boolean isInBioPAXNameSpace(String nameSpace)
	{
		return nameSpace != null && nameSpace.startsWith(BioPAXLevel.BP_PREFIX);
	}

	protected PropertyEditor createAndRegisterBeanEditor(String pName,
	                                                     Class javaClass)
	{
		PropertyEditor editor =
				PropertyEditor.createPropertyEditor(javaClass, pName);

		if (editor != null)
		{
			Set<PropertyEditor> beanEditorsForProperty =
					this.propertyToEditorMap.get(pName);
			if (beanEditorsForProperty == null)
			{
				beanEditorsForProperty = new HashSet<PropertyEditor>();
			}
			beanEditorsForProperty.add(editor);

			propertyToEditorMap.put(pName, beanEditorsForProperty);
			registerEditorsWithClasses(editor);
		}
		else
		{
			log.warn("property = " + pName + "\njavaClass = " + javaClass);
		}
		return editor;
	}

	protected void registerEditorsWithClasses(PropertyEditor editor)
	{

		for (Class<? extends BioPAXElement> c : classToEditorMap
				.keySet())
		{
			if (editor.getDomain().isAssignableFrom(c))
			{
				//workaround for participants - can be replaced w/ a general
				// annotation based system. For the time being, I am just handling it
				//as a special case
				if (
						(
								editor.getProperty().equals("PARTICIPANTS") &&
								(
										conversion.class.isAssignableFrom(c) ||
										control.class.isAssignableFrom(c)
								)
						)
						||
						(
								editor.getProperty().equals("participant") &&
								(
										Conversion.class.isAssignableFrom(c) ||
										Control.class.isAssignableFrom(c)
								)
						)
						)
				{
					if (log.isInfoEnabled())
					{
						log.info("skipping restricted participant property");
					}
				}
				else
				{
					classToEditorMap.get(c).add(editor);
				}
			}

		}
	}

	protected void registerModelClass(String localName)
	{
		try
		{
			classToEditorMap.put(getModelInterface(localName),
					new HashSet<PropertyEditor>());
		}
		catch (IllegalBioPAXArgumentException e)
		{
			if (log.isInfoEnabled())
			{
				log.info("Skipping (" + e.getMessage() + ")");
			}
		}
	}

	private class SubClassFilterSet extends AbstractFilterSet<Class>
	{
		private Class filterClass = null;

		public SubClassFilterSet(Set<? extends Class> baseSet,
		                         Class filterClass)
		{
			super(baseSet);
			this.filterClass = filterClass;
		}

		protected boolean filter(Object value)
		{
			return filterClass.isAssignableFrom((Class) value);
		}
	}

	protected Class<? extends BioPAXElement> getModelInterface(String localName)
	{
		try
		{
			Class modelInterface =
					Class.forName(level.getPackageName() + "." + localName);
			if (BioPAXElement.class.isAssignableFrom(modelInterface))
			{
				return modelInterface;
			}
			else
			{
				throw new IllegalBioPAXArgumentException(
						"BioPAXElement is not assignable from class:"
						+ modelInterface.getSimpleName());
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalBioPAXArgumentException(
					"Could not locate interface for:" + localName);
		}
	}

}
