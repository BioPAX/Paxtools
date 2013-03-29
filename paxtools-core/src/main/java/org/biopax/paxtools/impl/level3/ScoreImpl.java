package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Score;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= Score.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	
	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES)
	@Boost(1.1f)
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
