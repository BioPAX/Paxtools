package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

import java.util.Set;
import java.util.HashSet;

@javax.persistence.Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class TemplateReactionImpl extends InteractionImpl implements TemplateReaction {
    private Set<PhysicalEntity> product;
    private Set<NucleicAcid> initiationRegion;
    private NucleicAcid template;
	private TemplateDirectionType templateDirection;

	public TemplateReactionImpl() {
        this.product = new HashSet<PhysicalEntity>();
        this.initiationRegion =  new HashSet<NucleicAcid>();
    }
	
	@Transient
    public Class<? extends TemplateReaction> getModelInterface()
	{
		return TemplateReaction.class;
	}

    @ManyToMany(targetEntity = PhysicalEntityImpl.class, cascade={CascadeType.ALL}) //Todo: make sequence entity?
    @JoinTable(name="product") 	
    public Set<PhysicalEntity> getProduct()
    {
        return product;
    }

    protected void setProduct(Set<PhysicalEntity> product)
    {
        this.product = product;
    }

    public void addProduct(PhysicalEntity product)
    {
    	if(product != null) {
    		this.product.add(product);
    		super.addParticipant(product);
    	}
    }

    public void removeProduct(PhysicalEntity product)
    {
    	if(product != null) {
    		super.removeParticipant(product);
        	this.product.remove(product);
    	}
    }

	@ManyToOne(targetEntity = NucleicAcidImpl.class, cascade = { CascadeType.ALL })
	protected NucleicAcid getTemplateX() {
		return this.template;
	}
	protected void setTemplateX(NucleicAcid template) {
		this.template = template;
	}
    
    public NucleicAcid getTemplate()
     {
         return this.template;
     }

     public void setTemplate(NucleicAcid template)
     {
         if(this.template!= null)
         {
            super.removeParticipant(this.template);
         }
         if(template != null) {
        	 this.template=template;
        	 super.addParticipant(template);
         }
     }

    @Enumerated
	public TemplateDirectionType getTemplateDirection()
	{
		return templateDirection;
	}

	public void setTemplateDirection(TemplateDirectionType templateDirection)
	{
		this.templateDirection = templateDirection;
	}

}
