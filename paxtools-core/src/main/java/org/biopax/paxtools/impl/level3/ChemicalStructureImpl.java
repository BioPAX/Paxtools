package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.paxtools.model.level3.StructureFormatType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ChemicalStructureImpl extends L3ElementImpl implements ChemicalStructure
{
	private StructureFormatType structureFormat;

	private String structureData;

	public ChemicalStructureImpl() {
	}

	@Transient
	public Class<? extends ChemicalStructure> getModelInterface()
	{
		return ChemicalStructure.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final ChemicalStructure that = (ChemicalStructure) element;

		return
			(structureData != null ?
				structureData.equals(that.getStructureData()) :
				that.getStructureData() == null)
				&&

				(structureFormat != null ?
					structureFormat.equals(that.getStructureFormat()) :
					that.getStructureFormat() == null);
	}

	public int equivalenceCode()
	{
		int result =
			29 + (structureFormat != null ? structureFormat.hashCode() : 0);
		result = 29 * result +
			(structureData != null ? structureData.hashCode() : 0);
		return result;
	}

	//
	// ChemicalStructure interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	@Column(columnDefinition="LONGTEXT")
	public String getStructureData()
	{
		return structureData;
	}

	public void setStructureData(String structureData)
	{
		this.structureData = structureData;
	}

	@Enumerated
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public StructureFormatType getStructureFormat()
	{
		return structureFormat;
	}

	public void setStructureFormat(StructureFormatType structureFormat)
	{
		this.structureFormat = structureFormat;
	}
}
