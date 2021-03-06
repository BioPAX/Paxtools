package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PositionStatusType;
import org.biopax.paxtools.model.level3.SequenceSite;


public class SequenceSiteImpl extends SequenceLocationImpl implements SequenceSite
{

	private PositionStatusType positionStatus;
	private int sequencePosition = UNKNOWN_INT;

	public SequenceSiteImpl()
    {
	}

    public Class<? extends SequenceSite> getModelInterface()
	{
		return SequenceSite.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof SequenceSite))
			return false;
		
		final SequenceSite that = (SequenceSite) element;
		return sequencePosition == that.getSequencePosition() &&
		    positionStatus != null ?
					positionStatus.equals(that.getPositionStatus()) :
					that.getPositionStatus() == null;
	}

	public int equivalenceCode()
	{
		int result = 29 + sequencePosition;
		result = 29 * result +
			(positionStatus != null ? positionStatus.hashCode() : 0);
		return result;
	}

    @Override
    public String toString()
    {
        return String.valueOf(this.getSequencePosition());
    }

    //
	// sequenceSite interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property POSITION-STATUS
	public PositionStatusType getPositionStatus()
	{
		return positionStatus;
	}

	public void setPositionStatus(PositionStatusType positionStatus)
	{
		this.positionStatus = positionStatus;
	}

    // Property sequence-POSITION
    
	public int getSequencePosition()
	{
		return sequencePosition;
	}

	public void setSequencePosition(int sequencePosition)
	{
		this.sequencePosition = sequencePosition;
	}
}
