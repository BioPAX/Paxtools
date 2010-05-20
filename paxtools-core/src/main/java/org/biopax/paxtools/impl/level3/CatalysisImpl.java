package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
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

	@ManyToMany(targetEntity= PhysicalEntityImpl.class, cascade={CascadeType.PERSIST})
	@JoinTable(name="cofactor")
	public Set<PhysicalEntity> getCofactor()
	{
		return cofactor;
	}

	private void setCofactor(Set<PhysicalEntity> cofactor)
	{
		this.cofactor = cofactor;
	}

	public void addCofactor(PhysicalEntity cofactor)
	{
		this.cofactor.add(cofactor);
		super.addParticipant(cofactor);
	}

	public void removeCofactor(PhysicalEntity cofactor)
	{
		super.removeParticipant(cofactor);
        this.cofactor.remove(cofactor);
	}


	protected boolean checkControlled(Process controlled)
	{
		return controlled instanceof Conversion;
	}
}
