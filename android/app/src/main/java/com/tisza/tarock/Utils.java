package com.tisza.tarock;

import java.util.*;

public class Utils
{
	public static <T0, T1> List<T1> map(List<T0> list, Function<T0, T1> f)
	{
		List<T1> result = new ArrayList<>();
		for (T0 t : list)
			result.add(f.apply(t));
		return result;
	}

	public static boolean equals(Object o0, Object o1)
	{
		if (o0 == null)
			return o1 == null;

		return o0.equals(o1);
	}

	public interface Function<T0, T1>
	{
		T1 apply(T0 param);
	}
}
