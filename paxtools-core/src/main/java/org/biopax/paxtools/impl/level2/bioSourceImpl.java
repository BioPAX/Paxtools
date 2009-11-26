package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.unificationXref;

/**
 * Created by IntelliJ IDEA. User: root Date: Feb 26, 2006 Time: 4:13:26 AM To
 * change this template use File | Settings | File Templates.
 */
class bioSourceImpl extends BioPAXLevel2ElementImpl implements bioSource
{
// ------------------------------ FIELDS ------------------------------

	private unificationXref TAXON_XREF;
	private openControlledVocabulary CELLTYPE;
	private openControlledVocabulary TISSUE;
	private String NAME;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + (TAXON_XREF != null ? TAXON_XREF.hashCode() : 0);
		result = 29 * result + (CELLTYPE != null ? CELLTYPE.hashCode() : 0);
		result = 29 * result + (TISSUE != null ? TISSUE.hashCode() : 0);
		return result;
	}

	public String toString()
	{
		String s = "";
		if (NAME != null)
		{
			if (s.length() > 0) s += ":";
			s += NAME;
		}
		if (CELLTYPE != null)
		{
			if (s.length() > 0) s += ":";
			s += CELLTYPE;
		}
		if (TISSUE != null)
		{
			if (s.length() > 0) s += ":";
			s += TISSUE;
		}
		return s;
	}

	// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final bioSource bioSource = (bioSource) element;

		return
			(CELLTYPE != null ?
				CELLTYPE.equals(bioSource.getCELLTYPE()) :
				bioSource.getCELLTYPE() == null)
				&&
				(TAXON_XREF != null ?
					TAXON_XREF.equals(bioSource.getTAXON_XREF()) :
					bioSource.getTAXON_XREF() == null)
				&&
				(TISSUE != null ?
					!TISSUE.equals(bioSource.getTISSUE()) :
					bioSource.getTISSUE() != null);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return bioSource.class;
	}

// --------------------- Interface bioSource ---------------------


	public unificationXref getTAXON_XREF()
	{
		return TAXON_XREF;
	}

	public void setTAXON_XREF(unificationXref TAXON_XREF)
	{
		this.TAXON_XREF = TAXON_XREF;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public openControlledVocabulary getCELLTYPE()
	{
		return CELLTYPE;
	}

	public void setCELLTYPE(openControlledVocabulary CELLTYPE)
	{
		this.CELLTYPE = CELLTYPE;
	}

	public openControlledVocabulary getTISSUE()
	{
		return TISSUE;
	}

	public void setTISSUE(openControlledVocabulary TISSUE)
	{
		this.TISSUE = TISSUE;
	}

	public String getNAME()
	{
		return NAME;
	}

	public void setNAME(String NAME)
	{
		this.NAME = NAME;
	}
}
