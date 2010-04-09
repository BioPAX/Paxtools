package org.biopax.paxtools.proxy.level3;

import java.util.Collections;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

/**
 * Main container class to access BioPAX objects.
 * 
 * @deprecated requires re-factoring; alternatively, use ModelProxy or persist only individual elements and use the core ModelImp...
 * 
 * TODO why 'objectSet' is used in Collections.unmodifiableSet(objectSet) and not, e.g., using idMap.values()?
 */
//@Entity(name="model")
public class ModelForPersistence implements Model
{
// ------------------------------ FIELDS ------------------------------
	private static final long serialVersionUID = -3701438841490642931L;

	private Map<String, BioPAXElement> idMap;
	private Set<BioPAXElement> objectSet;
	private Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private BioPAXFactory factory;
    private Map<String, BioPAXElement> exposedIdMap;
    private Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;

// --------------------------- CONSTRUCTORS ---------------------------

    public ModelForPersistence() {
    	this(new org.biopax.paxtools.proxy.level3.BioPAXFactoryForPersistence());
	}
    
	public ModelForPersistence(BioPAXFactory factory)
	{
		idMap = new HashMap<String, BioPAXElement>();
		objectSet = new HashSet<BioPAXElement>();
		nameSpacePrefixMap = new HashMap<String, String>();
		this.factory = factory;
		this.level = factory.getLevel();
        this.exposedIdMap = Collections.unmodifiableMap(idMap);
        this.exposedObjectSet = Collections.unmodifiableSet(objectSet);
	}

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @deprecated 
     */
	@Transient
    public Map<String, BioPAXElement> getIdMap()
	{
        return exposedIdMap;
	}

	@Transient
    public BioPAXElement getByID(String id) {
        return this.idMap.get(id);
    }

    public Map<String, String> getNameSpacePrefixMap()
	{
		return nameSpacePrefixMap;
	}

    public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
	}

    //TODO BioPAXElementProxy must be shared for L2 and L3 - see also ModelProxy!
    //@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = BioPAXElementProxy.class)
	//@JoinColumn(name="objects_x")
	public Set<BioPAXElement> getObjects()
	{
		return exposedObjectSet;
	}

    @Transient
	public <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy)
	{
		return new ClassFilterSet<T>(objectSet, filterBy);
	}


	public void remove(BioPAXElement aBioPAXElement)
	{
		this.idMap.remove(aBioPAXElement.getRDFId());
		this.objectSet.remove(aBioPAXElement);
	}
                            
	public <T extends BioPAXElement> T addNew(Class<T> c, String id)
	{
		T paxElement = factory.reflectivelyCreate(c);
		this.setIdAndAdd(paxElement, id);
		return paxElement;
	}


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
			this.objectSet.add(aBioPAXElement);
			this.idMap.put(rdfId, aBioPAXElement);
            
        }
	}

	@Basic
    public BioPAXLevel getLevel()
	{
		return level;
	}


    public void setAddDependencies(boolean addDependencies) {
        this.addDependencies = addDependencies;
    }

    public boolean isAddDependencies() {
        return addDependencies;
    }

    public boolean containsID(String id) {
        return idMap.containsKey(id);
    }

	public synchronized void updateID(String oldID, String newID) {
		if (this.containsID(oldID)) {
			BioPAXElement bpe = getByID(oldID);
			remove(bpe);
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
