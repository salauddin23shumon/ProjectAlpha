
package com.sss.bitm.projectalpha.nearByPlace.mapDirectionPojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Step {

    @SerializedName("distance")
    @Expose
    private DistanceEnd distance;
    @SerializedName("duration")
    @Expose
    private DurationEnd duration;
    @SerializedName("end_location")
    @Expose
    private EndLocationEnd endLocation;
    @SerializedName("html_instructions")
    @Expose
    private String htmlInstructions;
    @SerializedName("polyline")
    @Expose
    private Polyline polyline;
    @SerializedName("start_location")
    @Expose
    private StartLocationEnd startLocation;
    @SerializedName("travel_mode")
    @Expose
    private String travelMode;
    @SerializedName("maneuver")
    @Expose
    private String maneuver;

    public DistanceEnd getDistance() {
        return distance;
    }

    public void setDistance(DistanceEnd distance) {
        this.distance = distance;
    }

    public DurationEnd getDuration() {
        return duration;
    }

    public void setDuration(DurationEnd duration) {
        this.duration = duration;
    }

    public EndLocationEnd getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(EndLocationEnd endLocation) {
        this.endLocation = endLocation;
    }

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public StartLocationEnd getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(StartLocationEnd startLocation) {
        this.startLocation = startLocation;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

}
