package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.tisza.tarock.*;
import com.tisza.tarock.server.*;

public class ServerActivity extends Activity
{
	private EditText portField;
	private EditText name0Field;
	private EditText name1Field;
	private EditText name2Field;
	private EditText name3Field;
	private Button startButton;
	
	private boolean running = false;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
		portField = (EditText)findViewById(R.id.port_field_server);
		name0Field = (EditText)findViewById(R.id.name0_field);
		name1Field = (EditText)findViewById(R.id.name1_field);
		name2Field = (EditText)findViewById(R.id.name2_field);
		name3Field = (EditText)findViewById(R.id.name3_field);
		startButton = (Button)findViewById(R.id.start_button);
		
		SharedPreferences sp = getSharedPreferences("server", Context.MODE_PRIVATE);
		name0Field.setText(sp.getString("name0", ""));
		name1Field.setText(sp.getString("name1", ""));
		name2Field.setText(sp.getString("name2", ""));
		name3Field.setText(sp.getString("name3", ""));
		int port = sp.getInt("port", -1);
		portField.setText(port < 0 ? "" : port + "");
		
		startButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				int port = Integer.parseInt(portField.getText().toString());
				String name0 = name0Field.getText().toString();
				String name1 = name1Field.getText().toString();
				String name2 = name2Field.getText().toString();
				String name3 = name3Field.getText().toString();
				
				SharedPreferences sp = getSharedPreferences("server", Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("name0", name0);
				editor.putString("name1", name1);
				editor.putString("name2", name2);
				editor.putString("name3", name3);
				editor.putInt("port", port);
				editor.commit();
				
				Intent serviceIntent = new Intent(ServerActivity.this, ServerService.class);
				if (!running)
				{
					serviceIntent.putExtra("port", port);
					serviceIntent.putExtra("name0", name0);
					serviceIntent.putExtra("name1", name1);
					serviceIntent.putExtra("name2", name2);
					serviceIntent.putExtra("name3", name3);
					startService(serviceIntent);
					startButton.setText(R.string.server_stop);
					running = true;
				}
				else
				{
					stopService(serviceIntent);
					startButton.setText(R.string.server_start);
					running = false;
				}
			}
		});
	}
}
