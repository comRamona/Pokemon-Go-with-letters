package com.example.rama.androidtut.UtilityClasses;

/**
 * Created by Ramona on 02/01/2017.
 */


        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import com.example.rama.androidtut.R;

        import java.util.List;

public class LeaderboardListAdapter extends BaseAdapter {

    private  Context mContext;
    private LayoutInflater inflater;
    private List<ScoreItem> itemsItems;



    public LeaderboardListAdapter(Context context, List<ScoreItem> itemsItems) {
        this.mContext = context;
        this.itemsItems = itemsItems;

    }

    @Override
    public int getCount() {
        return itemsItems.size();
    }

    @Override
    public Object getItem(int location) {
        return itemsItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View scoreView, ViewGroup parent) {
        ViewHolder holder;
        if (inflater == null) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (scoreView == null) {

            scoreView = inflater.inflate(R.layout.list_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) scoreView.findViewById(R.id.topname);
            holder.score = (TextView) scoreView.findViewById(R.id.topscore);

            scoreView.setTag(holder);

        } else {
            holder = (ViewHolder) scoreView.getTag();
        }

        final ScoreItem m = itemsItems.get(position);
        holder.name.setText(m.getName());
        holder.score.setText(String.valueOf(m.getScore()));

        return scoreView;
    }

    static class ViewHolder {

        TextView name;
        TextView score;

    }

}
