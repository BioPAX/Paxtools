package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.model.level3.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

class PhysicalEntityImpl extends EntityImpl implements PhysicalEntity
{

	private CellularLocationVocabulary cellularLocation;
	private Set<PhysicalEntity> memberPhysicalEntity;

	private Set<Complex> ownerComplex;
	private Set<EntityFeature> feature;
	private Set<EntityFeature> noFeature;

	private final Log log = LogFactory.getLog(PhysicalEntityImpl.class);

	public PhysicalEntityImpl()
	{
		feature = new HashSet<EntityFeature>();
		noFeature = new HashSet<EntityFeature>();
		ownerComplex = new HashSet<Complex>();
		this.memberPhysicalEntity = new HashSet<PhysicalEntity>();
	}


	public Class<? extends PhysicalEntity> getModelInterface()
	{
		return PhysicalEntity.class;
	}

	//--------------------------------------------------------Section:In-Complex
	//TODO
	public Set<Complex> getComponentOf()
	{
		return ownerComplex;
	}

	//-----------------------------------------------Section: Cellular Location
	public CellularLocationVocabulary getCellularLocation()
	{
		return cellularLocation;
	}

	public void setCellularLocation(CellularLocationVocabulary location)
	{
		this.cellularLocation = location;
	}

	//------------------------------------------------------Section:Modified at
	public Set<EntityFeature> getFeature()
	{
		return feature;
	}

	public void addFeature(EntityFeature feature)
	{
		checkAndAddFeature(feature, feature.getFeatureOf());
		this.feature.add(feature);
	}


	public void removeFeature(EntityFeature feature)
	{
		checkAndRemoveFeature(feature, feature.getFeatureOf());
		this.feature.remove(feature);

	}

	public void setFeature(Set<EntityFeature> feature)
	{
		this.feature = feature;
	}

	public Set<EntityFeature> getNotFeature()
	{
		return noFeature;
	}


	public void addNotFeature(EntityFeature feature)
	{
		checkAndAddFeature(feature, feature.getNoFeatureOf());
		this.noFeature.add(feature);
	}

	public void removeNotFeature(EntityFeature feature)
	{
		checkAndRemoveFeature(feature, feature.getNoFeatureOf());
		this.noFeature.remove(feature);
	}

	public void setNotFeature(Set<EntityFeature> featureSet)
	{
		this.noFeature = featureSet;
	}

	public Set<PhysicalEntity> getMemberPhysicalEntity()
	{
		return this.memberPhysicalEntity;    //todo
	}

	public void addMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity)
	{
		this.memberPhysicalEntity.add(memberPhysicalEntity);   //todo
	}

	public void removeMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity)
	{
		this.memberPhysicalEntity.remove(memberPhysicalEntity); //todo
	}

	public void setMemberPhysicalEntity(Set<PhysicalEntity> memberPhysicalEntity
	)
	{
		this.memberPhysicalEntity = memberPhysicalEntity;             //todo
	}


	public Class<? extends PhysicalEntity> getPhysicalEntityClass()
	{
		return getModelInterface();
	}

	private void checkAndAddFeature(EntityFeature feature,
	                                Set<PhysicalEntity> target)
	{
		if (feature.getFeatureOf().contains(this) ||
		    feature.getNoFeatureOf().contains(this))
		{
			log.warn("Redundant attempt to set the inverse link!");

		}
		target.add(this);
	}

	private void checkAndRemoveFeature(EntityFeature feature,
	                                   Set<PhysicalEntity> target)
	{
		assert feature.getFeatureOf().contains(this) ^
		       feature.getNoFeatureOf().contains(this);
		target.remove(this);
	}

	// --------------------- Interface BioPAXElement ---------------------



	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof PhysicalEntity)) return false;
		PhysicalEntity that = (PhysicalEntity) element;
		return hasEquivalentCellularLocation(that) 
			&& hasEquivalentFeatures(that)
			&& SetEquivalanceChecker.isEquivalent(getMemberPhysicalEntity(), that.getMemberPhysicalEntity())
			&& super.semanticallyEquivalent(element); // StackOverflow BUG fixed: was isEquivalent !
	}

	@Override
	public int equivalenceCode()
	{
		return hashCode();
	}

	public boolean hasEquivalentCellularLocation(PhysicalEntity that)
	{
		boolean equivalency=false;
		if(that!=null)
		{
			equivalency = (cellularLocation != null) 
				? cellularLocation.isEquivalent(that.getCellularLocation()) 
				: that.getCellularLocation() == null;
		}
		return equivalency;
	}

	public boolean hasEquivalentFeatures(PhysicalEntity that)
	{
		boolean equivalency=false;
		if(that!=null)
		{
			equivalency = SetEquivalanceChecker.isEquivalent(this.getFeature(), that.getFeature()) &&
		       SetEquivalanceChecker.isEquivalent(this.getNotFeature(), that.getNotFeature());
		}
		return equivalency;
	}

	protected int locationAndFeatureCode()
	{
		int result = (cellularLocation != null ? cellularLocation.hashCode() : 0);
		result = 31 * result + (feature != null ? feature.hashCode() : 0);
		result = 31 * result + (noFeature != null ? noFeature.hashCode() : 0);
		return result;

	}

}
