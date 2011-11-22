package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.fixer.Fixer;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.util.AccessibleSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**

 */
public class Grouper
{
	private static final Log log = LogFactory.getLog(Grouper.class);

	public static Map<BioPAXElement, Group> inferGroups(Model model)
	{
		Map<BioPAXElement, Group> element2GroupMap = new HashMap<BioPAXElement, Group>();
		AccessibleSet<Group> groups = new AccessibleSet<Group>();
		Map<BioPAXElement, Set<Group>> delegated = new HashMap<BioPAXElement, Set<Group>>();
		Fixer.normalizeGenerics(model);
		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			addIfNotNull(element2GroupMap, groups, er, inferGroupFromER(er, delegated));
		}
		for (Complex complex : model.getObjects(Complex.class))
		{
			addIfNotNull(element2GroupMap, groups, complex, inferGroupFromComplex(complex, delegated));
		}
		return element2GroupMap;
	}

	private static void addIfNotNull(Map<BioPAXElement, Group> element2GroupMap, AccessibleSet<Group> groups,
			BioPAXElement element, final Group group)
	{
		if (group != null)
		{
			Group equivalentGroup = groups.access(group);
			if(equivalentGroup==null)
			{
				equivalentGroup=group;
				groups.add(equivalentGroup);
			}
			else
			{
			   equivalentGroup.sources.addAll(group.sources);
			}
			element2GroupMap.put(element, equivalentGroup);
		}

	}

	private static Group inferGroupFromComplex(Complex complex, Map<BioPAXElement, Set<Group>> delegated)
	{
		Group group = new Group(BinaryInteractionType.COMPONENT_OF, complex);
		boolean full = false;
		Set<PhysicalEntity> PElvlMembers = complex.getMemberPhysicalEntity();
		if (PElvlMembers.isEmpty())
		{
			for (PhysicalEntity component : complex.getComponent())
			{
				if (component instanceof SimplePhysicalEntity)
				{
					EntityReference er = ((SimplePhysicalEntity) component).getEntityReference();

					if (er != null && er.getMemberEntityReference().isEmpty())
					{
						group.addMember(er);
					} else
					{
						//must be generic
						delegateTo(component, group, delegated);
					}
				} else if (component instanceof Complex)
				{
					delegateTo(component, group, delegated);
				}
			}
		}
		else
		{
			//If this is a reactome generic it should not have any components?
			if (!complex.getComponent().isEmpty())
			{
				log.info("Generic complex with both membership types (" + complex.getRDFId() + "). Skipping.");
			} else
			{
				group = new Group(BinaryInteractionType.GENERIC_OF, complex);
				for (PhysicalEntity member : PElvlMembers)
				{
					if (member instanceof Complex)
					{
						delegateTo(member, group, delegated);
					} else
					{
						log.info("Non complex PE member for complex (" + member.getRDFId() + "->" + complex
								.getRDFId() +
						         "). Skipping");
					}
				}

			}
		}
		Set<Group> groups = delegated.get(complex);
		if (groups != null)
		{
			for (Group owner : groups)
			{
				owner.addSubgroup(group);
			}
		}
		return group;
	}

	private static void delegateTo(BioPAXElement member, Group owner, Map<BioPAXElement, Set<Group>> delegated)
	{
		Set<Group> groups = delegated.get(member);
		if (groups == null)
		{
			groups = new HashSet<Group>();
			delegated.put(member, groups);
		}
		groups.add(owner);
	}

	private static Group inferGroupFromER(EntityReference element, Map<BioPAXElement, Set<Group>> delegated)
	{
		Group group = new Group(BinaryInteractionType.GENERIC_OF, element);
		for (EntityReference member : element.getMemberEntityReference())
		{
			group.addMember(member);
		}
		for (PhysicalEntity pe : element.getEntityReferenceOf())
		{
			for (PhysicalEntity member : pe.getMemberPhysicalEntity())
			{
				if (member instanceof SimplePhysicalEntity)
				{
					EntityReference memberEr = ((SimplePhysicalEntity) member).getEntityReference();
					if (memberEr != null)
					{
						group.addMember(memberEr);
					}
				}
			}
		}
		Set<Group> owners = delegated.get(element);
		if (owners != null)
		{
			for (Group owner : owners)
			{
				owner.addSubgroup(group);
			}
		}
		return group.isEmpty() ? null : group;
	}
}
