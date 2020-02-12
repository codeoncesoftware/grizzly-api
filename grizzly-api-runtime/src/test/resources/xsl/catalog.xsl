<?xml version="1.0" encoding="ISO-8859-1"?>
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
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:foo="http://www.foo.org/" xmlns:bar="http://www.bar.org">
	<xsl:template match="/">
		<html>
			<body>
				<h2>My CD Collection</h2>
				<table border="1">
					<tr bgcolor="#9acd32">
						<th>Title</th>
						<th>Artist</th>
						<th>Country</th>
						<th>Company</th>
						<th>Price</th>
						<th>Year</th>
						<th>Image</th>
					</tr>
					<xsl:for-each select="catalog/foo:cd">
						<tr>
							<td>
								<xsl:value-of select="title" />
							</td>
							<td>
								<xsl:value-of select="artist" />
							</td>
							<td>
								<xsl:value-of select="country" />
							</td>
							<td>
								<xsl:value-of select="company" />
							</td>
							<td>
								<xsl:value-of select="price" />
							</td>
							<td>
								<xsl:value-of select="bar:year" />
							</td>
							<td>
								<img>
									<xsl:attribute name="src">
								        <xsl:value-of select="img" />
								    </xsl:attribute>
								</img>
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
