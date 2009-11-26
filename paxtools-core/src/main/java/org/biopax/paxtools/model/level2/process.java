package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * Tagging interface for entities that needs evidence and can be targeted by a
 * control : Pathway and Interaction
 */
public interface process extends entity, pathwayComponent
{


	/**
	 * This method adds the given evidence to this process.
	 *
	 * @param EVIDENCE
	 */
	public void addEVIDENCE(evidence EVIDENCE);

	/**
	 * This method remmoves the given evidence from this process. If process does
	 * not already contain this evidence this method does nothing
	 *
	 * @param EVIDENCE
	 */
	public void removeEVIDENCE(evidence EVIDENCE);

	/**
	 * 
	 * @return
	 */
	public Set<evidence> getEVIDENCE();

	void setEVIDENCE(Set<evidence> EVIDENCE);

	public Set<control> isCONTROLLEDOf();

	Set<pathway> isPATHWAY_COMPONENTSof();

	public Set<pathwayStep> isSTEP_INTERACTIONSOf();


}
