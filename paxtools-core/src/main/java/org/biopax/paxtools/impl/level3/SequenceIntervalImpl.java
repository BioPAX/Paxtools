package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceSite;

class SequenceIntervalImpl extends SequenceLocationImpl implements
                                                               SequenceInterval
{
	
	private SequenceSite sequenceIntervalBegin;
	private SequenceSite sequenceIntervalEnd;

	//
	// utilityClass (BioPAXElement) interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends SequenceInterval> getModelInterface()
	{
		return SequenceInterval.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final SequenceInterval that = (SequenceInterval) element;
		return
			(sequenceIntervalBegin != null ?
				sequenceIntervalBegin.equals(
					that.getSequenceIntervalBegin()) :
				that.getSequenceIntervalBegin() == null)
				&&
				(sequenceIntervalEnd != null ?
					sequenceIntervalEnd.equals(
						that.getSequenceIntervalEnd()) :
					that.getSequenceIntervalEnd() == null);
	}

	public int equivalenceCode()
	{
		int result = 29 + (sequenceIntervalBegin != null ?
			sequenceIntervalBegin.hashCode() : 0);
		result = 29 * result +
			(sequenceIntervalEnd != null ? sequenceIntervalEnd.hashCode() :
				0);
		return result;
	}

	//
	// sequenceInterval interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public SequenceSite getSequenceIntervalBegin()
	{
		return sequenceIntervalBegin;
	}

	public void setSequenceIntervalBegin(SequenceSite sequenceIntervalBegin)
	{
		this.sequenceIntervalBegin = sequenceIntervalBegin;
	}

	public SequenceSite getSequenceIntervalEnd()
	{
		return sequenceIntervalEnd;
	}

	public void setSequenceIntervalEnd(SequenceSite sequenceIntervalEnd)
	{
		this.sequenceIntervalEnd = sequenceIntervalEnd;
	}
}
