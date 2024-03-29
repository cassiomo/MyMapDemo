package com.example.mapdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDemoActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnMapLongClickListener,
		OnInfoWindowClickListener{

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private LocationClient mLocationClient;
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_demo_activity);
		mLocationClient = new LocationClient(this, this, this);
		mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
		if (mapFragment != null) {
			map = mapFragment.getMap();
			if (map != null) {
				Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
				map.setMyLocationEnabled(true);
			} else {
				Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
		}

		map.setOnMapLongClickListener(this);
		map.setOnInfoWindowClickListener(this);
		map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		if (isGooglePlayServicesAvailable()) {
			mLocationClient.connect();
		}

	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				mLocationClient.connect();
				break;
			}

		}
	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(), "Location Updates");
			}

			return false;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Location location = mLocationClient.getLastLocation();
		if (location != null) {
			Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
			map.animateCamera(cameraUpdate);
		} else {
			Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
		}
	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
	 @Override
	    public void onMapLongClick(final LatLng point) {
	      Toast.makeText(this, "Long Press", Toast.LENGTH_LONG).show();
	      // Custom code here...
	      showAlertDialogForPoint(point);
	    }

	   // Display the alert that adds the marker
	   private void showAlertDialogForPoint(final LatLng point) {
	      // inflate message_item.xml view
	      View  messageView = LayoutInflater.from(MapDemoActivity.this).
	        inflate(R.layout.message_item, null);
	      // Create alert dialog builder
	      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	      // set message_item.xml to AlertDialog builder
	      alertDialogBuilder.setView(messageView);

	      // Create alert dialog
	      final AlertDialog alertDialog = alertDialogBuilder.create();

	      // Configure dialog button (OK)
	      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", 
	        new DialogInterface.OnClickListener() {
	          @Override
	          public void onClick(DialogInterface dialog, int which) {
	              // Define color of marker icon
	              BitmapDescriptor defaultMarker =
	                 BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
	              // Extract content from alert dialog
	              String title = ((EditText) alertDialog.findViewById(R.id.etTitle)).
	                  getText().toString();
	              String snippet = ((EditText) alertDialog.findViewById(R.id.etSnippet)).
	                  getText().toString();
	              // Creates and adds marker to the map
	              Marker marker = map.addMarker(new MarkerOptions()
	                .position(point)
	                .title(title)
	                .snippet(snippet)       
	                .icon(defaultMarker));
	              dropPinEffect(marker);
	          }
	      });

	      // Configure dialog button (Cancel)
	      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", 
	      new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
	      });

	      // Display the dialog
	      alertDialog.show();
	  }
	   
	   private void dropPinEffect(final Marker marker) {
		      // Handler allows us to repeat a code block after a specified delay
		      final android.os.Handler handler = new android.os.Handler();
		      final long start = SystemClock.uptimeMillis();
		      final long duration = 1500;

		      // Use the bounce interpolator
		      final android.view.animation.Interpolator interpolator = 
		          new BounceInterpolator();

		      // Animate marker with a bounce updating its position every 15ms
		      handler.post(new Runnable() {
		          @Override
		          public void run() {
		              long elapsed = SystemClock.uptimeMillis() - start;
		              // Calculate t for bounce based on elapsed time 
		              float t = Math.max(
		                      1 - interpolator.getInterpolation((float) elapsed
		                              / duration), 0);
		              // Set the anchor
		              marker.setAnchor(0.5f, 1.0f + 14 * t);
		         
		              if (t > 0.0) {
		                  // Post this event again 15ms from now.
		                  handler.postDelayed(this, 15);
		              } else { // done elapsing, show window
		                  marker.showInfoWindow();
		              }
		          }
		      });
		  }

	   class CustomWindowAdapter implements InfoWindowAdapter{
			LayoutInflater mInflater;
			
			public CustomWindowAdapter(LayoutInflater i){
				mInflater = i;
			}

		        // This defines the contents within the info window based on the marker
			@Override
			public View getInfoContents(Marker marker) {
			    // Getting view from the layout file
			    View v = mInflater.inflate(R.layout.custom_info_window, null);
			    // Populate fields	
			    TextView title = (TextView) v.findViewById(R.id.tv_info_window_title);
			    title.setText(marker.getTitle());

			    TextView description = (TextView) v.findViewById(R.id.tv_info_window_description);
			    description.setText(marker.getSnippet());
			    // Return info window contents
			    return v;
			}
		        
		        // This changes the frame of the info window; returning null uses the default frame.
			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}
		}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Info Click", Toast.LENGTH_SHORT).show();
	}
	   
}