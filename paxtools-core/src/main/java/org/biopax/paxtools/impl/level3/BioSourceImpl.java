package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= BioSource.class)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BioSourceImpl extends NamedImpl implements BioSource
{
	private final static Log LOG = LogFactory.getLog(BioSourceImpl.class);
	
	private CellVocabulary celltype;
	private TissueVocabulary tissue;


	public BioSourceImpl(){
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends BioSource> getModelInterface()
	{
		return BioSource.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof BioSource))
			return false;
		
		final BioSource bioSource = (BioSource) element;

		return
			(celltype != null ?
				celltype.isEquivalent(bioSource.getCellType()) :
				bioSource.getCellType() == null)
			&&
				(tissue != null ?
					tissue.isEquivalent(bioSource.getTissue()) :
					bioSource.getTissue() == null)
			&& 
			// Named, XReferrable equivalence test
			super.semanticallyEquivalent(bioSource);
    }

	public int equivalenceCode()
	{
		int result = 29 *super.equivalenceCode() + (celltype != null ? celltype.hashCode() : 0);
		result = 29 * result + (tissue != null ? tissue.hashCode() : 0);
		return result;
	}

	//
	// BioSource interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    @ManyToOne(targetEntity = CellVocabularyImpl.class)//, cascade = {CascadeType.ALL})
	public CellVocabulary getCellType()
	{
		return celltype;
	}

	public void setCellType(CellVocabulary celltype)
	{
		this.celltype = celltype;
	}

	@ManyToOne(targetEntity = TissueVocabularyImpl.class)//, cascade = {CascadeType.ALL})
	public TissueVocabulary getTissue()
	{
		return tissue;
	}

	public void setTissue(TissueVocabulary tissue)
	{
		this.tissue = tissue;
	}


	@Override
	public String toString() {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(getRDFId()).append(" ");
			sb.append(getName().toString());
			if (tissue != null)
				sb.append(" tissue: ").append(tissue.getTerm().toString());
			if (celltype != null)
				sb.append(" celltype: ").append(celltype.getTerm().toString());
			return sb.toString();
		} catch (Exception e) {
			// possible issues - when in a persistent context (e.g., lazy collections init...)
			LOG.warn("toString(): ", e);
			return getRDFId();
		}
	}
}
