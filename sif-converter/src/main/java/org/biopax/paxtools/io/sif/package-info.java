/**
 * This package contains methods and algorithms for reducing BioPAX to Simple Interaction Format.
 *
 * Simple Interaction Format (SIF) was originally created for use with Cytoscape,
 * the open source bioinformatics software platform for visualizing molecular interaction networks. SIF is simple to
 * parse, and easy to load into Cytoscape and other third-party applications.
 *
 * Relations in a SIF file are formatted as:
 *
 * source relationship type target
 *
 * where source and target are a valid primary id and relationship type is one of the interaction inference rules.
 *
 * As its name suggests SIF, is a substantially simpler format compared to BioPAX. A BioPAX formatted network is
 * capable of storing rich biological semantics, including multi-participant relationships,
 * states and locations of entities, complexes and complex control relationships. A SIF network, on the other hand,
 * is only capable of storing pairwise interactions between entity references.
 */
package org.biopax.paxtools.io.sif;

