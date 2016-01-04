package org.biopax.paxtools.impl.level3;

import static org.biopax.paxtools.util.SetEquivalenceChecker.hasEquivalentIntersection;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BioSourceImpl extends NamedImpl implements BioSource
{
	private final static Logger LOG = LoggerFactory.getLogger(BioSourceImpl.class);
	
	private CellVocabulary celltype;
	private TissueVocabulary tissue;


	public BioSourceImpl(){
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
			&& hasEquivalentIntersection(
				new ClassFilterSet<Xref, UnificationXref>(getXref(), UnificationXref.class),
				new ClassFilterSet<Xref, UnificationXref>(bioSource.getXref(), UnificationXref.class));
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
	public CellVocabulary getCellType()
	{
		return celltype;
	}

	public void setCellType(CellVocabulary celltype)
	{
		this.celltype = celltype;
	}

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
			StringBuilder sb = new StringBuilder();
			sb.append(getUri()).append(" ");
			sb.append(getName().toString());
			if (tissue != null)
				sb.append(" tissue: ").append(tissue.getTerm().toString());
			if (celltype != null)
				sb.append(" celltype: ").append(celltype.getTerm().toString());
			sb.append(" xrefs: ").append(getXref().toString());
			return sb.toString();
		} catch (Exception e) {
			// possible issues - when in a persistent context (e.g., lazy collections init...)
			LOG.warn("Error in toString(): ", e);
			return getUri();
		}
	}
}
