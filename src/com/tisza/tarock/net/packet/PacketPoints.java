package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.server.*;

public class PacketPoints extends Packet
{
	private Points.Entry points;
	
	PacketPoints() {}
	
	public PacketPoints(Points.Entry points)
	{
		this.points = points;
	}

	public Points.Entry getPoints()
	{
		return points;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int[] pointsArray = new int[4];
		for (int i = 0; i < 4; i++)
		{
			pointsArray[i] = dis.readInt();
		}
		points = new Points.Entry(pointsArray);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		for (int i = 0; i < 4; i++)
		{
			dos.writeInt(points.getPoint(i));
		}
	}
}
