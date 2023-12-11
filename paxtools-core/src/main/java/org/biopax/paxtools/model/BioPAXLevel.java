package org.biopax.paxtools.model;

import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Enumeration type for BioPAX levels.
 */
public enum BioPAXLevel
{
	// define enum constants using default BioPAX factories
	// (L1 is not supported anymore)
	L2("biopax-level2.owl", new Level2FactoryImpl(), "org.biopax.paxtools.model.level2"),
	L3("biopax-level3.owl", new Level3FactoryImpl(), "org.biopax.paxtools.model.level3");

	// ------------------------------ FIELDS ------------------------------

	private final String filename;

	private BioPAXFactory factory;

	private final String packageName;

	// default L2 factory implementation
	private static class Level2FactoryImpl extends BioPAXFactory {
	    @Override
	    public BioPAXLevel getLevel() {
	    	return BioPAXLevel.L2;
	    }

	    public String mapClassName(Class<? extends BioPAXElement> aClass)
	    {
	        String name = "org.biopax.paxtools.impl.level2."
	                + aClass.getSimpleName()
	                + "Impl";
	        return name;
	    }
	}

	// default L3 factory implementation
	private static class Level3FactoryImpl extends BioPAXFactory {
	    @Override
	    public BioPAXLevel getLevel() {
	    	return BioPAXLevel.L3;
	    }

		/**
		 * Maps a BioPAX L3 interface/type to the concrete java implementation class.
		 * Example: org.biopax.paxtools.model.level3.Protein -> org.biopax.paxtools.impl.level3.ProteinImpl
		 *
		 * @param aClass BioPAX type (model interface)
		 * @return the full name of the implementation class
		 * @throws NullPointerException when aClass is null
		 */
		public String mapClassName(Class<? extends BioPAXElement> aClass)
	    {
	        String name = "org.biopax.paxtools.impl.level3."
	                + aClass.getSimpleName()
	                + "Impl";
	        return name;
	    }
	}


	/**
	 * This is the prefix used for all biopax releases.
	 */
	public static final String BP_PREFIX = "http://www.biopax.org/release/";

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Default constructor
	 * @param filename File name of the owl file.
	 * @param factory BioPAX factory implementation (default)
	 * @param pm package name of the model implementation
	 */
	BioPAXLevel(String filename, BioPAXFactory factory, String pm)
	{
		this.filename = filename;
		this.packageName = pm;
		this.factory = factory;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * This method returns the filename of the owl file
	 * @return the filename of the owl file
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * This method returns the default factory for this level
	 * @return the default factory for this level
	 */
	public BioPAXFactory getDefaultFactory()
	{
		return factory;
	}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * This method loads the level file as resource and returns it as
	 * an input stream
	 * @return an input stream from the owl file.
	 */
	public InputStream getLevelFileAsStream()
	{
		return this.getClass().getResourceAsStream(filename);
	}

// --------------------- ACCESORS and MUTATORS---------------------

	/**
	 * This method returns the namespace defined for this level.
	 * @return namespace defined for this level.
	 */
	public String getNameSpace()
	{
		return BP_PREFIX + filename + "#";
	}

	/**
	 * This method returns true if the given string starts with the
	 * BP_PREFIX
	 * @param nameSpace to be checked
	 * @return rue if the given string starts with the BP_PREFIX
	 */
	public static boolean isInBioPAXNameSpace(String nameSpace)
	{
		return nameSpace != null && nameSpace.startsWith(BP_PREFIX);
	}

	public static BioPAXLevel getLevelFromNameSpace(String namespace)
	{
//		if (isInBioPAXNameSpace(namespace))
//		{
//			{
//				for (BioPAXLevel level : BioPAXLevel.values())
//				{
//					if (namespace.equalsIgnoreCase(level.getNameSpace()))
//					{
//						return level;
//					}
//				}
//			}
//		}
//		return null;
		return stream().filter(level -> level.getNameSpace().equalsIgnoreCase(namespace)).findAny().orElse(null);
	}

	public String getPackageName()
	{
		return packageName;
	}

	public boolean hasElement(BioPAXElement element)
	{
		return element.getModelInterface().getPackage().getName().equals(this.packageName);
	}


	/**
	 * Gets the BioPAX type (java interface) by its short name e.g. "Protein".
	 * @param localName a BioPAX type name
	 * @return the BioPAX interface class
	 * @throws IllegalBioPAXArgumentException when class is null, not found, or is not a BioPAX type
	 */
  public Class<? extends BioPAXElement> getInterfaceForName(String localName)
  {
		if(localName == null) {
			throw new IllegalBioPAXArgumentException("Class name was null");
		}
    try
    {
      Class modelInterface = Class.forName(this.packageName + "." + localName);
      if (BioPAXElement.class.isAssignableFrom(modelInterface))
      {
        return modelInterface;
      } else
      { //not a BioPAX spec class from the classpath, e.g. String, Object, etc.
        throw new IllegalBioPAXArgumentException("BioPAXElement is not assignable from class:"
            + modelInterface.getSimpleName());
      }
    }
    catch (ClassNotFoundException e)
    {
      throw new IllegalBioPAXArgumentException("Could not locate interface for:" + localName);
    }
  }

	public static Stream<BioPAXLevel> stream() {
		return Stream.of(BioPAXLevel.values());
	}
}
