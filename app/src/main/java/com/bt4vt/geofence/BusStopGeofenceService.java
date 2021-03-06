/*
 * Copyright 2015 Ben Sechrist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bt4vt.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bt4vt.repository.domain.Stop;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Manages geofences for {@link com.bt4vt.repository.domain.Stop} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class BusStopGeofenceService implements ResultCallback<Status> {

  private static final float GEOFENCE_RADIUS_IN_METERS = 25;
  private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
  private static final int GEOFENCE_LOITERING_DELAY = 10000; // 10 seconds

  private Context context;

  private GoogleApiClient googleApiClient;

  private PendingIntent geofencePendingIntent;

  @Inject
  public BusStopGeofenceService(Context context) {
    this.context = context;

    googleApiClient = new GoogleApiClient.Builder(context)
        .addApi(LocationServices.API)
        .build();
    googleApiClient.connect();
  }

  public void registerGeofence(Stop stop) {
    if (!googleApiClient.isConnected()) {
      if (googleApiClient.isConnecting()) {
        throw new IllegalStateException("Google Location API already connecting");
      } else {
        googleApiClient.blockingConnect();
      }
    }
    Geofence geofence = generateGeofence(stop);
    LocationServices.GeofencingApi.addGeofences(googleApiClient,
        getGeofencingRequest(Collections.singletonList(geofence)),
        getGeofencePendingIntent()).setResultCallback(this);
  }

  public void unregisterGeofence(Stop stop) {
    if (!googleApiClient.isConnected()) {
      if (googleApiClient.isConnecting()) {
        throw new IllegalStateException("Google Location API already connecting");
      } else {
        googleApiClient.blockingConnect();
      }
    }
    LocationServices.GeofencingApi.removeGeofences(googleApiClient,
        Collections.singletonList(stop.toString()));
  }

  public void unregisterAllGeofences() {
    if (googleApiClient.isConnected()) {
      LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeofencePendingIntent());
    } else {
      googleApiClient = new GoogleApiClient.Builder(context)
          .addApi(LocationServices.API)
          .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
              LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                  getGeofencePendingIntent());
            }

            @Override
            public void onConnectionSuspended(int i) {
              throw new RuntimeException(String.valueOf(i));
            }
          })
          .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
              throw new RuntimeException(connectionResult.toString());
            }
          })
          .build();
      googleApiClient.connect();
    }
  }

  @Override
  public void onResult(Status status) {
    int statusCode = status.getStatusCode();
    if (statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES) {
      throw new IllegalStateException(GeofenceStatusCodes.getStatusCodeString(statusCode));
    }
  }

  private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
    builder.addGeofences(geofences);
    return builder.build();
  }

  private Geofence generateGeofence(Stop stop) {
    return new Geofence.Builder()
        // Must be to string to use valueOf when the geofence is triggered
        .setRequestId(stop.toString())
        .setCircularRegion(
            stop.getLatitude(),
            stop.getLongitude(),
            GEOFENCE_RADIUS_IN_METERS
        )
        .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
            Geofence.GEOFENCE_TRANSITION_EXIT)
        .setLoiteringDelay(GEOFENCE_LOITERING_DELAY)
        .build();
  }

  private PendingIntent getGeofencePendingIntent() {
    // Reuse the PendingIntent if we already have it.
    if (geofencePendingIntent != null) {
      return geofencePendingIntent;
    }
    Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
    // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
    // calling addGeofences() and removeGeofences().
    geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
        FLAG_UPDATE_CURRENT);
    return geofencePendingIntent;
  }
}
