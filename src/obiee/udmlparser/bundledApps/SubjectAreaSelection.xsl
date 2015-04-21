<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" media-type="text/plain" />
	<xsl:template match="/">
		<busMatrix>
			<!-- Presentation catalogues are OBIEE Subject Areas -->
			<xsl:for-each select="//PresentationCatalog">
				<SubjectArea>
					<SubjectAreaID>
						<xsl:value-of select="PresentationCatalogID" />
					</SubjectAreaID>
					<xsl:variable name="SAID" select="PresentationCatalogID" />
					<SubjectAreaName>
						<xsl:value-of select="PresentationCatalogName" />
					</SubjectAreaName>
					<BusinessModelID>
						<xsl:value-of select="PresentationCatalogMappingID" />
					</BusinessModelID>
					<!-- Selecting only Logical Tables for this Subject Area and Business 
						Model -->
					<LogicalTableIDList>
						<xsl:variable name="BMMLID" select="PresentationCatalogMappingID" />
						<xsl:for-each select="..//BusinessCatalog[./BusinessCatalogID=$BMMLID]//LogicalTableID">
							<xsl:variable name="LogicalTable" select="." />
							<xsl:if test="count(../../..//PresentationColumn[starts-with(./PresentationColumnID, concat($SAID, '..')) and starts-with(./PresentationColumnMappingID, concat($LogicalTable, '.'))]) &gt; 0">
								<LogicalTableID>
									<xsl:attribute name="joins"><xsl:value-of select="count(../../..//LogicalTableID[@type='DIM'][normalize-space(text()) = $LogicalTable])" /></xsl:attribute>
									<xsl:value-of select="$LogicalTable" />
								</LogicalTableID>
							</xsl:if>
						</xsl:for-each>
					</LogicalTableIDList>
				</SubjectArea>
			</xsl:for-each>
			<!-- copying all logical joins (simplified view) -->
			<xsl:for-each select="//LogicalJoin/LogicalTableIDList">
				<LogicalJoin>
					<xsl:attribute name="type"><xsl:value-of select="../@type" /></xsl:attribute>
					<xsl:for-each select="./LogicalTableID">
						<xsl:copy-of select="." />
					</xsl:for-each>
				</LogicalJoin>
			</xsl:for-each>
		</busMatrix>
	</xsl:template>
</xsl:stylesheet>
