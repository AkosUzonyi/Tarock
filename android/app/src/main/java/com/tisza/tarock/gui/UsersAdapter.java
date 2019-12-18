package com.tisza.tarock.gui;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.squareup.picasso.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class UsersAdapter extends BaseAdapter
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

	public void setUsers(Collection<User> newUsers)
	{
		users = new ArrayList<>(newUsers);
		Collections.sort(users);
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return fixedLength >= 0 ? fixedLength : users.size();
	}

	@Override
	public Object getItem(int position)
	{
		return users.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view;
		ViewHolder holder;

		if (convertView == null)
		{
			view = inflater.inflate(R.layout.user, parent, false);

			holder = new ViewHolder();
			holder.profilePictureView = view.findViewById(R.id.user_image);
			holder.nameView = view.findViewById(R.id.user_name);
			holder.isFriendView = view.findViewById(R.id.is_user_friend);
			holder.isOnlineView = view.findViewById(R.id.is_user_online);
			holder.selectUser = view.findViewById(R.id.select_user);

			view.setTag(holder);
		}
		else
		{
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}

		User user = position >= users.size() ? USER_ANYBODY : users.get(position);

		holder.nameView.setText(user.getName());
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

		return view;
	}

	private static class ViewHolder
	{
		public ImageView profilePictureView;
		public TextView nameView;
		public View isFriendView;
		public ImageView isOnlineView;
		public ImageView selectUser;
	}

	public interface UserClickedListener
	{
		void userClicked(User users);
	}
}
