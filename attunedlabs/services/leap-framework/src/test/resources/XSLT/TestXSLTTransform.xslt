<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<Event>
			<id>
				<xsl:value-of select="/Event/id" />
			</id>
			<object>
				<xsl:for-each select="/Event/object">
					<metaData>
						<data>TEST XML-XSLT</data>
						<contentType>XML</contentType>
					</metaData>
					<processMetaData>PROCESSING DATA</processMetaData>
					<content>
						<apiVersion>NOT AVAILABLE</apiVersion>
						<data>NOT AVAILABLE</data>
						<contentType>NOT AVAILABLE</contentType>
					</content>
				</xsl:for-each>
			</object>
		</Event>
	</xsl:template>
</xsl:stylesheet>