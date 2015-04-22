# <font color='#545454'>UDML2XML</font> #



## <font color='#838383'>The UDML story</font> ##
<font color='#999999'>
<a href='http://www.oracle.com/us/solutions/business-analytics/business-intelligence/enterprise-edition/overview/index.html'>OBIEE</a> provides administrators and developers with UDML, a <b>non documented</b> and <b>unsupported</b> but (I think) <b>RELIABLE</b> feature.<br>
So... we can get UDML statements by running nqUDMLgen commands (many thanks for this feature).<br>
</font>


## <font color='#838383'>An Integration story</font> ##
<font color='#999999'>
I cannot help it... I like the idea of sharing information across applications, more so if we have the chance to expose a wealth of intelligence, semantics and metadata.<br>
</font>


## <font color='#838383'>The Parser story</font> ##
<font color='#999999'>
Ok, so... sometimes, getting the <b>UDML version</b> of the repository won't be enough... Let's say we need to rapidly share OBIEE metadata with another tool, e.g. a data dictionary... This is a real world scenario I found in many projects.<br>
So this is my kind of business case... a tool extracting metadata to some independent format (this time, a long-standing W3C recommendation), XML.<br>
<br>
<br>
So, it's just:<br>
<i>a parser utility, generating XML structures out of <b>OBIEE</b> UDML repository statements.</i>

Oh... and now (March 2013) you can also "reverse-engineer" your business models, generating <a href='http://code.google.com/p/udmlparser/wiki/NewFeatures'>Bus Matrices</a>!<br>
</font>


## <font color='#939393'>One more thing</font> ##
<font color='#999999'>
<b>Thanks to Denis Burlaka (denis.burlaka@gmail.com) for contributing to this project!</b>
</font>


|![http://udmlparser.googlecode.com/svn/trunk/UDML.jpg](http://udmlparser.googlecode.com/svn/trunk/UDML.jpg)|![https://github.com/danielgalassi/udmlparser/blob/master/XML.jpg](https://github.com/danielgalassi/udmlparser/blob/master/XML.jpg)|
|:----------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------|

![http://udmlparser.googlecode.com/svn/trunk/BusMatrix.jpg](http://udmlparser.googlecode.com/svn/trunk/BusMatrix.jpg)


---

**FREE SOFTWARE, USE IT AND GET INVOLVED! Feedback is always appreciated.**
