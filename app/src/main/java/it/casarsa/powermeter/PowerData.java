package it.casarsa.powermeter;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by fcasarsa on 31/12/2014.
 */
public class PowerData implements Parcelable {

    private double volt;
    private double ampere;
    private double watt;
    private double voltAmpere;
    private double cosFi;
    private double powerFactor;

    DecimalFormat decimalFormat = new DecimalFormat();

    // constructor
    public PowerData (String json) {
        decimalFormat.applyLocalizedPattern("###.##0");

        try {
            JSONObject powerDataJson = new JSONObject(json);
            this.watt = powerDataJson.getDouble("W");
            this.ampere = powerDataJson.getDouble("A");
            this.volt = powerDataJson.getDouble("V");
            this.voltAmpere = powerDataJson.getDouble("V");
            this.cosFi = powerDataJson.getDouble("cos");
            this.powerFactor = powerDataJson.getDouble("fp");



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double getWatt () {
        return this.watt;
    }

    public double getAmpere () {
        return this.ampere;
    }

    public double getVolt () {
        return this.volt;
    }

    public String getWattText () {
        return decimalFormat.format(this.watt);
    }

    public String getPowerFactorText () {
        return decimalFormat.format(this.powerFactor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // watch for the order, should be same as PowerData(Parcel in )
        dest.writeDouble(this.volt);
        dest.writeDouble(this.ampere);
        dest.writeDouble(this.watt);
        dest.writeDouble(this.voltAmpere);
        dest.writeDouble(this.cosFi);
        dest.writeDouble(this.powerFactor);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<PowerData> CREATOR = new Parcelable.Creator<PowerData>() {

        @Override
        public PowerData createFromParcel(Parcel source) {
            return new PowerData(source);
        }

        @Override
        public PowerData[] newArray(int size) {
            return new PowerData[size];
        }

    };
    private PowerData(Parcel in) {
        this.volt = in.readDouble();
        this.ampere = in.readDouble();
        this.watt= in.readDouble();
        this.voltAmpere = in.readDouble();
        this.cosFi = in.readDouble();
        this.powerFactor = in.readDouble();
    }


}
