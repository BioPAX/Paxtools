package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.PositionStatusType;
import org.biopax.paxtools.model.level2.sequenceSite;

/**
 */
class sequenceSiteImpl extends BioPAXLevel2ElementImpl implements sequenceSite
{
// ------------------------------ FIELDS ------------------------------

	private int SEQUENCE_POSITION = BioPAXElement.UNKNOWN_INT;

	private PositionStatusType POSITION_STATUS;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + SEQUENCE_POSITION;
		result = 29 * result +
			(POSITION_STATUS != null ? POSITION_STATUS.hashCode() : 0);
		return result;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if (!(element instanceof sequenceSite))
		{
			return false;
		}

		final sequenceSite that = (sequenceSite) element;
		return
			(SEQUENCE_POSITION == that.getSEQUENCE_POSITION()) &&
				(POSITION_STATUS != null ?
					POSITION_STATUS.equals(that.getPOSITION_STATUS()) :
					that.getPOSITION_STATUS() == null);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return sequenceSite.class;
	}

// --------------------- Interface sequenceSite ---------------------


	public int getSEQUENCE_POSITION()
	{
		return SEQUENCE_POSITION;
	}

	public void setSEQUENCE_POSITION(int SEQUENCE_POSITION)
	{
		this.SEQUENCE_POSITION = SEQUENCE_POSITION;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public PositionStatusType getPOSITION_STATUS()
	{
		return POSITION_STATUS;
	}

	public void setPOSITION_STATUS(PositionStatusType POSITION_STATUS)
	{
		this.POSITION_STATUS = POSITION_STATUS;
	}
	
	@Override
	public String toString() {
		return getSEQUENCE_POSITION() + " (" + getPOSITION_STATUS() + ")";
	}
}
