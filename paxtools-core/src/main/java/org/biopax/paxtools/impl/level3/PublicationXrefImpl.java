package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
		this.url = new HashSet<String>();
		this.source = new HashSet<String>();
		this.author = new HashSet<String>();
	}

	@Transient
    public Class<? extends PublicationXref> getModelInterface()
	{
		return PublicationXref.class;
	}

	//
	// PublicationXref interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    // Property author
    @ElementCollection
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    @FieldBridge(impl=SetStringBridge.class)
	public Set<String> getAuthor()
	{
		return author;
	}

	public void setAuthor(Set<String> author)
	{
		this.author = author;
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

    @ElementCollection
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    @FieldBridge(impl=SetStringBridge.class)
	public Set<String> getSource()
	{
		return source;
	}

	public void setSource(Set<String> source)
	{
		this.source = source;
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

    
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	@Column(columnDefinition="LONGTEXT")
 	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

        // Property url
    @ElementCollection
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    @FieldBridge(impl=SetStringBridge.class)
	public Set<String> getUrl()
	{
		return url;
	}

	public void setUrl(Set<String> url)
	{
		this.url = url;
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
    
    @Column(name="published") //default one caused MySQLIntegrityConstraintViolationException: Column 'year' in field list is ambiguous
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
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
