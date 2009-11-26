package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface publicationXref extends xref
{
// -------------------------- OTHER METHODS --------------------------

	public void addAUTHORS(String AUTHORS);

	public void addSOURCE(String SOURCE);

	public void addURL(String URL);


	public Set<String> getAUTHORS();

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getSOURCE();


	public String getTITLE();


	public Set<String> getURL();


	public int getYEAR();

	public void removeAUTHORS(String AUTHORS);

	public void removeSOURCE(String SOURCE);

	public void removeURL(String URL);

	void setAUTHORS(Set<String> AUTHORS);

	void setSOURCE(Set<String> SOURCE);

	public void setTITLE(String TITLE);

	void setURL(Set<String> URL);

	public void setYEAR(int YEAR);
}