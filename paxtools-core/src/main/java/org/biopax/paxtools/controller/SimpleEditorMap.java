package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides a simple editor map for a level with a given factory.
 * @author Emek Demir
 */
public enum SimpleEditorMap implements EditorMap
{

	L1(BioPAXLevel.L1),
	L2(BioPAXLevel.L2),
	L3(BioPAXLevel.L3);

	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);

	SimpleEditorMapImpl impl;

	SimpleEditorMap(BioPAXLevel level)
	{
		this.impl = new SimpleEditorMapImpl(level);
	}

	public static SimpleEditorMap get(BioPAXLevel level)
	{
		for (SimpleEditorMap value : values())
		{
			if(value.getLevel().equals(level)) return value;
		}
		//should never reach here
		throw new IllegalBioPAXArgumentException("Unknown level:" + level);
	}

	class SimpleEditorMapImpl extends EditorMapAdapter implements EditorMap
	{
		private BioPAXLevel level;

		SimpleEditorMapImpl(BioPAXLevel level)
		{
			this.level = level;
			InputStream stream = this.getClass().getResourceAsStream(level + "Editor.properties");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			try
			{
				String line = reader.readLine();
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreElements())
				{
					this.registerModelClass(st.nextToken());
				}

				while ((line = reader.readLine()) != null)
				{
					st = new StringTokenizer(line);
					String domain = st.nextToken();
					String propertyName = st.nextToken();

					Class<? extends BioPAXElement> domainInterface = getModelInterface(domain);

					createAndRegisterBeanEditor(propertyName, domainInterface);
				}
			}
			catch (IOException e)
			{
				log.error("Could not initialize " + "Editor Map", e);
			}
			finally
			{
				try
				{
					stream.close();
				}
				catch (IOException ignore)
				{
					log.error("Could not close stream! Exiting");
					System.exit(1);
				}
			}
		}

		public BioPAXLevel getLevel()
		{
			return level;
		}
	}


	@Override public PropertyEditor getEditorForProperty(String property, Class javaClass)
	{
		return impl.getEditorForProperty(property, javaClass);
	}

	@Override public Set<PropertyEditor> getEditorsForProperty(String property)
	{
		return impl.getEditorsForProperty(property);
	}

	@Override public Set<PropertyEditor> getEditorsOf(BioPAXElement bpe)
	{
		return impl.getEditorsOf(bpe);

	}

	@Override public Set<ObjectPropertyEditor> getInverseEditorsOf(BioPAXElement bpe)
	{
		return impl.getInverseEditorsOf(bpe);
	}

	@Override public Set<Class<? extends BioPAXElement>> getKnownSubClassesOf(Class<? extends BioPAXElement>
			                                                                          javaClass)
	{
		return impl.getKnownSubClassesOf(javaClass);
	}

	public BioPAXLevel getLevel()
	{
		return impl.getLevel();

	}
}
