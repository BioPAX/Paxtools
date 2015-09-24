package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public class PublicationXrefImpl extends XrefImpl implements PublicationXref
{
	private String title;
	private Set<String> url;
	private Set<String> source;
	private Set<String> author;
	private int year = UNKNOWN_INT;

	/**
	 * Constructor.
	 */
	public PublicationXrefImpl()
	{
		this.url = BPCollections.I.createSet();
		this.source = BPCollections.I.createSet();
		this.author = BPCollections.I.createSet();
	}

    public Class<? extends PublicationXref> getModelInterface()
	{
		return PublicationXref.class;
	}

	//
	// PublicationXref interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    // Property author
	public Set<String> getAuthor()
	{
		return author;
	}

	public void addAuthor(String author)
	{
		if(author != null && author.length() > 0)
			this.author.add(author);
	}

	public void removeAuthor(String author)
	{
		if(author != null)
			this.author.remove(author);
	}

	public Set<String> getSource()
	{
		return source;
	}

	public void addSource(String source)
	{
		if(source != null && source.length() > 0)
			this.source.add(source);
	}

	public void removeSource(String source)
	{
		if(source != null)
			this.source.remove(source);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
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

    // Property year
	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement other) {
		if(!(other instanceof PublicationXref)) return false;
		
		PublicationXref that = (PublicationXref) other;
		boolean eqv = (year == that.getYear()) &&
			(title != null ? 
				title.equals(that.getTitle()) 
				: that.getTitle() == null)
			&& author.containsAll(that.getAuthor())
			&& source.containsAll(that.getSource())
			&& url.containsAll(that.getUrl());
		
		return eqv	&& super.semanticallyEquivalent(other);
	}
}
