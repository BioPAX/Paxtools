# XRegistry resources

## registry.json
The Consensus Registry JSON file downloaded from https://bioregistry.io/download,   
https://raw.githubusercontent.com/biopragmatics/bioregistry/main/exports/registry/registry.json 
Contains the metadata about the collections of bio identifiers, such as the prefix, regexp, name, synonyms, etc.

## spellmap.json
An additional mapping from identifier db/type name spelling variants
(non-alphanumeric chars are removed) to the corresponding Prefix.
All the keys and values should be upper case. In addition to the predefined map entries,
each prefix, name, synonym (removing non-alphanumeric chars) in the registry  
will be also auto-added to this map at run time.

## synonymap.json
An additional mapping from names/synonyms to the Bioregistry Prefixes.
