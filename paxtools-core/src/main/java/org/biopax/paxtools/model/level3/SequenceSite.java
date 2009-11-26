package org.biopax.paxtools.model.level3;


public interface SequenceSite extends SequenceLocation
{
// -------------------------- OTHER METHODS --------------------------

	// Property POSITION-STATUS

    PositionStatusType getPositionStatus();

    void setPositionStatus(PositionStatusType positionStatusType);


    // Property SEQUENCE-POSITION

    int getSequencePosition();

    void setSequencePosition(int sequencePosition);
}
