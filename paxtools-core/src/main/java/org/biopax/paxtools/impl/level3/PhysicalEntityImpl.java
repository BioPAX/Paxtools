package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.biopax.paxtools.util.DataSourceFilterFactory;
import org.biopax.paxtools.util.OrganismFieldBridge;
import org.biopax.paxtools.util.OrganismFilterFactory;
import org.biopax.paxtools.util.ParentPathwayFieldBridge;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.*;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Proxy(proxyClass=PhysicalEntity.class)
@Indexed
@Boost(1.3f)
@FullTextFilterDefs( { //filters are global (must define on any @Indexed entity), names - unique!
    @FullTextFilterDef(name = "organism", impl = OrganismFilterFactory.class), 
    @FullTextFilterDef(name = "datasource", impl = DataSourceFilterFactory.class) 
})
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PhysicalEntityImpl extends EntityImpl implements PhysicalEntity
{
	private CellularLocationVocabulary cellularLocation;
	private Set<PhysicalEntity> memberPhysicalEntity;
	private Set<Complex> componentOf;
	private Set<EntityFeature> feature;
	private Set<EntityFeature> notFeature;
	private Set<Control> controllerOf;
	private final Log log = LogFactory.getLog(PhysicalEntityImpl.class);
	private Set<PhysicalEntity> memberPhysicalEntityOf;

	public PhysicalEntityImpl()
	{
		feature = new HashSet<EntityFeature>();
		notFeature = new HashSet<EntityFeature>();
		controllerOf = new HashSet<Control>();
		componentOf = new HashSet<Complex>();
		memberPhysicalEntityOf = new HashSet<PhysicalEntity>(); //TODO make generic?
		memberPhysicalEntity = new HashSet<PhysicalEntity>();
	}

	@Transient
	public Class<? extends PhysicalEntity> getModelInterface()
	{
		return PhysicalEntity.class;
	}

	@Fields({
		@Field(name="pathway", index=Index.TOKENIZED, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name="organism", index = Index.UN_TOKENIZED, bridge=@FieldBridge(impl=OrganismFieldBridge.class))
		//this also associates (index) small molecules with organisms!
	})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ComplexImpl.class, mappedBy = "component")
	public Set<Complex> getComponentOf()
	{
		return componentOf;
	}

	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@ManyToOne(targetEntity = CellularLocationVocabularyImpl.class)
	public CellularLocationVocabulary getCellularLocation()
	{
		return cellularLocation;
	}

	public void setCellularLocation(CellularLocationVocabulary location)
	{
		this.cellularLocation = location;
	}

	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityFeatureImpl.class)
	@JoinTable(name="feature")
	public Set<EntityFeature> getFeature()
	{
		return feature;
	}

	public void addFeature(EntityFeature feature)
	{
		if (feature != null) {
			checkAndAddFeature(feature, feature.getFeatureOf());
			this.feature.add(feature);
		}
	}


	public void removeFeature(EntityFeature feature)
	{
		if (feature != null) {
			checkAndRemoveFeature(feature, feature.getFeatureOf());
			this.feature.remove(feature);
		}
	}

	protected void setFeature(Set<EntityFeature> feature)
	{
		this.feature = feature;
	}

	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityFeatureImpl.class)
	@JoinTable(name="notfeature")
	public Set<EntityFeature> getNotFeature()
	{
		return notFeature;
	}


	public void addNotFeature(EntityFeature feature)
	{
		if (feature != null) {
			checkAndAddFeature(feature, feature.getNotFeatureOf());
			this.notFeature.add(feature);
		}
	}

	public void removeNotFeature(EntityFeature feature)
	{
		if (feature != null) {
			checkAndRemoveFeature(feature, feature.getNotFeatureOf());
			this.notFeature.remove(feature);
		}
	}

	protected void setNotFeature(Set<EntityFeature> featureSet)
	{
		this.notFeature = featureSet;
	}


	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="memberPhysicalEntity") 	
	public Set<PhysicalEntity> getMemberPhysicalEntity()
	{
		return this.memberPhysicalEntity;    //TODO
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
			this.memberPhysicalEntity.remove(oldMember); // TODO (what?)
			oldMember.getMemberPhysicalEntityOf().remove(this);
		}
	}

	protected void setMemberPhysicalEntity(Set<PhysicalEntity> memberPhysicalEntity)
	{
		this.memberPhysicalEntity = memberPhysicalEntity; //TODO (what?)
	}


	@Fields({
		@Field(name="pathway", index=Index.TOKENIZED, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name="organism", index = Index.UN_TOKENIZED, bridge=@FieldBridge(impl=OrganismFieldBridge.class))
	})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class, mappedBy = "memberPhysicalEntity")
	public Set<PhysicalEntity> getMemberPhysicalEntityOf()
	{
		return memberPhysicalEntityOf;
	}


	private void checkAndAddFeature(EntityFeature feature,
	                                Set<PhysicalEntity> target)
	{
		if (feature.getFeatureOf().contains(this) ||
		    feature.getNotFeatureOf().contains(this))
		{
			if(log.isWarnEnabled())
				log.warn("Redundant attempt to set the inverse link! " 
						+ " this " + getModelInterface().getSimpleName() 
						+ " " + getRDFId() + " and - " 
						+ feature.getModelInterface().getSimpleName() + " "
						+ feature.getRDFId());
		}
		target.add(this);
	}

	private void checkAndRemoveFeature(EntityFeature feature,
	                                   Set<PhysicalEntity> target)
	{
		assert feature.getFeatureOf().contains(this) ^
		       feature.getNotFeatureOf().contains(this);
		target.remove(this);
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
		       && SetEquivalanceChecker
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
					SetEquivalanceChecker.isEquivalent(this.getFeature(), that.getFeature()) &&
					SetEquivalanceChecker.isEquivalent(this.getNotFeature(), that.getNotFeature());
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

	@ManyToMany(targetEntity = ControlImpl.class, mappedBy = "peController")
	public Set<Control> getControllerOf()
	{
		return controllerOf;
	}

	protected void setControllerOf(Set<Control> controllerOf)
	{
		this.controllerOf = controllerOf;
	}

	protected void setMemberPhysicalEntityOf(Set<PhysicalEntity> memberPhysicalEntityOf)
	{
		this.memberPhysicalEntityOf = memberPhysicalEntityOf;
	}

	protected void setComponentOf(Set<Complex> componentOf)
	{
		this.componentOf = componentOf;
	}

}
