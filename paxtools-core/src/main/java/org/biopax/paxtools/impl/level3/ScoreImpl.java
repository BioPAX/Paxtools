package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;

import java.util.Set;

class ScoreImpl extends L3ElementImpl implements Score
{

	private String value;
    private Provenance scoreSource;
    private final ReferenceHelper referenceHelper;

    public ScoreImpl()
    {
        this.referenceHelper = new ReferenceHelper(this);
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

// --------------------- Interface Xreferrable ---------------------


    public Set<Xref> getXref() {
        return referenceHelper.getXref();
    }

    public void setXref(Set<Xref> Xref) {
        referenceHelper.setXref(Xref);
    }

    public void addXref(Xref Xref) {
        referenceHelper.addXref(Xref);
    }

    public void removeXref(Xref Xref) {
        referenceHelper.removeXref(Xref);
    }


}
