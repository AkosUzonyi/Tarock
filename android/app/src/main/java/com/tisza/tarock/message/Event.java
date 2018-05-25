package com.tisza.tarock.message;

public interface Event
{
	public void handle(EventHandler handler);
}
