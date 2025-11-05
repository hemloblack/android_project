package com.example.recycleview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MyAdapter adapter;
    ArrayList<MyItem> items;
    boolean isGrid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        Button switchBtn = findViewById(R.id.switchBtn);

        items = new ArrayList<>();
        items.add(new MyItem(R.drawable.sample_image1, "Mountains", "A peaceful mountain view."));
        items.add(new MyItem(R.drawable.sample_image2, "City Night", "Lights shining in the dark."));
        items.add(new MyItem(R.drawable.sample_image3, "Beach", "Relaxing sound of the waves."));
        items.add(new MyItem(R.drawable.sample_image, "Forest", "Walking through tall green trees."));

        adapter = new MyAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGrid) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    switchBtn.setText("Grid");
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    switchBtn.setText("List");
                }
                isGrid = !isGrid;
            }
        });
    }
}
