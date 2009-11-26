/*
 * HiRDBTrial.java
 * sample program
 *
 * 2007.01 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.evidence;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.sequenceEntity;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level2.transport;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.biopax.paxtools.model.level2.xref;

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
	boolean bProgress = false;
	boolean bPrintTime = false;
	long vacuumLoopCount = 10000;
	boolean bCleanUpload = false;
	
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
			else if (key.equals("progress")) {
				bProgress = toBoolean(value);
			}
			else if (key.equals("printTime")) {
				bPrintTime = toBoolean(value);
			}
			else if (key.equals("vacuumLoopCount")) {
				vacuumLoopCount = toLong(value, vacuumLoopCount);
			}
			else if (key.equals("cleanUpload")) {
				bCleanUpload = toBoolean(value);
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

	long toLong(String s, long defaultValue) {
		try {
			return Long.getLong(s);
		}
		catch (Exception e) {
		}
		return defaultValue;
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
			context.getConnect().bProgress = bProgress;
			context.getConnect().bPrintTime = bPrintTime;
			context.getConnect().vacuumLoopCount = vacuumLoopCount;
			context.getConnect().bCleanUpload = bCleanUpload;
			context.setup();
			long startTime = printTime();
			System.out.println("### START ###########################");
			if (command.equals("uploadbigfilefirst")) {
				if (filePath.length() > 0) {
					File f = new File(filePath);
					if (f != null) {
						context.getConnect().uploadOWLBigFileFirst(f.getName(), new FileInputStream(f));
					}
				}
			}
			else if (command.equals("upload")) {
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
			else if (command.equals("keyword")) {
				List<BioPAXElement> list = context.createKeywordSearch().search(params.get(0));
				if (list != null) {
					for (BioPAXElement bp : list) {
						System.out.println("rdf:ID " + bp.getRDFId());
					}
				}
			}
			else if (command.equals("getPathwayList")) {
				Set<pathway> ps = context.createPathwaySearch().getPathwayList();
				for (pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPathwayListByNAME")) {
				Set<pathway> ps = context.createPathwaySearch().getPathwayListByNAME(params.get(0), toBoolean(params.get(1)));
				for (pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPathwayListByORGANISM")) {
				Set<pathway> ps = context.createPathwaySearch().getPathwayListByORGANISM(params.get(0));
				for (pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSuperPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathway> ps = context.createPathwaySearch().getSuperPathwayList((pathway)e);
					for (pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllSuperPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathway> ps = context.createPathwaySearch().getAllSuperPathwayList((pathway)e);
					for (pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllSubPathwayList")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathway> ps = context.createPathwaySearch().getAllSubPathwayList((pathway)e);
					for (pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getTopLevelPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					pathway p = context.createPathwaySearch().getTopLevelPathway((pathway)e);
					System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getNEXT_STEPListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathwayStep> ps = context.createPathwaySearch().getNEXT_STEPListOfPathway((pathway)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllNEXT_STEPListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathwayStep> ps = context.createPathwaySearch().getAllNEXT_STEPListOfPathway((pathway)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getPreviousStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathwayStep> ps = context.createPathwaySearch().getPreviousStepListOfPathway((pathway)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllPreviousStepListOfPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<pathwayStep> ps = context.createPathwaySearch().getAllPreviousStepListOfPathway((pathway)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getEC_NUMBERListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<String> ps = context.createPathwaySearch().getEC_NUMBERListInPathway((pathway)e);
					for (String p: ps)
						System.out.println(p);
				}
			}
			else if (command.equals("getPhysicalEntityListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<physicalEntity> ps = context.createPathwaySearch().getPhysicalEntityListInPathway((pathway)e);
					for (physicalEntity p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getInteractionListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<interaction> ps = context.createPathwaySearch().getInteractionListInPathway((pathway)e);
					for (interaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllEvidenceListInPathway")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathway) {
					Set<evidence> ps = context.createPathwaySearch().getAllEvidenceListInPathway((pathway)e);
					for (evidence p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getAllPublicationXrefListInPathway")) {
				//BioPAXElement e = context.getElementByRDFID(params.get(0));
				//if (e != null && e instanceof pathway) {
				//	Set<publicationXref> ps = context.createPathwaySearch().getAllPublicationXrefListInPathway((pathway)e);
				//	for (publicationXref p: ps)
				//		System.out.println(p.getRDFId());
				//}
			}
			else if (command.equals("getAllURLListInPathway")) {
				//BioPAXElement e = context.getElementByRDFID(params.get(0));
				//if (e != null && e instanceof pathway) {
				//	Set<String> ps = context.createPathwaySearch().getAllURLListInPathway((pathway)e);
				//	for (String p: ps)
				//		System.out.println(p);
				//}
			}
			else if (command.equals("getAllUnificationXrefListInPathway")) {
				//BioPAXElement e = context.getElementByRDFID(params.get(0));
				//if (e != null && e instanceof pathway) {
				//	Set<unificationXref> ps = context.createPathwaySearch().getAllUnificationXrefListInPathway((pathway)e);
				//	for (unificationXref p: ps)
				//		System.out.println(p.getRDFId());
				//}
			}
			else if (command.equals("getAllRelationshipXrefListInPathway")) {
				//BioPAXElement e = context.getElementByRDFID(params.get(0));
				//if (e != null && e instanceof pathway) {
				//	Set<relationshipXref> ps = context.createPathwaySearch().getAllRelationshipXrefListInPathway((pathway)e);
				//	for (relationshipXref p: ps)
				//		System.out.println(p.getRDFId());
				//}
			}
			else if (command.equals("getPathwayListByTERMOfEVIDENCE_CODE")) {
				Set<pathway> ps = context.createPathwaySearch().getPathwayListByTERMOfEVIDENCE_CODE(params.get(0));
				for (pathway p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getInteractionList")) {
				Set<interaction> ps = context.createInteractionSearch().getInteractionList();
				for (interaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getControlList")) {
				Set<control> ps = context.createInteractionSearch().getControlList();
				for (control p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getCatalysisList")) {
				Set<catalysis> ps = context.createInteractionSearch().getCatalysisList();
				for (catalysis p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getModulationList")) {
				Set<modulation> ps = context.createInteractionSearch().getModulationList();
				for (modulation p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getConversionList")) {
				Set<conversion> ps = context.createInteractionSearch().getConversionList();
				for (conversion p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getBiochemicalReactionList")) {
				Set<biochemicalReaction> ps = context.createInteractionSearch().getBiochemicalReactionList();
				for (biochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTransportWithBiochemicalReactionList")) {
				Set<transportWithBiochemicalReaction> ps = context.createInteractionSearch().getTransportWithBiochemicalReactionList();
				for (transportWithBiochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getComplexAssemblyList")) {
				Set<complexAssembly> ps = context.createInteractionSearch().getComplexAssemblyList();
				for (complexAssembly p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getTransportList")) {
				Set<transport> ps = context.createInteractionSearch().getTransportList();
				for (transport p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getInteractionListByNAME")) {
				Set<interaction> ps = context.createInteractionSearch().getInteractionListByNAME(params.get(0), toBoolean(params.get(1)));
				for (interaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getInteractionListByINTERACTION_TYPE")) {
				Set<interaction> ps = context.createInteractionSearch().getInteractionListByINTERACTION_TYPE(params.get(0));
				for (interaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getBiochemicalReactionListByEC_NUMBER")) {
				Set<biochemicalReaction> ps = context.createInteractionSearch().getBiochemicalReactionListByEC_NUMBER(params.get(0));
				for (biochemicalReaction p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getControlListByCONTROLLED")) {
				Set<control> ps = context.createInteractionSearch().getControlListByCONTROLLED(params.get(0), toBoolean(params.get(1)));
				for (control p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getCONTROLLERListByCONTROLLED")) {
				Set<physicalEntityParticipant> ps = context.createInteractionSearch().getCONTROLLERListByCONTROLLED(params.get(0), toBoolean(params.get(1)));
				for (physicalEntityParticipant p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPhysicalEntityList")) {
				Set<physicalEntity> ps = context.createPhysicalEntitySearch().getPhysicalEntityList();
				for (physicalEntity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getComplexList")) {
				Set<complex> ps = context.createPhysicalEntitySearch().getComplexList();
				for (complex p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getDnaList")) {
				Set<dna> ps = context.createPhysicalEntitySearch().getDnaList();
				for (dna p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getProteinList")) {
				Set<protein> ps = context.createPhysicalEntitySearch().getProteinList();
				for (protein p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getRnaList")) {
				Set<rna> ps = context.createPhysicalEntitySearch().getRnaList();
				for (rna p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSmallMoleculeList")) {
				Set<smallMolecule> ps = context.createPhysicalEntitySearch().getSmallMoleculeList();
				for (smallMolecule p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPhysicalEntityListByNAME")) {
				Set<physicalEntity> ps = context.createPhysicalEntitySearch().getPhysicalEntityListByNAME(params.get(0), toBoolean(params.get(1)));
				for (physicalEntity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getPhysicalEntityListByORGANISM")) {
				Set<physicalEntity> ps = context.createPhysicalEntitySearch().getPhysicalEntityListByORGANISM(params.get(0));
				for (physicalEntity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getSequenceEntityListBySEQUENCE")) {
				Set<sequenceEntity> ps = context.createPhysicalEntitySearch().getSequenceEntityListBySEQUENCE(params.get(0), toBoolean(params.get(1)));
				for (sequenceEntity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("isNEXT_STEPof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathwayStep) {
					Set<pathwayStep> ps = context.createMiscSearch().isNEXT_STEPof((pathwayStep)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPATHWAY_COMPONENTSof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof pathwayComponent) {
					Set<pathway> ps = context.createMiscSearch().isPATHWAY_COMPONENTSof((pathwayComponent)e);
					for (pathway p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isSTEP_INTERACTIONSof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof process) {
					Set<pathwayStep> ps = context.createMiscSearch().isSTEP_INTERACTIONSof((process)e);
					for (pathwayStep p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isXREFof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof xref) {
					Set<XReferrable> ps = context.createMiscSearch().isXREFof((xref)e);
					for (XReferrable p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPARTICIPANTSof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof InteractionParticipant) {
					Set<interaction> ps = context.createMiscSearch().isPARTICIPANTSof((InteractionParticipant)e);
					for (interaction p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isCOMPONENTSof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof physicalEntityParticipant) {
					complex p = context.createMiscSearch().isCOMPONENTSof((physicalEntityParticipant)e);
					System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("isPHYSICAL_ENTITYof")) {
				BioPAXElement e = context.getElementByRDFID(params.get(0));
				if (e != null && e instanceof physicalEntity) {
					Set<physicalEntityParticipant> ps = context.createMiscSearch().isPHYSICAL_ENTITYof((physicalEntity)e);
					for (physicalEntityParticipant p: ps)
						System.out.println(p.getRDFId());
				}
			}
			else if (command.equals("getEntityListByDATA_SOURCE")) {
				Set<entity> ps = context.createMiscSearch().getEntityListByDATA_SOURCE(params.get(0));
				for (entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByDBAndIDOfXREF")) {
				Set<entity> ps = context.createMiscSearch().getEntityListByDBAndIDOfXREF(params.get(0), params.get(1));
				for (entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByNAME")) {
				Set<entity> ps = context.createMiscSearch().getEntityListByNAME(params.get(0), toBoolean(params.get(1)));
				for (entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getDataSourceList")) {
				Set<String> ps = context.createMiscSearch().getDataSourceList();
				for (String p: ps)
					System.out.println(p);
			}
			else if (command.equals("getEntityListByAVAIRABILITY")) {
				Set<entity> ps = context.createMiscSearch().getEntityListByAVAIRABILITY(params.get(0));
				for (entity p: ps)
					System.out.println(p.getRDFId());
			}
			else if (command.equals("getEntityListByCOMMENT")) {
				Set<entity> ps = context.createMiscSearch().getEntityListByCOMMENT(params.get(0));
				for (entity p: ps)
					System.out.println(p.getRDFId());
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
