# udmlparser
##An Integration story

I cannot help it... I like the idea of sharing information across applications, more so if we have the chance to expose a wealth of intelligence, semantics and metadata.

##The Parser story

Ok, so... sometimes, getting the UDML version of the repository won't be enough... Let's say we need to rapidly share OBIEE metadata with another tool, e.g. a data dictionary... This is a real world scenario I found in many projects.
So this is my kind of business case... a tool extracting metadata to some independent format (this time, a long-standing W3C recommendation), XML.


###So, it's just:
a parser utility, generating XML structures out of OBIEE UDML repository statements.

Oh... and now (March 2013) you can also "reverse-engineer" your business models, generating Bus Matrices!
