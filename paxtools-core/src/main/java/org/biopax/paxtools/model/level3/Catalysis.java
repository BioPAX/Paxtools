package org.biopax.paxtools.model.level3;

import java.util.Set;


/**
 * Definition: A control interaction in which a physical entity (a catalyst) increases the rate of a conversion
 * interaction by lowering its activation energy. Instances of this class describe a pairing between a catalyzing
 * entity and a catalyzed conversion.
 * <p/>
 * Rationale: Catalysis, theoretically, is always bidirectional since it acts by lowering the activation energy.
 * Physiologically, however, it can have a direction because of the concentration of the participants. For example,
 * the oxidative decarboxylation catalyzed by Isocitrate dehydrogenase always happens in one direction under
 * physiological conditions since the produced carbon dioxide is constantly removed from the system.
 * <p/>
 * Usage: A separate catalysis instance should be created for each different conversion that a physicalEntity may
 * catalyze and for each different physicalEntity that may catalyze a conversion. For example,
 * a bifunctional enzyme that catalyzes two different biochemical reactions would be linked to each of those
 * biochemical reactions by two separate instances of the catalysis class. Also, catalysis reactions from multiple
 * different organisms could be linked to the same generic biochemical reaction (a biochemical reaction is generic if
 * it only includes small molecules). Generally, the enzyme catalyzing a conversion is known and the use of this
 * class is obvious, however, in the cases where a catalyzed reaction is known to occur but the enzyme is not known,
 * a catalysis instance can be created without a controller specified.
 * <p/>
 * Synonyms: facilitation, acceleration.
 * <p/>
 * Examples: The catalysis of a biochemical reaction by an enzyme, the enabling of a transport interaction by a
 * membrane pore complex, and the facilitation of a complex assembly by a scaffold protein. Hexokinase -> (The
 * "Glucose + ATP -> Glucose-6-phosphate +ADP" reaction). A plasma membrane Na+/K+ ATPase is an active transporter
 * (antiport pump) using the energy of ATP to pump Na+ out of the cell and K+ in. Na+ from cytoplasm to extracellular
 * space would be described in a transport instance. K+ from extracellular space to cytoplasm would be described in a
 * transport instance. The ATPase pump would be stored in a catalysis instance controlling each of the above
 * transport instances. A biochemical reaction that does not occur by itself under physiological conditions,
 * but has been observed to occur in the presence of cell extract, likely via one or more unknown enzymes present in
 * the extract, would be stored in the CONTROLLED property, with the CONTROLLER property empty.
 */
public interface Catalysis extends Control
{


	/**
	 * Any cofactor(s) or coenzyme(s) required for catalysis of the conversion by the enzyme. This is a suproperty
	 * of participants.
	 * @return cofactor(s) or coenzyme(s) required for catalysis of the conversion
	 */
	@Key Set<PhysicalEntity> getCofactor();

	/**
	 * Any cofactor(s) or coenzyme(s) required for catalysis of the conversion by the enzyme. This is a suproperty
	 * of participants.
	 * @param cofactor cofactor(s) or coenzyme(s) required for catalysis of the conversion
	 */
	void addCofactor(PhysicalEntity cofactor);

	/**
	 * Any cofactor(s) or coenzyme(s) required for catalysis of the conversion by the enzyme. This is a suproperty
	 * of participants.
	 * @param cofactor cofactor(s) or coenzyme(s) required for catalysis of the conversion
	 */
	void removeCofactor(PhysicalEntity cofactor);


	/**
	 * This property represents the direction of this catalysis under all physiological conditions if there is one.
	 * Note that chemically a catalyst will increase the rate of the reaction in both directions. In biology,
	 * however, there are cases where the enzyme is expressed only when the controlled bidirectional conversion is
	 * on one side of the chemical equilibrium. olled bidirectional conversion is on one side of the chemical
	 * equilibrium. For example E.Coli's lac operon ensures that lacZ gene is only synthesized when there is enough
	 * lactose in the medium. If that is the case and the controller, under biological conditions,
	 * is always catalyzing the conversion in one direction then this fact can be captured using this property. If
	 * the enzyme is active for both directions, or the conversion is not bidirectional,
	 * this property should be left empty.
	 * @return direction of this catalysis under all physiological conditions if there is one
	 */
	CatalysisDirectionType getCatalysisDirection();

	/**
	 * This property represents the direction of this catalysis under all physiological conditions if there is one.
	 * Note that chemically a catalyst will increase the rate of the reaction in both directions. In biology,
	 * however, there are cases where the enzyme is expressed only when the controlled bidirectional conversion is
	 * on one side of the chemical equilibrium. olled bidirectional conversion is on one side of the chemical
	 * equilibrium. For example E.Coli's lac operon ensures that lacZ gene is only synthesized when there is enough
	 * lactose in the medium. If that is the case and the controller, under biological conditions,
	 * is always catalyzing the conversion in one direction then this fact can be captured using this property. If
	 * the enzyme is active for both directions, or the conversion is not bidirectional,
	 * this property should be left empty.
	 * @param catalysisDirection direction of this catalysis under all physiological conditions if there is one
	 */
	void setCatalysisDirection(CatalysisDirectionType catalysisDirection);
}
