package com.example.rama.androidtut.UtilityClasses;

/**
 * Created by Ramona on 02/01/2017.
 */


        import android.content.Context;
        import android.graphics.Color;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import com.example.rama.androidtut.R;

        import java.util.List;

public class ListItemAdapter extends BaseAdapter {

    private  Context mContext;
    private LayoutInflater inflater;
    private List<ListItem> itemsItems;



    public ListItemAdapter(Context context, List<ListItem> itemsItems) {
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
    public View getView(int position, View twoFieldsView, ViewGroup parent) {
        ViewHolder holder;
        if (inflater == null) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (twoFieldsView == null) {

            twoFieldsView = inflater.inflate(R.layout.list_row, parent, false);
            holder = new ViewHolder();
            holder.fieldOne = (TextView) twoFieldsView.findViewById(R.id.topname);
            holder.fieldTwo = (TextView) twoFieldsView.findViewById(R.id.topscore);

            twoFieldsView.setTag(holder);

        } else {
            holder = (ViewHolder) twoFieldsView.getTag();
        }

        final ListItem m = itemsItems.get(position);
        holder.fieldOne.setText(m.getFieldOne().toString());

        holder.fieldTwo.setText(String.valueOf(m.getFieldTwo()));
        return twoFieldsView;
    }

    static class ViewHolder {

        TextView fieldOne;
        TextView fieldTwo;

    }

}
