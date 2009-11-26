package org.biopax.paxtools.model.level2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class PaxtoolsProperties extends Properties
{
// ------------------------------ FIELDS ------------------------------

	public static final String defaultProperties =
		"paxtools.default.properties";
	private static PaxtoolsProperties instance;
	private static Log log = LogFactory.getLog(PaxtoolsProperties.class);

// -------------------------- STATIC METHODS --------------------------

	static
	{
		String s = System.getenv("PaxtoolsProperties");
		if (s == null)
		{
			s = defaultProperties;
		}
		instance = new PaxtoolsProperties();
		try
		{
			instance.load(PaxtoolsProperties.class.getResourceAsStream(s));
		}
		catch (IOException e)
		{
			log.warn("Could not find specified properties file. " +
				"Falling back to defaults");

			try
			{
				instance.load(PaxtoolsProperties.class.getResourceAsStream(
					defaultProperties));
			}
			catch (IOException e1)
			{
				log.error("Missing properties file");
				System.exit(0);
			}
		}
	}


	public static PaxtoolsProperties getInstance()
	{
		return instance;
	}

	public static boolean is(String s)
	{
		return Boolean.valueOf(instance.getProperty(s));
	}
}
