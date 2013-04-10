package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.util.*;
import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalenceChecker;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
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
@FullTextFilterDefs( { //these filters are global (can be defined on any @Indexed entity), names - unique!
    @FullTextFilterDef(name = BioPAXElementImpl.FILTER_BY_ORGANISM, impl = OrganismFilterFactory.class), 
    @FullTextFilterDef(name = BioPAXElementImpl.FILTER_BY_DATASOURCE, impl = DataSourceFilterFactory.class) 
})
@DynamicUpdate @DynamicInsert
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
		@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO, bridge=@FieldBridge(impl=OrganismFieldBridge.class))
		//- can even associate small molecules with organisms (impossible to do explicitly in BioPAX)!
	})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ComplexImpl.class, mappedBy = "component")
	public Set<Complex> getComponentOf()
	{
		return componentOf;
	}

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@ManyToOne(targetEntity = CellularLocationVocabularyImpl.class)
	public CellularLocationVocabulary getCellularLocation()
	{
		return cellularLocation;
	}

	public void setCellularLocation(CellularLocationVocabulary location)
	{
		this.cellularLocation = location;
	}

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
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

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
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


	@Fields({
		//using memberPhysicalEntity in addition to other props to generate "pathway" index field helps
		//in rare cases when this one does not participate in any interaction or complex but some of its members do
		@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO, bridge=@FieldBridge(impl=OrganismFieldBridge.class)),
		@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="memberPhysicalEntity") 	
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
			this.memberPhysicalEntity.remove(oldMember); // TODO (what?)
			oldMember.getMemberPhysicalEntityOf().remove(this);
		}
	}

	protected void setMemberPhysicalEntity(Set<PhysicalEntity> memberPhysicalEntity)
	{
		this.memberPhysicalEntity = memberPhysicalEntity; //TODO (what?)
	}


	@Fields({
		@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO, bridge=@FieldBridge(impl=OrganismFieldBridge.class))
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

	//not indexed (organism, pathway fields) here, as it's done via EntityImpl participantOf super-property
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
