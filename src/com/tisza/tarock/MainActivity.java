package com.tisza.tarock;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		View v = View.inflate(this, R.layout.activity_main, null);
		TextView tv = (TextView)v.findViewById(R.id.tv);
		tv.setText("jjj");
		setContentView(v);
		
	}
}
