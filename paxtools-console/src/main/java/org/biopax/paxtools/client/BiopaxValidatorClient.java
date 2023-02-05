package org.biopax.paxtools.client;

/*
 * #%L
 * BioPAX Validator Client
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.biopax.validator.jaxb.Behavior;
import org.biopax.validator.jaxb.ValidatorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Simple (example) BioPAX Validator client 
 * to upload and check BioPAX OWL files.
 * 
 * @author rodche
 *
 */
public class BiopaxValidatorClient {
	private static final Logger log = LoggerFactory.getLogger(BiopaxValidatorClient.class);
	
	/**
	 * Default BioPAX Validator's URL
	 */
	public static final String 
		DEFAULT_VALIDATOR_URL = "http://www.biopax.org/validator/check.html";
	
	/**
	 * The Java Option to set a BioPAX Validator URL
	 * (if set, overrides the default and URL provided by the Constructor arg.)
	 */
	public static final String JVM_PROPERTY_URL = "biopax.validator.url";
	
	public static enum RetFormat {
		HTML,// errors as HTML/Javascript 
		XML, // errors as XML
		OWL; // modified BioPAX only (when 'autofix' or 'normalize' is true)
	}
	
	private String url;

	
    /**
     * Main Constructor
     * 
     * It configures for the validator's URL
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * and result format ().
     * 
     * @param url - validator's file-upload form address
     */
    public BiopaxValidatorClient(String url) {
//		if(url == null || url.isEmpty())
//			this.url = System.getProperty(JVM_PROPERTY_URL, DEFAULT_VALIDATOR_URL);
//		else 
//			this.url = url;
//		
		// 1) use the arg (if not empty/null) or the default URL
    	this.url = (url == null || url.isEmpty())
			? DEFAULT_VALIDATOR_URL : url;
    	
    	// 2) override if the JVM option is set to another value
    	this.url = System.getProperty(JVM_PROPERTY_URL, this.url);
		
    	// 3) get actual location (force through redirects, if any)   	
		try {
			this.url = location(this.url);
//			System.out.println("Location: " + this.url);
		} catch (IOException e) {
			log.warn("Failed to resolve to actual web service " +
				"URL using: " + url + " (if there is a 301/302/307 HTTP redirect, " +
					"then validation requests (using HTTP POST method) will probably fail...)", e);
		}
	}
    
    
    /**
     * Default Constructor
     * 
     * It configures for the default validator URL.
     */
    public BiopaxValidatorClient() {
    	this(null);
	}
       

    /**
     * Checks a BioPAX OWL file(s) or resource 
     * using the online BioPAX Validator 
     * and prints the results to the output stream.
     * 
     * @param autofix true/false (experimental)
     * @param profile validation profile name
     * @param retFormat xml, html, or owl (no errors, just modified owl, if autofix=true)
	 * @param filterBy filter validation issues by the error/warning level
	 * @param maxErrs errors threshold - max no. critical errors to collect before quitting
	 *                (warnings not counted; null/0/negative value means unlimited)
     * @param biopaxUrl check the BioPAX at the URL
     * @param biopaxFiles an array of BioPAX files to validate
     * @param out validation report data output stream
     * @throws IOException when there is an I/O error
     */
    public void validate(boolean autofix, String profile, RetFormat retFormat, Behavior filterBy,
    		Integer maxErrs, String biopaxUrl, File[] biopaxFiles, OutputStream out) throws IOException 
    {
    	MultipartEntityBuilder meb = MultipartEntityBuilder.create();    	
    	meb.setCharset(Charset.forName("UTF-8"));
    	
    	if(autofix)
    		meb.addTextBody("autofix", "true");
//TODO: add options (normalizer.fixDisplayName, normalizer.inferPropertyOrganism, normalizer.inferPropertyDataSource, normalizer.xmlBase)
    	if(profile != null && !profile.isEmpty())
    		meb.addTextBody("profile", profile);
    	if(retFormat != null)
    		meb.addTextBody("retDesired", retFormat.toString().toLowerCase());
    	if(filterBy != null)
    		meb.addTextBody("filter", filterBy.toString());
    	if(maxErrs != null && maxErrs > 0)
    		meb.addTextBody("maxErrors", maxErrs.toString());
    	if(biopaxFiles != null && biopaxFiles.length > 0)
    		for (File f : biopaxFiles) //important: use MULTIPART_FORM_DATA content-type
    			meb.addBinaryBody("file", f, ContentType.MULTIPART_FORM_DATA, f.getName());
    	else if(biopaxUrl != null) {
    		meb.addTextBody("url", biopaxUrl);
    	} else {
    		log.error("Nothing to do (no BioPAX data specified)!");
        	return;
    	}
    	
    	//execute the query and get results as string
    	HttpEntity httpEntity = meb.build();
    	String content = Executor.newInstance()//Executor.newInstance(httpClient)
    			.execute(Request.Post(url).body(httpEntity))
    				.returnContent().asString();  	

    	//save: append to the output stream (file)
		BufferedReader res = new BufferedReader(new StringReader(content));
		String line;
		PrintWriter writer = new PrintWriter(out);
		while((line = res.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		res.close();
    }
    
    public void setUrl(String url) {
		this.url = url;
	}
    
    public String getUrl() {
		return url;
	}
    
    /**
     * Converts a biopax-validator XML response to the java object.
     *
     * @param xml input XML data - validation report - to import
     * @return validation report object
     * @throws JAXBException when there is an JAXB unmarshalling error
     */
    public static ValidatorResponse unmarshal(String xml) throws JAXBException {
    	JAXBContext jaxbContext = JAXBContext.newInstance("org.biopax.validator.jaxb");
		Unmarshaller un = jaxbContext.createUnmarshaller();
		Source src = new StreamSource(new StringReader(xml));
		ValidatorResponse resp = un.unmarshal(src, ValidatorResponse.class).getValue();
		return resp;
    }
 
    
    private String location(final String url) throws IOException {
        String location = url; //initially the same
    	// discover actual location, avoid going in circles:
        int i=0;
        for(String loc = url; loc != null && i<5; i++ ) 
        { 	
        	//do POST for location (Location header present if there's a 301/302/307 redirect on the way)
        	loc = Request.Post(loc).execute()
        			.handleResponse(new ResponseHandler<String>() {
						@Override
						public String handleResponse(HttpResponse httpResponse)
								throws ClientProtocolException, IOException {
							Header header = httpResponse.getLastHeader("Location");
							return (header != null) ? header.getValue().trim() : null;
						}
			});
        	 
        	if(loc != null) {
        		location = loc;
        		log.info("BioPAX Validator location: " + loc);
        	}
    	}
        
        return location;
    }
    
}