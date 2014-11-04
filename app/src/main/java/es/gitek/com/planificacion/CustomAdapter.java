package es.gitek.com.planificacion;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ikerib on 29/10/14.
 */
public class CustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> data;

    public CustomAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.data = data;
        this.context=context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.of_list_item, null);
            holder.txtRef = (TextView) view.findViewById(R.id.ref);
            holder.txtOf = (TextView) view.findViewById(R.id.of);
            holder.txtAmaituta = (TextView) view.findViewById(R.id.amaituta);
            holder.txtDenbora = (TextView) view.findViewById(R.id.denbora);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.txtRef.setText(data.get(position).get("ref"));
        holder.txtOf.setText(data.get(position).get("of"));
        holder.txtAmaituta.setText(data.get(position).get("amaituta"));

        if ( data.get(position).get("denbora").equals("0") ) {
            holder.txtDenbora.setVisibility(View.GONE);
        } else {
            holder.txtDenbora.setText("Orduak : ".concat( data.get(position).get("denbora") ));
        }

        if (data.get(position).get("amaituta").equals("1")) {
            holder.txtRef.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtOf.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
//        else {
//            holder.txtAmaituta.setTypeface(null, Typeface.NORMAL);
//        }
        return view;

    }

    class ViewHolder{
        TextView txtRef;
        TextView txtOf;
        TextView txtAmaituta;
        TextView txtDenbora;
    }

}