package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

import javax.persistence.*;

/**
 * This is the default implementation of the {@link Model}.
 */
@Entity
public class ModelImpl implements Model
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID = -2087521863213381434L;
	private final Map<String, BioPAXElement> idMap;
    private final Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private transient BioPAXFactory factory;
    private transient final Map<String, BioPAXElement> exposedIdMap;
    private transient final Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;

// --------------------------- CONSTRUCTORS ---------------------------


    protected ModelImpl(BioPAXLevel level)
	{
	   this(level.getDefaultFactory());
	}

	public ModelImpl(BioPAXFactory factory)
	{
		idMap = new HashMap<String, BioPAXElement>();
        nameSpacePrefixMap = new HashMap<String, String>();
		this.factory = factory;
		this.level = factory.getLevel();
        this.exposedIdMap = Collections.unmodifiableMap(idMap);
        this.exposedObjectSet = new UnmodifiableImplicitSet(idMap.values());

    }

	
	private Long id = 0L;
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long value) {
		id = value;
	}
	
	
// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     *@deprecated use getByID(id) or containsID(id) instead.
     */
	@Transient
    public Map<String, BioPAXElement> getIdMap()
	{
        return exposedIdMap;
	}

    public boolean containsID(String id) {
        return this.idMap.containsKey(id);
    }
    
    @Transient
    public BioPAXElement getByID(String id) {
        return this.idMap.get(id);
    }


    @Transient
    @ElementCollection
    @MapKey(name="ns")
    @Column(name="namespace")
    public Map<String, String> getNameSpacePrefixMap()
	{
		return nameSpacePrefixMap;
	}

    private void setNameSpacePrefixMap(Map<String, String> nameSpacePrefixMap) {
    	synchronized (this.nameSpacePrefixMap) {
			this.nameSpacePrefixMap.clear();
			this.nameSpacePrefixMap.putAll(nameSpacePrefixMap);
		}
	}
    
    
    @Transient
    public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Model ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

    @ElementCollection(targetClass=BioPAXElementImpl.class)
    @ManyToMany(targetEntity=BioPAXElementImpl.class)
	public Set<BioPAXElement> getObjects()
	{
		return exposedObjectSet;
	}

	public <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy)
	{
		return new ClassFilterSet<T>(exposedObjectSet, filterBy);
	}

    private void setObjects(Set<BioPAXElement> objects) {   	
    	synchronized (idMap) {
    		idMap.clear();
        	for(BioPAXElement bpe : objects) {
        		if(!containsID(bpe.getRDFId()))
        			add(bpe);
        	}
		}
    }
	
	public void remove(BioPAXElement aBioPAXElement)
	{
		this.idMap.remove(aBioPAXElement.getRDFId());
	}
                            
	public <T extends BioPAXElement> T addNew(Class<T> c, String id)
	{
		T paxElement = factory.reflectivelyCreate(c);
		this.setIdAndAdd(paxElement, id);
		return paxElement;
	}

	/**
	 * This method returns true if and only if the given object is
	 * @param aBioPAXElement
	 * @return
	 */
	public boolean contains(BioPAXElement aBioPAXElement)
	{
		return this.idMap.get(aBioPAXElement.getRDFId()) == aBioPAXElement;
	}

// -------------------------- OTHER METHODS --------------------------

	private void setIdAndAdd(BioPAXElement bp, String id)
	{
		bp.setRDFId(id);
		this.add(bp);
	}

	public void add(BioPAXElement aBioPAXElement)
	{
		String rdfId = aBioPAXElement.getRDFId();
        if(!this.level.hasElement(aBioPAXElement))
        {
            throw new IllegalBioPAXArgumentException(
                "Given object is of wrong level");
        }
        if (rdfId == null)
		{
			throw new IllegalBioPAXArgumentException(
				"null ID: every object must have an RDF ID");
		}

		else if (this.idMap.containsKey(rdfId))
		{
			throw new IllegalBioPAXArgumentException(
				"I already have an object with the same ID: " + rdfId +
					". Try removing it first");
		}

		else if (this.contains(aBioPAXElement))
		{
			throw new IllegalBioPAXArgumentException(
				"duplicate element:" + aBioPAXElement);
		}
		else
		{
			this.idMap.put(rdfId, aBioPAXElement);
		}
	}


    public BioPAXLevel getLevel()
	{
		return level;
	}

    // used by hibernate only
    private void setLevel(BioPAXLevel level) {
		this.level = level;
		this.factory = level.getDefaultFactory();
	}
    
    
    @Transient
    public void setAddDependencies(boolean value) {
        this.addDependencies = value;
    }

    @Transient
    public boolean isAddDependencies() {
        return addDependencies;
    }

    private class UnmodifiableImplicitSet implements Set<BioPAXElement>
	{
		private final Collection<BioPAXElement> elements;

		public UnmodifiableImplicitSet(
			Collection<BioPAXElement> elements)
		{

			this.elements = elements;
		}

		public int size()
		{
			return elements.size();
		}

		public boolean isEmpty()
		{
			return elements.isEmpty();
		}

		public boolean contains(Object o)
		{
			return elements.contains(o);
		}

		public Iterator<BioPAXElement> iterator()
		{
			return elements.iterator();
		}

		public Object[] toArray()
		{
			return elements.toArray();
		}

		public <T> T[] toArray(T[] a)
		{
            return elements.toArray(a);
		}

		public boolean add(BioPAXElement bioPAXElement)
		{
			throw new UnsupportedOperationException();
		}

		public boolean remove(Object o)
		{
			throw new UnsupportedOperationException();
		}

		public boolean containsAll(Collection<?> c)
		{
			return elements.containsAll(c);
		}

		public boolean addAll(Collection<? extends BioPAXElement> c)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}

	public synchronized void updateID(String oldID, String newID) {
		if (this.containsID(oldID)) {
			BioPAXElement bpe = getByID(oldID);
			this.idMap.remove(oldID);
			try {
				setIdAndAdd(bpe, newID);
			} catch (IllegalBioPAXArgumentException e) {
				// roolback and fail
				setIdAndAdd(bpe, oldID); 
				throw new IllegalBioPAXArgumentException(
					"Updating ID failed (model is unchanged): " +
					"cannot use new ID: " + newID, e);
			}
			
		} else {
			throw new IllegalBioPAXArgumentException(
					"I do not have an object with the ID: " + oldID);
		}
	}
}
