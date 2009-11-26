package org.biopax.paxtools.proxy;

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

/**
 * Main container class to access BioPAX objects.
 */
public class ModelForPersistence implements Model
{
// ------------------------------ FIELDS ------------------------------

	private Map<String, BioPAXElement> idMap;
	private Set<BioPAXElement> objectSet;
	private Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private BioPAXFactory factory;
    private Map<String, BioPAXElement> exposedIdMap;
    private Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;

// --------------------------- CONSTRUCTORS ---------------------------

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
    public Map<String, BioPAXElement> getIdMap()
	{
        return exposedIdMap;
	}

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

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Model ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<BioPAXElement> getObjects()
	{
		return exposedObjectSet;
	}

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
}
