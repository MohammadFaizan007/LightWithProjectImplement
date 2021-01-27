package com.inferrix.db.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inferrix.db.R;
import com.inferrix.db.adapter.AvailableProjectAdapter;
import com.inferrix.db.databaseModules.SqlHelper;
import com.inferrix.db.pogoClasses.Project;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvailableProject extends AppCompatActivity {
    @BindView(R.id.recyclerViewUsers)
    RecyclerView recyclerViewUsers;
    @BindView(R.id.create_project)
    Button createProject;
    private List<Project> listUsers;
    private SqlHelper databaseHelper;
    private AvailableProjectAdapter availableProjectAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.available_project_list );
        ButterKnife.bind ( this );
        getSupportActionBar().setTitle("Projects");
        initViews();
        initObjects();
        createProject.setOnClickListener (v -> {
            Intent projects = new Intent ( AvailableProject.this, CreateProjectActivity.class );
            startActivity ( projects );
        });
    }
    private void initViews() {
        recyclerViewUsers = (RecyclerView) findViewById ( R.id.recyclerViewUsers );
        createProject = (Button) findViewById ( R.id.create_project );
    }
    private void initObjects() {
        listUsers = new ArrayList<> ();
        availableProjectAdapter = new AvailableProjectAdapter (this,listUsers);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager (getApplicationContext());
        recyclerViewUsers.setLayoutManager(mLayoutManager);
        recyclerViewUsers.setItemAnimator(new DefaultItemAnimator ());
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setAdapter(availableProjectAdapter);
        databaseHelper = new SqlHelper (AvailableProject.this);

//        String emailFromIntent = getIntent().getStringExtra("EMAIL");
//        textViewName.setText(emailFromIntent);

        getDataFromSQLite();
    }

    private void getDataFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... params) {
                listUsers.clear();
                listUsers.addAll(databaseHelper.getAllProject());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                availableProjectAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
