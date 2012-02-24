package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.Map;

/**
 */
public class PEStateChange
{
    Map<EntityFeature,ChangeType> deltaFeatures;
    Conversion conv;
    SimplePhysicalEntity left;
    SimplePhysicalEntity right;


    public PEStateChange(SimplePhysicalEntity left, SimplePhysicalEntity right, BioPAXElement element, Conversion conv)
    {
        this.left = left;
        this.right= right;
        this.conv = conv;
        this.deltaFeatures=ChangeType.getDeltaFeatures(left,right);
    }

    @Override
    public String toString()
    {
        return this.left.getStandardName()+"--"+this.right.getDisplayName()+"("+this.deltaFeatures+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PEStateChange)) return false;

        PEStateChange that = (PEStateChange) o;

        if (conv != null ? !conv.equals(that.conv) : that.conv != null) return false;
        if (deltaFeatures != null ? !deltaFeatures.equals(that.deltaFeatures) : that.deltaFeatures != null)
            return false;
        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;

        return true;
    }

    public Map<EntityFeature, ChangeType> getDeltaFeatures() {
        return deltaFeatures;
    }

    public Conversion getConv() {
        return conv;
    }

    public SimplePhysicalEntity getLeft() {
        return left;
    }

    public SimplePhysicalEntity getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        int result = deltaFeatures != null ? deltaFeatures.hashCode() : 0;
        result = 31 * result + (conv != null ? conv.hashCode() : 0);
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
