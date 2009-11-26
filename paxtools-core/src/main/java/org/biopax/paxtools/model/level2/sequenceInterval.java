package org.biopax.paxtools.model.level2;


public interface sequenceInterval extends sequenceLocation
{
// -------------------------- OTHER METHODS --------------------------

	public sequenceSite getSEQUENCE_INTERVAL_BEGIN();

// --------------------- ACCESORS and MUTATORS---------------------

	public sequenceSite getSEQUENCE_INTERVAL_END();

	public void setSEQUENCE_INTERVAL_BEGIN(
		sequenceSite SEQUENCE_INTERVAL_BEGIN);

	public void setSEQUENCE_INTERVAL_END(sequenceSite SEQUENCE_INTERVAL_END);
}

