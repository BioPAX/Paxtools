package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.util.AccessibleSet;

import java.util.*;

/**

 */
public class Grouper
{
	private static final Log log = LogFactory.getLog(Grouper.class);

	Map<BioPAXElement, Group> element2GroupMap = new HashMap<BioPAXElement, Group>();

	AccessibleSet<Group> groups = new AccessibleSet<Group>();

	Map<BioPAXElement, Set<Group>> delegated = new HashMap<BioPAXElement, Set<Group>>();


	Set<EntityReference> ersToBeGrouped ;

	Set<Complex> complexesToBeGrouped;

	public static GroupMap inferGroups(Model model)
	{
		ModelUtils.normalizeGenerics(model);
		Grouper grouper =new Grouper();
		return new GroupMap(grouper.inferGroups(model, grouper));
	}

	private Map<BioPAXElement, Group> inferGroups(Model model, Grouper grouper)
	{
		ersToBeGrouped = new HashSet<EntityReference>(model.getObjects(EntityReference.class));
		complexesToBeGrouped = new HashSet<Complex>(model.getObjects(Complex.class));

		for (EntityReference er : ersToBeGrouped)
		{
			addIfNotNull(er, inferGroupFromER(er, model));
		}
		for (Complex complex : complexesToBeGrouped)
		{
			addIfNotNull(complex, inferGroupFromComplex(complex, model));
		}

		return element2GroupMap;

	}

	private void addIfNotNull(BioPAXElement element, final Group group)
	{
		if (group != null)
		{
			Group equivalentGroup = groups.access(group);
			if (equivalentGroup == null)
			{
				equivalentGroup = group;
				groups.add(equivalentGroup);
			} else
			{
				equivalentGroup.sources.addAll(group.sources);
			}
			element2GroupMap.put(element, equivalentGroup);
		}

	}

	private Group inferGroupFromComplex(Complex complex, Model model)
	{
		Group group = new Group(BinaryInteractionType.COMPONENT_OF, complex);
		Set<PhysicalEntity> PElvlMembers = complex.getMemberPhysicalEntity();
		if (PElvlMembers.isEmpty())
		{
			for (PhysicalEntity component : complex.getComponent())
			{
				if (component instanceof SimplePhysicalEntity)
				{
					EntityReference er = this.element2GroupMap.get(component);
					if(er==null)
						 er = ((SimplePhysicalEntity) component).getEntityReference();

					if (er != null)
					{
						group.addMember(er);
					} else
					{
						addOrDelegate(component, group);
					}
				} else if (component instanceof Complex)
				{
					addOrDelegate(component, group);
				}
			}
		} else
		{
			//If this is a reactome generic it should not have any components?
			if (!complex.getComponent().isEmpty())
			{
				log.info("Generic complex with both membership types (" + complex.getRDFId() + "). Skipping.");
			} else
			{
				group = new Group(BinaryInteractionType.GENERIC_OF, complex);
				group.genericType= Complex.class;
				for (PhysicalEntity member : PElvlMembers)
				{
					if (member instanceof Complex)
					{
						addOrDelegate(member, group);
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
		ModelUtils.copySimplePointers(model, complex, group);
		return group;
	}

	private void addOrDelegate(BioPAXElement member, Group owner)
	{
		Group subgroup = element2GroupMap.get(member);
		if (subgroup != null) owner.addSubgroup(subgroup);
		else
		{
			Set<Group> groups = delegated.get(member);
			if (groups == null)
			{
				groups = new HashSet<Group>();
				delegated.put(member, groups);
			}
			groups.add(owner);
		}
	}

	private Group inferGroupFromER(EntityReference element, Model model)
	{
		Group group = new Group(BinaryInteractionType.GENERIC_OF, element);

		for (EntityReference member : element.getMemberEntityReference())
		{
			if(group.type==null)
			{
				group.genericType = member.getModelInterface();
			}
			group.addMember(member);
		}

		Set<Group> owners = delegated.get(element);
		if (owners != null)
		{
			for (Group owner : owners)
			{
				owner.addSubgroup(group);
			}
		}
		ModelUtils.copySimplePointers(model, element, group);
		return group.isEmpty() ? null : group;
	}
}
