package com.yun.yunmaster.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yun.yunmaster.R;
import com.yun.yunmaster.activity.OrderDetailActivity;
import com.yun.yunmaster.adapter.OrderToPickAdapter;
import com.yun.yunmaster.model.EventBusEvent;
import com.yun.yunmaster.model.OrderItem;
import com.yun.yunmaster.network.base.callback.ResponseCallback;
import com.yun.yunmaster.network.base.presenter.RecyclerViewPresenter;
import com.yun.yunmaster.network.base.response.BaseResponse;
import com.yun.yunmaster.network.httpapis.OrderApis;
import com.yun.yunmaster.response.AllOrdersResponse;
import com.yun.yunmaster.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.yun.yunmaster.network.base.presenter.RecyclerViewPresenter.REQUEST_REFRESH;

/**
 * Created by jslsxu on 2018/3/29.
 */

public class HomeOrderListView extends RelativeLayout implements RecyclerViewPresenter.PresenterInterface{
    private Context mContext;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.emptyView)
    LinearLayout emptyView;

    protected OrderToPickAdapter mAdapter;
    protected RecyclerViewPresenter mPresenter;
    protected String start = "";
    public HomeOrderListView(Context context){
        super(context);
        mContext = context;
        init();
    }
    public HomeOrderListView(Context context, AttributeSet attributes){
        super(context, attributes);
        mContext = context;
        init();
    }

    public List<OrderItem> orderPickInfoList(){
        return mAdapter.getData();
    }

    private void init(){
        LayoutInflater.from(mContext).inflate(R.layout.order_list_view, this);
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new OrderToPickAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mPresenter = new RecyclerViewPresenter();
        mPresenter.bind(mRecyclerView, mAdapter, mRefreshLayout, this, true);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                OrderItem orderItem = mAdapter.getItem(position);
                OrderDetailActivity.intentTo(getContext(), orderItem.oid);
            }
        });
        requestData(RecyclerViewPresenter.REQUEST_REFRESH);
    }

    public void receiveNewOrder(OrderItem orderItem){
        mAdapter.addData(0, orderItem);
//        mRecyclerView.smoothScrollToPosition(0);
        mAdapter.notifyDataSetChanged();
        emptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void refresh(){
        requestData(REQUEST_REFRESH);
    }

    @Override
    public void requestData(final int requestType) {
        String from = requestType == REQUEST_REFRESH ? "" : start;
        OrderApis.getAllOrderList(from, new ResponseCallback<AllOrdersResponse>() {
            @Override
            public void onSuccess(AllOrdersResponse baseData) {
                start = baseData.data.next;
                mPresenter.endRequest(requestType, baseData.data.list, baseData.data.hasMore());
                EventBus.getDefault().post(new EventBusEvent.OrderPickDataChangedEvent());
                emptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFail(int statusCode, @Nullable BaseResponse failDate, @Nullable Throwable error) {
                ToastUtil.showToast(failDate.getErrmsg());
                mPresenter.endRequest(requestType);
                emptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
