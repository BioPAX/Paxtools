/*
 * HiRDBTrial.java
 * sample program
 *
 * 2007.01 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

/**
 *	HiRDB sample program
 */
public class HiRDBTrial {
	String command = null;
	ArrayList<String> params = new ArrayList<String>();
	String iniFileName = "HiRDBTrialCmd.ini";
	String jdbc = "jdbc:postgresql://localhost/PAXTOOLS";
	String user = "paxtools";
	String password = "";
	String indexBase = "";
	String paxtoolsPersistenceUnitName = HiRDBConnect.PUNAME_POSTGRESQL;
	String filePath = "";
	boolean bShowSQL = false;
	
	/**
	 *	main
	 */
	public static void main(String[] args) throws Exception {
		HiRDBTrial o = new HiRDBTrial();
		o.subMain(args);
	}
	
	public void setOptions(String args[]) {
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.equals("-i") || s.equals("-ini")) {
				iniFileName = args[++i];
				break;
			}
		}

		Properties ini = new Properties();
		File iniFile = new File(iniFileName);
		if (iniFile.isFile()) {
			try {
				FileInputStream fis = new FileInputStream(iniFile);
				ini.load(fis);
				fis.close();
			}
			catch (Exception e) {
			}
		}

		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.charAt(0) == '-') {
				String propName = s.substring(1);
				String value = args[++i];
				ini.setProperty(propName, value);
			}
			else {
				if (command == null)
					command = s;
				else
					params.add(s);
			}
		}

		Enumeration<Object> ks = ini.keys();
		while (ks.hasMoreElements()) {
			String key = (String)ks.nextElement();
			String value = ini.getProperty(key, "");
			if (key.equals("puName")) {
				paxtoolsPersistenceUnitName = value;
			}
			else if (key.equals("jdbc")) {
				jdbc = value;
			}
			else if (key.equals("user")) {
				user = value;
			}
			else if (key.equals("password")) {
				password = value;
			}
			else if (key.equals("indexBase")) {
				indexBase = value;
			}
			else if (key.equals("showSQL")) {
				bShowSQL = toBoolean(value);
			}
			else if (key.equals("f") || key.equals("file") || key.equals("filePath")) {
				filePath = value;
			}
		}
	}

	boolean toBoolean(String s) {
		return (s.equalsIgnoreCase("true") ||
			s.equalsIgnoreCase("ok") ||
			s.equalsIgnoreCase("good") ||
			s.equalsIgnoreCase("yes") ||
			s.equalsIgnoreCase("t") ||
			s.equalsIgnoreCase("y") ||
			s.equalsIgnoreCase("on"));
	}

	static long printTime() {
		java.util.Date d = new java.util.Date();
		System.out.println(d.toString());
		return d.getTime();
	}

	/**
	 *	submain
	 */
	public void subMain(String[] args) throws Exception {
		setOptions(args);
		if (command == null || command.length() == 0)
			return;
		try {
			HiRDB context = new HiRDB(paxtoolsPersistenceUnitName, jdbc, user, password, indexBase);
			if (command.equals("buildddl")) {
				if (filePath.length() > 0) {
					context.getConnect().buildDDL(filePath);
				}
				return;
			}
			context.getConnect().bShowSQL = bShowSQL;
			context.setup();
			long startTime = printTime();
			System.out.println("### START ###########################");
			if (command.equals("upload")) {
				if (filePath.length() > 0) {
					File f = new File(filePath);
					if (f != null) {
						context.getConnect().uploadOWL(f.getName(), new FileInputStream(f));
					}
				}
			}
			else if (command.equals("download")) {
				if (filePath.length() > 0) {
					File f = new File(filePath);
					FileOutputStream oStream = new FileOutputStream(f);
					if (oStream != null) {
						context.getConnect().downloadOWL(f.getName(), oStream);
						oStream.close();
					}
				}
			}
			else if (command.equals("list")) {
				Set<String>keys = context.getConnect().listOWL();
				for (String key: keys) {
					System.out.println(key);
				}
			}
			// KeywordSearch
			else if (command.equals("keyword")) {
				List<BioPAXElement> list = context.createKeywordSearch().search(params.get(0));
				if (list != null) {
					for (BioPAXElement bp : list) {
						System.out.println("rdf:ID " + bp.getRDFId());
					}
				}
			}
			// PathwaySearch
			else if (command.equals("getPathwayList")) {
				Set<Pathway> ps = context.createPathwaySearch().getPathwayList();
				for (Pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPathwayListByName")) {
				Set<Pathway> ps = context.createPathwaySearch().getPathwayListByName(params.get(0));
				for (Pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPathwayListByOrganism")) {
				Set<Pathway> ps = context.createPathwaySearch().getPathwayListByOrganism(params.get(0));
				for (Pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSuperPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<Pathway> ps = context.createPathwaySearch().getSuperPathwayList((Pathway)e);
					for (Pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllSuperPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<Pathway> ps = context.createPathwaySearch().getAllSuperPathwayList((Pathway)e);
					for (Pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllSubPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<Pathway> ps = context.createPathwaySearch().getAllSubPathwayList((Pathway)e);
					for (Pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getTopLevelPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Pathway p = context.createPathwaySearch().getTopLevelPathway((Pathway)e);
					System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getNextStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<PathwayStep> ps = context.createPathwaySearch().getNextStepListOfPathway((Pathway)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllNextStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<PathwayStep> ps = context.createPathwaySearch().getAllNextStepListOfPathway((Pathway)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getPreviousStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<PathwayStep> ps = context.createPathwaySearch().getPreviousStepListOfPathway((Pathway)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllPreviousStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<PathwayStep> ps = context.createPathwaySearch().getAllPreviousStepListOfPathway((Pathway)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getECNumberListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<String> ps = context.createPathwaySearch().getECNumberListInPathway((Pathway)e);
					for (String p: ps)
						System.out.println(p);
				}
			}
			else if (command.equals("getPhysicalEntityListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<Entity> ps = context.createPathwaySearch().getPhysicalEntityListInPathway((Pathway)e);
					for (Entity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getEntityReferenceListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<EntityReference> ps = context.createPathwaySearch().getEntityReferenceListInPathway((Pathway)e);
					for (EntityReference p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getInteractionListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Pathway) {
					Set<Interaction> ps = context.createPathwaySearch().getInteractionListInPathway((Pathway)e);
					for (Interaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getPathwayListByTermOfEvidenceCode")) {
				Set<Pathway> ps = context.createPathwaySearch().getPathwayListByTermOfEvidenceCode(params.get(0));
				for (Pathway p: ps)
					System.out.println(p.getRDFId());
			}
			// InteractionSearch
			else if (command.equals("getInteractionList")) {
				Set<Interaction> ps = context.createInteractionSearch().getInteractionList();
				for (Interaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getControlList")) {
				Set<Control> ps = context.createInteractionSearch().getControlList();
				for (Control p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getCatalysisList")) {
				Set<Catalysis> ps = context.createInteractionSearch().getCatalysisList();
				for (Catalysis p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getModulationList")) {
				Set<Modulation> ps = context.createInteractionSearch().getModulationList();
				for (Modulation p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTemplateReactionRegulationList")) {
				Set<TemplateReactionRegulation> ps = context.createInteractionSearch().getTemplateReactionRegulationList();
				for (TemplateReactionRegulation p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getConversionList")) {
				Set<Conversion> ps = context.createInteractionSearch().getConversionList();
				for (Conversion p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getBiochemicalReactionList")) {
				Set<BiochemicalReaction> ps = context.createInteractionSearch().getBiochemicalReactionList();
				for (BiochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTransportWithBiochemicalReactionList")) {
				Set<TransportWithBiochemicalReaction> ps = context.createInteractionSearch().getTransportWithBiochemicalReactionList();
				for (TransportWithBiochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getComplexAssemblyList")) {
				Set<ComplexAssembly> ps = context.createInteractionSearch().getComplexAssemblyList();
				for (ComplexAssembly p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getDegradationList")) {
				Set<Degradation> ps = context.createInteractionSearch().getDegradationList();
				for (Degradation p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTransportList")) {
				Set<Transport> ps = context.createInteractionSearch().getTransportList();
				for (Transport p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getGeneticInteractionList")) {
				Set<GeneticInteraction> ps = context.createInteractionSearch().getGeneticInteractionList();
				for (GeneticInteraction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getMolecularInteractionList")) {
				Set<MolecularInteraction> ps = context.createInteractionSearch().getMolecularInteractionList();
				for (MolecularInteraction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTemplateReactionList")) {
				Set<TemplateReaction> ps = context.createInteractionSearch().getTemplateReactionList();
				for (TemplateReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getInteractionListByInteractionType")) {
				Set<Interaction> ps = context.createInteractionSearch().getInteractionListByInteractionType(params.get(0));
				for (Interaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getBiochemicalReactionListByECNumber")) {
				Set<BiochemicalReaction> ps = context.createInteractionSearch().getBiochemicalReactionListByECNumber(params.get(0));
				for (BiochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getConversionListByPhysicalEntityAndInteractionTypeTerm")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<Conversion> ps = context.createInteractionSearch().getConversionListByPhysicalEntityAndInteractionTypeTerm((PhysicalEntity)e, params.get(1));
					for (Conversion p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getControlListByController")) {
				Set<Control> ps = context.createInteractionSearch().getControlListByController(params.get(0));
				for (Control p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getControlledListByController")) {
				Set<Process> ps = context.createInteractionSearch().getControlledListByController(params.get(0));
				for (Process p: ps)
					System.out.println(p.getRDFId());
			}
			// PhysicalEntitySearch
			else if (command.equals("getPhysicalEntityList")) {
				Set<PhysicalEntity> ps = context.createPhysicalEntitySearch().getPhysicalEntityList();
				for (PhysicalEntity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getComplexList")) {
				Set<Complex> ps = context.createPhysicalEntitySearch().getComplexList();
				for (Complex p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getDnaList")) {
				Set<Dna> ps = context.createPhysicalEntitySearch().getDnaList();
				for (Dna p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getProteinList")) {
				Set<Protein> ps = context.createPhysicalEntitySearch().getProteinList();
				for (Protein p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getRnaList")) {
				Set<Rna> ps = context.createPhysicalEntitySearch().getRnaList();
				for (Rna p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSmallMoleculeList")) {
				Set<SmallMolecule> ps = context.createPhysicalEntitySearch().getSmallMoleculeList();
				for (SmallMolecule p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPhysicalEntityListByName")) {
				Set<PhysicalEntity> ps = context.createPhysicalEntitySearch().getPhysicalEntityListByName(params.get(0));
				for (PhysicalEntity p: ps)
					System.out.println(p.getRDFId());
			}
			// EntityReferenceSearch
			else if (command.equals("getEntityReferenceList")) {
				Set<EntityReference> ps = context.createEntityReferenceSearch().getEntityReferenceList();
				for (EntityReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getDnaReferenceList")) {
				Set<DnaReference> ps = context.createEntityReferenceSearch().getDnaReferenceList();
				for (DnaReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getProteinReferenceList")) {
				Set<ProteinReference> ps = context.createEntityReferenceSearch().getProteinReferenceList();
				for (ProteinReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getRnaReferenceList")) {
				Set<RnaReference> ps = context.createEntityReferenceSearch().getRnaReferenceList();
				for (RnaReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSmallMoleculeReferenceList")) {
				Set<SmallMoleculeReference> ps = context.createEntityReferenceSearch().getSmallMoleculeReferenceList();
				for (SmallMoleculeReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityReferenceListByName")) {
				Set<EntityReference> ps = context.createEntityReferenceSearch().getEntityReferenceListByName(params.get(0));
				for (EntityReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSequenceEntityReferenceListByOrganism")) {
				Set<SequenceEntityReference> ps = context.createEntityReferenceSearch().getSequenceEntityReferenceListByOrganism(params.get(0));
				for (SequenceEntityReference p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSequenceEntityReferenceListBySequence")) {
				Set<SequenceEntityReference> ps = context.createEntityReferenceSearch().getSequenceEntityReferenceListBySequence(params.get(0), toBoolean(params.get(1)));
				for (SequenceEntityReference p: ps)
					System.out.println(p.getRDFId());
			}
			// MiscSearch (back pointer)
			else if (command.equals("isParticipantOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Entity) {
					Set<Interaction> ps = context.createMiscSearch().isParticipantOf((Entity)e);
					for (Interaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isControllerOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Entity) {
					Set<Control> ps = context.createMiscSearch().isControllerOf((Entity)e);
					for (Control p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isCofactorOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<Catalysis> ps = context.createMiscSearch().isCofactorOf((PhysicalEntity)e);
					for (Catalysis p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isControlledOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Process) {
					Set<Control> ps = context.createMiscSearch().isControlledOf((Process)e);
					for (Control p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isStepProcessOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Process) {
					Set<PathwayStep> ps = context.createMiscSearch().isStepProcessOf((Process)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isStepConversionOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Conversion) {
					Set<BiochemicalPathwayStep> ps = context.createMiscSearch().isStepConversionOf((Conversion)e);
					for (BiochemicalPathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isInteractionTypeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof ControlledVocabulary) {
					Set<Interaction> ps = context.createMiscSearch().isInteractionTypeOf((ControlledVocabulary)e);
					for (Interaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isConfidenceOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Score) {
					Set<Evidence> ps = context.createMiscSearch().isConfidenceOf((Score)e);
					for (Evidence p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isEvidenceCodeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EvidenceCodeVocabulary) {
					Set<Evidence> ps = context.createMiscSearch().isEvidenceCodeOf((EvidenceCodeVocabulary)e);
					for (Evidence p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isExperimentalFormOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EvidenceCodeVocabulary) {
					Set<Evidence> ps = context.createMiscSearch().isEvidenceCodeOf((EvidenceCodeVocabulary)e);
					for (Evidence p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isFeatureOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityFeature) {
					Set<PhysicalEntity> ps = context.createMiscSearch().isFeatureOf((EntityFeature)e);
					for (PhysicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isNotFeatureOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityFeature) {
					Set<PhysicalEntity> ps = context.createMiscSearch().isNotFeatureOf((EntityFeature)e);
					for (PhysicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isXrefOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Xref) {
					Set<XReferrable> ps = context.createMiscSearch().isXrefOf((Xref)e);
					for (XReferrable p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isExperimentalFormDescriptionOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof ExperimentalFormVocabulary) {
					Set<ExperimentalForm> ps = context.createMiscSearch().isExperimentalFormDescriptionOf((ExperimentalFormVocabulary)e);
					for (ExperimentalForm p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isExperimentalFeatureOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityFeature) {
					Set<ExperimentalForm> ps = context.createMiscSearch().isExperimentalFeatureOf((EntityFeature)e);
					for (ExperimentalForm p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isComponentStoichiometryOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<Complex> ps = context.createMiscSearch().isComponentStoichiometryOf((PhysicalEntity)e);
					for (Complex p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isFeatureLocationOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof SequenceLocation) {
					Set<EntityFeature> ps = context.createMiscSearch().isFeatureLocationOf((SequenceLocation)e);
					for (EntityFeature p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isDataSourceOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Provenance) {
					Set<Entity> ps = context.createMiscSearch().isDataSourceOf((Provenance)e);
					for (Entity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isRegulatoryElementOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<TemplateReaction> ps = context.createMiscSearch().isRegulatoryElementOf((PhysicalEntity)e);
					for (TemplateReaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPathwayComponentOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Process) {
					Set<Pathway> ps = context.createMiscSearch().isPathwayComponentOf((Process)e);
					for (Pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isBoundToOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof BindingFeature) {
					Set<BindingFeature> ps = context.createMiscSearch().isBoundToOf((BindingFeature)e);
					for (BindingFeature p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isMemberEntityOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityReference) {
					Set<EntityReference> ps = context.createMiscSearch().isMemberEntityOf((EntityReference)e);
					for (EntityReference p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPathwayOrderOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PathwayStep) {
					Set<Pathway> ps = context.createMiscSearch().isPathwayOrderOf((PathwayStep)e);
					for (Pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isEntityFeatureOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityFeature) {
					Set<EntityReference> ps = context.createMiscSearch().isEntityFeatureOf((EntityFeature)e);
					for (EntityReference p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isProductOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<TemplateReaction> ps = context.createMiscSearch().isProductOf((PhysicalEntity)e);
					for (TemplateReaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isKEQOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof KPrime) {
					Set<BiochemicalReaction> ps = context.createMiscSearch().isKEQOf((KPrime)e);
					for (BiochemicalReaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isEntityReferenceTypeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityReferenceTypeVocabulary) {
					Set<EntityReference> ps = context.createMiscSearch().isEntityReferenceTypeOf((EntityReferenceTypeVocabulary)e);
					for (EntityReference p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isDeltaGOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof DeltaG) {
					Set<BiochemicalReaction> ps = context.createMiscSearch().isDeltaGOf((DeltaG)e);
					for (BiochemicalReaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isInteractionScoreOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Score) {
					Set<GeneticInteraction> ps = context.createMiscSearch().isInteractionScoreOf((Score)e);
					for (GeneticInteraction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isExperimentalFormEntityOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Entity) {
					Set<ExperimentalForm> ps = context.createMiscSearch().isExperimentalFormEntityOf((Entity)e);
					for (ExperimentalForm p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isNextStepOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PathwayStep) {
					Set<PathwayStep> ps = context.createMiscSearch().isNextStepOf((PathwayStep)e);
					for (PathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isMemberLocationOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof SequenceLocation) {
					Set<SequenceLocation> ps = context.createMiscSearch().isMemberLocationOf((SequenceLocation)e);
					for (SequenceLocation p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isParticipantStoichiometryOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Stoichiometry) {
					Set<Conversion> ps = context.createMiscSearch().isParticipantStoichiometryOf((Stoichiometry)e);
					for (Conversion p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isEvidenceOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof Evidence) {
					Set<Observable> ps = context.createMiscSearch().isEvidenceOf((Evidence)e);
					for (Observable p: ps)
						System.out.println(((BioPAXElement)p).getRDFId());
				}
			}
			else if (command.equals("isBindsToOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<PhysicalEntity> ps = context.createMiscSearch().isBindsToOf((PhysicalEntity)e);
					for (PhysicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isTemplateOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<TemplateReaction> ps = context.createMiscSearch().isTemplateOf((PhysicalEntity)e);
					for (TemplateReaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isStructureOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof ChemicalStructure) {
					Set<SmallMoleculeReference> ps = context.createMiscSearch().isStructureOf((ChemicalStructure)e);
					for (SmallMoleculeReference p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPhysicalEntityOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<Stoichiometry> ps = context.createMiscSearch().isPhysicalEntityOf((PhysicalEntity)e);
					for (Stoichiometry p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isTissueOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof TissueVocabulary) {
					Set<BioSource> ps = context.createMiscSearch().isTissueOf((TissueVocabulary)e);
					for (BioSource p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isSequenceIntervalBeginOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof SequenceSite) {
					Set<SequenceInterval> ps = context.createMiscSearch().isSequenceIntervalBeginOf((SequenceSite)e);
					for (SequenceInterval p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isOrganismOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof BioSource) {
					Set<BioPAXElement> ps = context.createMiscSearch().isOrganismOf((BioSource)e);
					for (BioPAXElement p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isCellTypeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof CellVocabulary) {
					Set<BioSource> ps = context.createMiscSearch().isCellTypeOf((CellVocabulary)e);
					for (BioSource p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPhenotypeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhenotypeVocabulary) {
					Set<GeneticInteraction> ps = context.createMiscSearch().isPhenotypeOf((PhenotypeVocabulary)e);
					for (GeneticInteraction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isCellularLocationOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof CellularLocationVocabulary) {
					Set<PhysicalEntity> ps = context.createMiscSearch().isCellularLocationOf((CellularLocationVocabulary)e);
					for (PhysicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isTaxonXrefOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof UnificationXref) {
					Set<BioSource> ps = context.createMiscSearch().isTaxonXrefOf((UnificationXref)e);
					for (BioSource p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isFeatureTypeOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof SequenceModificationVocabulary) {
					Set<ModificationFeature> ps = context.createMiscSearch().isFeatureTypeOf((SequenceModificationVocabulary)e);
					for (ModificationFeature p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isEntityReferenceOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof EntityReference) {
					Set<PhysicalEntity> ps = context.createMiscSearch().isEntityReferenceOf((EntityReference)e);
					for (PhysicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isSequenceIntervalEndOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof SequenceSite) {
					Set<SequenceInterval> ps = context.createMiscSearch().isSequenceIntervalEndOf((SequenceSite)e);
					for (SequenceInterval p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isComponentOf")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof PhysicalEntity) {
					Set<Complex> ps = context.createMiscSearch().isComponentOf((PhysicalEntity)e);
					for (Complex p: ps)
						System.out.println(p.getRDFId());
				}
			}
			// MiscSearch (Other)
			else if (command.equals("getEntityListByDataSource")) {
				Set<Entity> ps = context.createMiscSearch().getEntityListByDataSource(params.get(0));
				for (Entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByDbAndIdOfXref")) {
				Set<Entity> ps = context.createMiscSearch().getEntityListByDbAndIdOfXref(params.get(0), params.get(1));
				for (Entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByName")) {
				Set<Entity> ps = context.createMiscSearch().getEntityListByName(params.get(0));
				for (Entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getProvenanceList")) {
				Set<String> ps = context.createMiscSearch().getProvenanceList();
				for (String p: ps)
					System.out.println(p);
			}
			else if (command.equals("getEntityListByAvairability")) {
				Set<Entity> ps = context.createMiscSearch().getEntityListByAvairability(params.get(0));
				for (Entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByComment")) {
				Set<Entity> ps = context.createMiscSearch().getEntityListByComment(params.get(0));
				for (Entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTitleList")) {
				Set<String> ps = context.createMiscSearch().getTitleList();
				for (String p: ps)
					System.out.println(p);
			}
			else if (command.equals("getTissueList")) {
				Set<String> ps = context.createMiscSearch().getTissueList();
				for (String p: ps)
					System.out.println(p);
			}
			else {
				System.out.println("## COMMAND ERROR");
			}
			System.out.println("### END   ###########################");
			long timeSpan = printTime() - startTime;
			System.out.println(String.valueOf(timeSpan) + " ms");
			context.getSession().close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
