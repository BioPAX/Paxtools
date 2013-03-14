package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Emek Demir // todo annotate
 */
public class PEStateChange {

    Map<EntityFeature, ChangeType> deltaFeatures;
    HashMap<Control, Boolean> deltaControls = new HashMap<Control, Boolean>();

    private PhysicalEntity leftRoot;
    private PhysicalEntity rightRoot;
    Conversion conv;
    SimplePhysicalEntity left;
    SimplePhysicalEntity right;
    static PathAccessor controllers = new PathAccessor("Conversion/controlledOf/controller");


    public PEStateChange(
            SimplePhysicalEntity left,
            SimplePhysicalEntity right,
            PhysicalEntity leftRoot,
            PhysicalEntity rightRoot,
            BioPAXElement element,
            Conversion conv) {
        this.left = left;
        this.right = right;
        this.leftRoot = leftRoot;
        this.rightRoot = rightRoot;
        this.conv = conv;
        this.deltaFeatures = ChangeType.getDeltaFeatures(left, right, leftRoot, rightRoot);
        calculateDeltaControls();
    }


    @Override
    public String toString() {
        return this.left.getStandardName() + "--" + this.right.getDisplayName() + "(" + this.deltaFeatures + ")";
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

    public SimplePhysicalEntity changedFrom(SimplePhysicalEntity source) {
        if (this.left == source) return getFlow(true, true);
        else if (this.right == source) return getFlow(false, false);
        else return null;
    }

    public SimplePhysicalEntity changedInto(SimplePhysicalEntity source) {
        if (this.left == source) return getFlow(true, false);
        else if (this.right == source) return getFlow(false, true);
        else return null;
    }

    private SimplePhysicalEntity getFlow(boolean onLeft, boolean forward) {
        boolean flow = canFlow(onLeft, forward);

        return flow ? onLeft ? this.right : this.left : null;
    }

    /**
     * This method returns true iff conversion can go from/to the side specified by the first parameter in the direction
     * specified by the second parameter
     *
     * @param targetSide true if flow needs to be checked from/to the left side false otherwise
     * @param forward    true if we are asking if a flow can start from the targetSide, false if we are asking if a flow
     *                   can go to the targetSide.
     * @return
     */
    private boolean canFlow(boolean targetSide, boolean forward) {
        boolean flow = true;
        ConversionDirectionType cd = conv.getConversionDirection();
        if (cd != null)
            switch (cd) {
                case LEFT_TO_RIGHT:
                    flow = !(targetSide ^ forward);
                    break;
                case RIGHT_TO_LEFT:
                    flow = (targetSide ^ forward);
                    break;
                default:
                    flow = true;

            }
        return flow;
    }

    @Override
    public int hashCode() {
        int result = deltaFeatures != null ? deltaFeatures.hashCode() : 0;
        result = 31 * result + (conv != null ? conv.hashCode() : 0);
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    public PhysicalEntity getLeftRoot() {
        return leftRoot;
    }

    public PhysicalEntity getRightRoot() {
        return rightRoot;
    }

    public Map<Control, Boolean> getDeltaControls() {
        return deltaControls;

    }

    private void calculateDeltaControls() {
        Set<Control> left = this.leftRoot == null ? null : this.getLeftRoot().getControllerOf();
        Set<Control> right = this.rightRoot == null ? null : this.getRightRoot().getControllerOf();

        if (left != null)
            for (Control control : left) {
                deltaControls.put(control, true);
            }
        if (right != null)
            for (Control control : right) {
                if (deltaControls.containsKey(control)) {
                    deltaControls.remove(control);
                } else
                    deltaControls.put(control, false);

            }

    }

    public String getControllersAsString() {
        Set valueFromBean = controllers.getValueFromBean(conv);
        StringBuilder value = new StringBuilder();
        for (Object o : valueFromBean) {
            Controller controller = (Controller) o;
            value.append(controller.getName()).append( "[");
	        appendXrefs(controller, value);
	        value.append( "] ");
        }
        return value.toString();
    }

	private void appendXrefs(Controller controller, StringBuilder builder)
	{
		HashSet<SimplePhysicalEntity> simple = new HashSet<SimplePhysicalEntity>();
        if(controller instanceof PhysicalEntity) {
            Simplify.getSimpleMembers((PhysicalEntity) controller, simple);
            for (SimplePhysicalEntity spe : simple)
            {

                builder.append("(").append(spe.getEntityReference().getXref()).append(")");
            }
        }
	}
}
