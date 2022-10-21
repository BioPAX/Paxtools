package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.TemplateDirectionType;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public class TemplateReactionImpl extends InteractionImpl implements TemplateReaction {
  private Set<PhysicalEntity> product;
  private NucleicAcid template;
  private TemplateDirectionType templateDirection;

  public TemplateReactionImpl()
  {
    this.product = BPCollections.I.createSafeSet();
  }

  public Class<? extends TemplateReaction> getModelInterface()
  {
    return TemplateReaction.class;
  }

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
      if(this.product.add(product)) {
        super.addParticipant(product);
      }
    }
  }

  public void removeProduct(PhysicalEntity product)
  {
    if(product != null) {
      if (this.product.remove(product)) {
        super.removeParticipant(product);
      }
    }
  }

  public NucleicAcid getTemplate()
  {
    return this.template;
  }

  public void setTemplate(NucleicAcid template)
  {
    if(this.template != null) {
      super.removeParticipant(this.template);
    }
    if(template != null) {
      super.addParticipant(template);
    }
    this.template = template; //can be null
  }

  public TemplateDirectionType getTemplateDirection()
  {
    return templateDirection;
  }

  public void setTemplateDirection(TemplateDirectionType templateDirection)
  {
    this.templateDirection = templateDirection;
  }

//TODO: override add/remove participant to block direct use (allow only via add/remove "template", "product")?
}
