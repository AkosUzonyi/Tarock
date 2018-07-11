package com.tisza.tarock.gui;

import android.graphics.*;
import android.os.*;
import android.widget.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ProfilePictureLoader
{
	private ConcurrentMap<String, Bitmap> urlToBitmap = new ConcurrentHashMap<>();

	public void loadPictre(String url, ImageView imageView)
	{
		Bitmap bitmap = urlToBitmap.get(url);

		if (bitmap == null)
		{
			new ImageDonwloadTask(url, imageView).execute();
		}
		else
		{
			imageView.setImageBitmap(bitmap);
		}
	}

	private class ImageDonwloadTask extends AsyncTask<Void, Void, Bitmap>
	{
		private String url;
		private ImageView imageView;

		public ImageDonwloadTask(String url, ImageView imageView)
		{
			this.url = url;
			this.imageView = imageView;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Void... voids)
		{
			try
			{
				HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				return BitmapFactory.decodeStream(input);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			if (bitmap != null)
			{
				imageView.setImageBitmap(bitmap);
				urlToBitmap.put(url, bitmap);
			}
		}
	}
}
