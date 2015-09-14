package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface publicationXref extends xref
{
// -------------------------- OTHER METHODS --------------------------

	void addAUTHORS(String AUTHORS);

	void addSOURCE(String SOURCE);

	void addURL(String URL);


	Set<String> getAUTHORS();

// --------------------- ACCESORS and MUTATORS---------------------

	Set<String> getSOURCE();


	String getTITLE();


	Set<String> getURL();


	int getYEAR();

	void removeAUTHORS(String AUTHORS);

	void removeSOURCE(String SOURCE);

	void removeURL(String URL);

	void setAUTHORS(Set<String> AUTHORS);

	void setSOURCE(Set<String> SOURCE);

	void setTITLE(String TITLE);

	void setURL(Set<String> URL);

	void setYEAR(int YEAR);
}