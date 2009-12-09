package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.TemplateDirectionType;

import java.util.Set;
import java.util.HashSet;

/**
 * TODO:Class description
 * User: demir
 * Date: Aug 14, 2008
 * Time: 7:54:51 PM
 */
public class TemplateReactionImpl extends RestrictedInteractionAdapter implements TemplateReaction {
    private Set<PhysicalEntity> product;
    private Set<NucleicAcid> initiationRegion;
    private NucleicAcid template;
	private TemplateDirectionType templateDirection;

	public TemplateReactionImpl() {
        this.product = new HashSet<PhysicalEntity>();
        this.initiationRegion =  new HashSet<NucleicAcid>();
    }
	public Class<? extends TemplateReaction> getModelInterface()
	{
		return TemplateReaction.class;
	}


    public Set<PhysicalEntity> getProduct()
    {
        return product;
    }

    public void setProduct(Set<PhysicalEntity> product)
    {
        if (product == null)
        {
            product = new HashSet<PhysicalEntity>();
        }
        this.product = product;
    }

    public void addProduct(PhysicalEntity product)
    {
        this.product.add(product);
        addSubParticipant(product);
    }

    public void removeProduct(PhysicalEntity product)
    {
        removeSubParticipant(product);
        this.product.remove(product);
    }

     public Set<NucleicAcid> getInitiationRegion()
    {
        return initiationRegion;
    }

    public void setInitiationRegion(Set<NucleicAcid> initiationRegion)
    {
        if (initiationRegion == null)
        {
            initiationRegion = new HashSet<NucleicAcid>();
        }
        this.initiationRegion = initiationRegion;
    }

    public void addInitiationRegion(NucleicAcid initiationRegion)
    {
        this.initiationRegion.add(initiationRegion);
        addSubParticipant(initiationRegion);
    }

    public void removeInitiationRegion(NucleicAcid initiationRegion)
    {
        removeSubParticipant(initiationRegion);
        this.initiationRegion.remove(initiationRegion);
    }

    public NucleicAcid getTemplate()
     {
         return this.template;
     }

     public void setTemplate(NucleicAcid template)
     {
         if(this.template!= null)
         {
            removeSubParticipant(this.template);
         }
         this.template=template;
         addSubParticipant(template);
     }

	public TemplateDirectionType getTemplateDirection()
	{
		return templateDirection;
	}

	public void setTemplateDirection(TemplateDirectionType templateDirection)
	{
		this.templateDirection = templateDirection;
	}

}
