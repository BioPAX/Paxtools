package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.KPrime;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@Proxy(proxyClass= BiochemicalReaction.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BiochemicalReactionImpl extends ConversionImpl
	implements BiochemicalReaction
{
// ------------------------------ FIELDS ------------------------------

	private Set<Float> deltaS;
	private Set<KPrime> kEQ;
	private Set<Float> deltaH;
	private Set<DeltaG> deltaG;
	private Set<String> eCNumber;

// --------------------------- CONSTRUCTORS ---------------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public BiochemicalReactionImpl()
	{
		this.deltaG = BPCollections.I.createSafeSet();
		this.deltaH = BPCollections.I.createSet();
		this.deltaS = BPCollections.I.createSet();
		this.eCNumber = BPCollections.I.createSet();
		this.kEQ = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	@Transient
	public Class<? extends BiochemicalReaction> getModelInterface()
	{
		return BiochemicalReaction.class;
	}

// --------------------- Interface BiochemicalReaction ---------------------

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity = DeltaGImpl.class)
	@JoinTable(name="deltaG")	
	public Set<DeltaG> getDeltaG()
	{
		return deltaG;
	}

	protected void setDeltaG(Set<DeltaG> deltaG)
	{
		this.deltaG = deltaG;
	}

	public void addDeltaG(DeltaG deltaG)
	{
		if(deltaG != null)
			this.deltaG.add(deltaG);
	}

	public void removeDeltaG(DeltaG deltaG)
	{
		if(deltaG != null) 
			this.deltaG.remove(deltaG);
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ElementCollection
	@JoinTable(name="deltaH")	
	public Set<Float> getDeltaH()
	{
		return deltaH;
	}

	protected void setDeltaH(Set<Float> deltaH)
	{
		this.deltaH = deltaH;
	}

	public void addDeltaH(float deltaH)
	{
		this.deltaH.add(deltaH);
	}

	public void removeDeltaH(float deltaH)
	{
		this.deltaH.remove(deltaH);
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ElementCollection
	@JoinTable(name="deltaS")	
	public Set<Float> getDeltaS()
	{
		return deltaS;
	}

	protected void setDeltaS(Set<Float> deltaS)
	{
		this.deltaS = deltaS;
	}

	public void addDeltaS(float deltaS)
	{
		this.deltaS.add(deltaS);
	}

	public void removeDeltaS(float deltaS)
	{
		this.deltaS.remove(new Float(deltaS));
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ElementCollection
	@JoinTable(name="ECNumber")	
	@Field(name=FIELD_ECNUMBER, analyze=Analyze.YES)
	@FieldBridge(impl=SetStringBridge.class)
	public Set<String> getECNumber()
	{
		return eCNumber;
	}

	protected void setECNumber(Set<String> eCNumber)
	{
		this.eCNumber = eCNumber;
	}

	public void addECNumber(String eCNumber)
	{
		this.eCNumber.add(eCNumber);
	}

	public void removeECNumber(String eCNumber)
	{
		this.eCNumber.remove(eCNumber);
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity = KPrimeImpl.class)
	@JoinTable(name="keq")		
	public Set<KPrime> getKEQ()
	{
		return kEQ;
	}

	protected void setKEQ(Set<KPrime> kEQ)
	{
		this.kEQ = kEQ;
	}

	public void addKEQ(KPrime kEQ)
	{
		this.kEQ.add(kEQ);
	}

	public void removeKEQ(KPrime kEQ)
	{
		this.kEQ.remove(kEQ);
	}

}
