package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 */
@Entity @Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class CatalysisImpl extends ControlImpl implements Catalysis
{
// ------------------------------ FIELDS ------------------------------

	private CatalysisDirectionType catalysisDirection;

	private Set<PhysicalEntity> cofactor;

// --------------------------- CONSTRUCTORS ---------------------------

	public CatalysisImpl()
	{
		this.cofactor = new HashSet<PhysicalEntity>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	@Transient
	public Class<? extends Catalysis> getModelInterface()
	{
		return Catalysis.class;
	}

// --------------------- Interface catalysis ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	@Enumerated
	public CatalysisDirectionType getCatalysisDirection()
	{
		return catalysisDirection;
	}

	public void setCatalysisDirection(CatalysisDirectionType catalysisDirection)
	{
		this.catalysisDirection = catalysisDirection;
	}

	@ManyToMany(targetEntity = PhysicalEntityImpl.class) @JoinTable(name = "cofactor")
	public Set<PhysicalEntity> getCofactor()
	{
		return cofactor;
	}

	protected void setCofactor(Set<PhysicalEntity> cofactor)
	{
		this.cofactor = cofactor;
	}

	public void addCofactor(PhysicalEntity cofactor)
	{
		if (cofactor != null)
		{
			this.cofactor.add(cofactor);
			super.addParticipant(cofactor);
		}
	}

	public void removeCofactor(PhysicalEntity cofactor)
	{
		if (cofactor != null)
		{
			super.removeParticipant(cofactor);
			this.cofactor.remove(cofactor);
		}
	}


	@Override public void addController(Controller controller)
	{
		if (controller instanceof PhysicalEntity) super.addController(controller);
		else throw new IllegalBioPAXArgumentException("Catalysis can only be controlled with a Physical Entity");

	}

	protected boolean checkControlled(Process controlled)
	{
		return controlled instanceof Conversion;
	}
}
