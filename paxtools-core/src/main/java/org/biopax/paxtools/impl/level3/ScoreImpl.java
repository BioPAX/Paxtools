package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Score;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ScoreImpl extends XReferrableImpl implements Score
{

	private String value;
    private Provenance scoreSource;

    public ScoreImpl() {
	}
    
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

	
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

    @ManyToOne(targetEntity = ProvenanceImpl.class)//, cascade = {CascadeType.ALL})
    public Provenance getScoreSource()
    {
        return scoreSource;
    }

    public void setScoreSource(Provenance scoreSource)
    {
        this.scoreSource = scoreSource;
    }



}
