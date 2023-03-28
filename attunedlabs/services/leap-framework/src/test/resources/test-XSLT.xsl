<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<xsl:template match="/root">
		<result>
			<name>
				<xsl:value-of select="name"/>
			</name>
			<state>
				<xsl:value-of select="state"/>
			</state>
			<pin>
				<xsl:value-of select="pin"/>
			</pin>
		</result>
	</xsl:template>
</xsl:stylesheet>