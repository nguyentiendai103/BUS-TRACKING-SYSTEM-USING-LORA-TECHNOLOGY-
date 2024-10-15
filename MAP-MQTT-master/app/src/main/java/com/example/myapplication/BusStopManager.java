package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class BusStopManager {
    private static final String TAG = "BusStopManager";

    private DatabaseReference databaseReference;
    private List<Point> busStops;
    private OnBusStopsLoadedListener onBusStopsLoadedListener;

    public BusStopManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference("bus_stops"); // Đường dẫn đến các điểm dừng
        busStops = new ArrayList<>();
    }

    public void setOnBusStopsLoadedListener(OnBusStopsLoadedListener listener) {
        this.onBusStopsLoadedListener = listener;
    }

    public void fetchBusStops() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                busStops.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lon = snapshot.child("lon").getValue(Double.class);

                    if (lat != null && lon != null) {
                        busStops.add(Point.fromLngLat(lon, lat));
                    } else {
                        Log.e(TAG, "Invalid lat or lon values in snapshot: " + snapshot.getKey());
                    }
                }
                if (onBusStopsLoadedListener != null) {
                    onBusStopsLoadedListener.onBusStopsLoaded(busStops);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching bus stops: " + databaseError.getMessage());
            }
        });
    }

    public interface OnBusStopsLoadedListener {
        void onBusStopsLoaded(List<Point> busStops);
    }
}
