package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> Friends;
    private LayoutInflater inflater;

    public FriendAdapter(Context context, List<Friend> buddies) {
        this.context = context;
        this.Friends = buddies;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return Friends.size();
    }

    @Override
    public Object getItem(int position) {
        return Friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_friend, parent, false);
            holder = new ViewHolder();
            holder.nameText = convertView.findViewById(R.id.buddy_name);
            holder.workoutText = convertView.findViewById(R.id.buddy_workout);
            holder.frequencyText = convertView.findViewById(R.id.buddy_frequency);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Friend buddy = Friends.get(position);

        holder.nameText.setText(buddy.getDisplayName());
        holder.workoutText.setText("Workout: " + buddy.getWorkout());
        holder.frequencyText.setText("Weekly workouts: " + buddy.getWorkoutFrequency());

        return convertView;
    }

    private static class ViewHolder {
        TextView nameText;
        TextView workoutText;
        TextView frequencyText;
    }
}