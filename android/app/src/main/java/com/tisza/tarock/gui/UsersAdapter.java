package com.tisza.tarock.gui;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.squareup.picasso.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>
{
	private static final User USER_ANYBODY = new User(0, "", null, false, false, false);

	private int minimumLength;
	private boolean imageVisible = true;
	private Picasso picasso;
	private LayoutInflater inflater;
	private List<User> users = new ArrayList<>();

	public UsersAdapter(Context context)
	{
		this(context, 0);
	}

	public UsersAdapter(Context context, int minimumLength)
	{
		this.minimumLength = minimumLength;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		picasso = Picasso.with(context);
	}

	public void setImageVisible(boolean imageVisible)
	{
		this.imageVisible = imageVisible;
	}

	public void setUsers(List<User> users)
	{
		this.users = users;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount()
	{
		return Math.max(minimumLength, users.size());
	}

	private User getUserAtPosition(int position)
	{
		return position >= users.size() ? USER_ANYBODY : users.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return getUserAtPosition(position).getId();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = inflater.inflate(R.layout.user, parent, false);

		ViewHolder holder = new ViewHolder(view);
		holder.profilePictureView = view.findViewById(R.id.user_image);
		holder.nameView = view.findViewById(R.id.user_name);
		holder.isFriendView = view.findViewById(R.id.is_user_friend);
		holder.isOnlineView = view.findViewById(R.id.is_user_online);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		User user = getUserAtPosition(position);

		if (user == USER_ANYBODY)
		{
			holder.nameView.setText(R.string.anybody);
			holder.nameView.setAlpha(0.5F);
			holder.nameView.setTypeface(null, Typeface.ITALIC);
		}
		else
		{
			holder.nameView.setText(user.getName());
			holder.nameView.setAlpha(1F);
			holder.nameView.setTypeface(null, Typeface.NORMAL);
		}

		holder.profilePictureView.setVisibility(imageVisible ? View.VISIBLE : View.GONE);

		if (user == USER_ANYBODY)
			holder.profilePictureView.setImageResource(R.drawable.fb_unknown_image);
		else if (user.isBot())
			holder.profilePictureView.setImageResource(R.drawable.bot);
		else
			picasso.load(user.getImageURL()).error(R.drawable.fb_unknown_image).into(holder.profilePictureView);

		holder.isFriendView.setVisibility(user.isFriend() ? View.VISIBLE : View.GONE);

		holder.isOnlineView.setVisibility(user.isBot() ? View.GONE : View.VISIBLE);
		holder.isOnlineView.setImageResource(user.isOnline() ? R.drawable.online : R.drawable.offline);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView profilePictureView;
		public TextView nameView;
		public View isFriendView;
		public ImageView isOnlineView;

		public ViewHolder(View view)
		{
			super(view);
		}
	}
}
