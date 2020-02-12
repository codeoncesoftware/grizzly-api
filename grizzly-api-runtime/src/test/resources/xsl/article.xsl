<?xml version="1.0"?>
<!--
  ========================LICENSE_START=================================
  grizzly-api-runtime
  %%
  Copyright (C) 2019 - 2020 CODE ONCE SOFTWARE
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  =========================LICENSE_END==================================
  -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text" />

	<xsl:template match="/">
		Article -
		<xsl:value-of select="/Article/Title" />
		Authors :
		<xsl:apply-templates
			select="/Article/Authors/Author" />
	</xsl:template>

	<xsl:template match="Author">
		-
		<xsl:value-of select="." />
	</xsl:template>

</xsl:stylesheet>
