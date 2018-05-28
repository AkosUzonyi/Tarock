package com.tisza.tarock;

public class SentenceBuilder
{
	private StringBuilder stringBuilder = new StringBuilder();

	public void appendWord(String word)
	{
		if (word.isEmpty())
			return;

		if (stringBuilder.length() == 0)
		{
			stringBuilder.append(word.substring(0, 1).toUpperCase());
			stringBuilder.append(word.substring(1));
		}
		else
		{
			stringBuilder.append(" ");
			stringBuilder.append(word);
		}
	}

	@Override
	public String toString()
	{
		return stringBuilder.toString();
	}
}
