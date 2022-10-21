package org.biopax.paxtools.search;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.biopax.paxtools.controller.DataPropertyEditor;
import org.biopax.paxtools.controller.Fetcher;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A full-text searcher/indexer for BioPAX L3 models.
 *
 * @author rodche
 */
public class SearchEngine implements Indexer, Searcher {
	private static final Logger LOG = LoggerFactory.getLogger(SearchEngine.class);

	// search fields
	public static final String FIELD_URI = "uri";
	public static final String FIELD_KEYWORD = "keyword"; //anything, e.g., names, terms, comments, incl. - from child elements 
	public static final String FIELD_NAME = "name";
	public static final String FIELD_XREFID = "xrefid"; //xref.id
	public static final String FIELD_PATHWAY = "pathway"; //parent/owner pathways; to be inferred from the whole biopax model
	public static final String FIELD_N_PARTICIPANTS = "participants"; // num. of PEs or Genes in a process or Complex
	public static final String FIELD_N_PROCESSES = "processes"; // is same as 'size' used to be before cPath2 v7
	// Full-text search/filter fields (case sensitive) -
	//index organism names, cell/tissue type (term), taxonomy id, but only store BioSource URIs	
	public static final String FIELD_ORGANISM = "organism";
	//index data source names, but only URIs are stored in the index
	public static final String FIELD_DATASOURCE = "datasource";
	public static final String FIELD_TYPE = "type";

	//Default fields to use with the MultiFieldQueryParser;
	//one can still search in other fields directly, e.g.,
	//pathway:some_keywords datasource:"pid", etc.
	public final static String[] DEFAULT_FIELDS =
		{
			FIELD_KEYWORD, //includes all data type properties (names, terms, comments), 
			// also from child elements up to given depth (3), also includes pathway names (inferred)
			FIELD_NAME, // standardName, displayName, other names
			FIELD_XREFID, //xref.id (also direct child's xref.id, i.e., can find both xref and its owners using a xrefid:<id> query string)
		};

	/**
	 * A Key for the value in a 
	 * BioPAX element's annotations map
	 * where additional information about  
	 * corresponding search hit will be stored. 
	 */
	public enum HitAnnotation
	{
		HIT_EXCERPT,
		HIT_PROCESSES,
		HIT_PARTICIPANTS,
		HIT_ORGANISM,
		HIT_DATASOURCE,
		HIT_PATHWAY,
	}

	private final Model model;
	private int maxHitsPerPage;
	private final Analyzer analyzer;
	private final Path indexFile;
	private SearcherManager searcherManager;

	public final static int DEFAULT_MAX_HITS_PER_PAGE = 100;


	/**
	 * Constructor.
	 *
	 * @param model the BioPAX Model to index or search
	 * @param indexLocation index directory location
	 */
	public SearchEngine(Model model, String indexLocation) {
		this.model = model;
		this.indexFile = Paths.get(indexLocation);
		initSearcherManager();
		this.maxHitsPerPage = DEFAULT_MAX_HITS_PER_PAGE;
		Map<String,Analyzer> analyzersPerField = new HashMap<>();
		analyzersPerField.put(FIELD_NAME, new KeywordAnalyzer());
		analyzersPerField.put(FIELD_XREFID, new KeywordAnalyzer());
		analyzersPerField.put(FIELD_URI, new KeywordAnalyzer());
		analyzersPerField.put(FIELD_PATHWAY, new KeywordAnalyzer());
		this.analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzersPerField);
	}

	private void initSearcherManager() {
		try {
			if(Files.exists(indexFile))
				this.searcherManager =
					new SearcherManager(MMapDirectory.open(indexFile), new SearcherFactory());
			else
				LOG.info(indexFile + " does not exist.");
		} catch (IOException e) {
			LOG.warn("Could not create a searcher: " + e);
		}
	}

	/**
	 * Sets the maximum no. hits per search results page (pagination).
	 *
	 * @param maxHitsPerPage positive int value; otherwise - unlimited
	 */
	public void setMaxHitsPerPage(int maxHitsPerPage) {
		this.maxHitsPerPage = maxHitsPerPage;
	}

	/**
	 * Gets the maximum no. hits per search results page (pagination parameter).
	 * @return int value
	 */
	public int getMaxHitsPerPage() {
		return maxHitsPerPage;
	}

	public SearchResult search(String query, int page,
														 Class<? extends BioPAXElement> filterByType, String[] datasources,
														 String[] organisms)
	{
		SearchResult response;

		LOG.debug("search: " + query + ", page: " + page
			+ ", filterBy: " + filterByType
			+ "; extra filters: ds in (" + Arrays.toString(datasources)
			+ "), org. in (" + Arrays.toString(organisms) + ")");

		IndexSearcher searcher = null;

		try {
			QueryParser queryParser = new MultiFieldQueryParser(DEFAULT_FIELDS, analyzer);
			queryParser.setAllowLeadingWildcard(true);//TODO do we really want leading wildcards (e.g. *sulin)?
			searcher = searcherManager.acquire();

			//find and transform top docs to search hits (beans), considering pagination...
			if(!query.trim().equals("*")) { //if not "*" query, which is not supported out-of-the-box, then
				//create the lucene query
				Query q = queryParser.parse(query);
				LOG.debug("parsed lucene query is " + q.getClass().getSimpleName());

				//create filter: type AND (d OR d...) AND (o OR o...)
				Query filter = createFilter(filterByType, datasources, organisms);

				//final query with filter
				q = (filter != null)
					? new BooleanQuery.Builder().add(q,Occur.MUST).add(filter,Occur.FILTER).build()
					: q;

				//get the first page of top hits
				TopDocs topDocs = searcher.search(q, maxHitsPerPage);

				//get the required hits page if page>0
				if(page>0) {
					TopScoreDocCollector collector = TopScoreDocCollector.create(maxHitsPerPage*(page+1));
					searcher.search(q, collector);
					topDocs = collector.topDocs(page * maxHitsPerPage, maxHitsPerPage);
				}

				//transform docs to hits, use a highlighter to get excerpts
				response = transform(q, searcher, true, topDocs);

			} else {
				//find ALL objects of a particular BioPAX class (+ filters by organism, datasource)
				if(filterByType==null) {
					filterByType = Level3Element.class;
				}

				//replace q="*" with a search for the class or its sub-class name in the TYPE field
				BooleanQuery.Builder b = new BooleanQuery.Builder();
				for(Class<? extends BioPAXElement> subType : SimpleEditorMap.L3.getKnownSubClassesOf(filterByType)) {
					b.add(new TermQuery(new Term(FIELD_TYPE, subType.getSimpleName().toLowerCase())), Occur.SHOULD);
				}

				Query filter = createFilter(null, datasources, organisms);

				//combine star and filter queries into one special boolean
				Query q = (filter != null)
					? new BooleanQuery.Builder().add(b.build(),Occur.MUST).add(filter,Occur.FILTER).build()
					: b.build();

				//get the first page of top hits
				TopDocs topDocs = searcher.search(q, maxHitsPerPage);

				//get the required hits page if page>0
				if(page>0) {
					TopScoreDocCollector collector = TopScoreDocCollector.create(maxHitsPerPage*(page+1));
					searcher.search(q, collector);
					topDocs = collector.topDocs(page * maxHitsPerPage, maxHitsPerPage);
				}

				//convert
				response = transform(q, searcher, false, topDocs);
			}
		} catch (ParseException e) {
			throw new RuntimeException("getTopDocs: failed to parse the query string.", e);
		} catch (IOException e) {
			throw new RuntimeException("getTopDocs: failed.", e);
		} finally {
			try {
				if(searcher != null) {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {}
		}

		response.setPage(page);
		return response;
	}


	/**
	 * Returns a SearchResult
	 * that contains a List<BioPAXElement>,
	 * some parameters, totals, etc.
	 */
	private SearchResult transform(Query query, IndexSearcher searcher, boolean highlight, TopDocs topDocs)
		throws IOException
	{
		final SearchResult response = new SearchResult();
		final List<BioPAXElement> hits = new ArrayList<>();

		response.setMaxHitsPerPage(maxHitsPerPage);
		response.setHits(hits);

		for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			String uri = doc.get(FIELD_URI);
			BioPAXElement bpe = model.getByID(uri);
			Map<String,Object> annotations = bpe.getAnnotations();
			LOG.debug("transform: doc:" + scoreDoc.doc + ", uri:" + uri);

			// use the highlighter (get matching fragments)
			// for this to work, all keywords were stored in the index field
			if (highlight && doc.get(FIELD_KEYWORD) != null) {
				// use a Highlighter (store.YES must be enabled for 'keyword' field)
				QueryScorer scorer = new QueryScorer(query, FIELD_KEYWORD);
				//this fixes scoring/highlighting for all-field wildcard queries like q=insulin* 
				//but not for term/prefix queries, i.e, q=name:insulin*, q=pathway:brca2. TODO
				scorer.setExpandMultiTermQuery(true);

				//TODO try PostingsHighlighter once it's stable...
				// (see http://lucene.apache.org/core/4_10_0/highlighter/org/apache/lucene/search/postingshighlight/PostingsHighlighter.html)
				SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class='hitHL'>", "</span>");
				Highlighter highlighter = new Highlighter(formatter, scorer);
				highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, 80));

				final String text = StringUtils.join(doc.getValues(FIELD_KEYWORD), " ");
				try {
					TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text));
					String res = highlighter.getBestFragments(tokenStream, text, 7, "...");

					if(res != null && !res.isEmpty()) {
						annotations.put(HitAnnotation.HIT_EXCERPT.name(), res);
					}

				} catch (Exception e) {throw new RuntimeException(e);}

			} else if(highlight) {
				LOG.warn("Highlighter skipped, because KEYWORD field was null; hit: "
					+ uri + ", " + bpe.getModelInterface().getSimpleName());
			}

			// extract organisms (URI only) if not done before
			if(doc.get(FIELD_ORGANISM) != null && !annotations.containsKey(HitAnnotation.HIT_ORGANISM.name())) {
				Set<String> uniqueVals = new TreeSet<>();
				for(String o : doc.getValues(FIELD_ORGANISM)) {
					//note: only URIS are stored in the index					
					uniqueVals.add(o);
				}
				annotations.put(HitAnnotation.HIT_ORGANISM.name(), uniqueVals);
			}

			// extract values form the index if not previously done
			if(doc.get(FIELD_DATASOURCE) != null && !annotations.containsKey(HitAnnotation.HIT_DATASOURCE.name())) {
				Set<String> uniqueVals = new TreeSet<String>();
				for(String d : doc.getValues(FIELD_DATASOURCE)) {
					//note: only URIS are stored in the index
					uniqueVals.add(d);
				}
				annotations.put(HitAnnotation.HIT_DATASOURCE.name(), uniqueVals);
			}

			// extract only pathway URIs if not previously done
			//(because names and IDs used to be stored in the index field as well)
			if(doc.get(FIELD_PATHWAY) != null && !annotations.containsKey(HitAnnotation.HIT_PATHWAY.name())) {
				Set<String> uniqueVals = new TreeSet<String>();
				for(String d : doc.getValues(FIELD_PATHWAY)) {
					//only URIs were stored (though all names/ids were indexed/analyzed)
					if(!d.equals(uri)) //exclude itself
						uniqueVals.add(d);
				}
				annotations.put(HitAnnotation.HIT_PATHWAY.name(), uniqueVals);
			}

			//store the no. processes in the sub-network if not previously done
			if(doc.get(FIELD_N_PARTICIPANTS)!=null && !annotations.containsKey(HitAnnotation.HIT_PARTICIPANTS.name()))
				annotations.put(HitAnnotation.HIT_PARTICIPANTS.name(), Integer.valueOf(doc.get(FIELD_N_PARTICIPANTS)));
			if(doc.get(FIELD_N_PROCESSES)!=null && !annotations.containsKey(HitAnnotation.HIT_PROCESSES.name()))
				annotations.put(HitAnnotation.HIT_PROCESSES.name(), Integer.valueOf(doc.get(FIELD_N_PROCESSES)));

			//store the Lucene's score and explanation.
			String excerpt = (String) annotations.get(HitAnnotation.HIT_EXCERPT.name());
			if(excerpt == null) excerpt = "";
			excerpt += " -SCORE- " + scoreDoc.score + " -EXPLANATION- " + searcher.explain(query, scoreDoc.doc);
			annotations.put(HitAnnotation.HIT_EXCERPT.name(), excerpt);

			hits.add(bpe);
		}

		//set total no. hits	
		response.setTotalHits(topDocs.totalHits);

		return response;
	}


	public void index() {
		final int numObjects =  model.getObjects().size();
		LOG.info("index(), there are " + numObjects + " BioPAX objects to be (re-)indexed.");
		IndexWriter iw;
		try {
			//close the searcher manager if the old index exists
			if(searcherManager != null) {
				searcherManager.close();
				searcherManager = null;
			}
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			iw = new IndexWriter(FSDirectory.open(indexFile), conf);
			//cleanup
			iw.deleteAll();
			iw.commit();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create a new IndexWriter.", e);
		}
		final IndexWriter indexWriter = iw;

		ExecutorService exec = Executors.newFixedThreadPool(30);

		final AtomicInteger numLeft = new AtomicInteger(numObjects);

		final org.biopax.paxtools.util.Filter<DataPropertyEditor> dataPropertiesToConsider = editor -> {
			final String prop = editor.getProperty();
			//to include in the index, as keywords, only the following properties
			// (basically, to exclude float type properties, embedded xml, db names, etc.):
			return (prop.equalsIgnoreCase("author") || prop.equalsIgnoreCase("availability")
				|| prop.equalsIgnoreCase("chemicalFormula") || prop.equalsIgnoreCase("comment")
				|| prop.equalsIgnoreCase("controlType") || prop.equalsIgnoreCase("conversionDirection")
				|| prop.equalsIgnoreCase("eCNumber") || prop.equalsIgnoreCase("id")
				|| prop.equalsIgnoreCase("name") || prop.equalsIgnoreCase("displayName")
				|| prop.equalsIgnoreCase("standardName") || prop.equalsIgnoreCase("sequence")
				|| prop.equalsIgnoreCase("source") || prop.equalsIgnoreCase("year")
				|| prop.equalsIgnoreCase("term") || prop.equalsIgnoreCase("stepDirection")
				|| prop.equalsIgnoreCase("structureData") || prop.equalsIgnoreCase("templateDirection")
				|| prop.equalsIgnoreCase("title") || prop.equalsIgnoreCase("url")
			);
		};

		final Fetcher fetcher = new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter);
		fetcher.setSkipSubPathways(true);

		for(final BioPAXElement bpe : model.getObjects()) {
			// prepare & index each element in a separate thread
			exec.execute(() -> {
				// get or infer some important values if possible from this, child or parent objects:
				Set<String> keywords = ModelUtils.getKeywords(bpe, 3, dataPropertiesToConsider);
				// a hack to remove special (debugging) biopax comments
				for(String s : new HashSet<>(keywords)) {
					//exclude additional comments generated by normalizer, merger, etc.
					if(s.startsWith("REPLACED ") || s.contains("ADDED"))
						keywords.remove(s);
				}

				Map<String,Object> annotations = bpe.getAnnotations();
				annotations.put(FIELD_KEYWORD, keywords);
				annotations.put(FIELD_DATASOURCE, ModelUtils.getDatasources(bpe));
				annotations.put(FIELD_ORGANISM, ModelUtils.getOrganisms(bpe));
				annotations.put(FIELD_PATHWAY, ModelUtils.getParentPathways(bpe));
				//- includes itself if bpe is a pathway

				//set <numparticipants> (PEs/Genes), <numprocesses> (interactions/pathways) index fields:
				if(bpe instanceof Process) {
					int numProc = fetcher.fetch(bpe, Process.class).size(); //except itself
					int numPeAndG = fetcher.fetch(bpe, PhysicalEntity.class).size()
						+ fetcher.fetch(bpe, Gene.class).size();
					annotations.put(FIELD_N_PARTICIPANTS, Integer.toString(numPeAndG));
					annotations.put(FIELD_N_PROCESSES, Integer.toString(numProc));
				} else if(bpe instanceof Complex) {
					int numPEs = fetcher.fetch(bpe, PhysicalEntity.class).size();
					annotations.put(FIELD_N_PARTICIPANTS, Integer.toString(numPEs));
				}

				//add IDs of uni. and rel. xrefs of this element and its children
				if(bpe instanceof Entity || bpe instanceof EntityReference)
				{
					final Set<String> ids = new HashSet<>();

					//fetch all children of (implicit) type XReferrable, which means - either
					//BioSource or ControlledVocabulary or Evidence or Provenance or Entity or EntityReference
					//(we actually want only the latter two types and their sub-types; will skip the rest later on):
					Set<XReferrable> children = fetcher.fetch(bpe, XReferrable.class);
					//include itself (- for fetcher only gets child elements)
					children.add((XReferrable) bpe);
					for(XReferrable child : children) {
						//skip IDs of Evidence,CV,Provenance (included into 'keyword' field anyway)
						if(!(child instanceof Entity || child instanceof EntityReference))
							continue;
						// collect standard bio IDs (skip publications); try/use id-mapping to associate more IDs:
						for(Xref x : child.getXref()) {
							if (!(x instanceof PublicationXref) && x.getId()!=null && x.getDb()!=null) {
								ids.add(x.getId());
							}
						}
					}
					if(!ids.isEmpty()) {
						annotations.put(SearchEngine.FIELD_XREFID, ids);
					}
				}

				index(bpe, indexWriter);

				//count, log a progress message
				int left = numLeft.decrementAndGet();
				if(left % 10000 == 0)
					LOG.info("index(), biopax objects left to index: " + left);
			});
		}

		exec.shutdown(); //stop accepting new tasks	
		try { //wait
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted!", e);
		}

		try {
			indexWriter.close(); //wait for pending op., auto-commit, close.
		} catch (IOException e) {
			throw new RuntimeException("Failed to close IndexWriter.", e);
		}

		//finally, create a new searcher manager
		initSearcherManager();
	}


	// internal methods

	/**
	 * Creates a new Lucene Document that corresponds to a BioPAX object.
	 * It does not check whether the document exists (should not be there,
	 * because the {@link #index()} method cleans up the index)
	 *
	 * Some fields also include biopax data type property values not only from 
	 * the biopax object but also from its child elements, up to some depth 
	 * (using key-value pairs in the pre-computed bpe.annotations map):
	 *
	 *  'uri' - biopax object's absolute URI, analyze=no, store=yes;
	 *
	 *  'name' - names, analyze=yes, store=yes; boosted;
	 *
	 *  'keyword' - infer from this bpe and its child objects' data properties,
	 *            such as Score.value, structureData, structureFormat, chemicalFormula, 
	 *            availability, term, comment, patoData, author, source, title, url, published, 
	 *            up to given depth/level; and also all 'pathway' field values are included here; 
	 *            analyze=yes, store=yes;
	 *
	 *  'datasource', 'organism' and 'pathway' - infer from this bpe and its child objects 
	 *  									  up to given depth/level, analyze=no, store=yes;
	 *
	 *  'size' - number of child processes, an integer as string; analyze=no, store=yes
	 *
	 * @param bpe BioPAX object
	 * @param indexWriter index writer
	 */
	void index(BioPAXElement bpe, IndexWriter indexWriter) {
		// create a new document
		final Document doc = new Document();

		// save URI (not indexed field)
		Field field = new StoredField(FIELD_URI, bpe.getUri());
		doc.add(field);

		// index and store but not analyze/tokenize the biopax class name:
		field = new StringField(FIELD_TYPE, bpe.getModelInterface().getSimpleName().toLowerCase(), Field.Store.YES);
		doc.add(field);

		// make index fields from the annotations map (of pre-calculated/inferred values)
		Map<String,Object> annotations = bpe.getAnnotations();
		if(!annotations.isEmpty()) {
			if(annotations.containsKey(FIELD_PATHWAY)) {
				addPathways((Set<Pathway>)annotations.get(FIELD_PATHWAY), doc);
			}
			if(annotations.containsKey(FIELD_ORGANISM)) {
				addOrganisms((Set<BioSource>)annotations.get(FIELD_ORGANISM), doc);
			}
			if(annotations.containsKey(FIELD_DATASOURCE)) {
				addDatasources((Set<Provenance>)annotations.get(FIELD_DATASOURCE), doc);
			}
			if(annotations.containsKey(FIELD_KEYWORD)) {
				addKeywords((Set<String>)annotations.get(FIELD_KEYWORD), doc);
			}
			if(annotations.containsKey(FIELD_N_PARTICIPANTS)) {
				field = new StoredField(FIELD_N_PARTICIPANTS,
					Integer.parseInt((String)annotations.get(FIELD_N_PARTICIPANTS)));
				doc.add(field);
			}
			if(annotations.containsKey(FIELD_N_PROCESSES)) {
				doc.add(new StoredField(FIELD_N_PROCESSES,
					Integer.parseInt((String)annotations.get(FIELD_N_PROCESSES))));
			}
			if(annotations.containsKey(FIELD_XREFID)) {
				//index biological IDs as keywords
				addKeywords((Set<String>)annotations.get(FIELD_XREFID), doc);

				//index all IDs using "xrefid" fields
				for (String id : (Set<String>)annotations.get(FIELD_XREFID)) {
					Field f = new StringField(FIELD_XREFID, id.toLowerCase(), Field.Store.NO);
					doc.add(f);
				}
			}
		}

		//cleanup the biopax object from temporary annotations (saves memory)
		annotations.remove(FIELD_KEYWORD);
		annotations.remove(FIELD_DATASOURCE);
		annotations.remove(FIELD_ORGANISM);
		annotations.remove(FIELD_PATHWAY);
		annotations.remove(FIELD_N_PARTICIPANTS);
		annotations.remove(FIELD_N_PROCESSES);
		annotations.remove(FIELD_XREFID);

		// name (we store both original and lowercased names due to use of StringField and KeywordAnalyser)
		if(bpe instanceof Named) {
			Named named = (Named) bpe;
			if(named.getStandardName() != null) {
				String stdName = named.getStandardName().trim();
				doc.add(new StringField(FIELD_NAME, stdName.toLowerCase(), Field.Store.NO));
				doc.add(new StringField(FIELD_NAME, stdName, Field.Store.NO));
			}
			if(named.getDisplayName() != null && !named.getDisplayName().equalsIgnoreCase(named.getStandardName())) {
				String dspName = named.getDisplayName().trim();
				doc.add(new StringField(FIELD_NAME, dspName.toLowerCase(), Field.Store.NO));
				doc.add(new StringField(FIELD_NAME, dspName, Field.Store.NO));
			}
			for(String name : named.getName()) {
				if(!name.equalsIgnoreCase(named.getDisplayName()) && !name.equalsIgnoreCase(named.getStandardName())) {
					name = name.trim();
					doc.add(new StringField(FIELD_NAME, name.toLowerCase(), Field.Store.NO));
					doc.add(new StringField(FIELD_NAME, name, Field.Store.NO));
				}
				doc.add(new TextField(FIELD_KEYWORD, name.toLowerCase(), Field.Store.NO));
			}
		}

		// write
		try {
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			throw new RuntimeException("Failed to index; " + bpe.getUri(), e);
		}
	}

	private void addKeywords(Set<String> keywords, Document doc) {
		for (String keyword : keywords) {
			Field f = new TextField(FIELD_KEYWORD, keyword.toLowerCase(), Field.Store.YES);
			doc.add(f);
		}
	}

	private void addDatasources(Set<Provenance> set, Document doc) {
		for (Provenance p : set) {
			// Index and store URI (untokinized) - 
			// required to accurately calculate no. entities or to filter by data source (diff. datasources may share same names)
			doc.add(new StringField(FIELD_DATASOURCE, p.getUri(), Field.Store.YES));
			// index names as well
			for (String s : p.getName())
				doc.add(new TextField(FIELD_DATASOURCE, s.toLowerCase(), Field.Store.NO));
		}
	}

	private void addOrganisms(Set<BioSource> set, Document doc) {
		for(BioSource bs : set) {
			// store URI as is (not indexed, untokinized)
			doc.add(new StoredField(FIELD_ORGANISM, bs.getUri()));

			// add organism names
			for(String s : bs.getName()) {
				doc.add(new TextField(FIELD_ORGANISM, s.toLowerCase(), Field.Store.NO));
			}
			// add taxonomy
			for(UnificationXref x : new ClassFilterSet<>(bs.getXref(), UnificationXref.class)) {
				if(x.getId() != null)
					doc.add(new TextField(FIELD_ORGANISM, x.getId().toLowerCase(), Field.Store.NO));
			}
			// include tissue type terms
			if (bs.getTissue() != null) {
				for (String s : bs.getTissue().getTerm())
					doc.add(new TextField(FIELD_ORGANISM, s.toLowerCase(), Field.Store.NO));
			}
			// include cell type terms
			if (bs.getCellType() != null) {
				for (String s : bs.getCellType().getTerm()) {
					doc.add(new TextField(FIELD_ORGANISM, s.toLowerCase(), Field.Store.NO));
				}
			}
		}
	}

	private void addPathways(Set<Pathway> set, Document doc) {
		for(Pathway pw : set) {
			//add URI as is (do not lowercase; do not index; store=yes - required to report hits, e.g., as xml)
			doc.add(new StoredField(FIELD_PATHWAY, pw.getUri()));

			// add names to the 'pathway' (don't store) and 'keywords' (store, don't index) fields
			for (String s : pw.getName()) {
				doc.add(new TextField(FIELD_PATHWAY, s.toLowerCase(), Field.Store.NO));
				doc.add(new StoredField(FIELD_KEYWORD, s.toLowerCase()));//for highlighting only, not indexed
			}

			// add unification xref IDs too
			for (UnificationXref x : new ClassFilterSet<Xref, UnificationXref>(
				pw.getXref(), UnificationXref.class)) {
				if (x.getId() != null) {
					// index in both 'pathway' (don't store) and 'keywords' (store, don't index)
					doc.add(new TextField(FIELD_PATHWAY, x.getId().toLowerCase(), Field.Store.NO));
					doc.add(new StoredField(FIELD_KEYWORD, x.getId().toLowerCase()));//for highlighting only, not indexed
				}
			}
		}
	}

	/*
	 * Creates a search filter like
	 * type AND (datasource OR datasource...)
	 *      AND (organism OR organism OR...)
	 *
	 * Both names (partial or full) and URIs should work as filter values.
	 */
	private Query createFilter(Class<? extends BioPAXElement> type, String[] datasources, String[] organisms)
	{
		BooleanQuery.Builder builder = new BooleanQuery.Builder();

		//AND datasources
		if (datasources != null && datasources.length > 0) {
			builder.add(subQuery(datasources, FIELD_DATASOURCE), Occur.MUST);
		}
		//AND organisms
		if (organisms != null && organisms.length > 0) {
			builder.add(subQuery(organisms, FIELD_ORGANISM), Occur.MUST);
		}
		//AND type
		if(type != null) { //add biopax class filter
			BooleanQuery.Builder query = new BooleanQuery.Builder().
				add(new TermQuery(new Term(FIELD_TYPE, type.getSimpleName().toLowerCase())), Occur.SHOULD);//OR
			//for each biopax subclass (interface), add the name to the filter query
			for(Class<? extends BioPAXElement> subType : SimpleEditorMap.L3.getKnownSubClassesOf(type)) {
				query.add(new TermQuery(new Term(FIELD_TYPE, subType.getSimpleName().toLowerCase())), Occur.SHOULD);//OR
			}
			builder.add(query.build(), Occur.MUST);
		}

		BooleanQuery filter = builder.build();
		//TODO: use LRUQueryCache with the filter somewhere, e.g.:
		// Query q = queryCache.doCache(filter, defaultCachingPolicy);

		if(!filter.clauses().isEmpty()) {
			return filter;
		} else
			return null;
	}


	/*
	 * Values are joint with OR, but if a value
	 * has whitespace symbols, it also makes a sub-query,
	 * in which terms are joint with AND. This is to filter
	 * by datasource/organism's full name, partial name, uri,
	 * using multiple datasources/organisms.
	 */
	private Query subQuery(String[] filterValues, String filterField)
	{
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		final Pattern pattern = Pattern.compile("\\s");
		for(String v : filterValues) {
			//if v has whitespace chars (several words), make a "word1 AND word2 AND..." subquery
			if(pattern.matcher(v).find()) {
				BooleanQuery.Builder bq = new BooleanQuery.Builder();
				try {
					//use the same analyser as when indexing
					TokenStream tokenStream = analyzer.tokenStream(filterField, new StringReader(v));
					CharTermAttribute chattr = tokenStream.addAttribute(CharTermAttribute.class);
					tokenStream.reset();
					while(tokenStream.incrementToken()) {
						//'of', 'and', 'for',.. never occur as tokens (this is how the analyzer works)
						String token = chattr.toString();
						bq.add(new TermQuery(new Term(filterField, token)), Occur.MUST);
					}
					tokenStream.end();
					tokenStream.close();
				} catch (IOException e) {
					//should never happen as we use StringReader
					throw new RuntimeException("Failed to open a token stream; "
						+ "field:" + filterField + ", value:" + v,e);
				}
				query.add(bq.build(), Occur.SHOULD);
			} else {
				query.add(new TermQuery(new Term(filterField, v.toLowerCase())), Occur.SHOULD);
			}
		}

		return query.build();
	}

}
