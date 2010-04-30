package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@javax.persistence.Entity
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

    @ManyToMany(targetEntity = PhysicalEntityImpl.class) //Todo: make sequence entity?
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
        this.product.add(product);
        super.addParticipant(product);
    }

    public void removeProduct(PhysicalEntity product)
    {
        super.removeParticipant(product);
        this.product.remove(product);
    }


    @ManyToOne(targetEntity = NucleicAcidImpl.class)
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
         this.template=template;
         super.addParticipant(template);
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
