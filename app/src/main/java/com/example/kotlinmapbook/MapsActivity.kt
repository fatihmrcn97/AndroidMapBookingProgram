package com.example.kotlinmapbook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList





class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    var locationManager:LocationManager?= null
    var locationListener:LocationListener?=null
    private lateinit var mMap: GoogleMap



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(MyListener)
        locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener=object :LocationListener{
            override fun onLocationChanged(p0: Location?) {

                if(p0!=null){
                    var userlocation = LatLng(p0!!.latitude,p0!!.longitude)
                    mMap.addMarker(MarkerOptions().position(userlocation).title("Your Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,17f))

                }

            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
             }

            override fun onProviderEnabled(p0: String?) {
             }

            override fun onProviderDisabled(p0: String?) {
             }

        }


        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)

        }else{
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)

            val intent = intent
            val info = intent.getStringExtra("info")
            if(info.equals("new"))
            {
                        mMap.clear()
                val lastLocation= locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastUserLocatin = LatLng(lastLocation.latitude,lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocatin,14f))
            }else{

                mMap.clear()
                val latidue= intent.getDoubleExtra("latitude",0.0)
                val longitude=intent.getDoubleExtra("longitude",0.0)
                val name = intent.getStringExtra("name")
                val location = LatLng(latidue,longitude)
                mMap.addMarker(MarkerOptions().position(location).title("Your Places"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,17f))






            }


        }


    }

    val MyListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng?) {

            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            var adress = ""

            try {

                val addressList = geocoder.getFromLocation(p0!!.latitude,p0!!.longitude,1)
                if(addressList != null && addressList.size>0){
                    if(addressList[0].thoroughfare != null){
                        adress += addressList[0].thoroughfare

                        if(addressList[0].subThoroughfare !=null){
                            adress+=addressList[0].subThoroughfare
                        }
                    }
                }else{
                    adress="New Place"
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
            mMap.addMarker(MarkerOptions().position(p0!!).title(adress))

            Toast.makeText(applicationContext,"New place Created",Toast.LENGTH_LONG).show()

            //
            //              SAVING DATABAASE
            //
            try {
                val latidude=p0.latitude.toString()
                val longitude=p0.longitude.toString()

                val database=openOrCreateDatabase("Places",Context.MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latidue VARCHAR , longitude VARCHAR)")
                val toCompile="INSERT INTO places (name,latidue,longitude) VALUES (?,?,?)"
                val sqLiteStatement=database.compileStatement(toCompile)
                sqLiteStatement.bindString(1,adress)
                sqLiteStatement.bindString(2,latidude)
                sqLiteStatement.bindString(3,longitude)

                sqLiteStatement.execute()


            }catch (e : Exception){
                e.printStackTrace()
            }

        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults.size>0){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )== PackageManager.PERMISSION_GRANTED){
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
