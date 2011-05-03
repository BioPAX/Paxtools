package org.biopax.paxtools.examples;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Prints all the genes (aka proteins) in the L2 pathway 
 * and sub-pathways (*conditions apply), and also - trace 
 * where they come from.
 * 
 * * Note: it doesn't traverse the 'NEXT-STEP' property!
 * (as doing so may take you beyond the pathway of interest)
 * 
 * @author rodch
 */
public class PathwayGenesExtractor implements Visitor {

	static final String OUT = "geneset.txt";	
	
	pathway pw;
	Map<String,Set<String>> geneset;
	Traverser traverser;
	Collection<BioPAXElement> visited;
	String path = "";
	Collection<pathway> subpathways;
	Collection<interaction> interactions;
	
	public PathwayGenesExtractor(pathway pw) {
		traverser = new Traverser(SimpleEditorMap.get(BioPAXLevel.L2), this);
		geneset = new HashMap<String, Set<String>>();
		subpathways = new HashSet<pathway>();
		interactions = new HashSet<interaction>();
		visited = new HashSet<BioPAXElement>();
		this.pw = pw;
	}
	
	void run() {
		traverser.traverse(pw, null);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		if(args.length != 2) {
			System.out.println("\nUse Parameters: " +
					"biopaxFile pathwayFullRdfId\n");
			System.exit(-1);
		}
			
		BioPAXIOHandler handler = new SimpleIOHandler();
		FileInputStream fileInputStream = new FileInputStream(args[0]);
		Model model = handler.convertFromOWL(fileInputStream);
		
		String pwId = args[1]; // gets pathway ID
		pathway pw = (pathway) model.getByID(pwId);
		
		// extract proteins
		PathwayGenesExtractor extractor = new PathwayGenesExtractor(pw);
		extractor.run();
		
		PrintWriter out = new PrintWriter(OUT);
		out.println("rdf:IDs of proteins in the pathway : " + pw.getNAME() + " and its sub-pathways.");
		Set<String> glist = new HashSet<String>(); // to keep all IDs
			
		for(String key : extractor.geneset.keySet()) {
			glist.addAll(extractor.geneset.get(key));
			StringBuffer sb = new StringBuffer(key);
			for(String name : extractor.geneset.get(key)) {
				sb.append(", ").append(name);
			}
			out.println(sb.toString());
		}
		
		out.println("\nALL IDs:");
		for(String g : glist) {
			out.println(g);
		}
		
		out.println("\nSub-pathways (rdfId : NAME):");
		for(pathway w : extractor.subpathways) {
			out.println(getLocalId(w) + " : " + w.getNAME());
		}		

		out.println("\nInteractions:");
		for(interaction it : extractor.interactions) {
			out.println(getLocalId(it) + " : " + it.getNAME());
		}
			
		out.close();
	}
	
	public void visit(BioPAXElement domain1, Object range, Model model, PropertyEditor editor) {
		
		// do not traverse the NEXT-STEP
		if(editor.getProperty().equals("NEXT-STEP")) {
			return;
		}
		
		if (range != null && range instanceof  BioPAXElement && !visited.contains(range))
		{
			BioPAXElement bpe = (BioPAXElement) range;
			path += getIdent(bpe);
			System.out.print(path + editor.getProperty() + "=" 
					+ getLocalId(bpe) + " " + bpe.getModelInterface().getSimpleName());
			if(bpe instanceof entity && ((entity) bpe).getNAME() != null) {
				System.out.print(" {"
						+ ((entity) bpe).getNAME()
						.replace("(name copied from entity in Homo sapiens)", "(name from human)")
						+ "}");
			}

			if(bpe instanceof pathway) {
				subpathways.add((pathway) bpe);
			} else if(bpe instanceof interaction) {
				interactions.add((interaction) bpe);
			}
			
			if (bpe instanceof protein) {
				protein p = (protein) bpe;
				String id = getLocalId(p);
				
				Set<String> refs = new HashSet<String>();
				for (xref x : p.getXREF()) {
					if (x instanceof unificationXref || x instanceof relationshipXref) {
						refs.add(x.getID());
					}
				}

				System.out.print(" (" + refs.size() + " ADDED!)");
				
				if (geneset.containsKey(id)) {
					geneset.get(id).addAll(refs);
				} else {
					geneset.put(id, refs);
				}

			}
			System.out.println();
			
			visited.add(bpe);

			// go deeper
			traverser.traverse(bpe, model);
			
			path = path.substring(0, path.length()-4);	
		}
	}
	
	// get remarks
	private String getIdent(BioPAXElement bpe) {
		String ident = "----";

		if(bpe instanceof pathway) {
			ident = "-pw-";
		} else if(bpe instanceof interaction) {
			ident = "-in-";
		} else if (bpe instanceof protein) {
			ident = "-pr-";
		} else if (bpe instanceof complex) {
			ident = "-co-";
		} 
		return ident;
	}
	
	// Gets the local part of the RDF ID (- beyond the last '#')
	static String getLocalId(BioPAXElement bpe) {
		String id = bpe.getRDFId();
		return id.replaceFirst("^.+?#", "");
	}

}
