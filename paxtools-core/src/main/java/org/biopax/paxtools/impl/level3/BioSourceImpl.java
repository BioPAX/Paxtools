package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;

import java.util.Set;

class BioSourceImpl extends L3ElementImpl implements BioSource
{

	private final NameHelper nameHelper;
	private UnificationXref taxonXref;
	private CellVocabulary celltype;
	private TissueVocabulary tissue;

	public BioSourceImpl()
	{
		nameHelper = new NameHelper();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

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
				(taxonXref != null ?
					taxonXref.isEquivalent(bioSource.getTaxonXref()) :
					bioSource.getTaxonXref() == null)
				&&
				(tissue != null ?
					!tissue.isEquivalent(bioSource.getTissue()) :
					bioSource.getTissue() == null);


    }

	public int equivalenceCode()
	{
		int result = 29 + (taxonXref != null ? taxonXref.hashCode() : 0);
		result = 29 * result + (celltype != null ? celltype.hashCode() : 0);
		result = 29 * result + (tissue != null ? tissue.hashCode() : 0);
		return result;
	}

	//
	// BioSource interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    // Property celltype

	public CellVocabulary getCellType()
	{
		return celltype;
	}

	public void setCellType(CellVocabulary celltype)
	{
		this.celltype = celltype;
	}

    // Property TAXON-Xref

	public UnificationXref getTaxonXref()
	{
		return taxonXref;
	}

	public void setTaxonXref(UnificationXref taxonXref)
	{
		this.taxonXref = taxonXref;
	}

    // Property tissue

	public TissueVocabulary getTissue()
	{
		return tissue;
	}

	public void setTissue(TissueVocabulary tissue)
	{
		this.tissue = tissue;
	}

	//
	// named interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	public Set<String> getName()
	{
		return nameHelper.getName();
	}

	public void setName(Set<String> name)
	{
		nameHelper.setName(name);
	}

	public void addName(String name)
	{
		nameHelper.addName(name);
	}

	public void removeName(String name)
	{
		nameHelper.removeName(name);
	}

	public String getDisplayName()
	{
		return nameHelper.getDisplayName();
	}

	public void setDisplayName(String displayName)
	{
		nameHelper.setDisplayName(displayName);
	}

	public String getStandardName()
	{
		return nameHelper.getStandardName();
	}

	public void setStandardName(String standardName)
	{
		nameHelper.setStandardName(standardName);
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
