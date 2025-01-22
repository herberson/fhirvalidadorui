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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.xml.sax.SAXException;

import br.com.tarea.fhir.val.to.FhirDef;
import br.com.tarea.fhir.val.to.FhirDefType;
import br.com.tarea.fhir.val.util.FileUtil;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.validation.FhirValidator;

public class ResouceValidator {
	private static final Logger LOG = Logger.getLogger(ResouceValidator.class);
	
	public FhirValidator build(final FhirContext ctx, final String resourcesPath) throws IOException, ParserConfigurationException, SAXException {
		final FileUtil fileUtil = new FileUtil();
		
		final ResourcesLoader loader = new ResourcesLoader();
		final ValidationSupportChain supportChain = new ValidationSupportChain();
		
		if (StringUtils.isBlank(resourcesPath)) {
			LOG.warn("Path to FHIR profile definitions was not provided. Using HAPI-FHIR default configurations of profiles.");
		}
		else {
			final PrePopulatedValidationSupport ppSupport  = new PrePopulatedValidationSupport(ctx);
			LOG.info("Load definition file :: start");
			
			final List<List<FhirDef>> resources = loader.load(resourcesPath, fileUtil);
			
			LOG.info("Load definition file :: end");
			
			for (final List<FhirDef> lsitem : resources) {
				for (FhirDef item : lsitem) {
					LOG.info(
							  "\n  ->             URL: " + item.getUrl()
							+ "\n  -> Base Definition: " + item.getBaseDefinition()
							+ "\n  ->            Type: " + item.getType()
							+ "\n  ->            Path: " + item.getFilePath()
							+ "\n  ->         Version: " + item.getVersion()
							);
					
					final String sdPath = item.getFilePath();
					
					if (FhirDefType.CODE_SYSTEM.equals(item.getType())) 
					{
						if (ctx.getVersion().getVersion() == FhirVersionEnum.R4)
						{
							ppSupport.addCodeSystem(loadResource(org.hl7.fhir.r4.model.CodeSystem.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R4B)
						{
							ppSupport.addCodeSystem(loadResource(org.hl7.fhir.r4b.model.CodeSystem.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R5)
						{
							ppSupport.addCodeSystem(loadResource(org.hl7.fhir.r5.model.CodeSystem.class, ctx, sdPath));
						}
					} 
					else if (FhirDefType.VALUESET.equals(item.getType())) 
					{
						if (ctx.getVersion().getVersion() == FhirVersionEnum.R4)
						{
							ppSupport.addValueSet(loadResource(org.hl7.fhir.r4.model.ValueSet.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R4B)
						{
							ppSupport.addValueSet(loadResource(org.hl7.fhir.r4b.model.ValueSet.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R5)
						{
							ppSupport.addValueSet(loadResource(org.hl7.fhir.r5.model.ValueSet.class, ctx, sdPath));
						}
					} 
					else 
					{
						if (ctx.getVersion().getVersion() == FhirVersionEnum.R4)
						{
							ppSupport.addStructureDefinition(loadResource(org.hl7.fhir.r4.model.StructureDefinition.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R4B)
						{
							ppSupport.addStructureDefinition(loadResource(org.hl7.fhir.r4b.model.StructureDefinition.class, ctx, sdPath));
						}
						else if (ctx.getVersion().getVersion() == FhirVersionEnum.R5)
						{
							ppSupport.addStructureDefinition(loadResource(org.hl7.fhir.r5.model.StructureDefinition.class, ctx, sdPath));
						}
					}
				}
			}
			supportChain.addValidationSupport(ppSupport);
		}
		
        supportChain.addValidationSupport(new DefaultProfileValidationSupport(ctx));
        supportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));
        supportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));
        
        final FhirInstanceValidator validatorModule = new FhirInstanceValidator(new CachingValidationSupport(supportChain));

        validatorModule.setBestPracticeWarningLevel(BestPracticeWarningLevel.Ignore);
        validatorModule.setErrorForUnknownProfiles(true);
        validatorModule.setAnyExtensionsAllowed(false);

        return ctx.newValidator().registerValidatorModule(validatorModule);
	}
	
	protected <T extends IBaseResource> T loadResource(final Class<T> type, final FhirContext ctx, final String resourceName)
			throws IOException {
		final InputStream stream = new FileInputStream(resourceName);
		final String string = IOUtils.toString(stream, StandardCharsets.UTF_8);
		final IParser newJsonParser = EncodingEnum.detectEncodingNoDefault(string).newParser(ctx);
		T resource = newJsonParser.parseResource(type, string);
		
		LOG.info(resource);
		
		return resource;
	}

}
