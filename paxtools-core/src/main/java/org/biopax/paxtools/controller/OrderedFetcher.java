package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.BioPaxIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**

 */
public class OrderedFetcher {
    SimpleEditorMap editorMap = SimpleEditorMap.get(BioPAXLevel.L3);
    List<Layer> layers = new ArrayList<Layer>();
    Layer attributeLayer;
    boolean fetchAttributes;


    public OrderedFetcher(boolean fetchAttributes)
    {
        this.fetchAttributes = fetchAttributes;
        String line;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(OrderedFetcher.class.getResourceAsStream("L3Editor.properties.fetchOrder")));
        try {
            while ((line = reader.readLine()) != null) {
                layers.add(new Layer(line));
            }
        } catch (IOException e)
        {
            throw new BioPaxIOException(e);

        }
        if (fetchAttributes)
            attributeLayer = new AttributeLayer();
    }

    public Set<BioPAXElement> fetch(Set<? extends BioPAXElement> elements) {
        HashSet<BioPAXElement> values = new HashSet<BioPAXElement>(elements);
        for (Layer layer : layers) {
            layer.fetch(values);
        }
        if (fetchAttributes)
            attributeLayer.fetchOnce(values);
        return values;
        //TODO handle non-object fields
    }

    private class Layer {
        private boolean cyclic;
        public List<PropertyEditor> editors = new ArrayList<PropertyEditor>();

        public Layer(String line) {
            StringTokenizer st = new StringTokenizer(line);
            String start = st.nextToken();
            this.cyclic = start.endsWith("*");
            while (st.hasMoreElements()) {

                String editorString = st.nextToken();
                String[] split = editorString.split("\\.");
                String domain = split[0];
                String property = split[1];
                addEditor(domain, property);
            }
        }

        private Layer() {
        }

        private void addEditor(String domain, String property) {
            Class<? extends BioPAXElement> domainClass = BioPAXLevel.L3.getInterfaceForName(domain);
            editors.addAll(editorMap.getSubclassEditorsForProperty(property, domainClass));
        }

        public void fetch(Set<BioPAXElement> elements) {
            if (!cyclic) {
                elements.addAll(this.fetchOnce(elements));
            } else {
                Set<BioPAXElement> newBpes = new HashSet<BioPAXElement>(elements);
                boolean exhausted = false;

                while (!exhausted) {
                    newBpes = this.fetchOnce(newBpes);
                    exhausted = elements.containsAll(newBpes);
                    if (!exhausted) elements.addAll(newBpes);
                }
            }
        }

        public Set<BioPAXElement> fetchOnce(Set<? extends BioPAXElement> elements) {
            Set<BioPAXElement> newElements = new HashSet<BioPAXElement>();
            for (PropertyEditor editor : editors) {
                newElements.addAll(getValuesFromBeans(elements, editor));
            }
            return newElements;
        }

    }

    protected Set getValuesFromBeans(
            Set<? extends BioPAXElement> elements,
            PropertyEditor editor) {
        return editor.getValueFromBeans(elements);
    }

    private class AttributeLayer extends Layer {
        public AttributeLayer() {
            SimpleEditorMap editorMap = SimpleEditorMap.get(BioPAXLevel.L3);
            HashMap<String,PropertyEditor> tempmap = new HashMap<String, PropertyEditor>();
            Iterator<PropertyEditor> iter = editorMap.iterator();
            while (iter.hasNext()) {
                PropertyEditor next = iter.next();
                if (!(next instanceof ObjectPropertyEditor))
                {
                    if(!tempmap.containsKey(next.getProperty()))
                    {
                        tempmap.put(next.getProperty(),next);
                        this.editors.add(next);
                    }
                    
                }
            }
        }

        public Set<BioPAXElement> fetchOnce(Set<? extends BioPAXElement> elements) {
            for (PropertyEditor editor : editors)
            {
                    getValuesFromBeans(elements, editor);
            }
            return null;
        }

        @Override
        public void fetch(Set<BioPAXElement> elements) {
            throw new UnsupportedOperationException();
        }
    }


}

