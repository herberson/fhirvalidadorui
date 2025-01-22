package br.com.tarea.fhir.msg;

import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationBundle extends ListResourceBundle
{
	private Map<String, String> values = new HashMap<>();
	
	@Override
	protected Object[][] getContents() 
	{
		if (values.isEmpty())
		{
			// UI
			values.put(MessagesKeys.windowTitle.name(), "Validação FHIR");
			
			values.put(MessagesKeys.btnSelDir.name(), "Selecionar");
			values.put(MessagesKeys.labelBtnSelDir.name(), "<html><b>Local arquivos definição:</b></html>");
			values.put(MessagesKeys.hintBtnSelDir.name(), "Local onde estão os arquivos de definição do profile.");
			values.put(MessagesKeys.titleBtnSelDir.name(), "Selecionar diretório/pasta");
			
			values.put(MessagesKeys.btnSelectJsonFile.name(), "Selecionar");
			values.put(MessagesKeys.labelBtnSelectJsonFile.name(), "<html><b>Arquivo JSON exemplo:</b></html>");
			values.put(MessagesKeys.hintBtnSelectJsonFile.name(), "Arquivo JSON a ser validado.");
			values.put(MessagesKeys.titleBtnSelectJsonFile.name(), "Selecionar arquivo JSON");
			
			values.put(MessagesKeys.cmbFhirVersion.name(), "<html>Versão (<i>release</i>) do FHIR:</html>");
			
			values.put(MessagesKeys.btnExecuteValidation.name(), "Validar");
			
			values.put(MessagesKeys.labelProgressBar.name(), "Verificando...");
			
			values.put(MessagesKeys.txtValidationMessages.name(), "<html><b>Mensagens Validação</b></html>");
			
			values.put(MessagesKeys.chkScrollLock.name(), "Congelar barra de rolagem");
			values.put(MessagesKeys.chkLineWrap.name(), "Quebra de linha");
			values.put(MessagesKeys.chkErrorsOnly.name(), "Apenas falhas");
			
			values.put(MessagesKeys.btnSelectMessagesFile.name(), "Selecionar");
			values.put(MessagesKeys.labelBtnSelectMessagesFile.name(), "<html><b>Arquivo de mensagens:</b></html>");
			values.put(MessagesKeys.hintBtnSelectMessagesFile.name(), "Arquivo para gravação das mensagens apresentadas.");
			values.put(MessagesKeys.titleBtnSelectMessagesFile.name(), "Escolher arquivo");
			
			values.put(MessagesKeys.labelLogPanel.name(), "<html><b>Log</b></html>");
			
			values.put(MessagesKeys.btnHelp.name(), "Licença");
			
			// ValidarItemCallable
			values.put(MessagesKeys.msgValidationError.name(), "FALHA");
			values.put(MessagesKeys.msgValidationSuccess.name(), "SUCESSO");
		}
		
		final int size = values.size();
		final Object[][] rb = new Object[size][2];
		
		final AtomicInteger ix = new AtomicInteger(0);
		
		values.entrySet().forEach(entry -> 
		{
			rb[ix.get()][0] = entry.getKey();
			rb[ix.get()][1] = entry.getValue();
			ix.incrementAndGet();
		});
		
		return rb;
	}
}
