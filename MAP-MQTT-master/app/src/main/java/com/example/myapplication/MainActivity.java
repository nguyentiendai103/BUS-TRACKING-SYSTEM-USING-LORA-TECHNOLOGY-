package com.example.myapplication;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING_TRAFFIC;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.extension.style.sources.Source;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationManager;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineClearValue;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;
import com.mapbox.navigation.ui.voice.model.SpeechVolume;
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.maps.GeoJSONSourceData;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    Point nearestStop = null;
    private PointAnnotationManager pointAnnotationManager;
    private RoteManager routerManager;
    private MapView mapView;
    private MaterialButton setRoute;
    private FloatingActionButton focusLocationBtn;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private DatabaseReference databaseReference;
    private Double latA, longB;
    private Spinner spinner;
    private String selectedId = "";
    private TextView distanceTextView;
    private TextView timeTextView;
    private GeoJsonSource geoJsonSource;
    private static final double K = 1.0; // Hệ số tỉ lệ
    private MapboxMap mapboxMap;

    // Tính khoảng cách giữa hai điểm

    private final String GEOJSON_SOURCE_ID = "line-source";
    private final String LINE_LAYER_ID = "line-layer";

    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
        }
    };
    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if (style != null) {
                        routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };
    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(18.0).bearing(bearing).pitch(45.0)
                .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Permission granted! Restart this app", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private MapboxSpeechApi speechApi;
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;

    private MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(new Expected.Transformer<SpeechError, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechError input) {
                    mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            }, new Expected.Transformer<SpeechValue, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechValue input) {
                    mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            });
        }
    };

    private MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };

    VoiceInstructionsObserver voiceInstructionsObserver = new VoiceInstructionsObserver() {
        @Override
        public void onNewVoiceInstructions(@NonNull VoiceInstructions voiceInstructions) {
            speechApi.generate(voiceInstructions, speechCallback);
        }
    };

    private boolean isVoiceInstructionsMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        focusLocationBtn = findViewById(R.id.focusLocation);
        setRoute = findViewById(R.id.setRoute);
        routerManager = new RoteManager(this, mapboxNavigation, new NavigationLocationProvider(), routeLineApi, routeLineView);

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this)
                .withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        speechApi = new MapboxSpeechApi(MainActivity.this, getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(MainActivity.this, Locale.US.toLanguageTag());
        spinner = findViewById(R.id.spinner);
        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);
        fetchDataFromFirebase();
        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
        // Đọc dữ liệu từ Realtime Database và gán vào biến A và B
        MapboxSoundButton soundButton = findViewById(R.id.soundButton);
        soundButton.unmute();
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVoiceInstructionsMuted = !isVoiceInstructionsMuted;
                if (isVoiceInstructionsMuted) {
                    soundButton.muteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(0f));
                } else {
                    soundButton.unmuteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(1f));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        focusLocationBtn.hide();
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchRoute();
            }
        });

        mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setEnabled(true);
                        locationComponentSettings.setPulsingEnabled(true);
                        return null;
                    }
                });
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_pin);
                AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
                addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point point) {
                        pointAnnotationManager.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap)
                                .withPoint(point);
                        pointAnnotationManager.create(pointAnnotationOptions);
                        return true;
                    }
                });
                focusLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusLocation = true;
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        focusLocationBtn.hide();
                    }
                });
            }
        });
    }
    @SuppressLint("MissingPermission")
    private void fetchRoute() {
        // Xóa điểm đánh dấu và tuyến đường cũ
        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
        }
        Bitmap invisibleIconBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        // Lấy tọa độ điểm đích từ Firebase
        Point destination = Point.fromLngLat(longB, latA);
        Bitmap originalIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bus);
        Bitmap startIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps);

        // Thay đổi kích thước của biểu tượng
        int newWidth = 180;
        int newHeight = 150;
        Bitmap resizedIconBitmap = Bitmap.createScaledBitmap(originalIconBitmap, newWidth, newHeight, false);
        Bitmap resizedStartIconBitmap = Bitmap.createScaledBitmap(startIconBitmap, newWidth, newHeight, false);

        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MainActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                if (location == null) {
                    Toast.makeText(MainActivity.this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    return;
                }

                setRoute.setEnabled(false);
                setRoute.setText("Đang tìm tuyến xe gần nhất...");

                Point origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                routerManager.getBusStops(selectedId, new RoteManager.OnBusStopsFetchedListener() {
                    @Override
                    public void onBusStopsFetched(List<Point> busStops) {
                        if (busStops.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không có điểm dừng nào trong RouteManager", Toast.LENGTH_SHORT).show();
                            setRoute.setEnabled(true);
                            setRoute.setText("Tìm tuyến xe gần nhất");
                            return;
                        }
                        List<Point> filteredStops = new ArrayList<>();
                        double distanceToDestination = getDistance(origin, destination);
                        List<Point> coordinates1 = new ArrayList<>();
                        coordinates1.add(origin);
                        coordinates1.addAll(filteredStops);
                        coordinates1.add(destination);
                        double distanceToPath = getDistanceToPath(origin,coordinates1);
                        runOnUiThread(() -> {
                            distanceTextView.setText(String.format(Locale.US, "Khoảng cách: %.2f km", distanceToPath));
                        });
                        for (Point stop : busStops) {
                            double distanceFromStopToDestination = getDistance(stop, destination);
                            double distanceToStop = getDistance(origin, stop);

                            if (distanceToStop < distanceToDestination &&
                                    distanceFromStopToDestination < distanceToDestination) {
                                filteredStops.add(stop);
                            }
                        }
                        List<Point> sortedStops = sortStopsByProximityToDestination(filteredStops, origin, destination);
                        List<Point> coordinates = new ArrayList<>();
                        coordinates.add(origin);
                        coordinates.addAll(sortedStops);
                        coordinates.add(destination);
                        RouteOptions.Builder builder = RouteOptions.builder()
                                .coordinatesList(coordinates)
                                .alternatives(true)
                                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC);

                        applyDefaultNavigationOptions(builder);
                        mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                            @Override
                            public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                                mapboxNavigation.setNavigationRoutes(list);
                                DirectionsRoute route = list.get(0).getDirectionsRoute();

                                double distanceInKm = route.distance() / 1000.0;
                                double durationInMinutes = route.duration() / 60.0;

                                runOnUiThread(() -> {
                                    distanceTextView.setText(String.format(Locale.US, "Khoảng cách: %.2f km", distanceInKm));
                                    timeTextView.setText(String.format(Locale.US, "Thời gian: %.2f min", durationInMinutes));
                                    setRoute.setEnabled(true);
                                    setRoute.setText("Tìm tuyến xe gần nhất");
                                });

                                PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                                        .withPoint(destination)
                                        .withIconImage(resizedIconBitmap);
                                pointAnnotationManager.create(pointAnnotationOptions);
                                PointAnnotationOptions pointAnnotationOptions1 = new PointAnnotationOptions()
                                        .withPoint(origin)
                                        .withIconImage(resizedStartIconBitmap);
                                pointAnnotationManager.create(pointAnnotationOptions1);
                                for (Point stop : busStops) {
                                    PointAnnotationOptions pointAnnotationOptions3 = new PointAnnotationOptions()
                                            .withPoint(stop)
                                            .withIconImage(invisibleIconBitmap);
                                    pointAnnotationOptions3.withIconSize(0.1f);
                                    pointAnnotationManager.create(pointAnnotationOptions3);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                                Log.e("RouteFetchFailure", "Lỗi: " + list);
                                for (RouterFailure failure : list) {
                                    Log.e("RouteFetchFailure", "Lỗi: " + failure.getMessage());
                                    Log.e("RouteFetchFailure", "Chi tiết: " + failure.getClass());
                                }
                                runOnUiThread(() -> {
                                    setRoute.setEnabled(true);
                                    setRoute.setText("Tìm tuyến xe gần nhất");
                                    Toast.makeText(MainActivity.this, "Tìm tuyến xe thất bại", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                                // Xử lý khi yêu cầu bị hủy
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private double getDistance(Point point1, Point point2) {
        double x1 = point1.longitude(); // Kinh độ của điểm 1
        double y1 = point1.latitude();  // Vĩ độ của điểm 1
        double x2 = point2.longitude(); // Kinh độ của điểm 2
        double y2 = point2.latitude();  // Vĩ độ của điểm 2

        // Tính toán khoảng cách Euclidean giữa hai điểm
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }


    private double getDistanceToPath(Point point, List<Point> path) {
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < path.size() - 1; i++) {
            Point start = path.get(i);
            Point end = path.get(i + 1);

            double x0 = point.longitude();
            double y0 = point.latitude();
            double x1 = start.longitude();
            double y1 = start.latitude();
            double x2 = end.longitude();
            double y2 = end.latitude();

            // Tính hệ số a, b, c của đường thẳng
            double a = y2 - y1;
            double b = x1 - x2;
            double c = x2 * y1 - x1 * y2;

            // Tính khoảng cách từ điểm đến đoạn đường thẳng
            double distanceToLine = calculateDi(x0, y0, a, b, c);

            // Tính khoảng cách giữa điểm và điểm đầu, điểm kết thúc của đoạn đường
            double distanceToStart = calculateLi(x0, y0, x1, y1);
            double distanceToEnd = calculateLi(x0, y0, x2, y2);

            // Kiểm tra xem khoảng cách đến đường thẳng có nhỏ hơn khoảng cách đến điểm đầu và điểm kết thúc không
            if (distanceToLine < distanceToStart && distanceToLine < distanceToEnd) {
                minDistance = Math.min(minDistance, distanceToLine);
            } else {
                minDistance = Math.min(minDistance, Math.min(distanceToStart, distanceToEnd));
            }
        }

        return minDistance;
    }


    public static double calculateDi(double x0, double y0, double a, double b, double c) {
        return K * (Math.abs(a * x0 + b * y0 + c) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));
    }

    public static double calculateLi(double x1, double y1, double x2, double y2) {
        return K * Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    private List<Point> sortStopsByProximityToDestination(List<Point> stops, Point origin, Point destination) {
        // Sắp xếp các điểm dừng theo khoảng cách từ điểm xuất phát đến điểm đích
        Collections.sort(stops, (p1, p2) -> {
            double distanceToP1 = getDistance(origin, p1);
            double distanceToP2 = getDistance(origin, p2);
            return Double.compare(distanceToP1, distanceToP2);
        });
        return stops;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }
    private void readLocationFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ snapshot
                    String latString = dataSnapshot.child("Lat").getValue(String.class);
                    String longString = dataSnapshot.child("Long").getValue(String.class);

                    // Kiểm tra và chuyển đổi thành số nếu dữ liệu tồn tại
                    if (latString != null && longString != null) {
                        try {
                            double lat = Double.parseDouble(latString);
                            double lon = Double.parseDouble(longString);

                            // Log tọa độ ra Logcat để kiểm tra
                            Log.d("Location", "Lat: " + lat + ", Long: " + lon);

                            // Cập nhật biến latA và longB với giá trị mới
                            latA = lat;
                            longB = lon;

                            // Cập nhật bản đồ đến vị trí mới


                            // Cập nhật đường đi mới
                            fetchRoute();
                            if (focusLocation) {
                                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                        .center(Point.fromLngLat(lon, lat))
                                        .zoom(15.0) // Hoặc bất kỳ giá trị zoom nào bạn muốn
                                        .build());
                                focusLocation = false; // Đặt lại giá trị để không cập nhật camera nữa
                            }

                        } catch (NumberFormatException e) {
                            Log.e("Location", "Lỗi chuyển đổi latitude hoặc longitude: " + e.getMessage());
                        }
                    } else {
                        // Xử lý khi dữ liệu không tồn tại
                        Toast.makeText(MainActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    // Xử lý khi dataSnapshot không tồn tại
                    Toast.makeText(MainActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu
                Toast.makeText(MainActivity.this, "Lỗi khi đọc dữ liệu từ Firebase: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }
    private void fetchDataFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Tham chiếu đến gốc của Firebase Realtime Database

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> spinnerData = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nodeName = snapshot.getKey(); // Lấy tên của mỗi node gốc

                    // Kiểm tra xem nodeName có phải là số không
                    if (isNumeric(nodeName)) {
                        spinnerData.add(nodeName);
                    }
                }

                if (spinnerData.isEmpty()) {
                    // Xử lý khi không có dữ liệu
                    spinnerData.add("No data available");
                }

                setupSpinner(spinnerData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức kiểm tra xem một chuỗi có phải là số không
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setupSpinner(List<String> spinnerData) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedId = spinnerData.get(position); // Lấy tên của node gốc được chọn từ Spinner
                databaseReference = FirebaseDatabase.getInstance().getReference().child(selectedId);
                readLocationFromFirebase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Xử lý khi không có phần tử nào được chọn
                selectedId = "53";
                Log.d("SelectedNode", "Selected node: " + selectedId); // Log để kiểm tra giá trị
            }
        });
    }

}


