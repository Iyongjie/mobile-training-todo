//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.todo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.todo.R;
import com.couchbase.todo.db.DAO;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created by hideki on 6/26/17.
 */
public class UsersAdapter extends ArrayAdapter<String> {
    private static final String TAG = UsersAdapter.class.getSimpleName();

    private final String listID;
    private final Query query;

    public UsersAdapter(Context ctxt, String listID) {
        super(ctxt, 0);
        this.listID = listID;

        query = query();
        DAO.get().addChangeListener(
            query,
            change -> {
                clear();
                ResultSet rs = change.getResults();
                Result result;
                while ((result = rs.next()) != null) { add(result.getString(0)); }
                notifyDataSetChanged();
            });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                R.layout.view_user,
                parent,
                false);
        }

        String id = getItem(position);
        final Document user = DAO.get().fetchDocument(id);
        if (user == null) { return convertView; }

        // text
        TextView userText = convertView.findViewById(R.id.user_name);
        userText.setText(user.getString("username"));

        return convertView;
    }

    private Query query() {
        return DAO.get().createQuery(SelectResult.expression(Meta.id))
            .where(Expression.property("type").equalTo(Expression.string("task-list.user"))
                .and(Expression.property("taskList.id").equalTo(Expression.string(listID))));
    }
}