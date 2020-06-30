package com.amane.adapter;

import android.app.Activity;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

/**
 * MainActivity的适配器
 */
public abstract class MainAdapter extends Activity implements AMapGestureListener,
        AMap.OnMapClickListener, AMap.OnMyLocationChangeListener, AMap.OnPOIClickListener,
        View.OnTouchListener, AMap.OnCameraChangeListener {

    @Override
    public abstract boolean onTouch(View v, MotionEvent event);

    @Override
    public abstract void onCameraChange(CameraPosition cameraPosition);

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    public abstract void onMapClick(LatLng latLng);

    @Override
    public abstract void onMyLocationChange(Location location);

    @Override
    public abstract void onPOIClick(Poi poi);

    @Override
    public void onDoubleTap(float v, float v1) {

    }

    @Override
    public void onSingleTap(float v, float v1) {

    }

    @Override
    public void onFling(float v, float v1) {

    }

    @Override
    public void onScroll(float v, float v1) {

    }

    @Override
    public abstract void onLongPress(float v, float v1);

    @Override
    public void onDown(float v, float v1) {

    }

    @Override
    public void onUp(float v, float v1) {

    }

    @Override
    public void onMapStable() {

    }

    public abstract class AdaptTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public abstract void onTextChanged(CharSequence s, int start, int before, int count);

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public abstract class AdaptPoiKeywordSearchListener implements PoiSearch.OnPoiSearchListener {

        public abstract void onPoiSearched(PoiResult poiResult, int i);

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    }

    public abstract class AdaptPoiIDSearchListener implements PoiSearch.OnPoiSearchListener {

        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {

        }

        public abstract void onPoiItemSearched(PoiItem poiItem, int i);
    }

    public abstract class AdaptGeocodeSearchListener implements GeocodeSearch.OnGeocodeSearchListener {

        public abstract void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i);

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    }
}
