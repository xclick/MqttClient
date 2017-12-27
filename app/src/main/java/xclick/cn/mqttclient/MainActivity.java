package xclick.cn.mqttclient;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import xclick.cn.mqttclient.model.CartInfo;
import xclick.cn.mqttclient.service.MyMqttService;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView ;
    private MyRecyclerViewAdapter recyclerViewAdapter ;
    private MyMqttService myMqttService ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new MyRecyclerViewAdapter(MainActivity.this);
        recyclerViewAdapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(CartInfo cartInfo) {
                if(!myMqttService.sendTurnOnRequest(cartInfo)){
                    Toast.makeText(MainActivity.this, "开灯指令发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);

        MyMqttService.OnMessageArrived onMessageArrived = new MyMqttService.OnMessageArrived(){
            @Override
            public void messageArrived(String topic, String messagePayload){
                if("factory/cart/info".equalsIgnoreCase(topic)){
                    if(messagePayload!=null&&messagePayload.length()>0){
                        String[] ss = messagePayload.split("\\|");
                        if(ss.length>=3){
                            CartInfo cartInfo = new CartInfo();
                            cartInfo.setId(ss[0]);
                            cartInfo.setName(ss[1]);
                            cartInfo.setStatus(ss[2]);
                            if(recyclerViewAdapter.AddCartInfo(cartInfo)){
                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        };


        this.myMqttService = new MyMqttService(MainActivity.this,onMessageArrived);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
            case R.id.refresh_carts:
                recyclerViewAdapter.Clear();
                if(!this.myMqttService.sendCartInfoRequest()){
                    Toast.makeText(this, "刷新小车列表发生错误", Toast.LENGTH_SHORT).show();
                }
                /*
                {
                    CartInfo cartInfo = new CartInfo();
                    cartInfo.setName("192.168.0.101");
                    cartInfo.setId("192.168.0.101");
                    cartInfo.setStatus("N");
                    recyclerViewAdapter.AddCartInfo(cartInfo);
                }

                {
                    CartInfo cartInfo = new CartInfo();
                    cartInfo.setName("192.168.0.102");
                    cartInfo.setId("192.168.0.102");
                    cartInfo.setStatus("N");
                    recyclerViewAdapter.AddCartInfo(cartInfo);
                }

                recyclerViewAdapter.notifyDataSetChanged();
                */
                //Toast.makeText(this, "刷新小车列表", Toast.LENGTH_SHORT).show();
                break ;
        }
        return true ;
    }



    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>{
        private List<CartInfo> cartInfoList ;
        private Context context ;
        private OnItemClickListener onItemClickListener ;

        public MyRecyclerViewAdapter(Context context) {
            this.context = context;
            this.cartInfoList = new ArrayList<CartInfo>();
        }

        public void Clear(){
            this.cartInfoList.clear();
        }

        public boolean AddCartInfo(CartInfo cartInfo){
            if(cartInfo==null) return false ;
            for(CartInfo c : cartInfoList){
                if(c.getId().equalsIgnoreCase(cartInfo.getId())){
                    c.setStatus(cartInfo.getStatus());
                    return true ;
                }
            }
            this.cartInfoList.add(cartInfo);
            return true ;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card, null);
            MyViewHolder viewHolder = new MyViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            if(this.cartInfoList==null) return ;
            if(i<0 || i>=this.cartInfoList.size()) return ;
            final CartInfo cartInfo = this.cartInfoList.get(i);
            myViewHolder.cartNameView.setText(cartInfo.getName());
            if("Y".equalsIgnoreCase(cartInfo.getStatus()))
                myViewHolder.cartStatusView.setText("Opened");
            else
                myViewHolder.cartStatusView.setText("Closed");

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(cartInfo);
                }
            };
            myViewHolder.cartNameView.setOnClickListener(listener);
            myViewHolder.cartStatusView.setOnClickListener(listener);
        }


        @Override
        public int getItemCount() {
            if(this.cartInfoList!=null) return this.cartInfoList.size() ;
            return 0 ;
        }

        public OnItemClickListener getOnItemClickListener(){
            return this.onItemClickListener ;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener){
            this.onItemClickListener = onItemClickListener ;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            protected  TextView cartNameView ;
            protected TextView cartStatusView;

            public MyViewHolder(View view) {
                super(view);
                this.cartNameView = (TextView)view.findViewById(R.id.cart_name);
                this.cartStatusView = (TextView)view.findViewById(R.id.cart_status);
            }
        }
    }


    public interface OnItemClickListener{
        void onItemClick(CartInfo cartInfo);
    }
}
