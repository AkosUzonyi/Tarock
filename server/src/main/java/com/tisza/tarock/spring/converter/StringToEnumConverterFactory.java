package com.tisza.tarock.spring.converter;

import org.springframework.core.convert.converter.*;

public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>>
{
	@Override
	public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType)
	{
		return new StringToEnumConverter(targetType);
	}

	@SuppressWarnings("rawtypes")
	private static class StringToEnumConverter<T extends Enum> implements Converter<String, T>
	{
		private final Class<T> enumType;

		public StringToEnumConverter(Class<T> enumType)
		{
			this.enumType = enumType;
		}

		@SuppressWarnings("unchecked")
		public T convert(String source)
		{
			return (T) Enum.valueOf(enumType, source);
		}
	}
}
