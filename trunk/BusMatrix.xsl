<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" media-type="text/plain"/>
	<xsl:template match="/">
	<busMatrix>
	<!--xsl:for-each select="//PresentationCatalog/PresentationCatalogID">
	<PresentationCatalog>
		<xsl:copy-of select="."/>
		<xsl:copy-of select="../PresentationCatalogMappingID"/>
	</PresentationCatalog>
	</xsl:for-each-->
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
	<BusinessCatalog>
		<xsl:copy-of select="."/>
		<xsl:variable name="BMMLID" select="."/>
		<PresentationCatalogIDList>
			<xsl:for-each select="../..//PresentationCatalog/PresentationCatalogMappingID [text() = $BMMLID]">
				<xsl:copy-of select="../PresentationCatalogID"/>
			</xsl:for-each>
		</PresentationCatalogIDList>
		<LogicalTableIDList>
			<xsl:for-each select="..//LogicalTableIDList/LogicalTableID">
				<xsl:copy>
					<xsl:variable name="LgclTblID" select="."/>
					<!--xsl:attribute name="joins"><xsl:value-of select="count(../../../..//LogicalTableID[@type='DIM'] [text() = $LgclTblID])"/></xsl:attribute-->
					<xsl:attribute name="joins"><xsl:value-of select="count(../../../..//LogicalTableID[@type='DIM'] [text() = $LgclTblID])"/></xsl:attribute>
					<xsl:value-of select="."/>
				</xsl:copy>
			</xsl:for-each>
		</LogicalTableIDList>
	</BusinessCatalog>
	</xsl:for-each>
	<xsl:for-each select="//LogicalJoin/LogicalTableIDList">
	<LogicalJoin>
		<xsl:for-each select="./LogicalTableID">
			<xsl:copy-of select="."/>
		</xsl:for-each>
	</LogicalJoin>
	</xsl:for-each>
	</busMatrix>
	</xsl:template>
</xsl:stylesheet>
