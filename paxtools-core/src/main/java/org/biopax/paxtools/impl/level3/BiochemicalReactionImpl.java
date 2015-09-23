package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.KPrime;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


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

	public Class<? extends BiochemicalReaction> getModelInterface()
	{
		return BiochemicalReaction.class;
	}

// --------------------- Interface BiochemicalReaction ---------------------

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

	public Set<Float> getDeltaH()
	{
		return deltaH;
	}

	public void addDeltaH(float deltaH)
	{
		this.deltaH.add(deltaH);
	}

	public void removeDeltaH(float deltaH)
	{
		this.deltaH.remove(deltaH);
	}

	public Set<Float> getDeltaS()
	{
		return deltaS;
	}

	public void addDeltaS(float deltaS)
	{
		this.deltaS.add(deltaS);
	}

	public void removeDeltaS(float deltaS)
	{
		this.deltaS.remove(new Float(deltaS));
	}

	public Set<String> getECNumber()
	{
		return eCNumber;
	}

	public void addECNumber(String eCNumber)
	{
		this.eCNumber.add(eCNumber);
	}

	public void removeECNumber(String eCNumber)
	{
		this.eCNumber.remove(eCNumber);
	}

	public Set<KPrime> getKEQ()
	{
		return kEQ;
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
