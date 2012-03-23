package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.Histogram;
import org.biopax.paxtools.causality.util.TermCounter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ModificationMatcher
{
	public Histogram getModificationFeatureOverlapHistogram(Model model)
	{
		Histogram hist = new Histogram(1);

		for (EntityReference er : model.getObjects(ProteinReference.class))
		{
			Map<Provenance, Set<ModificationFeature>> modif = collectModifications(er);
			Histogram h = getOverlapHistogram(modif);
			hist.add(h);
		}
		return hist;
	}

	static Map<Provenance, TermCounter> tc = new HashMap<Provenance, TermCounter>();
	
	protected Map<Provenance, Set<ModificationFeature>> collectModifications(EntityReference er)
	{
		Map<Provenance, Set<ModificationFeature>> map =
			new HashMap<Provenance, Set<ModificationFeature>>();

		for (SimplePhysicalEntity pe : er.getEntityReferenceOf())
		{
			Set<Provenance> ds = pe.getDataSource();

			for (EntityFeature ef : pe.getFeature())
			{
				if (ef instanceof ModificationFeature)
				{
					ModificationFeature mf = (ModificationFeature) ef;

					for (Provenance prov : ds)
					{
						if (!map.containsKey(prov))
							map.put(prov, new HashSet<ModificationFeature>());

						map.get(prov).add(mf);

						if (!tc.containsKey(prov)) tc.put(prov, new TermCounter());
						if (mf.getModificationType() != null)
							tc.get(prov).addTerm(mf.getModificationType().toString());
					}
				}
			}
		}
		return map;
	}
	
	protected Histogram getOverlapHistogram(Map<Provenance, Set<ModificationFeature>> map)
	{
		Map<Integer, Map<SequenceModificationVocabulary, Set<Provenance>>> ali = alignFeatures(map);

		Histogram h = new Histogram(1);

		for (Integer pos : ali.keySet())
		{
			Map<SequenceModificationVocabulary, Set<Provenance>> m = ali.get(pos);
			for (SequenceModificationVocabulary type : m.keySet())
			{
				h.count(m.get(type).size());
			}
		}
		return h;
	}

	protected Map<Integer, Map<SequenceModificationVocabulary, Set<Provenance>>> alignFeatures(
		Map<Provenance, Set<ModificationFeature>> map)
	{
		if (map.size() > 1)
		{
			System.out.print("");
		}

		Map<Integer, Map<SequenceModificationVocabulary, Set<Provenance>>> align =
			new HashMap<Integer, Map<SequenceModificationVocabulary, Set<Provenance>>>();

		for (Provenance prov : map.keySet())
		{
			for (ModificationFeature mf : map.get(prov))
			{
				SequenceModificationVocabulary type = mf.getModificationType();
				if (type == null) continue;

				SequenceLocation fl = mf.getFeatureLocation();
				if (!(fl instanceof SequenceSite)) continue;

				SequenceSite ss = (SequenceSite) fl;
				PositionStatusType ps = ss.getPositionStatus();
				if (ps != PositionStatusType.EQUAL) continue;

				int pos = ss.getSequencePosition();
				if (!align.containsKey(pos))
					align.put(pos, new HashMap<SequenceModificationVocabulary, Set<Provenance>>());

				Map<SequenceModificationVocabulary, Set<Provenance>> m = align.get(pos);
				if (!m.containsKey(type)) m.put(type, new HashSet<Provenance>());

				m.get(type).add(prov);
			}
		}
		return align;
	}
}
