package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.util.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;


public class PhysicalEntityImpl extends EntityImpl implements PhysicalEntity
{
	private CellularLocationVocabulary cellularLocation;
	private Set<PhysicalEntity> memberPhysicalEntity;
	private Set<Complex> componentOf;
	private Set<EntityFeature> feature;
	private Set<EntityFeature> notFeature;
	private Set<Control> controllerOf;
	private static final Log log = LogFactory.getLog(PhysicalEntityImpl.class);
	private Set<PhysicalEntity> memberPhysicalEntityOf;

	public PhysicalEntityImpl()
	{
		feature = BPCollections.I.createSafeSet();
		notFeature = BPCollections.I.createSafeSet();
		controllerOf = BPCollections.I.createSafeSet();
		componentOf = BPCollections.I.createSafeSet();
		memberPhysicalEntityOf = BPCollections.I.createSafeSet(); //TODO make generic?
		memberPhysicalEntity = BPCollections.I.createSafeSet();
	}

	public Class<? extends PhysicalEntity> getModelInterface()
	{
		return PhysicalEntity.class;
	}

	public Set<Complex> getComponentOf()
	{
		return componentOf;
	}

	public CellularLocationVocabulary getCellularLocation()
	{
		return cellularLocation;
	}

	public void setCellularLocation(CellularLocationVocabulary location)
	{
		this.cellularLocation = location;
	}

	public Set<EntityFeature> getFeature()
	{
		return feature;
	}

	public void addFeature(EntityFeature feature)
	{
		if (feature != null) {
			this.feature.add(feature);
			feature.getFeatureOf().add(this);
		}
	}

	public void removeFeature(EntityFeature feature)
	{
		if (feature != null) {
			this.feature.remove(feature);
			feature.getFeatureOf().remove(this);
		}
	}

	public Set<EntityFeature> getNotFeature()
	{
		return notFeature;
	}

	public void addNotFeature(EntityFeature feature)
	{
		if (feature != null) {
			this.notFeature.add(feature);
			feature.getNotFeatureOf().add(this);
		}
	}

	public void removeNotFeature(EntityFeature feature)
	{
		if (feature != null) {
			this.notFeature.remove(feature);
			feature.getNotFeatureOf().remove(this);
		}
	}

	public Set<PhysicalEntity> getMemberPhysicalEntity()
	{
		return this.memberPhysicalEntity;    //TODO (what?)
	}

	public void addMemberPhysicalEntity(PhysicalEntity newMember)
	{
		if (newMember != null) {
			this.memberPhysicalEntity.add(newMember);
			newMember.getMemberPhysicalEntityOf().add(this);
		}
	}

	public void removeMemberPhysicalEntity(PhysicalEntity oldMember)
	{
		if (oldMember != null) {
			this.memberPhysicalEntity.remove(oldMember);
			oldMember.getMemberPhysicalEntityOf().remove(this);
		}
	}

	public Set<PhysicalEntity> getMemberPhysicalEntityOf()
	{
		return memberPhysicalEntityOf;
	}

	// --------------------- Interface BioPAXElement ---------------------


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if (!(element instanceof PhysicalEntity))
			return false;

		PhysicalEntity that = (PhysicalEntity) element;
		return hasEquivalentCellularLocation(that)
		       && hasEquivalentFeatures(that)
		       && SetEquivalenceChecker
				.isEquivalent(getMemberPhysicalEntity(), that.getMemberPhysicalEntity())
		       &&
		       super.semanticallyEquivalent(element); // StackOverflow BUG fixed: was isEquivalent !
	}

	@Override
	public int equivalenceCode()
	{
		return hashCode();
	}

	public boolean hasEquivalentCellularLocation(PhysicalEntity that)
	{
		boolean equivalency = false;
		if (that != null)
		{
			equivalency = (cellularLocation != null)
			              ? cellularLocation.isEquivalent(that.getCellularLocation())
			              : that.getCellularLocation() == null;
		}
		return equivalency;
	}

	public boolean hasEquivalentFeatures(PhysicalEntity that)
	{
		boolean equivalency = false;
		if (that != null)
		{
			equivalency =
					SetEquivalenceChecker.isEquivalent(this.getFeature(), that.getFeature()) &&
					SetEquivalenceChecker.isEquivalent(this.getNotFeature(), that.getNotFeature());
		}
		return equivalency;
	}

	protected int locationAndFeatureCode()
	{
		int result = (cellularLocation != null ? cellularLocation.hashCode() : 0);
		result = 31 * result + (feature != null ? feature.hashCode() : 0);
		result = 31 * result + (notFeature != null ? notFeature.hashCode() : 0);
		return result;

	}

	public Set<Control> getControllerOf()
	{
		return controllerOf;
	}

}
