package org.biopax.paxtools.io.simpleIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMapAdapter;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides a simple editor map for a level with a given factory.
 *
 * @author Emek Demir
 */
public class SimpleEditorMap extends EditorMapAdapter
{
	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);

	public SimpleEditorMap()
	{
		this(null);
	}

	public SimpleEditorMap(BioPAXLevel level)

	{
		super(level);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(this.getClass().getResourceAsStream(
						"Level" + this.getLevel().getValue() + "Editor.properties")));
		try
		{
			String line = reader.readLine();
			StringTokenizer st = new StringTokenizer(line);
			while(st.hasMoreElements())
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
			log.error("Could not initialize " +
			          "Editor Map", e);
		}


	}


}
