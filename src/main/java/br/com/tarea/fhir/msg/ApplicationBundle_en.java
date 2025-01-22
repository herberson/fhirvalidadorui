package br.com.tarea.fhir.msg;

import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationBundle_en extends ListResourceBundle
{
	private Map<String, String> values = new HashMap<>();
	
	@Override
	protected Object[][] getContents() 
	{
		if (values.isEmpty())
		{
			// UI
			values.put(MessagesKeys.windowTitle.name(), "FHIR Validation");
			
			values.put(MessagesKeys.btnSelDir.name(), "Select");
			values.put(MessagesKeys.labelBtnSelDir.name(), "<html><b>Definition files path:</b></html>");
			values.put(MessagesKeys.hintBtnSelDir.name(), "Path where then FHIR profile definition is located.");
			values.put(MessagesKeys.titleBtnSelDir.name(), "Select folder");
			
			values.put(MessagesKeys.btnSelectJsonFile.name(), "Select");
			values.put(MessagesKeys.labelBtnSelectJsonFile.name(), "<html><b>JSON sample file:</b></html>");
			values.put(MessagesKeys.hintBtnSelectJsonFile.name(), "JSON file to validate.");
			values.put(MessagesKeys.titleBtnSelectJsonFile.name(), "Select JSON file");
			
			values.put(MessagesKeys.cmbFhirVersion.name(), "FHIR Version/Release:");
			
			values.put(MessagesKeys.btnExecuteValidation.name(), "Validate");
			
			values.put(MessagesKeys.labelProgressBar.name(), "Verifying...");
			
			values.put(MessagesKeys.txtValidationMessages.name(), "<html><b>Validation Messages</b></html>");
			
			values.put(MessagesKeys.chkScrollLock.name(), "Scroll lock");
			values.put(MessagesKeys.chkLineWrap.name(), "Word wrap");
			values.put(MessagesKeys.chkErrorsOnly.name(), "Errors only");
			
			values.put(MessagesKeys.btnSelectMessagesFile.name(), "Select");
			values.put(MessagesKeys.labelBtnSelectMessagesFile.name(), "<html><b>Messages file:</b></html>");
			values.put(MessagesKeys.hintBtnSelectMessagesFile.name(), "File where the messages will be written.");
			values.put(MessagesKeys.titleBtnSelectMessagesFile.name(), "File chooser");
			
			values.put(MessagesKeys.labelLogPanel.name(), "<html><b>Log</b></html>");
			
			values.put(MessagesKeys.btnHelp.name(), "License");
			
			// ValidarItemCallable
			values.put(MessagesKeys.msgValidationError.name(), "FAIL");
			values.put(MessagesKeys.msgValidationSuccess.name(), "SUCCESS");
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
