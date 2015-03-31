# Introduction #

I like the idea of applications sharing a semantic layer.

I like the idea of opening file formats to a user community.

I like the idea of interoperability.

With that in mind, the question hammering my head is basically:

**how can I make repository metadata integration easy and independent of a manual process?**




# Business Case #

And the business case is... having to integrate repository metadata across the company, among different applications, make it available.

**Make repository metadata available in some easy to read, useful format.**



# Details #

Oracle BI EE metadata is stored mainly in two ways,
  1. the repository file
  1. report definition files
Report definition files use XML persistence making easy (or at least possible) to read the content.

On the other hand, repository file is a binary file you can open with the Administrator tool and UDML scripts.

UDML is awesome, providing an API to many repository developers.

Administration Tool itself provides a number of metadata extract features (many of them not available from a command line prompt or accessible other way than using this OOTB application).


So... UDML has a structure indeed, data structures lead to syntax grammar.
Syntax grammar can be parsed!


Parsing UDML statements the application effectively reads pieces of information and the resulting XML file is brought up to life... or better say... brought up to our community.