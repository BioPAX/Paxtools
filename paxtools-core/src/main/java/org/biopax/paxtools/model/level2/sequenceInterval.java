package org.biopax.paxtools.model.level2;


public interface sequenceInterval extends sequenceLocation
{
// -------------------------- OTHER METHODS --------------------------

	sequenceSite getSEQUENCE_INTERVAL_BEGIN();

// --------------------- ACCESORS and MUTATORS---------------------

	sequenceSite getSEQUENCE_INTERVAL_END();

	void setSEQUENCE_INTERVAL_BEGIN(
		sequenceSite SEQUENCE_INTERVAL_BEGIN);

	void setSEQUENCE_INTERVAL_END(sequenceSite SEQUENCE_INTERVAL_END);
}

