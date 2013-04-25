<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>
	<xsl:template match="/">
	<html>
	<head>

	<style type='text/css'>
		h1 {font-family: Helvetica, sans-serif; font-weight: bold; font-size: 24pt; color: #676767;}
		h2 {font-family: Helvetica, sans-serif; font-weight: bold; font-size: 20pt; color: #767676;}
		h3 {font-family: Helvetica, sans-serif; font-size: 14pt; color: #777777;}
		h4 {font-family: Helvetica, sans-serif; font-size: 14pt; color: #888888;}
		li {font-family: Helvetica, sans-serif; font-size: 10pt; color: #474747;}
		a  {font-family: Helvetica, sans-serif; font-size: 10pt; color: #444444;}
		*  {font-family: Helvetica, sans-serif; font-size: 8pt; color: #333333;}
		tr {height:30; font-size: 8.5pt;}
		tr:hover {background: rgb(248,248,248);}
		table {
				border-spacing: 0 0;
				margin: 1px;
				border-right: 1px solid #CCCCCC;
				font-family: Helvetica, sans-serif; font-size: 8.5pt;}
		thead th {
				font-family: Helvetica, sans-serif; font-size: 8pt;
				background: #EFEFEF;
				border-left: 1px solid #CCCCCC;
				border-top: 1px solid #CCCCCC;}
        tbody td {
        		font-family: Helvetica, sans-serif; font-size: 8.5pt;
				border-bottom: 1px solid #E3E3E3;
				border-left: 1px solid #E3E3E3;}
	</style>

	<title>Oracle Business Intelligence Metadata</title>
	</head>
	<body>
	<!-- Business Models list Section -->
	<h1>OBIEE Business Models
		<span style="font-size: 24px; color: #676767; -moz-transform: scaleX(-1); -o-transform: scaleX(-1); -webkit-transform: scaleX(-1); transform: scaleX(-1); display: inline-block;">
			&#169;
		</span>
	</h1>
	<ul>
	<xsl:for-each select="//BusinessCatalog/BusinessCatalogID">
	<!-- Creating the Business Models list -->
	<li><a href="#{.}"><xsl:value-of select="."/></a></li>
	</xsl:for-each>
	</ul>
	<br/>
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
		<br/>

		<!-- Matrix Section -->
		<h4>Business Model Bus Matrix
		</h4>
		<table>
		<tbody>
		<thead>
		<tr>
			<td style="text-align:center; font-family: Helvetica, sans-serif; font-size: 8pt; border-top: 1px solid #EFEFEF; border-left: 1px solid #EFEFEF; border-bottom: 1px solid #E3E3E3; background-color: rgb(250,250,250);">Fact tables (below) / Dimensions (right)</td>
			<!-- Dimension tables list -->
			<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins > 0]">
			<xsl:sort data-type="number" select="@joins" order="descending"/>
			<xsl:choose>
				<xsl:when test="contains(., 'DO NOT USE') or contains(., 'DEPRECATED') or contains(., 'for Foldering')">
					<th style="font-family: Helvetica, sans-serif; font-size: 8pt; color: #555555; background-color: rgb(255, 255, 153);" title="Identified as a deprecated practice."><xsl:value-of select="substring(., $bmlength+2)"/></th>
				</xsl:when>
				<xsl:otherwise>
					<th style="background: #EFEFEF; font-family: Helvetica, sans-serif; font-size: 8pt; font-weight: bold; color: #555555;"><xsl:value-of select="substring(., $bmlength+2)"/></th>
				</xsl:otherwise>
			</xsl:choose>
			</xsl:for-each>
		</tr>
		</thead>
			<!-- Fact tables list and ticks -->
			<xsl:for-each select="../LogicalTableIDList/LogicalTableID [@joins = 0]">
				<tr>
				<!-- Fact Table Name -->
				<xsl:choose>
					<xsl:when test="contains(., 'DO NOT USE') or contains(., 'DEPRECATED') or contains(., 'for Foldering')">
						<td style="font-family: Helvetica, sans-serif; font-weight: bold; font-size: 8pt; color: #555555; background-color: rgb(255, 255, 200)" width="300" title="Identified as a deprecated practice."><xsl:value-of select="substring(., $bmlength+2)"/></td>
					</xsl:when>
					<xsl:otherwise>
						<td style="background: #EFEFEF; font-family: Helvetica, sans-serif; font-size: 8pt; font-weight: bold; color: #555555;"><xsl:value-of select="substring(., $bmlength+2)"/></td>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:variable name="factTbl" select="."/>
				<!-- Matching each logical dimension table -->
				<xsl:for-each select="../../LogicalTableIDList/LogicalTableID [@joins > 0]">
					<xsl:sort data-type="number" select="@joins" order="descending"/>
					<xsl:variable name="dimTbl" select="normalize-space(text())"/>
					<td align="center" style="font-family: Helvetica, sans-serif;" width="130">&#160;
					<!-- Finding logical join -->
					<xsl:for-each select="../../..//LogicalJoin/LogicalTableID[@type='FACT' and ../LogicalTableID[@type='DIM'] [text()=$dimTbl]] [text()=$factTbl]">
						<!-- "Join found" tick, the for-each loop flags (deprecated) logical FK-based -->
						<li><xsl:for-each select="../../..//LogicalJoin/LogicalTableID[@type='FACT' and ../LogicalTableID[@type='DIM'] [text()=$dimTbl]] [text()=$factTbl][../@type='LogicalForeignKey-based']"><a style="font-family: Helvetica, sans-serif; font-size: 9pt; color: red" title="Suggestion: Consider using a standard logical join instead.">*</a></xsl:for-each></li>
						</xsl:for-each>
					</td>
				</xsl:for-each>
				</tr>
			</xsl:for-each>
		</tbody>
		</table>

		<br/><br/>
		<hr style="height: 1px; border: 0; background-color: #AAAAAA; width: 70%;"/>

	</xsl:for-each>
	<div style="padding-top: 20px;">
		<p style="color: rgb(128, 128, 128); float: left;">Mozilla Firefox&#8482; or Google Chrome&#169; are strongly recommended for best results.</p>
		<p style="color: rgb(128, 128, 128); float: right;">Generated using UDMLParser / Bus Matrix application.</p>
	</div>
	</body>
	</html>
	</xsl:template>
</xsl:stylesheet>
