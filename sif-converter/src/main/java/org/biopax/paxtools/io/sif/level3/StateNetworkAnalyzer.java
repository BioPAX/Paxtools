package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

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
    Map<BioPAXElement, Set<PEStateChange>> stateChanges;

    public static enum CType {

        DIRECT("SimplePhysicalEntity/controllerOf"),
        VIA_COMPLEX("SimplePhysicalEntity/componentOf*/controllerOf"),
        VIA_GENERIC("SimplePhysicalEntity/memberPhysicalEntityOf*/controllerOf");

        PathAccessor accessor;

        CType(String s) {
            accessor = new PathAccessor(s);
        }

        public Set<Control> getControls(SimplePhysicalEntity spe) {
            return new HashSet<Control>(this.accessor.getValueFromBean(spe));
        }
    }

    public void analyzeStates(Model model) {

        GroupMap groupMap = Grouper.inferGroups(model);
        stateChanges = new HashMap<BioPAXElement, Set<PEStateChange>>();
        for (EntityReference pr : model.getObjects(EntityReference.class)) {
            for (SimplePhysicalEntity spe : pr.getEntityReferenceOf()) {
                for (Interaction interaction : spe.getParticipantOf()) {
                    if (interaction instanceof Conversion) {
                        Conversion conv = (Conversion) interaction;
                        Simplify.entityHasAChange(pr, conv, groupMap, stateChanges);
                    }
                }
            }

        }
    }

    public Set<SimplePhysicalEntity> getPrecedingStates(SimplePhysicalEntity spe) {
        Set<SimplePhysicalEntity> result = new HashSet<SimplePhysicalEntity>();
        EntityReference er = spe.getEntityReference();
        Set<PEStateChange> peStateChanges = stateChanges.get(er);
        for (PEStateChange peStateChange : peStateChanges) {
            if (peStateChange.right.equals(spe)) {
                result.add(peStateChange.left);
            }
        }
        return result;
    }

    public Set<PEStateChange> getAllStates(SimplePhysicalEntity spe) {
        return stateChanges.get(spe);
    }

    public Set<SimplePhysicalEntity> getSucceedingStates(SimplePhysicalEntity spe) {
        Set<SimplePhysicalEntity> result = new HashSet<SimplePhysicalEntity>();
        EntityReference er = spe.getEntityReference();
        Set<PEStateChange> peStateChanges = stateChanges.get(er);
        for (PEStateChange peStateChange : peStateChanges) {
            if (peStateChange.left.equals(spe)) {
                result.add(peStateChange.right);
            }
        }
        return result;
    }

    public void writeStateNetworkAnalysis(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        for (BioPAXElement bpe : stateChanges.keySet()) {
            if (bpe instanceof EntityReference) {
                EntityReference er = (EntityReference) bpe;
                writer.write("\nEntity:" + er.getName() + "Refs:" + er.getXref() + "\n");
                for (PEStateChange sChange : stateChanges.get(bpe))
                {
                    writer.write("\n STATE CHANGE\n");
                    writeState(writer, sChange.left, "Left");
                    writeState(writer, sChange.right, "Right");

                    Map<EntityFeature, ChangeType> deltaFeatures = sChange.deltaFeatures;
                    if(!deltaFeatures.isEmpty())
                    writer.write("\nFeatures that are changed:\n");
                    for (EntityFeature ef : deltaFeatures.keySet()) {
                        writer.write(ef + ":" + deltaFeatures.get(ef) + "\n");
                    }
                }
                writer.write("\n");
            }

        }
        writer.flush();

    }

    private void writeState(Writer writer, SimplePhysicalEntity spe,
                            String side) throws IOException {
        writer.write(side + ":" + spe.getName());
        boolean knownControl=false;
        for (CType cType : CType.values()) {
            Set<Control> controls = cType.getControls(spe);
            if (!controls.isEmpty())
            {
                if(!knownControl)
                {
                    knownControl=true;
                    writer.write("\nControls:\n");
                }
                writer.write(cType + ":");
                for (Control control : controls)
                {
                    writer.write("\t\t"+control.getControlType()+":"+ control.getName() + "\n");
                }
            }
        }
        writer.write("\n");
    }

}
