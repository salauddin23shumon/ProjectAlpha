
package com.sss.bitm.projectalpha.nearByPlace.mapDirectionPojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EndLocationEnd {

    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

}
