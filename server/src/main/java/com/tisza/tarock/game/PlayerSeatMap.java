package com.tisza.tarock.game;

import java.util.*;
import java.util.stream.*;

public class PlayerSeatMap<V> implements Map<PlayerSeat, V>, Iterable<V>
{
	private static final Set<PlayerSeat> KEY_SET = Collections.unmodifiableSet(Arrays.stream(PlayerSeat.getAll()).collect(Collectors.toSet()));

	@SuppressWarnings("unchecked")
	private final V[] values = (V[])new Object[4];

	public PlayerSeatMap()
	{
		this(null);
	}

	public PlayerSeatMap(V initValue)
	{
		fill(initValue);
	}

	public void fill(V value)
	{
		Arrays.fill(values, value);
	}

	@Override
	public int size()
	{
		return 4;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public boolean containsKey(Object key)
	{
		return key instanceof PlayerSeat;
	}

	@Override
	public boolean containsValue(Object value)
	{
		for (Object v : values)
		{
			if (v.equals(value))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(Object key)
	{
		if (key instanceof PlayerSeat)
			return values[((PlayerSeat)key).asInt()];

		return null;
	}

	@Override
	public V put(PlayerSeat key, V value)
	{
		V prev = values[key.asInt()];
		values[key.asInt()] = value;
		return prev;
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends PlayerSeat, ? extends V> m)
	{
		m.forEach(this::put);
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<PlayerSeat> keySet()
	{
		return KEY_SET;
	}

	@Override
	public Collection<V> values()
	{
		return Arrays.asList(values);
	}

	@Override
	public Set<Entry<PlayerSeat, V>> entrySet()
	{
		Set<Entry<PlayerSeat, V>> result = new HashSet<>();
		for (int i = 0; i < 4; i++)
		{
			result.add(new AbstractMap.SimpleEntry<>(PlayerSeat.fromInt(i), values[i]));
		}
		return result;
	}

	@Override
	public Iterator<V> iterator()
	{
		return Arrays.asList(values).iterator();
	}
}
