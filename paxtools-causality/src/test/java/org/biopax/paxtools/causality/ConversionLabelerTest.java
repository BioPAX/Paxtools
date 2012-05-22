package org.biopax.paxtools.causality;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.c.ConBox;
import org.biopax.paxtools.pattern.c.PathConstraint;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ConversionLabelerTest
{
	@Test
	@Ignore
	public void testLabelingConversions() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(getClass().getResourceAsStream("STAT2.owl"));
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/all.owl"));

		Pattern p = new Pattern(6, TemplateReaction.class);
		int i = 0;
		p.addConstraint(new PathConstraint("TemplateReaction/controlledOf"), i, ++i);
		p.addConstraint(ConBox.controllerPE(), i, ++i);
		p.addConstraint(ConBox.withSimpleMembers(), i, ++i);
		p.addConstraint(ConBox.genericEquiv(), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);

		Map<BioPAXElement,List<Match>> matches = Searcher.search(model, p);
		System.out.println("matches.key = " + matches.keySet().size());
		

		EntityReference er = (EntityReference) model.getByID("urn:miriam:uniprot:P52630");

		FeatureCollector fc = new FeatureCollector();
		Map<EntityReference,Set<ModificationFeature>> activating = fc.collectFeatures(model, true);
		Map<EntityReference,Set<ModificationFeature>> inhibiting = fc.collectFeatures(model, false);

		ConversionTypeLabeler ctl = new ConversionTypeLabeler();
		Map<Conversion,Integer> map = ctl.label(er, model, ubiq, activating, inhibiting);

		for (Conversion key : map.keySet())
		{
			System.out.print("key = " + key);
			System.out.println("\tvalue = " + map.get(key));
		}
	}

	public static Set<String> ubiq = new HashSet<String>(Arrays.asList(
		"http://biocyc.org/biopax/biopax-level3SmallMolecule159666",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule135584",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137847",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137835",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137826",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137582",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule138222",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131431",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131446",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131465",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131421",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131548",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131525",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule148207",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule132137",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule132532",
		"http://www.reactome.org/biopax/48892SmallMolecule966",
		"http://www.reactome.org/biopax/48892SmallMolecule964",
		"http://www.reactome.org/biopax/48892SmallMolecule963",
		"http://www.reactome.org/biopax/48892SmallMolecule121",
		"http://www.reactome.org/biopax/48892SmallMolecule151",
		"http://www.reactome.org/biopax/48892SmallMolecule148",
		"http://www.reactome.org/biopax/48892SmallMolecule145",
		"http://www.reactome.org/biopax/48892SmallMolecule162",
		"http://www.reactome.org/biopax/48892SmallMolecule167",
		"http://www.reactome.org/biopax/48892SmallMolecule195",
		"http://www.reactome.org/biopax/48892SmallMolecule187",
		"http://www.reactome.org/biopax/48892SmallMolecule269",
		"http://www.reactome.org/biopax/48892SmallMolecule569",
		"http://www.reactome.org/biopax/48892SmallMolecule414",
		"http://pid.nci.nih.gov/biopaxpid_3320",
		"http://pid.nci.nih.gov/biopaxpid_3321",
		"http://pid.nci.nih.gov/biopaxpid_3454",
		"http://pid.nci.nih.gov/biopaxpid_678",
		"http://pid.nci.nih.gov/biopaxpid_685",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule165158",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule165340",
		"http://www.reactome.org/biopax/48887SmallMolecule171",
		"http://www.reactome.org/biopax/48887SmallMolecule178",
		"http://www.reactome.org/biopax/48887SmallMolecule181",
		"http://www.reactome.org/biopax/48887SmallMolecule576",
		"http://www.reactome.org/biopax/48887SmallMolecule509",
		"http://www.reactome.org/biopax/48887SmallMolecule369",
		"http://www.reactome.org/biopax/48887SmallMolecule300",
		"http://www.reactome.org/biopax/48887SmallMolecule399",
		"http://www.reactome.org/biopax/48887SmallMolecule394",
		"http://www.reactome.org/biopax/48887SmallMolecule227",
		"http://www.reactome.org/biopax/48887SmallMolecule299",
		"http://www.reactome.org/biopax/48887SmallMolecule267",
		"http://www.reactome.org/biopax/48887SmallMolecule250",
		"http://www.reactome.org/biopax/48887SmallMolecule284",
		"http://www.reactome.org/biopax/48887SmallMolecule287",
		"http://www.reactome.org/biopax/48887SmallMolecule276",
		"http://www.reactome.org/biopax/48887SmallMolecule275",
		"http://www.reactome.org/biopax/48887SmallMolecule274",
		"http://pid.nci.nih.gov/biopaxpid_2514",
		"http://pid.nci.nih.gov/biopaxpid_3049",
		"http://pid.nci.nih.gov/biopaxpid_3192",
		"http://pid.nci.nih.gov/biopaxpid_3229",
		"http://www.reactome.org/biopax/48892SmallMolecule15",
		"http://www.reactome.org/biopax/48892SmallMolecule81",
		"http://www.reactome.org/biopax/48892SmallMolecule90",
		"http://www.reactome.org/biopax/48892SmallMolecule76",
		"http://www.reactome.org/biopax/48892SmallMolecule71",
		"http://www.reactome.org/biopax/48892SmallMolecule78",
		"http://www.reactome.org/biopax/48892SmallMolecule77",
		"http://www.reactome.org/biopax/48892SmallMolecule79",
		"http://www.reactome.org/biopax/48892SmallMolecule94",
		"http://www.reactome.org/biopax/48892SmallMolecule1",
		"http://www.reactome.org/biopax/48892SmallMolecule2",
		"http://pid.nci.nih.gov/biopaxpid_9774",
		"http://www.reactome.org/biopax/48887SmallMolecule30",
		"http://www.reactome.org/biopax/48887SmallMolecule24",
		"http://www.reactome.org/biopax/48887SmallMolecule22",
		"http://www.reactome.org/biopax/48887SmallMolecule10",
		"http://www.reactome.org/biopax/48887SmallMolecule12",
		"http://www.reactome.org/biopax/48887SmallMolecule11",
		"http://www.reactome.org/biopax/48887SmallMolecule18",
		"http://www.reactome.org/biopax/48887SmallMolecule17",
		"http://www.reactome.org/biopax/48887SmallMolecule1233",
		"http://www.reactome.org/biopax/48887SmallMolecule1232",
		"http://www.reactome.org/biopax/48887SmallMolecule1235",
		"http://www.reactome.org/biopax/48887SmallMolecule7",
		"http://www.reactome.org/biopax/48887SmallMolecule8",
		"http://www.reactome.org/biopax/48887SmallMolecule3",
		"http://www.reactome.org/biopax/48887SmallMolecule2",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule127479",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule126271",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule126074",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule126025",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule125519",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule125517",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule125521",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129851",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129842",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129864"
	));
}
