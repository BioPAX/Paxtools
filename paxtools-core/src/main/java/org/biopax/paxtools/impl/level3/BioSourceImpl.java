package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.TissueVocabulary;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
class BioSourceImpl extends NamedImpl implements BioSource
{


	private CellVocabulary celltype;
	private TissueVocabulary tissue;


	public BioSourceImpl()
	{
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
		final BioSource bioSource = (BioSource) element;

		return
			(celltype != null ?
				celltype.isEquivalent(bioSource.getCellType()) :
				bioSource.getCellType() == null)
				&&
				(tissue != null ?
					!tissue.isEquivalent(bioSource.getTissue()) :
					bioSource.getTissue() == null);


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

    @ManyToOne(targetEntity = CellVocabularyImpl.class)
	public CellVocabulary getCellType()
	{
		return celltype;
	}

	public void setCellType(CellVocabulary celltype)
	{
		this.celltype = celltype;
	}

	@ManyToOne(targetEntity = TissueVocabularyImpl.class)
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
		StringBuffer sb = new StringBuffer();
		sb.append(getRDFId()).append(" ");
		sb.append(getName().toString());
		if(tissue != null) sb.append(" tissue: ").append(tissue.getTerm().toString());
		if(celltype != null) sb.append(" celltype: ").append(celltype.getTerm().toString());
		return sb.toString();
	}
}
