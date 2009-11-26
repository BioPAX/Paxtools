package org.biopax.paxtools.model.level2;

/**
 * User: root Date: Aug 7, 2006 Time: 4:51:19 PM_DOT
 */
public interface sequenceEntity extends physicalEntity
{
// -------------------------- OTHER METHODS --------------------------

	public bioSource getORGANISM();
// --------------------- ACCESORS and MUTATORS---------------------

	public String getSEQUENCE();

	public void setORGANISM(bioSource ORGANISM);

	public void setSEQUENCE(String SEQUENCE);
}
