<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" media-type="text/plain"/>

<!-- first version, it is required to change the subject area name -->
<xsl:template match="/">
<busMatrix>
	<fact>Fact-SalesLeverageChart</fact>
	<dimList>
	<xsl:for-each select="//LogicalTableID[@type='FACT']">
	<xsl:if test=". = '&quot;Core&quot;.&quot;Fact - Sales Leverage Chart&quot;'">
		<dim><xsl:value-of select="../LogicalTableID[@type='DIM']"/></dim>
	</xsl:if>
	</xsl:for-each>
	</dimList>
</busMatrix>
</xsl:template>
</xsl:stylesheet>
