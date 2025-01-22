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
package br.com.tarea.fhir.val;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JTextArea;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import br.com.tarea.fhir.FhirCtx;
import br.com.tarea.fhir.val.rsc.ResouceValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;

public class ExecutarValidacao {
	private static final Logger logger = Logger.getLogger(ExecutarValidacao.class);
	
	public static String executar(final String localDef, 
			final String jsonSample, 
			final String jsonMessages, 
			final boolean apenasFalhas, 
			final JTextArea txtResValidacao, 
			final boolean scrollLock) throws Exception {
		final StopWatch stopWatch = StopWatch.createStarted();
		
		final FhirContext ctx =FhirCtx.current();
		final List<String> lsitems = new ArrayList<>();
		
		String jsonContent = null;
		
		boolean writeMsg = true;
		
		if (StringUtils.isBlank(jsonSample)) {
			throw new ExecutarValidacaoException("Com o arquivo JSON de exemplo inválido. " + localDef);
		} else {
			jsonContent = IOUtils.toString(new FileInputStream(jsonSample), "utf-8");
			
			final JsonNode json = asJsonNode(jsonContent);
			
			if (json instanceof ArrayNode) {
				final ArrayNode jsonArray = (ArrayNode) json;
				for (int i = 0; i < jsonArray.size(); i++) {
					lsitems.add(  jsonNodeAsString(jsonArray.get(i)) );
				}
			} else {
				lsitems.add(jsonContent);
			}
		}
		
		if (StringUtils.isBlank(jsonMessages)) {
			writeMsg = false;
		}
		
		logger.info("FHIR profiles load :: START");
		
		final ResouceValidator rval = new ResouceValidator();
		final FhirValidator fhirVal = rval.build(ctx, localDef);
		
		logger.info("FHIR profiles load :: END");
		
		final StringBuilder result = new StringBuilder();
				
		Map<String, Integer> resumo = new LinkedHashMap<>();
		
		boolean nl = false;
		
		int poolSize;
		
		try {
			poolSize = Integer.parseInt(System.getProperty("pool.size", "1"));
		} catch (NumberFormatException e) {
			poolSize = 1;
		}
		
		logger.info("pool.size: " + poolSize);
		
		final ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		final List<Future<ValidacaoItem>> list = new ArrayList<Future<ValidacaoItem>>();
		
		for (int i = 0; i < lsitems.size(); i++) {
			jsonContent = lsitems.get(i);
			
			final ValidarItemCallable call = new ValidarItemCallable(fhirVal, i, lsitems.size(), jsonContent, apenasFalhas);
			final Future<ValidacaoItem> item = executor.submit(call);
			
			list.add(item);
		}
				
		for (int i = 0; i < list.size(); i++) {
			if (i > 0 && nl) {
				result.append('\n');
			}
			
			ValidacaoItem validacao = list.get(i).get();
			
			for (SingleValidationMessage next : validacao.getMessages()) {
				String severity;
				Integer qtde;
				
				severity = next.getSeverity().getCode();
				qtde = resumo.get(severity);
				
				if (qtde == null) {
					qtde = 0;
				}
				
				resumo.put(severity, qtde);
				
			}
			
			if (validacao.isSuccess()) {
				if (!apenasFalhas) {
					nl = true;
				} else {
					nl = false;
				}
			} else {
				nl = true;
			}
			
			result.append(validacao.getResult());
			
			if (txtResValidacao != null && list.size() <= 10) {
				txtResValidacao.setText(result.toString());
			}
		}
		executor.shutdown();

		if (txtResValidacao != null && list.size() > 10) {
			txtResValidacao.setText(result.toString());
		}
		
		if (txtResValidacao != null) {
			txtResValidacao.setText(result.toString());
		}
		
		if (writeMsg) {
			IOUtils.write(result.toString(), new FileOutputStream(jsonMessages), "utf-8");
		}
		
		stopWatch.stop();
		
		logger.info(String.format("###### execution time: %s ######", stopWatch.toString()));
		
		return result.toString();
	}
	
	private static JsonNode asJsonNode(final String param) {
		ObjectMapper mapper;
		JsonNode rtn;
		
		try {
			mapper = new ObjectMapper();
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
			rtn = mapper.readTree(param);
		} catch (NullPointerException e) {
			rtn = null;
		} catch (IllegalArgumentException e) {
			rtn = null;
		} catch (IOException e) {
			rtn = null;
		}
				
		return rtn;
	}
	
	private static String jsonNodeAsString(final JsonNode json)  {
		ObjectMapper printer;
		String rtn;
		
		printer = new ObjectMapper();
		
		try {
			rtn =  printer.writeValueAsString(json);
		} catch (NullPointerException e) {
			rtn = "ERRO: " + e.getClass().getName() + " - " + e.getMessage();
		} catch (JsonProcessingException e) {
			rtn = "ERRO: " + e.getClass().getName() + " - " + e.getMessage();
		} finally {
			printer = null;
		}			
		
		return rtn;
	}
}
