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
	private static final User USER_ANYBODY = new User(0, "", null, false, false);

	private int actionButtonImageRes;
	private int fixedLength;
	private Picasso picasso;
	private LayoutInflater inflater;
	private List<User> users = new ArrayList<>();
	private UserClickedListener userClickedListener;

	public UsersAdapter(Context context, int actionButtonImageRes)
	{
		this(context, actionButtonImageRes, -1);
	}

	public UsersAdapter(Context context, int actionButtonImageRes, int fixedLength)
	{
		this.actionButtonImageRes = actionButtonImageRes;
		this.fixedLength = fixedLength;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		picasso = Picasso.with(context);
	}

	public void setUsersSelectedListener(UserClickedListener userClickedListener)
	{
		this.userClickedListener = userClickedListener;
	}

	public void setUsers(List<User> users)
	{
		this.users = users;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount()
	{
		return fixedLength >= 0 ? fixedLength : users.size();
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
		holder.selectUser = view.findViewById(R.id.select_user);

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

		if (user == USER_ANYBODY)
			holder.profilePictureView.setImageResource(R.drawable.fb_unknown_image);
		else if (user.isBot())
			holder.profilePictureView.setImageResource(R.drawable.bot);
		else
			picasso.load(user.getImageURL()).error(R.drawable.fb_unknown_image).into(holder.profilePictureView);

		holder.isFriendView.setVisibility(user.isFriend() ? View.VISIBLE : View.GONE);
		holder.isOnlineView.setImageResource(user.isOnline() ? R.drawable.online : R.drawable.offline);

		holder.selectUser.setVisibility(user != USER_ANYBODY ? View.VISIBLE : View.GONE);
		holder.selectUser.setImageResource(actionButtonImageRes);
		holder.selectUser.setOnClickListener(v ->
		{
			if (userClickedListener != null)
				userClickedListener.userClicked(user);
		});
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView profilePictureView;
		public TextView nameView;
		public View isFriendView;
		public ImageView isOnlineView;
		public ImageView selectUser;

		public ViewHolder(View view)
		{
			super(view);
		}
	}

	public interface UserClickedListener
	{
		void userClicked(User users);
	}
}
