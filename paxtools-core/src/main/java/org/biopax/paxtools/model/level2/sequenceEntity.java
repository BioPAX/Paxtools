package org.biopax.paxtools.model.level2;

/**
 * User: root Date: Aug 7, 2006 Time: 4:51:19 PM_DOT
 */
public interface sequenceEntity extends physicalEntity
{
// -------------------------- OTHER METHODS --------------------------

	bioSource getORGANISM();
// --------------------- ACCESORS and MUTATORS---------------------

	String getSEQUENCE();

	void setORGANISM(bioSource ORGANISM);

	void setSEQUENCE(String SEQUENCE);
}
