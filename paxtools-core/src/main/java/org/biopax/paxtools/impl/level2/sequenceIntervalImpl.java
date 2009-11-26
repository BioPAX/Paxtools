package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.sequenceInterval;
import org.biopax.paxtools.model.level2.sequenceSite;

/**
 */
class sequenceIntervalImpl extends BioPAXLevel2ElementImpl
	implements sequenceInterval
{
// ------------------------------ FIELDS ------------------------------

	private sequenceSite SEQUENCE_INTERVAL_BEGIN;
	private sequenceSite SEQUENCE_INTERVAL_END;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + (SEQUENCE_INTERVAL_BEGIN != null ?
			SEQUENCE_INTERVAL_BEGIN.hashCode() : 0);
		result = 29 * result +
			(SEQUENCE_INTERVAL_END != null ? SEQUENCE_INTERVAL_END.hashCode() :
				0);
		return result;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if (!(element instanceof sequenceInterval))
		{
			return false;
		}

		final sequenceInterval that = (sequenceInterval) element;
		return
			(SEQUENCE_INTERVAL_BEGIN != null ?
				SEQUENCE_INTERVAL_BEGIN.isEquivalent(
					that.getSEQUENCE_INTERVAL_BEGIN()) :
				that.getSEQUENCE_INTERVAL_BEGIN() == null)
				&&
				(SEQUENCE_INTERVAL_END != null ?
					SEQUENCE_INTERVAL_END.isEquivalent(
						that.getSEQUENCE_INTERVAL_END()) :
					that.getSEQUENCE_INTERVAL_END() != null);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return sequenceInterval.class;
	}

// --------------------- Interface sequenceInterval ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public sequenceSite getSEQUENCE_INTERVAL_END()
	{
		return SEQUENCE_INTERVAL_END;
	}

	public void setSEQUENCE_INTERVAL_END(sequenceSite SEQUENCE_INTERVAL_END)
	{
		this.SEQUENCE_INTERVAL_END = SEQUENCE_INTERVAL_END;
	}

	public sequenceSite getSEQUENCE_INTERVAL_BEGIN()
	{
		return SEQUENCE_INTERVAL_BEGIN;
	}

	public void setSEQUENCE_INTERVAL_BEGIN(sequenceSite SEQUENCE_INTERVAL_BEGIN)
	{
		this.SEQUENCE_INTERVAL_BEGIN = SEQUENCE_INTERVAL_BEGIN;
	}
	
	@Override
	public String toString() {
		String s = "";
		s+= (getSEQUENCE_INTERVAL_BEGIN()!=null)? getSEQUENCE_INTERVAL_BEGIN().toString() : "null";
		s += " .. ";
		s += (getSEQUENCE_INTERVAL_END()!=null)? getSEQUENCE_INTERVAL_END().toString() : "null";
		return s;
	}
}
