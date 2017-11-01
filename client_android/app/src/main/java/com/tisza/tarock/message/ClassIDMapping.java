package com.tisza.tarock.message;

import java.util.HashMap;
import java.util.Map;

class ClassIDMapping<T>
{
	private final Map<Byte, Class<? extends T>> idToClass = new HashMap<Byte, Class<? extends T>>();
	private final Map<Class<? extends T>, Byte> classToID = new HashMap<Class<? extends T>, Byte>();
	
	public void register(int id, Class<? extends T> cls)
	{
		if (id < Byte.MIN_VALUE || id > Byte.MAX_VALUE)
			throw new IllegalArgumentException();
		
		register((byte)id, cls);
	}
		
	public void register(byte id, Class<? extends T> cls)
	{
		if (idToClass.containsKey(id) || classToID.containsKey(cls))
			throw new IllegalArgumentException();
		
		idToClass.put(id, cls);
		classToID.put(cls, id);
	}
	
	public byte getID(T obj)
	{
		return classToID.get(obj.getClass());
	}
	
	public T createFromID(byte id)
	{
		Class<? extends T> cls = idToClass.get(id);
		T obj;
		try
		{
			obj = cls.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return obj;
	}
}
