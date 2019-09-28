package com.tisza.tarock.game;

import com.tisza.tarock.message.*;

public interface ActionButtonItem
{
	public Action getAction();
	public String toString();
	public default void onClicked() {};
}
