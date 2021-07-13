package com.tisza.tarock.api.model;

public class CreateGameSessionDTO
{
	public String type;
	public String doubleRoundType;

	public CreateGameSessionDTO(String type, String doubleRoundType)
	{
		this.type = type;
		this.doubleRoundType = doubleRoundType;
	}
}
