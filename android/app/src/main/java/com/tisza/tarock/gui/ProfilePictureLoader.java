package com.tisza.tarock.gui;

import android.graphics.*;
import android.os.*;
import android.widget.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ProfilePictureLoader
{
	private Map<String, Bitmap> urlToBitmap = new HashMap<>();
	private Map<ImageView, ImageDownloadTask> imageViewToTask = new HashMap<>();

	public void cancelDownload(ImageView imageView)
	{
		ImageDownloadTask imageDownloadTask = imageViewToTask.remove(imageView);
		if (imageDownloadTask != null)
			imageDownloadTask.cancel(true);
	}

	public void loadPicture(String url, ImageView imageView)
	{
		Bitmap bitmap = urlToBitmap.get(url);

		if (bitmap == null)
		{
			imageView.setImageDrawable(null);
			ImageDownloadTask imageDownloadTask = new ImageDownloadTask(url, imageView);
			imageDownloadTask.execute();
		}
		else
		{
			imageView.setImageBitmap(bitmap);
		}
	}

	private class ImageDownloadTask extends AsyncTask<Void, Void, Bitmap>
	{
		private String url;
		private ImageView imageView;

		public ImageDownloadTask(String url, ImageView imageView)
		{
			this.url = url;
			this.imageView = imageView;
		}

		@Override
		protected void onPreExecute()
		{
			imageViewToTask.put(imageView, this);
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
			imageViewToTask.remove(imageView);
		}
	}
}
