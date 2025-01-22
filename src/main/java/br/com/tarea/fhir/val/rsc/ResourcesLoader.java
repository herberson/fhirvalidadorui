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
package br.com.tarea.fhir.val.rsc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.xml.sax.SAXException;

import br.com.tarea.fhir.FhirCtx;
import br.com.tarea.fhir.val.to.FhirDef;
import br.com.tarea.fhir.val.to.FhirDefType;
import br.com.tarea.fhir.val.util.FileUtil;
import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;

public class ResourcesLoader {
    private static final Logger LOG = Logger.getLogger(ResourcesLoader.class);
    
	private static final String FHIR_BASE_DEF = "http://hl7.org/fhir/StructureDefinition";
	
	public List<List<FhirDef>> load(final String path, final FileUtil fileUtil) throws IOException, ParserConfigurationException, SAXException {
		final List<String> files = fileUtil.listFolder(path);
		
        final List<List<FhirDef>> loadOrder = new ArrayList<>();

        final List<FhirDef> lCodeSystem = new ArrayList<>();
        final List<FhirDef> lValueSet = new ArrayList<>();
        final List<FhirDef> lExtension = new ArrayList<>();
        final List<FhirDef> lDataType = new ArrayList<>();
        final List<FhirDef> lStructures = new ArrayList<>();
        
        final FhirContext ctx = FhirCtx.current();

    	final List<Class<?>> codeSystemTypes = Arrays.asList(org.hl7.fhir.r4.model.CodeSystem.class,
    			org.hl7.fhir.r4b.model.CodeSystem.class,
    			org.hl7.fhir.r5.model.CodeSystem.class);
    	
    	final List<Class<?>> valueSetTypes = Arrays.asList(org.hl7.fhir.r4.model.ValueSet.class,
    			org.hl7.fhir.r4b.model.ValueSet.class,
    			org.hl7.fhir.r5.model.ValueSet.class);
    	
    	final List<Class<?>> structureDefinitionTypes = Arrays.asList(org.hl7.fhir.r4.model.StructureDefinition.class,
    			org.hl7.fhir.r4b.model.StructureDefinition.class,
    			org.hl7.fhir.r5.model.StructureDefinition.class);
    	
        
        // carregando os XMLs para fazer a separação
        for (String item : files) {
        	
        	if (Arrays.asList(".git", ".DS_Store", ".sh", "fhirpkg.lock.json", "package.json").stream().anyMatch(ig -> item.contains(ig)))
        	{
        		continue;
        	}
        	
        	final String _item = item;
        	final InputStream is = new FileInputStream(Paths.get(_item).toFile());
        	
        	// Remove o Byte Of Mark (BOM) do arquivo XML.
        	final BOMInputStream bom = BOMInputStream.builder().setInputStream(is).get();
        	final String _body = IOUtils.toString(bom, StandardCharsets.UTF_8);
        	
        	final FhirDef def = FhirDef.newInstance();
        	
        	try {
        		final EncodingEnum fileEncoding = EncodingEnum.detectEncodingNoDefault(_body);
        		
        		if (fileEncoding != null) {
        			final IParser parser = fileEncoding.newParser(ctx);
        			final IBaseResource resource = parser.parseResource(_body);
        			
        			def.setResource(resource);
        			def.setFilePath(_item);
        			
        			final RuntimeResourceDefinition resourceDef = ctx.getResourceDefinition(resource);
        			
        			def.setUrl(getChildValue(resource, resourceDef, "url"));
        			def.setBaseDefinition(getChildValue(resource, resourceDef, "baseDefinition"));
        			def.setName(getChildValue(resource, resourceDef, "name"));
        			def.setVersion(getChildValue(resource, resourceDef, "version"));
        			
        			// Identificando o tipo de resource FHIR.
        			if (codeSystemTypes.stream().anyMatch(ic -> ic.isInstance(resource))) {
        				def.setType(FhirDefType.CODE_SYSTEM);
        				lCodeSystem.add(def);
        			} else if (valueSetTypes.stream().anyMatch(ic -> ic.isInstance(resource))) {
        				def.setType(FhirDefType.VALUESET);
        				lValueSet.add(def);
        			} else if (structureDefinitionTypes.stream().anyMatch(ic -> ic.isInstance(resource))) {
        				final String type = getChildValue(resource, resourceDef, "type");
        				
        				if (StringUtils.equalsIgnoreCase(type, "Extension")) {
        					def.setType(FhirDefType.EXTENSION);
        					lExtension.add(def);
        				} else {
        					final String kind = getChildValue(resource, resourceDef, "kind");
        					
        					if (StringUtils.equalsIgnoreCase("complex-type", kind)) {
        						def.setType(FhirDefType.DATA_TYPE);
        						lDataType.add(def);
        					} else {
        						def.setType(FhirDefType.STRUCTURE);
        						lStructures.add(def);
        					}
        				}
        			}
        		}
        	} catch (ca.uhn.fhir.parser.DataFormatException e) {
        		// nada a fazer
        		// O algoritmo que recupera a lista de
        		// arquivos acaba recuperando arquivos de
        		// controle do Git que não representam
        		// arquivos de definicão FHIR.
        		LOG.error("Arquivo: " + item , e);
        	} catch (NullPointerException e) {
        		// nada a fazer
        		// O algoritmo que recupera a lista de
        		// arquivos acaba recuperando arquivos de
        		// controle do Git que não representam
        		// arquivos de definicão FHIR.
        		LOG.error("Arquivo: " + item , e);
        	} catch (Exception e) {
        		LOG.warn(String.format("Falha inesperada ao ler o arquivo '%s'. %s", _item, e.getMessage()), e);
        		// O processo de leitura dos arquivos
        		// não pode parar.
        	}
        	
        	if (is != null) {
        		is.close();
        	}
        	
        	if (bom != null) {
        		bom.close();
        	}
        }

        Collections.sort(lCodeSystem);
        loadOrder.add(lCodeSystem);

        Collections.sort(lValueSet);
        loadOrder.add(lValueSet);

        orderDeps(lExtension, loadOrder);
        orderDeps(lDataType, loadOrder);
        orderDeps(lStructures, loadOrder);
		
		return loadOrder;
	}
	
	private void orderDeps(final List<FhirDef> lsdfhir, final List<List<FhirDef>> loadOrder) {
		List<FhirDef> lst;
		
		// dependencias entre extensions
		final Map<String, List<FhirDef>> depsmap = new LinkedHashMap<>();
		depsmap.put(FHIR_BASE_DEF, new ArrayList<>());
		
		for (FhirDef item : lsdfhir) {
			if (!StringUtils.startsWith(item.getBaseDefinition(), FHIR_BASE_DEF)) {
				depsmap.put(item.getBaseDefinition(), new ArrayList<>());
			}
		}
		
		for (FhirDef item : lsdfhir) {
			if (StringUtils.startsWith(item.getBaseDefinition(), FHIR_BASE_DEF)) {
				depsmap.get(FHIR_BASE_DEF).add(item);
			} else if (depsmap.get(item.getBaseDefinition()) != null) {
				depsmap.get(item.getBaseDefinition()).add(item);
			}
		}
		
		for (Map.Entry<String, List<FhirDef>> entry : depsmap.entrySet()) {
			lst = entry.getValue();
			Collections.sort(lst);
			loadOrder.add(lst);
		}
	}
	
    /**
     * Recupera o valor de um atributo da definição.
     * 
     * @param _resource
     * @param resourceDef
     * @param name
     * @return
     */
    private String getChildValue(final IBaseResource _resource, 
    		final RuntimeResourceDefinition resourceDef, 
    		final String name) 
    {
		final BaseRuntimeChildDefinition child = resourceDef.getChildByName(name);
		if (child != null) {
			final Optional<IBase> value = child.getAccessor().getFirstValueOrNull(_resource);
			return value.map(t -> (((IPrimitiveType<?>) t).getValueAsString())).orElse(null);
		} else {
			return null;
		}
    }
}
