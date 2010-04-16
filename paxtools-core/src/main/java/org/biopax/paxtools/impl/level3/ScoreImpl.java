package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;

import java.util.Set;

class ScoreImpl extends XReferrableImpl implements Score
{

	private String value;
    private Provenance scoreSource;

    public ScoreImpl()
    {
    }

    //
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends Score> getModelInterface()
	{
		return Score.class;
	}



	//
	// confidence interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

    public Provenance getScoreSource()
    {
        return scoreSource;
    }

    public void setScoreSource(Provenance scoreSource)
    {
        this.scoreSource = scoreSource;
    }

// ------------------------ INTERFACE METHODS ------------------------


}
