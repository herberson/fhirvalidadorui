/*
 * Copyright (C) 2023 Tarea Gerenciamento Ltda. (contato@tarea.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.tarea.fhir.val.to;

import org.hl7.fhir.instance.model.api.IBaseResource;

public class FhirDef implements Comparable<FhirDef> {
	private FhirDefType type;
	private String filePath;
	private String url;
	private String baseDefinition;
	private String name;
	private String version;
	private IBaseResource resource;

	public static FhirDef newInstance() {
		return new FhirDef();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\"type\": \"");
		builder.append(type);
		builder.append("\", \"filePath\": \"");
		builder.append(filePath);
		builder.append("\", \"url\": \"");
		builder.append(url);
		builder.append("\", \"baseDefinition\": \"");
		builder.append(baseDefinition);
		builder.append("\", \"name\": \"");
		builder.append(name);
		builder.append("\"}");
		return builder.toString();
	}
	
	public FhirDefType getType() {
		return type;
	}
	public void setType(FhirDefType type) {
		this.type = type;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBaseDefinition() {
		return baseDefinition;
	}
	public void setBaseDefinition(String baseDefinition) {
		this.baseDefinition = baseDefinition;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FhirDef other = (FhirDef) obj;
		if (type != other.type)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public int compareTo(FhirDef o) {
		if (o == null) {
			return -1;
		} else if (o.getUrl() == null || url == null) {
			return -1;
		} else {
			return o.getUrl().compareTo(url);
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public IBaseResource getResource() {
		return resource;
	}

	public void setResource(IBaseResource resource) {
		this.resource = resource;
	}
}
