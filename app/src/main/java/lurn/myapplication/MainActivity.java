package lurn.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Integer> data;
    private RecyclerView rvTantan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rvTantan = (RecyclerView) findViewById(R.id.rv_tantan);
        data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add(i);
        }
        CardAdapter mAdapter = new CardAdapter();
        rvTantan.setAdapter(mAdapter);
        rvTantan.setLayoutManager(new MyLayoutManager());
    }

    class CardAdapter extends RecyclerView.Adapter<Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false);
            return new Holder(inflate);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            int index = position % 3;
            switch (index) {
                case 0:
                    holder.iv.setImageResource(R.drawable.bg1);
                    break;
                case 1:
                    holder.iv.setImageResource(R.drawable.bg2);
                    break;
                case 2:
                    holder.iv.setImageResource(R.drawable.bg3);
                    break;
            }
            holder.tv.setText("这是" + position);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;

        public Holder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
            tv = itemView.findViewById(R.id.tv);
        }
    }


}
