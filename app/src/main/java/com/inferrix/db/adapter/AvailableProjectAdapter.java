package com.inferrix.db.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.inferrix.db.MainActivity;
import com.inferrix.db.R;
import com.inferrix.db.fragment.EditEnterGroupActivity;
import com.inferrix.db.pogoClasses.Project;

import java.util.List;


public class AvailableProjectAdapter extends RecyclerView.Adapter<AvailableProjectAdapter.UserViewHolder> {
    private List<Project> listUsers;
    private Context mContext;

    public AvailableProjectAdapter(Context context, List<Project> listUsers) {
        this.listUsers = listUsers;
        mContext = context;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflating recycler item view
        View itemView = LayoutInflater.from (parent.getContext ())
                .inflate (R.layout.available_project_adapter, parent, false);
        return new UserViewHolder (itemView);

    }


    @Override
    public void onBindViewHolder(@NonNull AvailableProjectAdapter.UserViewHolder holder, int position) {
        holder.textViewName.setText (listUsers.get (position).getProjectNname ());
//        Log.e ("ADAPTER_TIME=====>",listUsers.get (position).getDateTime ());


        holder.enter.setOnClickListener (v -> {
            Intent accountsIntent = new Intent (mContext, MainActivity.class);
            accountsIntent.putExtra ("project", listUsers.get (position));
            mContext.startActivity (accountsIntent);
        });
        holder.edit.setOnClickListener (v -> {
            Intent intent = new Intent(mContext, EditEnterGroupActivity.class);
//            intent.putExtra(Constants.MAIN_KEY, Constants.MY_NETWORK_CODE);
            intent.putExtra("project",listUsers.get (position));
//            intent.putExtra ("project", listUsers.get (position));
            mContext.startActivity (intent);

        });
//        holder.textViewEmail.setText(listUsers.get(position).getProjectId ());
//        holder.textViewPassword.setText(listUsers.get(position).getPassword());
    }

    @Override
    public int getItemCount() {

        return listUsers.size ();
    }

    /**
     * ViewHolder class
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView textViewName;
        public AppCompatTextView textViewEmail;
        public AppCompatTextView textViewPassword;
        public Button enter,edit;

        public UserViewHolder(View view) {
            super (view);
            textViewName = (AppCompatTextView) view.findViewById (R.id.textViewName);
            textViewEmail = (AppCompatTextView) view.findViewById (R.id.textViewEmail);
            textViewPassword = (AppCompatTextView) view.findViewById (R.id.textViewPassword);
            enter = (Button) view.findViewById (R.id.enter);
            edit = (Button) view.findViewById (R.id.edit);
        }
    }

}
