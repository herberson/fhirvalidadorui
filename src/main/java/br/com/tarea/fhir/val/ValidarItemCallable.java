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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

import br.com.tarea.fhir.FhirCtx;
import br.com.tarea.fhir.msg.Messages;
import br.com.tarea.fhir.msg.MessagesKeys;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidarItemCallable implements Callable<ValidacaoItem> {
	private static final Logger logger = Logger.getLogger(ValidarItemCallable.class);
	
	private final FhirValidator fhirVal;
	private final int index;
	private final int totalItens;
	private final String jsonContent;
	private final boolean apenasFalhas;
	
	private final FhirContext ctx = FhirCtx.current();
	
	public ValidarItemCallable(final FhirValidator fhirVal, final int index, final int totalItens, final String jsonContent, final boolean apenasFalhas) {
		this.fhirVal = fhirVal;
		this.index = index;
		this.totalItens = totalItens;
		this.jsonContent = jsonContent;
		this.apenasFalhas = apenasFalhas;
	}
	
	@Override
	public ValidacaoItem call() throws Exception {
		final ValidacaoItem rtn = new ValidacaoItem();
		final String ixs = String.format("%0" + String.valueOf(totalItens).length() + "d", index);
		
		logger.info("Validating item[" + ixs + "] :: START");
		
		final StopWatch stopWatch = StopWatch.createStarted();
		
		ValidationResult vres = fhirVal.validateWithResult(jsonContent);
		
		stopWatch.stop();
		
		boolean isSuccessfull = vres.isSuccessful();
		
		final List<SingleValidationMessage> myMessages = new ArrayList<>();
		
		final Map<String, Integer> resumo = new LinkedHashMap<>();
		
		for (SingleValidationMessage next : vres.getMessages()) {
            next.setSeverity(next.getSeverity());
            
            final String severity = next.getSeverity().getCode();
            Integer qtde = resumo.get(severity);
            
            if (ResultSeverityEnum.ERROR == next.getSeverity()) {
				isSuccessfull = false;
            }
            
            if (qtde == null) {
            	qtde = 0;
            }
            qtde++;
            
            next.setMessage(next.getMessage());
            
            if (apenasFalhas) {
            	if (ResultSeverityEnum.ERROR == next.getSeverity()) {
            		myMessages.add(next);
            	}
            } else {
            	myMessages.add(next);
            }
			
			resumo.put(severity, qtde);
		}
		
		rtn.setSuccess(isSuccessfull);
		rtn.setMessages(myMessages);
		
        logger.info("Validating item[" + ixs + "] :: " + (rtn.isSuccess() ? "SUCCESS" : ">>> FAIL <<<") + " \u0394 " + stopWatch.toString());
        logger.info("Validating item[" + ixs + "] :: END");
        
		vres = new ValidationResult(ctx, myMessages);
		
		String valAsString;
		
		final IBaseResource resource = ctx.newJsonParser().parseResource(jsonContent);
		final StringBuilder sbIdentifier = new StringBuilder();
		
		if (resource instanceof Bundle) {
			final Bundle bnd = (Bundle) resource;
			sbIdentifier.append("Bundle.identifier:\n");
			sbIdentifier.append("   system: ").append(bnd.getIdentifier().getSystem()).append("\n");
			sbIdentifier.append("    value: ").append(bnd.getIdentifier().getValue()).append("\n");
		} else {
			sbIdentifier.setLength(0);
		}
		
		if (rtn.isSuccess()) {
			if (!apenasFalhas) {
				rtn.getResult()
					.append("====== ")
					.append("item[")
					.append(ixs)
					.append("] >> ")
					.append(msg(MessagesKeys.msgValidationSuccess))
					.append(" << \u0394 ")
					.append(stopWatch.toString());
				
				int nf = 0;
				for (Map.Entry<String, Integer> entry : resumo.entrySet()) {
					rtn.getResult().append(String.format(" | %s: %d", entry.getKey(), entry.getValue()));
					nf++;
				}
				
				if (nf > 1)
				{
					rtn.getResult().append(" | ");
				}
				
				rtn.getResult()
					.append(" ======\n")
					.append(sbIdentifier);
				
				valAsString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(vres.toOperationOutcome());
				rtn.getResult().append(valAsString);
			}
		} else {
			rtn.getResult()
			.append("====== ")
			.append("item[")
			.append(ixs)
			.append("] >> ")
			.append(msg(MessagesKeys.msgValidationError))
			.append(" << \u0394 ")
			.append(stopWatch.toString());
			
			int nf = 0;
			for (Map.Entry<String, Integer> entry : resumo.entrySet()) {
				rtn.getResult().append(String.format(" | %s: %d", entry.getKey(), entry.getValue()));
				nf++;
			}
			
			if (nf > 1)
			{
				rtn.getResult().append(" |");
			}
			
			rtn.getResult()
				.append(" ======\n")
				.append(sbIdentifier);
			
			valAsString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(vres.toOperationOutcome());
			rtn.getResult().append(valAsString);
		}
		
		return rtn;
	}

    private static String msg(final MessagesKeys key)
    {
    	return Messages.getKey(key);
    }
}
