package com.myfirstgoogleapp.easytripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.myfirstgoogleapp.easytripplanner.R;
import com.myfirstgoogleapp.easytripplanner.adapters.MapTripAdapter;
import com.myfirstgoogleapp.easytripplanner.databinding.FragmentMappedBinding;
import com.myfirstgoogleapp.easytripplanner.models.Trip;
import com.myfirstgoogleapp.easytripplanner.models.TripRoute;
import com.myfirstgoogleapp.easytripplanner.utility.Common;
import com.myfirstgoogleapp.easytripplanner.utility.MapCameraListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class MappedFragment extends Fragment {
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String MARKER_ICON_ID = "red-pin-icon-id";
    private MapView mapView;
    private MapTripAdapter mapTripAdapter;
    private Point origin;
    private Point destination;
    private final List<TripRoute> tripRoutes;
    private final List<String> tripNames;
    MapboxDirections client;
    MapCameraListener mapCameraListener;
    int loop;
    Bundle savedInstanceState;

    private FragmentMappedBinding binding;

    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public MappedFragment() {
        tripNames = new ArrayList<>();
        tripRoutes = new ArrayList<>();
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                linkMap();
            } else {
                Toast.makeText(requireContext(), "Permission Denied, can not Show Map!!", Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCameraListener();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Mapbox.getInstance(requireContext(), getString(R.string.access_token));
        // Inflate the layout for this fragment
        binding = FragmentMappedBinding.inflate(inflater, container, false);
        // This contains the MapView in XML and needs to be called after the access token is configured.
        // Setup the MapView
        mapView = binding.mapView;
        return binding.getRoot();
    }

    private void linkMap() {
        mapView.onCreate(savedInstanceState);
        mapTripAdapter = new MapTripAdapter(requireContext(), tripNames, mapCameraListener);
        binding.listView.setAdapter(mapTripAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED) {
                linkMap();
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_PHONE_STATE);
            }
        } else {
            linkMap();
        }
        initData();
    }


    /**
     * Add the route and marker sources to the map
     */
    private synchronized void initSource(@NonNull Style loadedMapStyle, int index) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID + index));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID + index, FeatureCollection.fromFeatures(new Feature[]{
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**
     * Add the route and marker icon layers to the map
     */
    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
    private synchronized void initLayers(@NonNull Style loadedMapStyle, int index, String color) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID + index, ROUTE_SOURCE_ID + index);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
/*                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),*/
                lineWidth(5f),
                lineColor(Color.parseColor(color))
        );
        loadedMapStyle.addLayer(routeLayer);


        Drawable drawable = getResources()
                .getDrawable(R.drawable.location_on_maps, getResources().newTheme());

        drawable.setTint(Color.parseColor(color));

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(MARKER_ICON_ID + index, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(drawable)));

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID + index, ICON_SOURCE_ID + index).withProperties(
                iconImage(MARKER_ICON_ID + index),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f})));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel the Directions API request
        if (client != null)
            client.cancelCall();
        mapView.onDestroy();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void initData() {

        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null)
            return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference currentUserRef;
        currentUserRef = database.getReference("Users").child(userId);


        Query queryReference = currentUserRef
                .orderByChild("status")
                .equalTo(UpcomingFragment.TRIP_STATUS.DONE.name());


        queryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    Timber.i("onDataChange: %s", trip);
                    if (trip != null && trip.pushId != null) {
                        tripRoutes.add(new TripRoute(Point.fromLngLat(trip.locationFrom.longitude, trip.locationFrom.latitude),
                                Point.fromLngLat(trip.locationTo.longitude, trip.locationTo.latitude)));
                        tripNames.add(trip.name);
                        Timber.i("trips name : %s", tripNames);
                        mapTripAdapter.notifyDataSetChanged();
                        loop++;
                    }
                    if (loop >= snapshot.getChildrenCount() && loop != 0) {
                        showRoutes();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Timber.i("onCancelled: Canceled");
            }
        });
    }

    private void showRoutes() {
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            for (int index = 0; index < tripRoutes.size(); index++) {
                origin = tripRoutes.get(index).getOrigin();
                destination = tripRoutes.get(index).getDestination();
                initSource(style, index);
                initLayers(style, index, Common.MAPS_TRIPS_COLORS[index % 11]);

                // Retrieve and update the source designated for showing the directions route
                GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID + index);

                // Create a LineString with the directions route's geometry and
                // reset the GeoJSON source for the route LineLayer source
                if (source != null) {
                    source.setGeoJson(LineString.fromLngLats(tripRoutes.get(index).getPoints()));
                }
            }
        }));
    }

    private void initCameraListener() {
        mapCameraListener = position -> {
            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                    .include(new LatLng(tripRoutes.get(position).getOrigin().latitude(), tripRoutes.get(position).getOrigin().longitude())) // Origin
                    .include(new LatLng(tripRoutes.get(position).getDestination().latitude(), tripRoutes.get(position).getDestination().longitude())) // Destination
                    .build();

            mapView.getMapAsync(mapboxMap -> {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 150), 2000);
            });
        };
    }
}