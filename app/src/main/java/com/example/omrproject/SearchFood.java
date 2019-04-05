package com.example.omrproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.omrproject.Common.Common;
import com.example.omrproject.Interface.ItemClickListener;
import com.example.omrproject.Model.Food;
import com.example.omrproject.ViewHolder.FoodViewHolder;
import com.example.omrproject.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFood extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String searchMode = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    Map<String, String> suggestListById = new HashMap<>();

    MaterialSearchBar materialSearchBar;

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_food);

        // Init database
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        //RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get itent
        if(getIntent()!=null){
            searchMode = getIntent().getStringExtra("searchMode");
        }
        if(!searchMode.isEmpty()){
            loadListFood();
        }

        //Search
        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.setHint(searchMode.equalsIgnoreCase("byname")?"Nhập tên món ăn":"Nhập mã món ăn");
        //change by mode
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<String>();
                    if(searchMode.equalsIgnoreCase("byname")){
                        for(Map.Entry<String, String> search: suggestListById.entrySet()){
                            if(search.getValue().toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                                suggest.add(search.getValue());
                            }
                        }
                    }
                    else{
                        for(Map.Entry<String, String> search: suggestListById.entrySet()){
                            if(search.getKey().toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                                suggest.add(search.getValue());
                            }
                        }
                    }
                    materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search bar is close
                if(!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                    //when search finish
                    //Show result of search adapter
                    startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {
        if(searchMode.equalsIgnoreCase("byname")){
            FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodList.orderByChild("name").startAt(text.toString().substring(0,1).toUpperCase()+text.toString().substring(1)).endAt(text.toString()+"\uf8ff"), Food.class).build();
            searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                    holder.food_name.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImg()).into(holder.food_img);

                    final Food local = model;
                    holder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            //Start FoodDetail Activity
                            Intent foodDetail = new Intent (SearchFood.this, FoodDetail.class);
                            foodDetail.putExtra("foodId", searchAdapter.getRef(position).getKey());
                            startActivity(foodDetail);
                        }
                    });
                }

                @NonNull
                @Override
                public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);
                    return new FoodViewHolder(view);
                }
            };
        }
        else{
            FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodList.orderByKey().equalTo(text.toString()), Food.class).build();
            searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                    holder.food_name.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImg()).into(holder.food_img);

                    final Food local = model;
                    holder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            //Start FoodDetail Activity
                            Intent foodDetail = new Intent (SearchFood.this, FoodDetail.class);
                            foodDetail.putExtra("foodId", searchAdapter.getRef(position).getKey());
                            startActivity(foodDetail);
                        }
                    });
                }

                @NonNull
                @Override
                public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);
                    return new FoodViewHolder(view);
                }
            };
        }

        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot foodSnapshot: dataSnapshot.getChildren()){
                    Food item = foodSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());
                    suggestListById.put(foodSnapshot.getKey().toString(), item.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood(){
        //Get foodList by categoryId
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodList, Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImg()).into(holder.food_img);

                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start FoodDetail Activity
                        Intent foodDetail = new Intent (SearchFood.this, FoodDetail.class);
                        foodDetail.putExtra("foodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);
                return new FoodViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }
}