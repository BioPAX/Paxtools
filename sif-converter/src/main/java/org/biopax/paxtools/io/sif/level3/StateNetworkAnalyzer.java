package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class StateNetworkAnalyzer {
    Map<BioPAXElement, Set<PEStateChange>> stateChangeMap;
    private HashMap<SimplePhysicalEntity, Integer> stateActivityMap;


    public void analyzeStates(Model model) {


        GroupMap groupMap = Grouper.inferGroups(model);
        ModelUtils.replaceEquivalentFeatures(model);

        stateChangeMap = new HashMap<BioPAXElement, Set<PEStateChange>>();
        stateActivityMap = new HashMap<SimplePhysicalEntity, Integer>();

        Set<PEStateChange> stateChanges;
        for (EntityReference pr : model.getObjects(EntityReference.class)) {
            if ((stateChanges = stateChangeMap.get(pr)) == null) {
                stateChanges = new HashSet<PEStateChange>();
                stateChangeMap.put(pr, stateChanges);
            }

            for (SimplePhysicalEntity spe : pr.getEntityReferenceOf()) {
                scanInteractions(groupMap, stateChanges, pr, spe);

            }
        }
    }

    private void scanInteractions(GroupMap groupMap, Set<PEStateChange> stateChanges, EntityReference pr, PhysicalEntity spe) {
        for (Interaction interaction : spe.getParticipantOf()) {
            if (interaction instanceof Conversion) {
                Simplify.entityHasAChange(pr, (Conversion) interaction, groupMap, stateChanges);
            }
        }

        for (PhysicalEntity generic : spe.getMemberPhysicalEntityOf()) {
            scanInteractions(groupMap, stateChanges, pr, generic);
        }

        for (Complex complex : spe.getComponentOf()) {
            scanInteractions(groupMap, stateChanges, pr, complex);
        }
    }

    public Set<SimplePhysicalEntity> getPrecedingStates(SimplePhysicalEntity spe) {
        Set<SimplePhysicalEntity> result = new HashSet<SimplePhysicalEntity>();
        EntityReference er = spe.getEntityReference();
        Set<PEStateChange> peStateChanges = stateChangeMap.get(er);
        for (PEStateChange peStateChange : peStateChanges) {
            SimplePhysicalEntity next = peStateChange.changedInto(spe);
            if (next != null) {
                result.add(peStateChange.left);
            }
        }
        return result;
    }

    public Set<PEStateChange> getAllStates(EntityReference er) {
        return stateChangeMap.get(er);
    }

    public Set<SimplePhysicalEntity> getSucceedingStates(SimplePhysicalEntity spe) {
        Set<SimplePhysicalEntity> result = new HashSet<SimplePhysicalEntity>();
        EntityReference er = spe.getEntityReference();
        Set<PEStateChange> peStateChanges = stateChangeMap.get(er);
        for (PEStateChange peStateChange : peStateChanges) {
            SimplePhysicalEntity next = peStateChange.changedFrom(spe);
            if (next != null) {
                result.add(peStateChange.left);
            }
        }
        return result;
    }


    public void writeStateNetworkAnalysis(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        for (BioPAXElement bpe : stateChangeMap.keySet())
        {
            if (bpe instanceof EntityReference)
            {
                EntityReference er = (EntityReference) bpe;
                Set<PEStateChange> sc = stateChangeMap.get(bpe);
                for (PEStateChange sChange : sc)
                {
                    if(isEligibleProteinModification(sChange))
                    {

                        Set<Pathway> pathwayComponentOf = sChange.getConv().getPathwayComponentOf();
                        for (Pathway pathway : pathwayComponentOf)
                        {
                            writer.write(pathway.getName().toString()+";");
                        }
                        writer.write("\t");
                        writer.write(sChange.getConv().getName().toString());
                        writer.write("\t");
                        writer.write(er.getName().toString());
                        writer.write("\t");
                        writer.write(er.getXref().toString());
                        writer.write("\t");
                        writer.write(sChange.getControllersAsString());
                        writer.write("\t");
                        writer.write(getDeltaControl(sChange));
                        writer.write("\t");
                        writer.write(sChange.getDeltaFeatures().toString());
                        writer.write("\n");
                    }
                 }
            }

        }
        writer.flush();


    }

    private String getDeltaControl(PEStateChange sChange)
    {
        Map<Control, Boolean> dc = sChange.getDeltaControls();
        StringBuilder ctString = new StringBuilder();
        for (Control control : dc.keySet())
        {
            ctString
                    .append(dc.get(control) ? ChangeType.NOT_EXIST_TO_EXIST : ChangeType.EXIST_TO_NOT_EXIST)
                    .append(":")
                    .append(control.getControlType());
            for (Process process : control.getControlled())
            {
                ctString.append(process.getName()).append(" ,");
            }
            ctString.append("; ");


        }
        return ctString.toString();
    }

    private boolean isEligibleProteinModification(PEStateChange sChange)
    {
        if(!sChange.getDeltaControls().isEmpty())
        {
            for (EntityFeature ef : sChange.getDeltaFeatures().keySet())
            {
                if(ef instanceof ModificationFeature &&
                        !sChange.getDeltaFeatures().get(ef).equals(ChangeType.UNCHANGED))
                    
                    return true;
            }
        }
        return false;
    }





}
