package org.biopax.paxtools.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class provides a mapping between HGNC IDs and Symbols.
 *
 * If JVM option paxtools.core.use-latest-genenames=ture, then
 * it downloads the custom mapping data from genenames.org;
 * otherwise will use previously downloaded built-in mapping file.
 *
 * @author Ozgun Babur, Igor Rodchenkov
 */
public class HGNC {
	private static final Logger LOG = LoggerFactory.getLogger(HGNC.class);
	private static Map<String, String> sym2id = new HashMap<>();
	private static Map<String, String> id2sym = new HashMap<>();
	private static Map<String, String> old2new = new HashMap<>();
	private static Map<String, String> sym2eg = new HashMap<>();
	private static Map<String, String> eg2sym = new HashMap<>();

	//data columns there are (TSV): "HGNC ID", "Approved symbol", "Previous symbols", "NCBI Gene ID"
	//The 3rd or 4th column values can be blank (use a special line splitting, not ignoring \t\t and values after the last tab)
	public static final String GENENAMES_CUST_EXPORT_URL = "https://www.genenames.org/cgi-bin/download/custom" +
			"?col=gd_hgnc_id&col=gd_app_sym&col=gd_prev_sym&col=gd_pub_eg_id&status=Approved&hgnc_dbtag=on" +
			"&order_by=gd_app_sym_sort&format=text&submit=submit";

	/**
	 * Find HGNC Symbol by HGNC id or name (do not use NCBI/Entrez gene id as the input to avoid mistakes).
	 * @param idOrSymbol HGNC ID (with or without "HGNC:" banana) or gene symbol (e.g. previous version)
	 * @return primary HGNC gene symbol (if it's already the one, return the same)
	 */
	public static String getSymbolByHgncIdOrSym(String idOrSymbol)
	{
		if(StringUtils.isBlank(idOrSymbol)) {
			return null;
		}

		if(StringUtils.isNumeric(idOrSymbol)) {
			idOrSymbol = "HGNC:" + idOrSymbol;
		}
		if (id2sym.containsKey(idOrSymbol.toUpperCase())) //HGNC ids are uppercase, e.g. HGNC:1234
			return id2sym.get(idOrSymbol.toUpperCase());
		else if (sym2id.containsKey(idOrSymbol))
			return idOrSymbol;
		else if (old2new.containsKey(idOrSymbol))
			return old2new.get(idOrSymbol);
		//it's neither a known id nor old/new symbol (case-sensitive)
		else if (!idOrSymbol.toUpperCase().equals(idOrSymbol)) //try ignoring case
			return getSymbolByHgncIdOrSym(idOrSymbol.toUpperCase()); //TODO: why...
		else
			return null;
	}

	public static String getHgncId(String symbol)
	{
		String s = getSymbolByHgncIdOrSym(symbol);
		return (s != null) ? sym2id.get(s) : null;
	}

	/**
	 * Provides NCBI (Entrez) Gene ID of the given gene symbol.
	 * @param symbol gene symbol
	 * @return EG ID
	 */
	public static String getGeneId(String symbol)
	{
		String s = getSymbolByHgncIdOrSym(symbol);
		return (s != null) ? sym2eg.get(s) : null;
	}

	public static String getSymbolByGeneId(String id)
	{
		return eg2sym.get(id);
	}

	public static boolean containsGeneId(String id)
	{
		return eg2sym.containsKey(id);
	}

	static
	{
		Scanner reader = null;
		try {
			if(Boolean.getBoolean("paxtools.core.use-latest-genenames")) {
				try {
					URL genenamesUrl = new URL(GENENAMES_CUST_EXPORT_URL);
					reader = new Scanner(genenamesUrl.openStream());
					LOG.info("Loading the latest mapping from: " + genenamesUrl);
				} catch (IOException e) {
					reader = null;
					LOG.error("Failed getting the latest mapping from genenames.org; " + e);
				}
			}
			if(reader == null) {
				LOG.info("Loading the built-in genenames.txt mapping file");
				reader = new Scanner(HGNC.class.getResourceAsStream("genenames.txt"));
			}
			reader.useDelimiter("/t");
			String line = reader.nextLine();
			LOG.debug("Skipped genenames title line: " + line);

			while (reader.hasNextLine()) {
				line = reader.nextLine();
				String[] token = line.split("\t", -1); //preserve all tokens, e.g. empy as in "\t\t" and trailing
				String id = token[0].trim(); //hgnc id
				String sym = token[1].trim(); // hgnc symbol
				sym2id.put(sym, id);
				String ret = id2sym.put(id, sym);
				if(ret != null) {
					LOG.warn("Ambiguous mapping; {} maps to hgnc: {}, {}", sym, ret, id);
				}
				final String olds = token[2].trim(); //prev. symbols
				for (String old : olds.split(",")) {
					old = old.trim();
					old2new.put(old, sym);
				}
				String eg = token[3].trim();
				if(StringUtils.isNotBlank(eg)) {
					sym2eg.put(sym, eg);
					ret = eg2sym.put(eg, sym);
					if (ret != null) {
						LOG.warn("Ambiguous mapping; {} maps to ncbigene: {}, {}", sym, ret, eg);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to init gene id/symbol mappings", e);
		} finally {
			if(reader != null) {
				try {reader.close();}catch(Exception e){} //close quietly
			}
		}
	}
}
