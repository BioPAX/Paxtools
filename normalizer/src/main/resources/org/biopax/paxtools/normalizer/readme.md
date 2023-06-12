# XRegistry resources

## registry.json
Extract the prefix, regexp, name, synonyms, etc.
bioregistry.io/chebi:12345

## spellmap.json
An additional mapping from identifier db/type name spelling variants
(non-alphanumeric chars are removed) to the corresponding Prefix.
All the keys and values should be upper case. In addition to the predefined map entries,
each registry prefix, name, synonym (removing non-alphanumeric chars) will be also auto-added.

## synonymap.json
An additional mapping from names/synonyms to Prefixes.
