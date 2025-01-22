package br.com.tarea.fhir.msg;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class Messages
{
	private static final Logger logger = Logger.getLogger(Messages.class);
	
	private static ResourceBundle bundle;
	
	public static String getKey(final MessagesKeys key)
	{
		if (Objects.isNull(bundle))
		{
			try
			{
				logger.info(Locale.getDefault());
				bundle = ResourceBundle.getBundle("br.com.tarea.fhir.msg.ApplicationBundle", Locale.getDefault());
			}
			catch (MissingResourceException ex)
			{
				logger.error(ex.getMessage());;
			}
		}
		
		return bundle.getString(key.name());
	}
}
