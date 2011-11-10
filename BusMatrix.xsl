<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" media-type="text/plain"/>

<!-- first version, it is required to change the subject area name -->
	<xsl:template match="/">
	<busMatrix>
	<!--fact>Fact-SalesLeverageChart</fact>
	<dimList>
	<xsl:for-each select="//LogicalTableID[@type='FACT']">
	<xsl:if test=". = '&quot;Core&quot;.&quot;Fact - Sales Leverage Chart&quot;'">
		<dim><xsl:value-of select="../LogicalTableID[@type='DIM']"/></dim>
	</xsl:if>
	</xsl:for-each>
	</dimList-->
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
	<BusinessCatalog>
		<xsl:copy-of select="."/>
		<LogicalTableIDList>
			<xsl:for-each select="..//LogicalTableIDList/LogicalTableID">
				<xsl:copy>
<!-- Core.Dim - Parent Product -->
					<xsl:variable name="LgclTblID" select="."/>
					<!--xsl:attribute name="test"><xsl:value-of select="count(../../../..//LogicalTableID[@type='DIM'] [text() = $LgclTblID])"/></xsl:attribute-->
					<xsl:attribute name="test"><xsl:value-of select="count(../../../..//LogicalTableID [text() = $LgclTblID])"/></xsl:attribute>
					<xsl:value-of select="."/>
				</xsl:copy>
			</xsl:for-each>
		</LogicalTableIDList>
	</BusinessCatalog>
	</xsl:for-each>
	</busMatrix>
	</xsl:template>
</xsl:stylesheet>
