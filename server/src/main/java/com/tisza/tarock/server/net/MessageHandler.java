package com.tisza.tarock.server.net;

import com.tisza.tarock.proto.MainProto.*;

public interface MessageHandler
{
	void handleMessage(Message message);
	void connectionClosed();
}
