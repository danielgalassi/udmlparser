<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" media-type="text/plain"/>
	<xsl:template match="/">
	<busMatrix>
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
	<BusinessCatalog>
		<xsl:copy-of select="."/>
		<xsl:variable name="BMMLID" select="."/>
		<!-- copying all subject areas using this business model as source -->
		<PresentationCatalogIDList>
			<xsl:for-each select="../..//PresentationCatalog/PresentationCatalogMappingID [text() = $BMMLID]">
				<xsl:copy-of select="../PresentationCatalogID"/>
			</xsl:for-each>
		</PresentationCatalogIDList>
		<!-- counting the number of joins of logical dimension tables -->
		<LogicalTableIDList>
			<xsl:for-each select="..//LogicalTableIDList/LogicalTableID">
				<xsl:copy>
					<xsl:variable name="LgclTblID" select="normalize-space(text())"/>
					<xsl:attribute name="joins"><xsl:value-of select="count(../../..//LogicalTableID[@type='DIM'][normalize-space(text()) = $LgclTblID])"/></xsl:attribute>
					<xsl:value-of select="normalize-space(.)"/>
				</xsl:copy>
			</xsl:for-each>
		</LogicalTableIDList>
	</BusinessCatalog>
	</xsl:for-each>
	<!-- copying all logical joins (simplified view) -->
	<xsl:for-each select="//LogicalJoin/LogicalTableIDList">
	<LogicalJoin>
		<xsl:attribute name="type"><xsl:value-of select="../@type"/></xsl:attribute>
		<xsl:for-each select="./LogicalTableID">
			<xsl:copy-of select="."/>
		</xsl:for-each>
	</LogicalJoin>
	</xsl:for-each>
	</busMatrix>
	</xsl:template>
</xsl:stylesheet>
