package com.tisza.tarock.net;

import com.tisza.tarock.proto.MainProto.*;

public interface MessageHandler
{
	public void handleMessage(Message message);
	public void connectionClosed();
}
