package com.smartdot.mobile.portal.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.application.MyAppContext;

import io.rong.message.LocationMessage;

/**
 * 融云地图定位的界面
 */
public class AMAPLocationActivity extends FragmentActivity implements View.OnClickListener, LocationSource,
        GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener, AMap.OnCameraChangeListener {
    static public final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    private MapView mapView;

    private AMap aMap;

    private LocationManagerProxy mLocationManagerProxy;

    private Handler handler = new Handler();

    private OnLocationChangedListener listener;

    private LatLng myLocation = null;

    private Marker centerMarker;

    private boolean isMovingMarker = false;

    private BitmapDescriptor successDescripter;

    private GeocodeSearch geocodeSearch;

    private LocationMessage mMsg;

    private TextView tvCurLocation;

    private boolean model = false;

    private boolean isPerview;

    private TextView okTextView;

    private ImageView backImageView;

    private boolean isShowLocation = false;

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amaplocation);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                } else {
                    new AlertDialog.Builder(this).setMessage(R.string.request_location_permission)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            }).setNegativeButton(R.string.cancel, null).create().show();
                }
                return;
            }
        }

        initUI();
        initAmap();

        setUpLocationStyle();
    }

    private void initAmap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        if (getIntent().hasExtra("location")) {
            isPerview = true;
            mMsg = getIntent().getParcelableExtra("location");
            tvCurLocation.setVisibility(View.GONE);
            returns.setVisibility(View.GONE);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMsg.getLat(), mMsg.getLng()), 17);// 设置缩放监听
            aMap.moveCamera(cameraUpdate);

            if (model) {
                CameraPosition location = new CameraPosition.Builder().target(new LatLng(mMsg.getLat(), mMsg.getLng()))
                        .zoom(18).bearing(0).tilt(0).build();
                show(location);
            } else {
                Bitmap markerIcon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_usecarnow_position_succeed);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(R.drawable.ic_usecarnow_position_succeed);
                aMap.addMarker(new MarkerOptions().anchor(0.6f, 0.6f).position(new LatLng(mMsg.getLat(), mMsg.getLng()))
                        .title(mMsg.getPoi()).snippet(mMsg.getLat() + "," + mMsg.getLng()).draggable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmapDrawable.getBitmap())));
                System.out.println(getString(R.string.map_scaling) + aMap.getCameraPosition().zoom);

            }
            return;
        }

        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(23);// 设置缩放监听
        aMap.moveCamera(cameraUpdate);

        successDescripter = BitmapDescriptorFactory.fromResource(R.drawable.ic_usecarnow_position_succeed);
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

    private static final String MAP_FRAGMENT_TAG = "map";

    private SupportMapFragment aMapFragment;

    private void show(CameraPosition location) {
        AMapOptions aOptions = new AMapOptions();
        aOptions.zoomGesturesEnabled(true);
        aOptions.scrollGesturesEnabled(false);

        aOptions.camera(location);

        if (aMapFragment == null) {
            aMapFragment = SupportMapFragment.newInstance(aOptions);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content, aMapFragment, MAP_FRAGMENT_TAG);
            fragmentTransaction.commit();
        }
    }

    private ImageView returns;

    private void initUI() {
        returns = (ImageView) findViewById(R.id.myLocation);
        tvCurLocation = (TextView) findViewById(R.id.location);
        backImageView = (ImageView) findViewById(R.id.backimageView);
        okTextView = (TextView) findViewById(R.id.okTv);
        if (getIntent().hasExtra("showLocation")) {
            isShowLocation = getIntent().getBooleanExtra("showLocation", false);
            System.out.println("isShowLocation" + "true");
        } else {
            System.out.println("isShowLocation" + "false");
        }
        if (isShowLocation) {
            okTextView.setVisibility(View.GONE);
        }

        returns.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        okTextView.setOnClickListener(this);
    }

    @Override
    @TargetApi(23)
    public void onClick(View v) {
        if (v.getId() == R.id.myLocation) {
            if (myLocation != null) {
                CameraUpdate update = CameraUpdateFactory.changeLatLng(myLocation);
                aMap.animateCamera(update);
            }
        } else if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.backimageView) {
            finish();
        } else if (v.getId() == R.id.okTv) {
            if (mMsg != null) {

                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_ASK_PERMISSIONS);
                        } else {
                            new AlertDialog.Builder(this).setMessage(R.string.request_storage_permission)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    }).setNegativeButton(R.string.cancel, null).create().show();
                        }
                    }
                }
                MyAppContext.getInstance().getLastLocationCallback().onSuccess(mMsg);
                MyAppContext.getInstance().setLastLocationCallback(null);
                finish();
            } else {
                MyAppContext.getInstance().getLastLocationCallback().onFailure(getString(R.string.locate_failed));
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 100, this);
    }

    @Override
    public void deactivate() {
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 0) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null) {
                endAnim();
                centerMarker.setIcon(successDescripter);
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                String formatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                String shortAdd = formatAddress.replace(regeocodeAddress.getProvince(), "")
                        .replace(regeocodeAddress.getCity(), "").replace(regeocodeAddress.getDistrict(), "");
                tvCurLocation.setText(shortAdd);
                double latitude = regeocodeResult.getRegeocodeQuery().getPoint().getLatitude();
                double longitude = regeocodeResult.getRegeocodeQuery().getPoint().getLongitude();
                mMsg = LocationMessage.obtain(latitude, longitude, shortAdd, getMapUrl(latitude, longitude));
                Log.e("LocationChange", shortAdd + latitude + "----" + longitude);
            } else {
                Toast.makeText(AMAPLocationActivity.this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AMAPLocationActivity.this, R.string.seaarch_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            if (listener != null) {
                listener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            }
            myLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());// 获取当前位置经纬度
            tvCurLocation.setText(aMapLocation.getRoad() + aMapLocation.getStreet() + aMapLocation.getPoiName());// 当前位置信息

            double latitude = aMapLocation.getLatitude();
            double longitude = aMapLocation.getLongitude();
            mMsg = LocationMessage.obtain(latitude, longitude,
                    aMapLocation.getRoad() + aMapLocation.getStreet() + aMapLocation.getPoiName(),
                    getMapUrl(latitude, longitude));
            Log.e("LocationInit", aMapLocation.getRoad() + aMapLocation.getStreet() + aMapLocation.getPoiName()
                    + latitude + "----" + longitude);

            addChooseMarker();
        }
    }

    private void addChooseMarker() {
        // 加入自定义标签
        MarkerOptions centerMarkerOption = new MarkerOptions().position(myLocation).icon(successDescripter);
        centerMarker = aMap.addMarker(centerMarkerOption);
        centerMarker.setPositionByPixels(mapView.getWidth() / 2, mapView.getHeight() / 2);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraUpdate update = CameraUpdateFactory.zoomTo(17f);
                aMap.animateCamera(update, 1000, new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        aMap.setOnCameraChangeListener(AMAPLocationActivity.this);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        }, 1000);
    }

    private void setMovingMarker() {
        if (isMovingMarker)
            return;

        isMovingMarker = true;
        centerMarker.setIcon(successDescripter);
        hideLocationView();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (centerMarker != null) {
            setMovingMarker();
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLonPoint point = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 50, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        if (centerMarker != null) {
            animMarker();
        }
        showLocationView();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    private ValueAnimator animator = null;

    private void animMarker() {
        isMovingMarker = false;
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(mapView.getHeight() / 2, mapView.getHeight() / 2 - 30);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(150);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                centerMarker.setPositionByPixels(mapView.getWidth() / 2, Math.round(value));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                centerMarker.setIcon(successDescripter);
            }
        });
        animator.start();
    }

    private void endAnim() {
        if (animator != null && animator.isRunning())
            animator.end();
    }

    private void hideLocationView() {
        ObjectAnimator animLocation = ObjectAnimator.ofFloat(tvCurLocation, "TranslationY",
                -tvCurLocation.getHeight() * 2);
        animLocation.setDuration(200);
        animLocation.start();
    }

    private void showLocationView() {
        ObjectAnimator animLocation = ObjectAnimator.ofFloat(tvCurLocation, "TranslationY", 0);
        animLocation.setDuration(200);
        animLocation.start();
    }

    private void setUpLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_now));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.strokeColor(R.color.main_theme_color);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        aMap.setMyLocationStyle(myLocationStyle);
    }

    private Uri getMapUrl(double x, double y) {
        String url = "http://restapi.amap.com/v3/staticmap?location=" + y + "," + x
                + "&zoom=16&scale=2&size=408*240&markers=mid,,A:" + y + "," + x + "&key="
                + "ee95e52bf08006f63fd29bcfbcf21df0";
        Log.e("getMapUrl", url);
        return Uri.parse(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_location_menu, menu);

        if (isPerview) {
            menu.getItem(0).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @TargetApi(23)
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send_location) {
            if (mMsg != null) {

                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_ASK_PERMISSIONS);
                        } else {
                            new AlertDialog.Builder(this).setMessage(R.string.request_location_permission)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    }).setNegativeButton(R.string.cancel, null).create().show();
                        }
                        return false;
                    }
                }
                MyAppContext.getInstance().getLastLocationCallback().onSuccess(mMsg);
                MyAppContext.getInstance().setLastLocationCallback(null);
                finish();
            } else {
                MyAppContext.getInstance().getLastLocationCallback()
                        .onFailure(getResources().getString(R.string.locate_failed));
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        initUI();
                        initAmap();
                        setUpLocationStyle();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
