package com.tisza.tarock.message;

public interface Action
{
	public void handle(ActionHandler handler);
}
