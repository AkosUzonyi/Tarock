package com.tisza.tarock.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.tisza.tarock.R;

public class ClientActivity extends Activity
{
	private Button connectButton;
	private EditText nameField;
	private EditText hostField;
	private EditText portField;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		connectButton = (Button)findViewById(R.id.start_button);
		nameField = (EditText)findViewById(R.id.name_field);
		hostField = (EditText)findViewById(R.id.host_field);
		portField = (EditText)findViewById(R.id.port_field);
		
		SharedPreferences sp = getSharedPreferences("client", Context.MODE_PRIVATE);
		nameField.setText(sp.getString("name", ""));
		hostField.setText(sp.getString("host", ""));
		int port = sp.getInt("port", -1);
		portField.setText(port < 0 ? "" : port + "");
		
		connectButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				String name = nameField.getText().toString();
				String host = hostField.getText().toString();
				int port = Integer.parseInt(portField.getText().toString());
				
				SharedPreferences sp = getSharedPreferences("client", Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("name", name);
				editor.putString("host", host);
				editor.putInt("port", port);
				editor.commit();
				
				Intent gameIntent = new Intent(ClientActivity.this, GameActivtiy.class);
				gameIntent.putExtra("host", host);
				gameIntent.putExtra("port", port);
				gameIntent.putExtra("name", name);
				startActivity(gameIntent);
			}
		});
	}
}
