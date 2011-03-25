package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.deltaGprimeO;
import org.biopax.paxtools.model.level2.kPrime;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
class biochemicalReactionImpl extends conversionImpl
	implements biochemicalReaction
{
// ------------------------------ FIELDS ------------------------------

	private Set<Double> DELTA_S;
	private Set<kPrime> KEQ;

	private Set<Double> DELTA_H;
	private Set<deltaGprimeO> DELTA_G;
	private Set<String> EC_NUMBER;

// --------------------------- CONSTRUCTORS ---------------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public biochemicalReactionImpl()
	{
		this.DELTA_G = new HashSet<deltaGprimeO>();
		this.DELTA_H = new HashSet<Double>();
		this.DELTA_S = new HashSet<Double>();
		this.EC_NUMBER = new HashSet<String>();
		this.KEQ = new HashSet<kPrime>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return biochemicalReaction.class;
	}

// --------------------- Interface biochemicalReaction ---------------------


	public void addDELTA_G(deltaGprimeO DELTA_G)
	{
		this.DELTA_G.add(DELTA_G);
	}

	public void removeDELTA_G(deltaGprimeO DELTA_G)
	{
		this.DELTA_G.remove(DELTA_G);
	}

	public void setDELTA_G(Set<deltaGprimeO> DELTA_G)
	{
		this.DELTA_G = DELTA_G;
	}

	public Set<deltaGprimeO> getDELTA_G()
	{
		return DELTA_G;
	}

	public void addDELTA_H(double DELTA_H)
	{
		this.DELTA_H.add(DELTA_H);
	}

	public void removeDELTA_H(double DELTA_H)
	{
		this.DELTA_H.remove(DELTA_H);
	}

	public void setDELTA_H(Set<Double> DELTA_H)
	{
		this.DELTA_H = DELTA_H;
	}

	public Set<Double> getDELTA_H()
	{
		return DELTA_H;
	}

	public Set<Double> getDELTA_S()
	{
		return DELTA_S;
	}

	public void setDELTA_S(Set<Double> DELTA_S)
	{
		this.DELTA_S = DELTA_S;
	}

	public void addDELTA_S(double DELTA_S)
	{
		this.DELTA_S.add(DELTA_S);
	}

	public void removeDELTA_S(double DELTA_S)
	{
		this.DELTA_S.remove(new Double(DELTA_S));
	}

	public void addEC_NUMBER(String EC_NUMBER)
	{
		this.EC_NUMBER.add(EC_NUMBER);
	}

	public void removeEC_NUMBER(String EC_NUMBER)
	{
		this.EC_NUMBER.remove(EC_NUMBER);
	}

	public Set<String> getEC_NUMBER()
	{
		return EC_NUMBER;
	}

	public void setEC_NUMBER(Set<String> EC_NUMBER)
	{
		this.EC_NUMBER = EC_NUMBER;
	}

	public void addKEQ(kPrime KEQ)
	{
		this.KEQ.add(KEQ);
	}

	public void removeKEQ(kPrime KEQ)
	{
		this.KEQ.remove(KEQ);
	}

	public Set<kPrime> getKEQ()
	{
		return KEQ;
	}

	public void setKEQ(Set<kPrime> KEQ)
	{
		this.KEQ = KEQ;
	}
}
