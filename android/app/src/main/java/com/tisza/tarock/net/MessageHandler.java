package com.tisza.tarock.net;

import com.tisza.tarock.proto.MainProto.*;

public interface MessageHandler
{
	public void handleMessage(Message message);
	public void connectionError(ErrorType errorType);
	public void connectionClosed();

	public static enum ErrorType
	{
		VERSION_MISMATCH, INVALID_HELLO
	}
}
