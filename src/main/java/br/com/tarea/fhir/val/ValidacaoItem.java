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

import java.util.List;

import ca.uhn.fhir.validation.SingleValidationMessage;

public class ValidacaoItem {
	private StringBuilder result = new StringBuilder();
	private boolean success = false;
	private List<SingleValidationMessage> messages;

	public StringBuilder getResult() {
		return result;
	}

	public void setResult(StringBuilder result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<SingleValidationMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<SingleValidationMessage> messages) {
		this.messages = messages;
	}
}
