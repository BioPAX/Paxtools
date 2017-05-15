package org.biopax.paxtools.io.sbgn;

import java.util.*;

import org.ivis.layout.*;
import org.ivis.layout.sbgn.SbgnPDLayout;
import org.ivis.layout.sbgn.SbgnPDNode;
import org.ivis.layout.sbgn.SbgnProcessNode;
import org.sbgn.GlyphClazz;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.Bbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chilay COSE layout of an SBGN PD graph.
 *
 * @author: Istemi Bahceci, rodche (refactoring)
 */
class SBGNLayoutManager
{
    private static final Logger log = LoggerFactory.getLogger(SBGNLayoutManager.class);

    // Layout and root objects
    private Layout layout;
    private VCompound root;

    // mapping between view and layout level
    private Map <VNode, LNode> viewToLayout;
    private Map <String, VNode> layoutToView;
    private Map <Glyph,VNode>  glyphToVNode;
    private Map <String, Glyph> idToGLyph;
    private Map <String, Glyph> idToCompartmentGlyphs;
    private Map <String, Glyph> portIDToOwnerGlyph;
    private Map <String,Arc> idToArcs;

    /**
     * Applies CoSE layout to the given SBGN PD model.
     *
     * @param sbgn model where layout is performed and stored
     * @param doLayout whether to actually run the CoSE or just complete the SBGN model
     */
    public void createLayout(final Sbgn sbgn, boolean doLayout)
    {
        viewToLayout = new HashMap();
        glyphToVNode = new HashMap();
        idToGLyph = new HashMap();
        idToCompartmentGlyphs = new HashMap();
        portIDToOwnerGlyph = new HashMap();
        layoutToView = new HashMap();
        idToArcs = new HashMap<String, Arc>();

        this.layout = new SbgnPDLayout();
        final LGraphManager graphMgr = layout.getGraphManager();
        graphMgr.addRoot();

        root = new VCompound(new Glyph());

        // Detect compartment glyphs and put them in a hashmap;
        // also set compartment glyphs of members of complexes.
        for (Glyph g: sbgn.getMap().getGlyph())
        {
            if(g.getClazz().equals("compartment")) {
                idToCompartmentGlyphs.put(g.getId(), g);
            }
            //Set compartmentRef to all children of a Complex node.
            Glyph compartment = (Glyph)g.getCompartmentRef();
            if(compartment != null && g.getClazz().equals("complex")) {
                setCompartmentRefForComplexMembers(g, compartment, new HashSet<Glyph>());
            }
        }

        // Nest glyphs inside compartment glyphs according to their compartmentRef.
        // This list holds the glyphs that will be deleted after corresponding glyph
        // is added to child glyph of another glyph.
        if(!idToCompartmentGlyphs.isEmpty()) {
            List<Glyph> deletedList = new ArrayList<Glyph>();
            for (Glyph g : sbgn.getMap().getGlyph()) {
                Glyph containerCompartment = (Glyph) g.getCompartmentRef();
                if (containerCompartment != null) {
                    idToCompartmentGlyphs.get(containerCompartment.getId()).getGlyph().add(g);
                    deletedList.add(g);
                }
            }
            // Delete the duplicate glyphs, after they are moved to corresponding compartment glyph.
            for (Glyph g : deletedList) {
                sbgn.getMap().getGlyph().remove(g);
            }
        }

        // initialize the map for keeping ports and their owner glyphs
        // with entries like: <portID, ownerGlyph>
        initPortIdToGlyphMap(sbgn.getMap().getGlyph());

        //Remove ports from source and target field of ports
        //replace them with owner glyphs of these ports
        removePortsFromArcs(sbgn.getMap().getArc());

        // Assign logical operator and Process nodes to compartment
        assignProcessAndLogicOpNodesToCompartment(sbgn);

        // Create Vnodes for ChiLay layout component
        createVNodes(root, sbgn.getMap().getGlyph());

        for (VNode vNode: root.children) {
            createLNode(vNode, null);
        }

        // Create LEdges for ChiLay layout component
        createLEdges(sbgn.getMap().getArc());

        // Apply layout
        if(doLayout) {
            layout.runLayout();
        }

        graphMgr.updateBounds();

        // Here if any SbgnProcessNode node is returned from SBGNPD Layout
        // this means that we will have two additional port info. We should
        // add this information to libSbgn objects
        for (Object lNode : layout.getAllNodes()) {
            if (lNode instanceof SbgnProcessNode) {
                //Set geometry of corresponding node
                VNode vNode = layoutToView.get(((SbgnProcessNode) lNode).label);
                Bbox tempBbox = vNode.glyph.getBbox();
                tempBbox.setX((float) (((SbgnProcessNode) lNode).getLeft()));
                tempBbox.setY((float) (((SbgnProcessNode) lNode).getTop()));
                vNode.glyph.setBbox(tempBbox);

                //Created port objects in layout level
                SbgnPDNode inputLPort = ((SbgnProcessNode) lNode).getInputPort();
                SbgnPDNode outputLPort = ((SbgnProcessNode) lNode).getOutputPort();

                // New port objects
                Port inputPort = new Port();
                Port outputPort = new Port();

                // Set port attributes
                inputPort.setX((float) (inputLPort.getCenterX()));
                inputPort.setY((float) (inputLPort.getCenterY()));
                inputPort.setId(inputLPort.label);

                outputPort.setX((float) (outputLPort.getCenterX()));
                outputPort.setY((float) (outputLPort.getCenterY()));
                outputPort.setId((outputLPort.label));

                //Clear existing ports !
                vNode.glyph.getPort().clear();
                //Connect existing arcs to newly created ports
                //These ports are created by ChiLay and SBGNPD Layout
                connectArcToPort(inputLPort, inputPort);
                connectArcToPort(outputLPort, outputPort);

                //Add ports to the corresponding glyph
                vNode.glyph.getPort().add(inputPort);
                vNode.glyph.getPort().add(outputPort);
            }
        }

        // Update the bounds
        for (VNode vNode: root.children) {
            updateCompoundBounds(vNode.glyph, vNode.glyph.getGlyph());
        }

        // Clear inside of the compartmentGlyphs
        for (Glyph compGlyph: idToCompartmentGlyphs.values()) {
            //Again add the members of compartments
            for(Glyph memberGlyph: compGlyph.getGlyph() ) {
                sbgn.getMap().getGlyph().add(memberGlyph);
            }
            compGlyph.getGlyph().clear();
        }
    }

    /**
     * This method connects the existing arcs to the newly created
     * ports which are created by ChiLay and SBGNPD Layout.
     * @param lPort l level port object.
     * @param vPort v level port object.
     * */
    private void connectArcToPort(SbgnPDNode lPort, Port vPort)
    {
        //Iterate over the edges of l level port
        for  (Object e: (lPort.getEdges()))
        {
            //Ignore rigid edges
            if(((LEdge)e).type.equals("rigid edge"))
                continue;

            //Determine the if vPort is source or target
            Arc arc = idToArcs.get(((LEdge)e).label);
            if( lPort.label.equals(((LEdge)e).getSource().label ))
            {
                arc.setSource(vPort);
            }
            else if ( lPort.label.equals(((LEdge)e).getTarget().label ) )
            {
                arc.setTarget(vPort);
            }
        }
    }

    /**
     * This method finds process nodes and logical operator nodes in sbgn map and assigns them to a compartment by using majority rule.
     * @param sbgn Given Sbgn map.
     * */
    private void assignProcessAndLogicOpNodesToCompartment(Sbgn sbgn)
    {
        // Create a hashmap for keeping a node( generally logical operators and process nodes ) and its neighbours.
        // TreeMap value of the hash map keeps track of compartment nodes that includes neighbours of the node by String id and
        // Integer value holds the number of occurences of that compartment among the neighbours of the node as parent.
        HashMap <String, HashMap<String,Integer>>  nodetoNeighbours = new HashMap<String, HashMap<String,Integer>>();
        List<Glyph> glyphList = sbgn.getMap().getGlyph();
        List<Arc>   arcList   = sbgn.getMap().getArc();

        // Keeps track of process and logical operator nodes that will be assigned to a compartment.
        ArrayList<Glyph> targetNodes = new ArrayList<Glyph>();

        //Iterate over glyphs of sbgn map
        for(Glyph glyph: glyphList)
        {
            // Here logical operator nodes and process nodes are interested !
            String type = glyph.getClazz();
            if(type.equals(GlyphClazz.PROCESS) || type.equals(GlyphClazz.OMITTED_PROCESS)
                    || type.equals(GlyphClazz.UNCERTAIN_PROCESS) || type.equals(GlyphClazz.PHENOTYPE)
                    || type.equals(GlyphClazz.ASSOCIATION) || type.equals(GlyphClazz.DISSOCIATION)
                    || type.equals(GlyphClazz.AND) || type.equals(GlyphClazz.OR) || type.equals(GlyphClazz.NOT))
            {
                // Add a new value to hash map and also store the node as target node
                String processGlyphID = glyph.getId();
//                String rootID = "root"; //was not used anywhere
                nodetoNeighbours.put(processGlyphID, new HashMap<String, Integer>());
                targetNodes.add(glyph);

                // Iterate over arc list
                for(Arc arc: arcList)
                {
                    Glyph target = null;
                    Glyph source = null;

                    // If source and target of node is port find its owner glyph ! else just assign it.
                    if(arc.getSource() instanceof Port)
                        source = portIDToOwnerGlyph.get(((Port)arc.getSource()).getId());
                    else
                        source = (Glyph)arc.getSource();

                    if(arc.getTarget() instanceof Port)
                        target = portIDToOwnerGlyph.get(((Port)arc.getTarget()).getId());
                    else
                        target = (Glyph)arc.getTarget();

                    // If source of any arc is our node, then target must be neighbour of this node !
                    if(source.getId().equals(processGlyphID))
                    {
                        populateCompartmentOccurencesMap(target, nodetoNeighbours.get(processGlyphID));
                    }
                    // same as target part !!
                    else if(target.getId().equals(processGlyphID))
                    {
                        populateCompartmentOccurencesMap(source, nodetoNeighbours.get(processGlyphID));
                    }
                }
            }
        }

        //Finally assign nodes to compartments by majority rule
        for(Glyph glyph: targetNodes)
        {
            String id = glyph.getId();
            HashMap<String, Integer> compartmentsOfTargetNode = nodetoNeighbours.get(id);

            // Finally sort the hashmap to obtain the compartment that includes majority of the neighbours of the targetNode 
            List<Map.Entry<String,Integer>> entries = new LinkedList<Map.Entry<String,Integer>>(compartmentsOfTargetNode.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
                {
                    return -(o1.getValue().compareTo(o2.getValue()));
                }
            });

            if(entries.size() > 0)
            {
                // if the process belongs to root do not make any changes
                if(entries.get(0).getKey().equals("root"))
                    continue;

                Glyph compartment = idToCompartmentGlyphs.get(entries.get(0).getKey());
                if(compartment != null) {
                    //Set compartmentRef of process glyph also!
                    glyph.setCompartmentRef(compartment);
                    compartment.getGlyph().add(glyph);
                    //Remove it from sbgn also
                    sbgn.getMap().getGlyph().remove(glyph);
                }
            }
        }
    }

    /**
     * Updates a hashmap by incrementing the number of nodes in the compartment glyph that includes targetGlyph.
     * It is assumed that given hashmap includes compartment ids' as keys and number of nodes as values.This method 
     * is an utility method that will be used to populate a hashmap while determining the compartment node of a process
     * node by majority rule.
     *
     * @param targetGlyph glyph whose occurence will be updated in the given hashmap.
     * @param compartmentIDandOccurenceMap  Map that references number of nodes in a compartment by compartment ids .
     * */
    private void populateCompartmentOccurencesMap(Glyph targetGlyph,  HashMap<String, Integer> compartmentIDandOccurenceMap)
    {
        String rootID = "root";

        // if compartment ref of targetGlyph node is not null, increment its occurence by 1
        if(targetGlyph.getCompartmentRef() != null)
        {
            Glyph  containerCompartment = (Glyph)targetGlyph.getCompartmentRef();
            String compartmentID = containerCompartment.getId();
            Integer compartmentOccurrenceValue = compartmentIDandOccurenceMap.get(compartmentID);

            if( compartmentOccurrenceValue != null)
            {
                compartmentIDandOccurenceMap.put(compartmentID, compartmentOccurrenceValue + 1);
            }
            else
                compartmentIDandOccurenceMap.put(compartmentID, 1);
        }
        // else targetGlyph is in root graph so increment root graphs counter value by 1
        else
        {
            Integer compartmentOccurrenceValue = compartmentIDandOccurenceMap.get(rootID);

            if( compartmentOccurrenceValue != null)
            {
                compartmentIDandOccurenceMap.put(rootID, compartmentOccurrenceValue + 1);
            }
            else
                compartmentIDandOccurenceMap.put(rootID, 1);
        }
    }

    /**
     * Updates bounds of a compound node ( i.e. complex glyph ) from its children .
     * @param parent compound glyph.
     * @param childGlyphs related children of parent .
     * */
    private void updateCompoundBounds(Glyph parent,List<Glyph> childGlyphs)
    {
        float PAD = (float) 2.0;
        float minX = Float.MAX_VALUE; float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE; float maxY = Float.MIN_VALUE;

        for (Glyph tmpGlyph:childGlyphs)
        {
            if(!tmpGlyph.getClazz().equals("unit of information") && !tmpGlyph.getClazz().equals("state variable") )
            {
                if(tmpGlyph.getGlyph().size() > 0)
                    updateCompoundBounds(tmpGlyph, tmpGlyph.getGlyph());

                float w = tmpGlyph.getBbox().getW();
                float h = tmpGlyph.getBbox().getH();

                // Verify MIN and MAX x/y again:
                minX = Math.min(minX, (tmpGlyph.getBbox().getX()));
                minY = Math.min(minY, (tmpGlyph.getBbox().getY()));
                maxX = Math.max(maxX, (tmpGlyph.getBbox().getX())+w);
                maxY = Math.max(maxY, (tmpGlyph.getBbox().getY())+h);

                if (minX == Float.MAX_VALUE) minX = 0;
                if (minY == Float.MAX_VALUE) minY = 0;
                if (maxX == Float.MIN_VALUE) maxX = 0;
                if (maxY == Float.MIN_VALUE) maxY = 0;

                parent.getBbox().setX(minX - PAD);
                parent.getBbox().setY(minY - PAD);
                parent.getBbox().setW(maxX -  parent.getBbox().getX() + PAD);
                parent.getBbox().setH(maxY -  parent.getBbox().getY() + PAD);
            }
        }
    }

    /**
     * Recursively creates VNodes from Glyphs of Sbgn.
     *
     * @param parent Parent of the glyphs that are passed as second arguement.
     * @param glyphs Glyphs that are child of parent which is passed as first arguement.
     *
     * */
    private void createVNodes(VCompound parent,List<Glyph> glyphs)
    {
        for(Glyph glyph: glyphs )
        {
            if (!glyph.getClazz().equals("state variable") && !glyph.getClazz().equals("unit of information")  )
            {
                if(glyph.getClazz().equals("process"))
                {
                    VCompound v = new VCompound(glyph);
                }

                if(!isChildless(glyph))
                {
                    VCompound v = new VCompound(glyph);
                    idToGLyph.put(glyph.getId(), glyph);
                    glyphToVNode.put(glyph, v);
                    parent.children.add(v);
                    createVNodes(v, glyph.getGlyph());
                }

                else
                {
                    VNode v = new VNode(glyph);
                    idToGLyph.put(glyph.getId(), glyph);
                    glyphToVNode.put(glyph, v);
                    parent.children.add(v);
                }
            }
        }
    }

    /**
     * Creates LNodes from Arcs of Sbgn and adds it to the passed layout object.
     *
     * @param arcs List of arc objects from which the LEdges will be constructed for ChiLay Layout component.
     *
     * */
    private void createLEdges(List<Arc> arcs)
    {
        for(Arc arc: arcs )
        {
            LEdge lEdge = layout.newEdge(null);
            lEdge.type = arc.getClazz();
            lEdge.label = arc.getId();
            LNode sourceLNode = viewToLayout.get(glyphToVNode.get(arc.getSource()));
            LNode targetLNode = viewToLayout.get(glyphToVNode.get(arc.getTarget()));
            idToArcs.put(arc.getId(), arc);

            // Add edge to the layout
            layout.getGraphManager().add(lEdge, sourceLNode, targetLNode);
        }
    }

    /**
     * Helper function for creating LNode objects from VNode objects and adds them to the given layout.
     *
     * @param vNode  VNode object from which a corresponding LNode object will be created.
     * @param parent parent of vNode, if not null vNode will be added to layout as child node.
     * */

    private void createLNode(VNode vNode, VNode parent)
    {
        LNode lNode = layout.newNode(vNode);
        lNode.type  = vNode.glyph.getClazz();
        lNode.label = vNode.glyph.getId();
        LGraph rootLGraph = layout.getGraphManager().getRoot();

        //Add corresponding nodes to corresponding maps
        viewToLayout.put(vNode, lNode);
        layoutToView.put(lNode.label,vNode);

        // if the vNode has a parent, add the lNode as a child of the parent l-node.
        // otherwise, add the node to the root graph.
        if (parent != null)
        {
            LNode parentLNode = viewToLayout.get(parent);
            parentLNode.getChild().add(lNode);
        }

        else
        {
            rootLGraph.add(lNode);
        }

        lNode.setLocation(vNode.glyph.getBbox().getX(), vNode.glyph.getBbox().getY());

        if (vNode instanceof VCompound)
        {
            VCompound vCompound = (VCompound) vNode;
            // add new LGraph to the graph manager for the compound node
            layout.getGraphManager().add(layout.newGraph(null), lNode);
            // for each VNode in the node set create an LNode
            for (VNode vChildNode: vCompound.getChildren())
            {
                createLNode(vChildNode, vCompound);
            }
        }

        else
        {
            lNode.setWidth(vNode.glyph.getBbox().getW());
            lNode.setHeight(vNode.glyph.getBbox().getH());
        }
    }

    /**
     * Recursively sets compartmentRef fields of members of a complex glyph
     * to the same value as complex's compartment.
     *
     * @param glyph target glyph where compartmentRef(compartment parameter) will be set.
     * @param compartment compartmentRef value that will be set.
     * @param visited (initially an empty set of) previously processed glyphs, to escape infinite loops
     * */
    private void setCompartmentRefForComplexMembers(final Glyph glyph, final Glyph compartment,
                                                    final Set<Glyph> visited)
    {
        if(!visited.add(glyph))
            return;

        glyph.setCompartmentRef(compartment);
        if(glyph.getGlyph().size() > 0) {
            for(Glyph g: glyph.getGlyph() ) {
                setCompartmentRefForComplexMembers(g, compartment, visited);
            }
        }
    }

    /**
     * This method replaces ports of arc objects with their owners.
     * @param arcs Arc list of SBGN model
     * */
    private void removePortsFromArcs(List<Arc> arcs)
    {
        for(Arc arc: arcs )
        {
            // If source is port, first clear port indicators else retrieve it from hashmaps
            if (arc.getSource() instanceof Port )
            {
                Glyph source = portIDToOwnerGlyph.get(((Port)arc.getSource()).getId());
                arc.setSource(source);
            }

            // If target is port, first clear port indicators else retrieve it from hashmaps
            if (arc.getTarget() instanceof Port)
            {
                Glyph target = portIDToOwnerGlyph.get(((Port)arc.getTarget()).getId());
                arc.setTarget(target);
            }
        }
    }

    /**
     * This method initializes map for glyphs and their respective ports.
     * @param glyphs Glyph list of SBGN model
     * */
    private void initPortIdToGlyphMap(List<Glyph> glyphs)
    {
        for(Glyph glyph: glyphs)
        {
            for(Port p: glyph.getPort())
            {
                portIDToOwnerGlyph.put(p.getId(), glyph );
            }
            if(glyph.getGlyph().size() > 0)
                initPortIdToGlyphMap(glyph.getGlyph());
        }
    }

    /**
     * Returns true if a glyph includes child glyphs (state and info glyphs are out of count!)
     * @param targetGlyph target glyph that will be queried.
     * @return true/false
     * */
    private boolean isChildless(Glyph targetGlyph)
    {
        boolean checker = true;
        for(Glyph glyph: targetGlyph.getGlyph() )
        {
            if ( !glyph.getClazz().equals("state variable") && !glyph.getClazz().equals("unit of information")  )
            {
                checker = false;
                break;
            }
        }
        return checker;
    }
}
