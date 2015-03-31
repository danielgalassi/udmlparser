### Requirements ###

  * As a Java application you need JRE installed in whatever the target system is.

  * JAR files are often compiled using the latest JDK available.

  * Path: both relative and absolute path forms can be used, be sure you use "\\" (instead of a single backslash) when running the application on Windows OS.


### First things first ###

  * Download the JAR file from the Home Page (link to Google Drive).

  * If you want to download older versions, please follow this link to the old [Downloads](https://code.google.com/p/udmlparser/downloads/list) section.


### Parsing a UDML file ###

In OBI 10g, you can export the UDML version of the repository metadata using the nqUDMLgen tool. This tool is provided with OBIEE and it is located under `OBIHOME/server/Bin` (run it using -n -q -8 flags.)


  1. confirm the name and location of the UDML file
  1. choose the name and location of the resulting XML file
  1. in command line execute: `java -jar udmlparserXYZ.jar -udml=<UDMLfile> -rpdxml=<resulting File>`


nqUDMLgen application is also available in OBI 11g, just find the location and use it applying the same parameters. However, remember that an XML version of UDML (XUDML) is now supported by Oracle (google OBIEE 11g UDML for more details.)


### Step by Step ###

You can find a step by step guide (written in the days of OBI 10g, most ) [here](http://it.toolbox.com/wiki/index.php/Oracle_BI_EE_-_Encoding_Repository_metadata_in_XML).


### Do you want to generate a bus matrix for each business model? ###

Yes, little documentation, over 50 fact tables and a myriad of dimensions are a common scenario. Sad but true. Now... how can we get a bus matrix of a business model and find common dimensions across facts? Easy pease. I bundled an application in this parser to generate a bus matrix (it's BMML-based, there one BMML = one bus matrix)


  1. confirm the name and location of the UDML file
  1. choose the name and location of the resulting XML file
  1. in command line execute: `java -jar udmlparserXYZ.jar -udml=<UDMLfile> -udmltgt=<resulting File> -cmd=busmatrix`
  1. check the resulting HTML page :)




---

**I like the idea of getting issues reported**

**I also like the idea of someone contributing to the project**