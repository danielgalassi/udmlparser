<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>
	<xsl:template match="/">
	<html>
	<head>
	
	<style type='text/css'>
		h1 {font-family: Helvetica, sans-serif; font-weight: bold; font-size: 24pt; color: 111111;}
		h2 {font-family: Helvetica, sans-serif; font-weight: bold; font-size: 20pt; color: 333333;}
		h3 {font-family: Helvetica, sans-serif; font-size: 16; color: 333333;}
		h4 {font-family: Helvetica, sans-serif; font-size: 16; color: 333333;}
		li {font-family: Helvetica, sans-serif; font-size: 11pt; color: 000000;}
		* { font-family: Helvetica, sans-serif; }
		table { border-spacing: 0 0;
				margin: 1px;
				border-right: 1px solid #CCCCCC;}
		thead th {
            background: #EFEFEF;
            border-left: 1px solid #CCCCCC;
            border-top: 1px solid #CCCCCC;}
        tbody td {
            border-bottom: 1px solid #E3E3E3;
            border-left: 1px solid #E3E3E3;
        }
	</style>
	
	<title>Oracle Business Intelligence Metadata</title>
	</head>
	<body>
	<!-- Business Models list Section -->
	<h1>OBIEE Business Models</h1>
	<ul>
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
	<!-- Creating the Business Models list -->
	<li><a href="#{.}"><xsl:value-of select="."/></a></li>
	</xsl:for-each>
	</ul>
	<br style="font-family: Helvetica;"/>
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">			
		<!-- Business Model Header Section -->
		<h2><a name="{.}" id="{.}"></a><xsl:value-of select="."/></h2>
		<xsl:variable name="bmlength" select="string-length(.)"/>
		<!-- Subject Area Section -->
		<xsl:for-each select="../PresentationCatalogIDList">
			<h3>Subject Areas</h3>
			<ul>
			<!-- Creating the subject areas list -->
			<xsl:for-each select="PresentationCatalogID">
			<li><xsl:value-of select="."/></li>
			</xsl:for-each>
			</ul>
		</xsl:for-each>
		<br style="font-family: Helvetica;"/>
		<!-- Matrix Section -->
		<h4>Business Model Bus Matrix</h4>
		<br style="font-family: Helvetica;"/>
		<table>
		<tbody>
			<tr valign="bottom">
				<td style="font-family: Arial;" width="300"><font size="2">Fact tables (below) / Dimensions (right)</font></td>
				<!-- Dimension tables list -->
				<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins > 0]">
				<xsl:sort data-type="number" select="@joins" order="descending"/>
				<xsl:choose>
					<xsl:when test="contains(., 'DO NOT USE') or contains(., 'DEPRECATED') or contains(., 'for Foldering')">
						<td style="font-family: Arial; background-color: rgb(255, 255, 153)" width="115"><font size="2" title="Identified as a deprecated practice."><xsl:value-of select="substring(., $bmlength+2)"/></font></td>
					</xsl:when>
					<xsl:otherwise>
						<td style="font-family: Arial;" width="115"><font size="2"><xsl:value-of select="substring(., $bmlength+2)"/></font></td>
					</xsl:otherwise>
				</xsl:choose>
				</xsl:for-each>
			</tr>
			<!-- Fact tables list and ticks -->
			<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins = 0]">
				<tr>
				<!-- Fact Table Name -->
				<xsl:choose>
					<xsl:when test="contains(., 'DO NOT USE') or contains(., 'DEPRECATED') or contains(., 'for Foldering')">
						<td style="font-family: Arial; background-color: rgb(255, 255, 153)" width="300"><font size="2" title="Identified as a deprecated practice."><xsl:value-of select="substring(., $bmlength+2)"/></font></td>
					</xsl:when>
					<xsl:otherwise>
						<td style="font-family: Arial;" width="300"><font size="2"><xsl:value-of select="substring(., $bmlength+2)"/></font></td>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:variable name="factTbl" select="."/>
				<!-- Matching each logical dimension table -->
				<xsl:for-each select="../../LogicalTableIDList/LogicalTableID [@joins > 0]">
					<xsl:sort data-type="number" select="@joins" order="descending"/>
					<xsl:variable name="dimTbl" select="normalize-space(text())"/>
					<td align="center" style="font-family: Arial;" width="115">&#160;
					<!-- Finding logical join -->
					<xsl:for-each select="../../..//LogicalJoin/LogicalTableID[@type='FACT' and ../LogicalTableID[@type='DIM'] [text()=$dimTbl]] [text()=$factTbl]">
						<!-- "Join found" tick -->
						<li/>
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
	<br style="font-family: Arial;"/>
	<br style="font-family: Arial;"/>
	<div style="text-align: right; color: rgb(128, 128, 128); font-family: Helvetica,Arial,sans-serif;"><small>Generated using UDMLParser / Bus Matrix application.</small></div>
	</body>
	</html>
	</xsl:template>
</xsl:stylesheet>
