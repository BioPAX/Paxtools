package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Set;

@Entity
class ScoreImpl extends XReferrableImpl implements Score
{

	private String value;
    private Provenance scoreSource;

    //
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
    public Class<? extends Score> getModelInterface()
	{
		return Score.class;
	}



	//
	// confidence interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Basic
    public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

    @ManyToOne(targetEntity = ProvenanceImpl.class)
    public Provenance getScoreSource()
    {
        return scoreSource;
    }

    public void setScoreSource(Provenance scoreSource)
    {
        this.scoreSource = scoreSource;
    }



}
