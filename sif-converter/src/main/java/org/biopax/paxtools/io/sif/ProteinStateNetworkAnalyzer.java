package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class ProteinStateNetworkAnalyzer
{
    public static enum CType
    {
        DIRECT("Protein/controllerOf"),
        VIA_COMPLEX("Protein/componentOf*/controllerOf"),
        VIA_GENERIC("Protein/memberPhysicalEntityOf*/controllerOf");
        PathAccessor accessor;
        CType(String s) 
        {
            accessor = new PathAccessor(s);
        }

        public Set<Control> getControls(Protein protein)
        {
            return new HashSet<Control>(this.accessor.getValueFromBean(protein));
        }       
        
    }

    public void analyzeProteinStates(Model model)
   {
       Set<ProteinReference> prs= model.getObjects(ProteinReference.class);
       

       for (ProteinReference pr : prs) {
           Set<SimplePhysicalEntity> spe= pr.getEntityReferenceOf();
           for (SimplePhysicalEntity simplePhysicalEntity : spe)
           {
               Protein protein = (Protein) spe;
           }

       }
   }
    
    class ProteinStateChange
    {
        Set<EntityFeature> deltaFeature;
        boolean reversible =false;
        Protein left;
        Protein right;
        
        
                


    }
}
