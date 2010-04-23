package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.publicationXref;

import java.util.HashSet;
import java.util.Set;

/**
 */
class publicationXrefImpl extends xrefImpl implements publicationXref
{
// ------------------------------ FIELDS ------------------------------

	private Set<String> SOURCE;
	private Set<String> URL;
	private String TITLE;
	private Set<String> AUTHORS;
	private int YEAR = UNKNOWN_INT;

// --------------------------- CONSTRUCTORS ---------------------------

	public publicationXrefImpl()
	{
		super();
		this.AUTHORS = new HashSet<String>();
		this.SOURCE = new HashSet<String>();
		this.URL = new HashSet<String>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return publicationXref.class;
	}

// --------------------- Interface publicationXref ---------------------


	public Set<String> getSOURCE()
	{
		return SOURCE;
	}

	public void setSOURCE(Set<String> SOURCE)
	{
		this.SOURCE = SOURCE;
	}

	public void addSOURCE(String SOURCE)
	{
		this.SOURCE.add(SOURCE);
	}

	public void removeSOURCE(String SOURCE)
	{
		this.SOURCE.remove(SOURCE);
	}

	public Set<String> getURL()
	{
		return URL;
	}

	public void setURL(Set<String> URL)
	{
		this.URL = URL;
	}

	public void addURL(String URL)
	{
		this.URL.add(URL);
	}

	public void removeURL(String URL)
	{
		this.URL.remove(URL);
	}

	public String getTITLE()
	{
		return TITLE;
	}

	public void setTITLE(String TITLE)
	{
		this.TITLE = TITLE;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getAUTHORS()
	{
		return AUTHORS;
	}

	public void setAUTHORS(Set<String> AUTHORS)
	{
		this.AUTHORS = AUTHORS;
	}

	public void addAUTHORS(String AUTHORS)
	{
		this.AUTHORS.add(AUTHORS);
	}

	public void removeAUTHORS(String AUTHORS)
	{
		this.AUTHORS.remove(AUTHORS);
	}

	public int getYEAR()
	{
		return YEAR;
	}

	public void setYEAR(int YEAR)
	{
		this.YEAR = YEAR;
	}
}
