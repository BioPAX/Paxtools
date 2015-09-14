package org.biopax.paxtools.model.level2;


public interface sequenceSite extends sequenceLocation
{
// -------------------------- OTHER METHODS --------------------------

	PositionStatusType getPOSITION_STATUS();
// --------------------- ACCESORS and MUTATORS---------------------

	int getSEQUENCE_POSITION();

	void setPOSITION_STATUS(PositionStatusType POSITION_STATUS);

	void setSEQUENCE_POSITION(int SEQUENCE_POSITION);
}