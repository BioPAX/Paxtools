package org.biopax.paxtools.model.level2;


public interface sequenceSite extends sequenceLocation
{
// -------------------------- OTHER METHODS --------------------------

	public PositionStatusType getPOSITION_STATUS();
// --------------------- ACCESORS and MUTATORS---------------------

	public int getSEQUENCE_POSITION();

	public void setPOSITION_STATUS(PositionStatusType POSITION_STATUS);

	public void setSEQUENCE_POSITION(int SEQUENCE_POSITION);
}