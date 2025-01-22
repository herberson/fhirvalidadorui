package br.com.tarea.fhir;

import java.util.Objects;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public final class FhirCtx 
{
	private static FhirContext context = null;
	
	public static FhirContext current()
	{
		if (Objects.isNull(context))
		{
			context = useR4();
		}
		return context;
	}
	
	public static FhirContext use(final String version)
	{
		context = FhirContext.forCached(FhirVersionEnum.forVersionString(version));
		return context;
	}
	
	public static FhirContext useR4()
	{
		context = FhirContext.forCached(FhirVersionEnum.R4);
		return context;
	}
	
	public static FhirContext useR4B()
	{
		context = FhirContext.forCached(FhirVersionEnum.R4B);
		return context;
	}

	public static FhirContext useR5()
	{
		context = FhirContext.forCached(FhirVersionEnum.R5);
		return context;
	}
	
}
