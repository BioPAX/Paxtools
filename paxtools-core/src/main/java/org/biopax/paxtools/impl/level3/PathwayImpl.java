package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;

public class PathwayImpl extends ProcessImpl implements Pathway
{
	private Set<Process> pathwayComponent;
	private Set<PathwayStep> pathwayOrder;
	private BioSource organism;
	private Set<Control> controllerOf;

	public PathwayImpl()
	{
		this.pathwayComponent = BPCollections.I.createSafeSet();
		this.pathwayOrder = BPCollections.I.createSafeSet();
		this.controllerOf = BPCollections.I.createSafeSet();
	}

	public Class<? extends Pathway> getModelInterface()
	{
		return Pathway.class;
	}

	public Set<Process> getPathwayComponent()
	{
		return this.pathwayComponent;
	}

	public void addPathwayComponent(Process component)
	{
		if (component != null) {
			this.pathwayComponent.add(component);
			component.getPathwayComponentOf().add(this);
		}
	}

	public void removePathwayComponent(Process component)
	{
		if (component != null) {
			this.pathwayComponent.remove(component);
			component.getPathwayComponentOf().remove(this);
		}
	}

	public Set<PathwayStep> getPathwayOrder()
	{
		return pathwayOrder;
	}

	public void addPathwayOrder(PathwayStep pathwayOrder)
	{
		if (pathwayOrder != null) {
			this.pathwayOrder.add(pathwayOrder);
			((PathwayStepImpl) pathwayOrder).setPathwayOrderOf(this);
		}
	}

	public void removePathwayOrder(PathwayStep pathwayOrder)
	{
		if (pathwayOrder != null) {
			this.pathwayOrder.remove(pathwayOrder);
			((PathwayStepImpl) pathwayOrder).setPathwayOrderOf(null);
		}
	}

	public BioSource getOrganism()
	{
		return organism;
	}

	public void setOrganism(BioSource organism)
	{
		this.organism = organism;
	}

	public Set<Control> getControllerOf()
	{
		return controllerOf;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		boolean equivalence = false;
		if (element instanceof Pathway) { //super method is not called intentionally (similar to ConversionImpl)
			Pathway other = (Pathway) element;
			equivalence =
					(
					  (organism == null && other.getOrganism() == null)
						  || (organism != null && organism.isEquivalent(other.getOrganism()))
					)
					&& SetEquivalenceChecker.isEquivalent(this.getPathwayComponent(), other.getPathwayComponent())
					&& SetEquivalenceChecker.isEquivalent(this.getPathwayOrder(), other.getPathwayOrder());
		}
		return equivalence;
	}

}
