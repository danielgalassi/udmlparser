<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>
	<xsl:template match="/">
	<html>
	<head>
	<title>Oracle Business Intelligence Metadata Documentation</title>
	</head>
	<body>
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
		<!-- Business Model Header Section -->
		<big style="font-family: Arial;"><font size="+3"><big><span
		style="font-weight: bold;"><xsl:value-of select="."/></span></big></font></big>
		<br style="font-family: Arial;"/>
		<br style="font-family: Arial;"/>
		<br style="font-family: Arial;"/>
		
		<!-- Subject Area Section -->
		<xsl:for-each select="../PresentationCatalogIDList">
			<font style="font-family: Arial;" size="+1">
			<span style="font-weight: bold;">Subject Areas</span></font>
			<br style="font-family: Arial;"/>
			<ul style="font-family: Arial;">
			<!-- Creating the subject areas list -->
			<xsl:for-each select="PresentationCatalogID">
			<li><xsl:value-of select="."/></li>
			</xsl:for-each>
			</ul>
		</xsl:for-each>
		<br style="font-family: Arial;"/>
		<br style="font-family: Arial;"/>

		<!-- Matrix Section -->
		<font style="font-family: Arial;" size="+1"><span style="font-weight: bold;">Dimensional Model logical relationships</span></font>
		<br style="font-family: Arial;"/>
		<br style="font-family: Arial;"/>
		
		<table border="1">
		<tbody>
			<tr valign="bottom">
				<td style="font-family: Arial;"><font size="2">Fact tables (below) / Dimensions (right)</font></td>
				<!-- Dimension tables list -->
				<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins > 0]">
				<xsl:choose>
					<xsl:when test="contains(., 'DO NOT USE') or contains(., 'DEPRECATED')">
						<td style="font-family: Arial; background-color: rgb(255, 255, 153)"><font size="2"><xsl:value-of select="."/></font></td>
					</xsl:when>
					<xsl:otherwise>
						<td style="font-family: Arial;"><font size="2"><xsl:value-of select="."/></font></td>
					</xsl:otherwise>
				</xsl:choose>
				</xsl:for-each>
			</tr>
			<!-- Fact tables list and ticks -->
			<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins = 0]">
				<tr>
				<!-- Fact Table Name -->
				<td style="font-family: Arial;"><font size="2"><xsl:value-of select="."/></font></td>
				<xsl:variable name="factTbl" select="."/>
				<!-- Matching each logical dimension table -->
				<xsl:for-each select="../../LogicalTableIDList/LogicalTableID [@joins > 0]">
					<xsl:variable name="dimTbl" select="."/>
					<td align="center" style="font-family: Arial;">
					<!-- Finding logical join -->
					<xsl:for-each select="../../..//LogicalJoin/LogicalTableID[@type='FACT'] [text()=$factTbl]">
						<xsl:if test="../LogicalTableID[@type='DIM'] [text()=$dimTbl]">
							<!-- "Join found" tick -->
							<li/>
						</xsl:if>
					</xsl:for-each>
					</td>
				</xsl:for-each>
			</tr>
			</xsl:for-each>
		</tbody>
		</table>
		
		<br style="font-family: Arial;"/>
		<br style="font-family: Arial;"/>
		<hr style="width: 100%; height: 1px;"/>

	</xsl:for-each>
	</body>
	</html>
	</xsl:template>
</xsl:stylesheet>