package org.biopax.paxtools.model.level3;

/**
* Definition: An interaction in which at least one participant is a physical entity, e.g. a binding event.
* Comment: This class should be used by default for representing molecular interactions, such as those defined by PSI-MI level 2.5. The participants in a molecular interaction should be listed in the PARTICIPANTS slot. Note that this is one of the few cases in which the PARTICPANT slot should be directly populated with instances (see comments on the PARTICPANTS property in the interaction class description). If sufficient information on the nature of a molecular interaction is available, a more specific BioPAX interaction class should be used.
* Example: Two proteins observed to interact in a yeast-two-hybrid experiment where there is not enough experimental evidence to suggest that the proteins are forming a complex by themselves without any indirect involvement of other proteins. This is the case for most large-scale yeast two-hybrid screens.
*/
public interface MolecularInteraction extends Interaction
{

}
