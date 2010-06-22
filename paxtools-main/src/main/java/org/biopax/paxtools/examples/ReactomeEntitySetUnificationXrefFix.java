package org.biopax.paxtools.examples;

import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.Level2Factory;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.util.ClassFilterSet;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a class I wrote for fixing the unification xref problem in Reactome entity sets. It
 * basically reads in the model, goes over physical entities and looks for the converted from
 * reactome entity set comment. For each such generic entities it converts all of its unification
 * xrefs to relationship xrefs
 */
public class ReactomeEntitySetUnificationXrefFix
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 2)
		{
			fixReactome(new FileInputStream(new File(args[0])),
					new FileOutputStream(new File(args[1])));
		}
		else
		fixReactome(System.in, System.out);
	}

	public static void fixReactome(InputStream in, OutputStream out) throws IOException
	{
		SimpleReader reader = new SimpleReader();
		SimpleExporter exporter = new SimpleExporter(BioPAXLevel.L2);
		Level2Factory l2f = (Level2Factory) BioPAXLevel.L2.getDefaultFactory();
		Model level2 = reader.convertFromOWL(in);
		Set<physicalEntity> physicalEntitySet = new HashSet<physicalEntity>();
		physicalEntitySet.addAll(level2.getObjects(physicalEntity.class));
		for (physicalEntity pe : physicalEntitySet)
		{
			boolean entitySet = false;
			for (String comment : pe.getCOMMENT())
			{
				if (comment.contains("Converted from EntitySet in Reactome"))
				{
					entitySet = true;
					break;

				}

			}
			if (entitySet)
			{
				Set<unificationXref> unis = new HashSet<unificationXref>();
				unis.addAll(
						new ClassFilterSet<unificationXref>(pe.getXREF(), unificationXref.class));
				for (unificationXref uni : unis)
				{
					relationshipXref rel;
					String rid = uni.getRDFId() + "-r";
					BioPAXElement exists = level2.getByID(rid);
					if (exists != null)
					{
						rel = (relationshipXref) exists;
					}
					else
					{
						rel = l2f.createRelationshipXref();
						rel.setDB(uni.getDB());
						rel.setID(uni.getID());
						rel.setRDFId(rid);
						level2.add(rel);
					}
					pe.removeXREF(uni);
					if (uni.isXREFof().isEmpty())
					{
						level2.remove(uni);
					}
					pe.addXREF(rel);

				}
			}

		}

		exporter.convertToOWL(level2, out);
		out.close();
	}
}

