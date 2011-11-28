package org.biopax.paxtools.fixer;

import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import java.util.HashSet;
import java.util.Set;

import static org.biopax.paxtools.controller.FeatureUtils.findFeaturesAddedToSecond;

/**
 * This class is a container for several methods for fixing common issues and normalizing BioPAX models
 */
public class Fixer
{

	public static void normalizeGenerics(Model model)
	{

		Set<SimplePhysicalEntity> pes = model.getObjects(SimplePhysicalEntity.class);
		Set<SimplePhysicalEntity> pesToBeNormalized = new HashSet<SimplePhysicalEntity>();
		for (SimplePhysicalEntity pe : pes)
		{
			if (pe.getEntityReference() == null)
			{
				if (!pe.getMemberPhysicalEntity().isEmpty())
				{
					pesToBeNormalized.add(pe);
				}
			}
		}
		for (SimplePhysicalEntity pe : pesToBeNormalized)
		{
			try
			{
				createNewERandAddMembers(model, pe);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}


		}
	}

	private static void createNewERandAddMembers(Model model, SimplePhysicalEntity pe)
	{

		SimplePhysicalEntity first = (SimplePhysicalEntity) pe.getMemberPhysicalEntity().iterator().next();
		String syntheticId = "http://biopax.org/generated/fixer/Fixer/normalizeGenerics/" + pe.getRDFId();
		EntityReference generic =
				(EntityReference) model.addNew(first.getEntityReference().getModelInterface(), syntheticId);
		pe.setEntityReference(generic);
		copySimplePointers(model, pe, generic);

		for (EntityReference member : pe.getGenericEntityReferences())
		{
			generic.addMemberEntityReference(member);
		}
	}

	public static void copySimplePointers(Model model, Named pe, Named generic)
	{
		for (String name : pe.getName())
		{
			generic.addName(name);
		}
		for (Xref xref : pe.getXref())
		{
			if ((xref instanceof UnificationXref))
			{
				String id = "http://biopax.org/synthetic/u2r/" + xref.getRDFId();
				BioPAXElement byID = model.getByID(id);
				if (byID == null)
				{
					RelationshipXref rref = model.addNew(RelationshipXref.class, id);
					rref.setDb(xref.getDb());
					rref.setId(xref.getId());
					rref.setDbVersion(xref.getDbVersion());
					rref.setIdVersion(xref.getDbVersion());
					xref = rref;
				}
				else
				{
					xref= (Xref) byID;
				}
			}
			generic.addXref(xref);
		}
	}

	public void resolveFeatures(Model model)
	{
		if (!model.getLevel().equals(BioPAXLevel.L3))
		{
			//TODO Log error
		} else
		{
			resolveBindingFeatures(model);

			//For each entity reference:
			for (EntityReference er : model.getObjects(EntityReference.class))
			{
				for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
				{
					for (Interaction interaction : spe.getParticipantOf())
					{
						//we will do this left to right
						if (interaction instanceof Conversion)
						{
							Conversion cnv = (Conversion) (interaction);
							if (cnv.getLeft().contains(spe))
							{
								for (PhysicalEntity physicalEntity : cnv.getRight())
								{
									if (physicalEntity instanceof SimplePhysicalEntity)
									{
										SimplePhysicalEntity otherSPE = (SimplePhysicalEntity) (physicalEntity);
										if (otherSPE.getEntityReference().equals(spe.getEntityReference()))
										{
											Set<EntityFeature> added =
													findFeaturesAddedToSecond(physicalEntity, otherSPE, true);
											Set<EntityFeature> removed =
													findFeaturesAddedToSecond(otherSPE, physicalEntity, true);
										}
									}
								}
								//TODO HANDLE complexes?
							}
						}
					}
				}
			}
		}
	}


	private void resolveBindingFeatures(Model model)
	{
		ShallowCopy copier = new ShallowCopy(BioPAXLevel.L3);

		//For each Complex
		Set<Complex> complexes = model.getObjects(Complex.class);
		for (Complex complex : complexes)
		{
			resolveBindingFeatures(model, complex, copier);


		}
	}

	private void resolveBindingFeatures(Model model, Complex complex, ShallowCopy copier)
	{
		Set<PhysicalEntity> components = complex.getComponent();
		for (PhysicalEntity component : components)
		{
			resolveFeaturesOfComponent(model, complex, component, copier);
		}
	}

	private void resolveFeaturesOfComponent(Model model, Complex complex, PhysicalEntity component,
			ShallowCopy copier)
	{
		boolean connected = false;
		Set<EntityFeature> feature = component.getFeature();
		for (EntityFeature ef : feature)
		{
			if (ef instanceof BindingFeature)
			{
				BindingFeature bindsTo = ((BindingFeature) ef).getBindsTo();
				Set<PhysicalEntity> featureOf = bindsTo.getFeatureOf();
				if (!SetEquivalanceChecker.isEquivalentIntersection(complex.getComponent(), featureOf))
				{
					System.err.println(
							"The Complex" + complex.getName() + "(" + complex.getRDFId() + ") has  component" +
							component.getDisplayName() + "(" + component.getRDFId() + ") which has" +
							"a binding feature (" + ef.getRDFId() + "), but none of the bound " +
							"participants are in this complex");
					//TODO This is an error - fail.
					return;
				} else
				{
					connected = true;

				}
			}
		}
		if (!connected)
		{
			Set<Interaction> participantOf = component.getParticipantOf();
			for (Interaction interaction : participantOf)
			{
				//It is ok for complex members to control a participant
				if (!(interaction instanceof Control))
				{
					component = createCopy(model, complex, component, copier);
					break;
				}
			}

			BindingFeature bf = model.addNew(BindingFeature.class,
			                                 component.getRDFId() + "bond" + "in_Complex_" + complex.getRDFId());
			component.addFeature(bf);
			if (component instanceof SimplePhysicalEntity)
			{
				((SimplePhysicalEntity) component).getEntityReference().addEntityFeature(bf);
			}
		}
	}

	private PhysicalEntity createCopy(Model model, Complex complex, PhysicalEntity component, ShallowCopy copier)
	{
		//This is an aggressive fix - if a complex member is present in both an interaction that is not a control
		// and a complex, we are creating clone, adding it a binding feature to mark it  and put it  into the
		// complex and remove the old one.
		complex.removeComponent(component);
		component = copier.copy(model, component, component.getRDFId() + "in_Complex_" + complex.getRDFId());
		complex.addComponent(component);
		return component;
	}

}
