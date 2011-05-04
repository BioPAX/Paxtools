package org.biopax.paxtools.fixer;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import java.util.Set;

import static org.biopax.paxtools.controller.FeatureUtils.findFeaturesAddedToSecond;


/**
 * This class takes a L3 model and attempts to resolve features and not features within the context of that model.
 * It can aggressively resolve to a complete closed world semantics or can operate on a "relaxed" mode where it resolves
 * only when certain.
 * 
 * TODO merge this with the {@link ModelUtils} ?
 */
public class FeatureResolver {

    private boolean fix;

    ShallowCopy copier = new ShallowCopy();

    public void resolveFeatures(Model model) {
        if (!model.getLevel().equals(BioPAXLevel.L3)) {
            //Log error
        } else {
            resolveBindingFeatures(model);


            //For each entity reference:
            for (EntityReference er : model.getObjects(EntityReference.class))
            {
                for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
                {
                    for (Interaction interaction : spe.getParticipantOf())
                    {
                      //we will do this left to right
                      if(interaction instanceof Conversion)
                      {
                          Conversion cnv = (Conversion) (interaction);
                          if(cnv.getLeft().contains(spe))
                          {
                              for (PhysicalEntity physicalEntity : cnv.getRight())
                              {
                                if(physicalEntity instanceof SimplePhysicalEntity)
                                {
                                    SimplePhysicalEntity otherSPE = (SimplePhysicalEntity) (physicalEntity);
                                    if (otherSPE.getEntityReference().equals(spe.getEntityReference()))
                                    {
                                        Set<EntityFeature> added =
                                                findFeaturesAddedToSecond(physicalEntity, otherSPE,fix);
                                        Set<EntityFeature> removed =
                                                findFeaturesAddedToSecond(otherSPE,physicalEntity,fix);



                                    }
                                    }
                                }
                                //HANDLE complexes
                              }
                          }
                      }
                    }
                }



            }
        }



    private void resolveBindingFeatures(Model model) {
        //For each Complex
        Set<Complex> complexes = model.getObjects(Complex.class);
        for (Complex complex : complexes) {
            resolveBindingFeatures(model, complex);


        }
    }

    private void resolveBindingFeatures(Model model, Complex complex) {
        Set<PhysicalEntity> components = complex.getComponent();
        for (PhysicalEntity component : components) {
            resolveFeaturesOfComponent(model, complex, component);
        }
    }

    private void resolveFeaturesOfComponent(Model model, Complex complex, PhysicalEntity component) {
        boolean connected = false;
        Set<EntityFeature> feature = component.getFeature();
        for (EntityFeature ef : feature) {
            if (ef instanceof BindingFeature) {
                BindingFeature bindsTo = ((BindingFeature) ef).getBindsTo();
                Set<PhysicalEntity> featureOf = bindsTo.getFeatureOf();
                if (!SetEquivalanceChecker.isEquivalentIntersection(complex.getComponent(), featureOf)) {
                    System.err.println(
                            "The Complex" + complex.getName() + "(" + complex.getRDFId() + ") has  component"
                                    + component.getDisplayName() + "(" + component.getRDFId() + ") which has" +
                                    "a binding feature (" + ef.getRDFId() + "), but none of the bound " +
                                    "participants are in this complex");
                    //TODO This is an error - fail.
                    return ;
                } else {
                    connected = true;

                }
            }
        }
        if (!connected && fix)
        {
            Set<Interaction> participantOf = component.getParticipantOf();
            for (Interaction interaction : participantOf)
            {
                //It is ok for complex members to control a participant
                if(!(interaction instanceof Control) )
                {
                    component = createCopy(model, complex, component);
                    break;
                }
            }

            BindingFeature bf = model.addNew(
                BindingFeature.class, component.getRDFId() + "bond" + "in_Complex_"+complex.getRDFId());
            component.addFeature(bf);
            if(component instanceof SimplePhysicalEntity)
            {
                ((SimplePhysicalEntity) component).getEntityReference().addEntityFeature(bf);
            }
        }
    }

    private PhysicalEntity createCopy(Model model, Complex complex, PhysicalEntity component) {
        //This is an aggressive fix - if a complex member is present in both an interaction that is not a control
        // and a complex, we are creating clone, adding it a binding feature to mark it  and put it  into the
        // complex and remove the old one.
        complex.removeComponent(component);
        component = copier.copy(
                model, component, component.getRDFId() + "in_Complex_" + complex.getRDFId());
        complex.addComponent(component);
        return component;
    }


}
