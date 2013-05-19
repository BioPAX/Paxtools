package org.biopax.paxtools.io.sif;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a directed binary interaction between two entities. Most bioinformatics
 * software and algorithms consumes such interactions.
 * <p/>
 * User: demir Date: Dec 28, 2007 Time: 3:33:45 PM
 */
public class SimpleInteraction
{

	/**
	 * This variable stores the first element of the interaction. If the interaction is directed this
	 * is the source node.
	 */
	private BioPAXElement source;

	/**
	 * This variable stores the second element of the interaction. If the interaction is directed this
	 * is the target node.
	 */
	private BioPAXElement target;

	/**
	 * This variable stores the type of the interaction. For extensibity purposes this is not a fixed
	 * enum. For the values that are used by existing rules see #SIF_TYPES. Also all new types should
	 * be registered to http://www.biopaxwiki.org/SIF_TYPES
	 */
	private BinaryInteractionType type;

	/**
	 * Log for logging.
	 */
	Log log = LogFactory.getLog(SimpleInteraction.class);

	/**
	 * Mediators are other relevant BioPAX objects that can be used to get information about the
	 * binary interaction.
	 */
	private Set<BioPAXElement> mediators;

	/**
	 * Constructor with parameters, without mediators.
	 * @param source source of the interaction
	 * @param target target of the interaction
	 * @param type type of the interaction
	 */
	public SimpleInteraction(BioPAXElement source, BioPAXElement target, BinaryInteractionType type)
	{
		this.source = source;
		this.target = target;
		this.type = type;
		this.mediators = new HashSet<BioPAXElement>();
	}

	/**
	 * Constructor with parameters, with mediators.
	 * @param source source of the interaction
	 * @param target target of the interaction
	 * @param type type of the interaction
	 * @param mediator other elements related to the interaction
	 */
	public SimpleInteraction(BioPAXElement source, BioPAXElement target, BinaryInteractionType type,
			BioPAXElement... mediator)
	{
		this(source,target,type,Arrays.asList(mediator));
	}

	/**
	 * Constructor with parameters, with mediators.
	 * @param source source of the interaction
	 * @param target target of the interaction
	 * @param type type of the interaction
	 * @param mediator other elements related to the interaction
	 */
	public SimpleInteraction(BioPAXElement source, BioPAXElement target, BinaryInteractionType type,
			Collection<BioPAXElement> mediator)
	{
		this(source,target,type);
		this.mediators.addAll(mediator);
	}

	/**
	 * Gets  source element.
	 * @return source element
	 */
	public BioPAXElement getSource()
	{
		return source;
	}

	/**
	 * Sets source element.
	 * @param source source element
	 */
	public void setSource(BioPAXElement source)
	{
		this.source = source;
	}

	/**
	 * Gets target element.
	 * @return target element
	 */
	public BioPAXElement getTarget()
	{
		return target;
	}

	/**
	 * Sets target element.
	 * @param target target element
	 */
	public void setTarget(BioPAXElement target)
	{
		this.target = target;
	}

	/**
	 * Gets the type of the interaction.
	 * @return type of the interaction
	 */
	public BinaryInteractionType getType()
	{
		return type;
	}

	/**
	 * Sets the type of the interaction.
	 * @param type type of the interaction
	 */
	public void setType(BinaryInteractionType type)
	{
		this.type = type;
	}

	/**
	 * This method checks for source, target and type equality. Provenance and target equality is
	 * based on rdfIDs.
	 * @param o other object
	 * @return true if o is a simple interaction and its source, target and type is equal to this'.
	 */
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		SimpleInteraction that = (SimpleInteraction) o;

		return checkNullOrEquals(this.type, that.type) && checkParticipants(that);
	}

	/**
	 * Checks if two interactions are semantically the same. This happens when their source and
	 * targets are equal or both null. Also source and target can swap for undirected interaction
	 * types.
	 * @param that other interaction
	 * @return true if two interactions are semantically the same
	 */
	private boolean checkParticipants(SimpleInteraction that)
	{
		boolean equal;
		equal = (checkNullOrEquals(this.source, that.source) && checkNullOrEquals(this.target, that.target));
		if (!equal && (type == null || !type.isDirected()))
		{
			equal = (checkNullOrEquals(this.source, that.target) && checkNullOrEquals(this.target, that.source));
		}
		return equal;
	}

	/**
	 * Checks if the objects are either equal, or both are null.
	 * @param thisObject first object
	 * @param thatObject second object
	 * @return true if the objects are either equal, or both are null
	 */
	private boolean checkNullOrEquals(Object thisObject, Object thatObject)
	{
		return thisObject == null ? thatObject == null : thisObject.equals(thatObject);
	}

	/**
	 * This method returns a hashcode based on the source, target and types hashcodes.
	 * @return a hashcode based on the source, target and types hashcodes.
	 */
	public int hashCode()
	{
        int srcHash = source != null ? source.hashCode() : 0;
        int trgtHash = target != null ? target.hashCode() : 0;
		
		int result = 31 + (type != null ? type.hashCode() : 0);

		if(type != null)
        {
            int directionHash = 1;
            if(!type.isDirected())
            {
                // If it is not directional,
                // then A <-> B and B <-> A should produce
                // the same hash, so order them first
                if(srcHash < trgtHash)
                {
                    int tmpHash = srcHash;
                    srcHash = trgtHash;
                    trgtHash = tmpHash;
                }

                directionHash = 2;
            }

            result = 31 * result + directionHash;
        }

		result = 31 * result + srcHash;
		result = 31 * result + trgtHash;
		return result;
	}

	/**
	 * Returns "source type target".
	 * @return "source type target"
	 */
	public String toString()
	{
		String from = source.getRDFId();
		String to = target == null ? "null" : target.getRDFId();

		return from + "\t" + type + "\t" + to;
	}

	/**
	 * This method is used when we want to use readable names in the simple SIF format instead of
	 * IDs. Note that those names may not be unique. In that case the file will contain duplicate
	 * names. This method iterates fields of the element to find a name.
	 * @param element element to get a name.
	 * @return a name to use in SIF
	 */
	public String getANameForSIF(BioPAXElement element)
	{
		if (element instanceof XReferrable)
		{
			String symbol = getRelatedEntrezGeneID((XReferrable) element);
			if (symbol != null) return symbol;
		}

		if (element instanceof entity)
		{
			entity ent = (entity) element;
			if (ent.getSHORT_NAME() != null && ent.getSHORT_NAME().length() > 0)
			{
				return ent.getSHORT_NAME();
			}
			if (ent.getNAME() != null && ent.getNAME().length() > 0)
			{
				return ent.getNAME();
			}
			if (ent.getSYNONYMS() != null && !ent.getSYNONYMS().isEmpty())
			{
				String synonym = ent.getSYNONYMS().iterator().next();

				if (synonym != null && synonym.length() > 0)
				{
					return synonym;
				}
			}
		}

		return element.getRDFId();
	}

	/**
	 * Searches for the Entrez Gene ID in the references.
	 * @param obj object to search
	 * @return Entrez Gene ID
	 */
	public String getRelatedEntrezGeneID(XReferrable obj)
	{
		for (xref xr : obj.getXREF())
		{
			if (xr.getDB() != null && xr.getDB().equalsIgnoreCase("GENE_SYMBOL") && xr.getID() != null)
			{
				return xr.getID();
			}
		}
		return null;
	}

	/**
	 * If an interaction contains a complex as source or target, then this method creates new
	 * interactions using the members of the complex. If both ends are complex with members of size
	 * n and m, there will be n x m reduced interactions.
	 * @param reducedInts new interactions generated with complex members
	 */
	public void reduceComplexes(Set<SimpleInteraction> reducedInts)
	{
		if (!(this.getType() == BinaryInteractionType.COMPONENT_OF))
		{
			Set<physicalEntity> sourceSet = new HashSet<physicalEntity>();
			Set<physicalEntity> targetSet = new HashSet<physicalEntity>();

			recursivelyReduce(this.getSource(), sourceSet);
			recursivelyReduce(this.getTarget(), targetSet);
			for (physicalEntity source : sourceSet)
			{
				for (physicalEntity target : targetSet)
				{
					SimpleInteraction interaction = new SimpleInteraction(source, target, type);
					interaction.getMediators().addAll(this.getMediators());
					reducedInts.add(interaction);
				}
			}
		}
	}

	/**
	 * Gets member physicalEntity of the complex recursively.
	 * @param bpe element to get the related physicalEntity
	 * @param reduced related physicalEntity set
	 */
	private void recursivelyReduce(BioPAXElement bpe, Set<physicalEntity> reduced)
	{
		if (bpe instanceof physicalEntityParticipant)
		{
			recursivelyReduce(((physicalEntityParticipant) bpe).getPHYSICAL_ENTITY(), reduced);
		} else
		{
			if (bpe instanceof complex)
			{
				for (physicalEntityParticipant pep : ((complex) bpe).getCOMPONENTS())
				{
					physicalEntity pe = pep.getPHYSICAL_ENTITY();
					recursivelyReduce(pe, reduced);

				}

			} else if (bpe instanceof physicalEntity)
			{
				reduced.add((physicalEntity) bpe);
			}
		}
	}

	/**
	 * Gets other related elements of the interaction.
	 * @return mediators
	 */
	public Set<BioPAXElement> getMediators()
	{
		return mediators;
	}

	/**
	 * Adds a mediator to the interaction.
	 * @param element mediator
	 */
	public void addMediator(BioPAXElement element)
	{
		this.mediators.add(element);
	}
}
