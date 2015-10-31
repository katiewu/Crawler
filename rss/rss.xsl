<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="/">
		<html>
			<body>
				<xsl:for-each select="documentcollection/document/rss/channel">
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="./link" />
						</xsl:attribute>
						<h4>
							<xsl:value-of select="./title" disable-output-escaping="yes" />
						</h4>
					</a>
					<xsl:for-each select="./item">
						<h5>Title</h5>
								<xsl:choose>
									<xsl:when test="(./title) and (./link)">
										<a>
											<xsl:attribute name="href"><xsl:value-of
												select="./link" />
									</xsl:attribute>
											<xsl:value-of select="./title"
												disable-output-escaping="yes" />
										</a>
									</xsl:when>
									<xsl:when test="(./title) and not (./link)">
										<xsl:value-of select="./title"
											disable-output-escaping="yes" />
									</xsl:when>
									<xsl:otherwise>
										Title Not Exist.
									</xsl:otherwise>
								</xsl:choose>
						<h5>Description</h5>
								<xsl:choose>
									<xsl:when test="./description">
										<xsl:value-of select="./description"
											disable-output-escaping="yes" />
									</xsl:when>
									<xsl:otherwise>
										Description Not Exist
									</xsl:otherwise>
								</xsl:choose>
					</xsl:for-each>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>