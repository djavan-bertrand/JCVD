package com.sousoum.jcvdexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sousoum.jcvd.StorableFenceManager;

/**
 * Created by d.bertrand on 13/07/2016.
 */
public class FenceListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private StorableFenceManager mFenceManager;
    private FenceRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mFenceManager = new StorableFenceManager(this);

        mAdapter = new FenceRecyclerAdapter(this, mFenceManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
