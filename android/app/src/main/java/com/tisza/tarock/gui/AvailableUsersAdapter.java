package com.tisza.tarock.gui;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.tisza.tarock.*;

import java.util.*;

public class AvailableUsersAdapter extends BaseAdapter
{
	private final ProfilePictureLoader profilePictureLoader = new ProfilePictureLoader();

	private final LayoutInflater inflater;
	private List<User> users = new ArrayList<>();
	private List<User> selectedUsers = new ArrayList<>();

	public AvailableUsersAdapter(Context context)
	{
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setUsers(Collection<User> newUsers)
	{
		users = new ArrayList<>(newUsers);
		Collections.sort(users);
		selectedUsers.retainAll(users);
		notifyDataSetChanged();
	}

	public List<User> getSelectedUsers()
	{
		return selectedUsers;
	}

	public void clearSelectedUsers()
	{
		selectedUsers.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return users.size();
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
			holder.checkBox = view.findViewById(R.id.user_checkbox);

			view.setTag(holder);
		}
		else
		{
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}

		User user = users.get(position);

		holder.nameView.setText(user.getName());

		if (user.getImageURL() != null)
			profilePictureLoader.loadPictre(user.getImageURL(), holder.profilePictureView);

		holder.isFriendView.setVisibility(user.isFriend() ? View.VISIBLE : View.GONE);

		holder.checkBox.setChecked(selectedUsers.contains(user));
		holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
		{
			if (isChecked)
			{
				selectedUsers.add(user);
			}
			else
			{
				selectedUsers.remove(user);
			}
		});

		return view;
	}

	private static class ViewHolder
	{
		public ImageView profilePictureView;
		public TextView nameView;
		public View isFriendView;
		public CheckBox checkBox;
	}
}
