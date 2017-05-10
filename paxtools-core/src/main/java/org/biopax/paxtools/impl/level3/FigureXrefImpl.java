package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.FigureXref;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public class FigureXrefImpl extends XrefImpl implements FigureXref
{
	private PublicationXref publication;
	private Set<String> url;
	private String code;
	private String caption;

	/**
	 * Constructor.
	 */
	public FigureXrefImpl()
	{
		this.url = BPCollections.I.createSet();
	}

    public Class<? extends FigureXref> getModelInterface()
	{
		return FigureXref.class;
	}

	//
	// FigureXrefImpl interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public PublicationXref getPublication()
	{
		return publication;
	}

	public void setPublication(PublicationXref publication)
	{
		this.publication = publication;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getCaption()
	{
		return caption;
	}

	public void setCaption(String caption)
	{
		this.code = caption;
	}

	public Set<String> getUrl()
	{
		return url;
	}

	public void addUrl(String url)
	{
		if(url != null && url.length() > 0)
			this.url.add(url);
	}

	public void removeUrl(String url)
	{
		if(url != null)
			this.url.remove(url);
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement other) {
		if(!(other instanceof FigureXref)) return false;

		FigureXref that = (FigureXref) other;
		boolean eqv = (this.code == that.getCode())
			&& url.containsAll(that.getUrl());

		return eqv	&& super.semanticallyEquivalent(other);

	}

}
