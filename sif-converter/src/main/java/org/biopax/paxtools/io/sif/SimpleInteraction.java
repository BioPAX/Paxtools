package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Set;
import java.util.HashSet;

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

	private Set<publicationXref> pubs;

	Log log = LogFactory.getLog(SimpleInteraction.class);

	public SimpleInteraction(BioPAXElement source, BioPAXElement target,
	                         BinaryInteractionType type)
	{
		this.source = source;
		this.target = target;
		this.type = type;
		this.pubs = new HashSet<publicationXref>();
	}

	public BioPAXElement getSource()
	{
		return source;
	}

	public void setSource(BioPAXElement source)
	{
		this.source = source;
	}

	public BioPAXElement getTarget()
	{
		return target;
	}

	public void setTarget(BioPAXElement target)
	{
		this.target = target;
	}

	public BinaryInteractionType getType()
	{
		return type;
	}

	public void setType(BinaryInteractionType type)
	{
		this.type = type;
	}

	/**
	 * This method checks for source, target and type equality. Provenance and target equality is based
	 * on rdfIDs.
	 *
	 * @param o
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

		return checkNullOrEquals(this.type, that.type) &&
		       checkParticipants(that);


	}

	private boolean checkParticipants(SimpleInteraction that)
	{
		boolean equal;
		equal = (checkNullOrEquals(this.source, that.source) &&
		         checkNullOrEquals(this.target, that.target));
		if (!equal || type == null || !type.isDirected())
		{
			equal = (checkNullOrEquals(this.source, that.target) &&
			         checkNullOrEquals(this.target, that.source));
		}
		return equal;
	}

	private boolean checkNullOrEquals(Object thisObject, Object thatObject)
	{
		return thisObject == null ? thatObject == null :
		       thisObject.equals(thatObject);
	}

	/**
	 * This method returns a hashcode based on the source, target and types hashcodes.
	 *
	 * @return a hashcode based on the source, target and types hashcodes.
	 */
	public int hashCode()
	{
		int result;
		result = (source != null ? source.hashCode() : 0);
		result = (type == null ? 0 : type.isDirected() ? type.hashCode() * 31 : type.hashCode()) *
		         result +
		         (target != null ? target.hashCode() : 0);
		return result;
	}

	public String toString()
	{
		return source.getRDFId() + "\t" + type.toString() + "\t" + target.getRDFId();
	}

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
					interaction.getPubs().addAll(this.getPubs());
					reducedInts.add(interaction);
				}
			}
		}
	}

	private void recursivelyReduce(BioPAXElement bpe, Set<physicalEntity> reduced)
	{

		if (bpe instanceof physicalEntityParticipant)
		{
			recursivelyReduce(((physicalEntityParticipant) bpe).getPHYSICAL_ENTITY(), reduced);
		}
		else
		{
			if (bpe instanceof complex)
			{
				for (physicalEntityParticipant pep : ((complex) bpe).getCOMPONENTS())
				{
					physicalEntity pe = pep.getPHYSICAL_ENTITY();
					recursivelyReduce(pe, reduced);

				}

			}
			else if (bpe instanceof physicalEntity)
			{
				reduced.add((physicalEntity) bpe);
			}
		}


	}


	public Set<publicationXref> getPubs()
	{
		return pubs;
	}

	public void extractPublications(entity anEntity)
	{

		log.trace("extracting pubs from " + anEntity.getNAME() + "(" + anEntity.getRDFId() + ")");
		log.trace(pubs.size());
		getPubs().addAll(
				new ClassFilterSet<publicationXref>(
						anEntity.getXREF(),
						publicationXref.class));

		if (anEntity instanceof interaction)
		{
			Set<evidence> evidenceSet = ((interaction) anEntity).getEVIDENCE();
			for (evidence evidence : evidenceSet)
			{
				getPubs().addAll(
						new ClassFilterSet<publicationXref>(
								evidence.getXREF(),
								publicationXref.class));
			}

		}
		log.trace(pubs.size());
	}

}
