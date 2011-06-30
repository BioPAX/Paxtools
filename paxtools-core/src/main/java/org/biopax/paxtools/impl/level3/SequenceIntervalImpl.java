package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class SequenceIntervalImpl extends SequenceLocationImpl
	implements SequenceInterval
{
	
	private SequenceSite sequenceIntervalBegin;
	private SequenceSite sequenceIntervalEnd;

	public SequenceIntervalImpl() {
	}
	
	//
	// utilityClass (BioPAXElement) interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    @Transient
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
    @ManyToOne(targetEntity = SequenceSiteImpl.class)//, cascade={CascadeType.ALL})
	public SequenceSite getSequenceIntervalBegin()
	{
		return sequenceIntervalBegin;
	}

	public void setSequenceIntervalBegin(SequenceSite sequenceIntervalBegin)
	{
		this.sequenceIntervalBegin = sequenceIntervalBegin;
	}

    @ManyToOne(targetEntity = SequenceSiteImpl.class)//, cascade={CascadeType.ALL})
	public SequenceSite getSequenceIntervalEnd()
	{
		return sequenceIntervalEnd;
	}

	public void setSequenceIntervalEnd(SequenceSite sequenceIntervalEnd)
	{
		this.sequenceIntervalEnd = sequenceIntervalEnd;
	}
}
