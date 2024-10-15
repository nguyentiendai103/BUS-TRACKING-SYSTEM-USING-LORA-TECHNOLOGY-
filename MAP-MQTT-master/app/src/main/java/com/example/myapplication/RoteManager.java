// RouteManager.java
package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.maps.MapboxMap;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.geojson.Point;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;

import java.util.ArrayList;
import java.util.List;

public class RoteManager {
    private final MapboxNavigation mapboxNavigation;
    private final MapboxRouteLineApi routeLineApi;
    private final MapboxRouteLineView routeLineView;
    private final NavigationLocationProvider locationProvider;
    private final DatabaseReference busStopsReference;  // Thêm DatabaseReference
    private MapboxMap mapboxMap;

    public RoteManager(Context context, MapboxNavigation mapboxNavigation, NavigationLocationProvider locationProvider, MapboxRouteLineApi routeLineApi, MapboxRouteLineView routeLineView) {
        this.mapboxNavigation = mapboxNavigation;
        this.locationProvider = locationProvider;
        this.routeLineApi = routeLineApi;
        this.routeLineView = routeLineView;
        this.busStopsReference = FirebaseDatabase.getInstance().getReference("bus_stops");  // Khởi tạo DatabaseReference
    }

    public void fetchRoute(Point origin, Point destination, List<Point> waypoints) {
        RouteOptions.Builder builder = RouteOptions.builder()
                .coordinatesList(getRouteCoordinates(origin, destination, waypoints))
                .alternatives(true)
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC);

        mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
            @Override
            public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                mapboxNavigation.setNavigationRoutes(list);
                // Render route
                routeLineApi.setNavigationRoutes(list, routeLineErrorRouteSetValueExpected -> {
                    routeLineView.renderRouteDrawData(mapboxMap.getStyle(), routeLineErrorRouteSetValueExpected);
                });
            }

            @Override
            public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                // Handle failure
            }

            @Override
            public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                // Handle cancellation
            }
        });
    }

    private List<Point> getRouteCoordinates(Point origin, Point destination, List<Point> waypoints) {
        List<Point> coordinates = new ArrayList<>();
        coordinates.add(origin);
        coordinates.addAll(waypoints);
        coordinates.add(destination);
        return coordinates;
    }

    public void getBusStops(String routeId, OnBusStopsFetchedListener listener) {
        DatabaseReference busStopsReference = FirebaseDatabase.getInstance().getReference("Location").child(routeId);
        Log.d("ID", "ID:" + routeId);

        busStopsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Point> busStops = new ArrayList<>();
                for (DataSnapshot stopSnapshot : dataSnapshot.getChildren()) {
                    // Lấy tọa độ từ Firebase dưới dạng String
                    String latString = stopSnapshot.child("Lat").getValue(String.class);
                    String lngString = stopSnapshot.child("Long").getValue(String.class);

                    try {
                        // Chuyển đổi từ String sang Double
                        Double lat = Double.parseDouble(latString);
                        Double lng = Double.parseDouble(lngString);

                        // Kiểm tra phạm vi của latitude và longitude
                        if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                            // Thêm điểm dừng vào danh sách
                            busStops.add(Point.fromLngLat(lng, lat));
                        } else {
                            Log.e("FirebaseError", "Tọa độ ngoài phạm vi hợp lệ: Lat=" + lat + ", Long=" + lng);
                        }
                    } catch (NumberFormatException e) {
                        // Xử lý lỗi nếu không thể chuyển đổi
                        Log.e("FirebaseError", "Lỗi chuyển đổi số: " + e.getMessage());
                    }
                }

                // Gọi callback với danh sách điểm dừng
                listener.onBusStopsFetched(busStops);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
                Log.e("FirebaseError", "Lỗi khi đọc dữ liệu từ Firebase: " + databaseError.getMessage());
            }
        });
    }




    public interface OnBusStopsFetchedListener {
        void onBusStopsFetched(List<Point> busStops);
    }
}
