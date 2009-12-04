package org.biopax.paxtools.io.simpleIO;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Named;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides output in OWL format for BioPAX model(s),
 * does not depend on {@link com.hp.hpl.jena}.
 *
 * @author Emek Demir
 */
public class SimpleExporter
{
	private static final Log log = LogFactory.getLog(SimpleExporter.class);
	
    private EditorMap editorMap;
    private static final String owlNS = "http://www.w3.org/2002/07/owl#";
    private static final String xsdNS = "http://www.w3.org/2001/XMLSchema#";
    private static final String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDF_ID = "rdf:ID=\"";
    private static final String RDF_about = "rdf:about=\"";
    private static final String close = "\">";

    private String base;
    private String bp;
    private Map<String, String> nsMap;

	/**
     *
     * @param level BioPAX level in which the output will be
     */
    public SimpleExporter(BioPAXLevel level)
    {
        editorMap = new SimpleEditorMap(level);
    }

    /**
     * Converts a model into OWL format, and writes it into
     * the outputStream.
     *
     * @param model model to be converted into OWL format
     * @param outputStream output stream into which the output will be written
     * @throws IOException in case of I/O problems
     * @throws IllegalAccessException in case of problem related to access
     * @throws InvocationTargetException in case of problems related to invoke methods
     */
    public void convertToOWL(Model model, OutputStream outputStream)
            throws IOException {

        initialize(model);

        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream));

        writeHeader(out);

        writeObjects(out, model);

        out.write("\n</rdf:RDF>");
        out.close();
    }

    public void writeObject(Writer out, BioPAXElement bean) throws IOException {
		String name = bp + ":" + bean.getModelInterface().getSimpleName();
		writeIDLine(out, bean, name);
		
		Set<PropertyEditor> editors = editorMap.getEditorsOf(bean);
		if(editors==null || editors.isEmpty()) {
			log.warn("no editors for " + bean.getRDFId() + " | " 
					+ bean.getModelInterface().getSimpleName());
			out.write("\n</" + name + ">");
			return;
		}
		
		for (PropertyEditor editor : editors) {
			Object value = editor.getValueFromBean(bean);
			if (value != null && !editor.isUnknown(value))
			{
				if (editor.isMultipleCardinality()) {
					for (Object valueElement : ((Set) value)) {
						writeStatementFor(bean, editor, valueElement, out);
					}
				} else {
					writeStatementFor(bean, editor, value, out);
				}
			}
		}
		
		out.write("\n</" + name + ">");
	}
    
    private void writeObjects(Writer out, Model model) throws IOException
    {
        Set<BioPAXElement> bioPAXElements = model.getObjects();
        for (BioPAXElement bean : bioPAXElements)
        {
            writeObject(out, bean);
        }
    }

    private void writeStatementFor(BioPAXElement bean, PropertyEditor editor,
                                   Object value, Writer out)
            throws IOException
    {
        assert (bean != null && editor != null);
        
        //fix (for L3 only): skip 'name' if it's present in the displayName, etc..
        if(editor.getProperty().equalsIgnoreCase("name") 
        		&& bean instanceof Named) { // the latter maybe not necessary...
        	Named named = (Named) bean;
        	if(value != null && 
        		(value.equals(named.getDisplayName())
        				|| value.equals(named.getStandardName()))) {
        		return;
        	}
        }
        
        String prop = bp + ":" + editor.getProperty();
        out.write("\n <" + prop);

        if (value instanceof BioPAXElement)
        {
            String id = ((BioPAXElement) value).getRDFId();
            assert id!=null;
            if (base!=null && id.startsWith(base))
            {
                id = id.substring(id.lastIndexOf('#'));
            }
            out.write(" rdf:resource=\"" + id + "\" />");
        }
        else
        {
            String type = findLiteralType(editor);
            String valString = StringEscapeUtils.escapeXml(value.toString());
            out.write(" rdf:datatype = \"xsd:" + type + "\">" + valString +
                      "</" + prop + ">");
        }
    }

    private String findLiteralType(PropertyEditor editor)
    {
        Class range = editor.getRange();
        String type = null;
        if (range.isEnum() || range.equals(String.class))
        {
            type = "string";
        }
        else if (range.equals(double.class))
        {
            type = "double";
        }
        else if (range.equals(int.class))
        {
            type = "int";
        }
        else if (range.equals(float.class))
        {
            type = "float";
        }
        return type;
    }


    private void writeIDLine(Writer out, BioPAXElement bpe, String name)
            throws IOException
    {

        out.write("\n\n<" + name +
                  " ");
        String s = bpe.getRDFId();
        int index = s.lastIndexOf('#') + 1;

        if (s.substring(0, index).equals(base))
        {
            String id = s.substring(index);
            out.write(RDF_ID + id + close);
        }
        else
        {
            out.write(RDF_about + s + close);
        }


    }

    private void initialize(Model model)
    {
        base = null;
        bp = null;
        nsMap = model.getNameSpacePrefixMap();

        String owlPre = null;
        String rdfPre = null;
        String xsdPre = null;

	    Map<String, String> reverseMap = new HashMap<String, String>();
        for (String pre : nsMap.keySet())
        {

            String ns = nsMap.get(pre);
            if (ns.equalsIgnoreCase(
                    rdfNS))

            {
                rdfPre = pre;
            }
            else if (ns.equalsIgnoreCase(
                    owlNS))

            {
                owlPre = pre;
            }
            else if (ns.equalsIgnoreCase(
                    xsdNS))

            {
                xsdPre = pre;
            }

            reverseMap.put(ns, pre);
        }
        if (owlPre != null)
        {
            reverseMap.remove(nsMap.get(owlPre));
            nsMap.remove(owlPre);
        }
        if (rdfPre != null)
        {
            reverseMap.remove(nsMap.get(rdfPre));
            nsMap.remove(rdfPre);
        }

        if (xsdPre != null)
        {
            reverseMap.remove(nsMap.get(xsdPre));
            nsMap.remove(xsdPre);
        }

        nsMap.put("rdf", rdfNS);
        nsMap.put("owl", owlNS);
        nsMap.put("xsd", xsdNS);

    }

    private void writeHeader(Writer out)
            throws IOException
    {
        //Header
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.write("\n<rdf:RDF");
        String bpns = this.editorMap.getLevel().getNameSpace();
        for (String pre : nsMap.keySet())
        {
            String ns = nsMap.get(pre);
            if (pre.equals(""))
            {
                base = ns;
            }
            else
            {
                if (ns.equalsIgnoreCase(bpns))
                {
                    bp = pre;
                }
	            else if(pre.equals("bp"))
                {
	                pre = "oldbp";
                }
                pre = ":" + pre;

            }
            out.write("\n xmlns" + pre + "=\"" + ns + "\"");
        }
        if (bp == null)
        {
            bp = "bp";
            out.write("\n xmlns:bp" + "=\"" + bpns + "\"");
        }
        if (base != null)
        {
            out.write("\n xml:base=\"" + base + "\"");
        }

        out.write(">");
        out.write("\n<owl:Ontology rdf:about=\"\">");
        out.write(
                "\n <owl:imports rdf:resource=\""+bpns+"\" />");
        out.write("\n</owl:Ontology>");
    }

}
