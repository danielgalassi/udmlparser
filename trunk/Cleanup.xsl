<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" media-type="text/plain"/>
	<xsl:template match="/">
	<busMatrix>
	<xsl:for-each select="//BusinessCatalog">
	<BusinessCatalog>
		<xsl:copy-of select="./BusinessCatalogID"/>
		<PresentationCatalogIDList>
			<xsl:for-each select="./PresentationCatalogIDList/PresentationCatalogID">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</PresentationCatalogIDList>

		<LogicalTableIDList>
			<xsl:for-each select="LogicalTableIDList/LogicalTableID">
			<xsl:sort data-type="number" select="@joins" order="descending"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</LogicalTableIDList>
	</BusinessCatalog>
	</xsl:for-each>
	<xsl:for-each select="//LogicalJoin">
	<xsl:copy-of select="."/>
	</xsl:for-each>
	</busMatrix>
	</xsl:template>
</xsl:stylesheet>
