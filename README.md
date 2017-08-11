[![Bintray Release](https://api.bintray.com/packages/djavan/maven/com.sousoum%3Ajcvd/images/download.svg) ](https://bintray.com/djavan/maven/com.sousoum%3Ajcvd/_latestVersion)
[![Jitpack Release](https://jitpack.io/v/djavan-bertrand/JCVD.svg)](https://jitpack.io/#djavan-bertrand/JCVD)
[![Build Status](https://travis-ci.org/djavan-bertrand/JCVD.svg?branch=master)](https://travis-ci.org/djavan-bertrand/JCVD)
[![codecov](https://codecov.io/gh/djavan-bertrand/JCVD/branch/master/graph/badge.svg)](https://codecov.io/gh/djavan-bertrand/JCVD)

# JCVD

As our friend Jean-Claude Van Damme once said:<br/> *"There are a people who are not successful because they are not "**aware**" they aren't attentive to the fact that they exist. Those poor people, they just don't know"*

**So be like JCVD, be aware!**

![alt jcvd_aware](https://raw.githubusercontent.com/djavan-bertrand/JCVD/master/resources/jcvd_aware.gif)

JCVD is a library that helps you using the [Awareness API](https://developers.google.com/awareness/) from the Google Play Services.

The Awareness API is a fantastic tool to enhance your app and make them context aware: location, activity, timing... But it lacks a way to retrieve the list of added fences and to associate fences to custom values.

What if you want to present a list of all added fences to your users?<br/>
What if you want to add to a fence an url to get some data when the fence is triggered?<br/>
Well, you just have to use the JCVD library!

It stores your fences, add them to the Google API and informs the caller about the status of the operation.

An example is also provided. It shows you how to create and add a fence. You can also see the list of all the added fences.

The status of this library is currently in beta, I would love to hear about what you think of it, what is missing according to you.

## Import the library to your project 

### With JCentral

Simply add the dependency to the jcvd library in the build.gradle.
 
```
dependencies {
    // ...
    compile 'com.sousoum:jcvd:1.1.0'
}
```

### With Jitpack

First add the [Jitpack](https://jitpack.io/) repository to your project build.gradle file:

```
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```

Then add the dependency to the jcvd library in the build.gradle.
 
```
dependencies {
    // ...
    compile 'com.github.djavan-bertrand:JCVD:1.1.0'
}
```

Adding this dependency will also add the following permissions to your apk:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
```

## How to use

A working example is available [in the project](https://github.com/djavan-bertrand/JCVD/tree/master/example).

Here are the main points:

### Declare a StorableFenceManager.

```
private StorableFenceManager mFenceManager;
```

### Then create it and declare your class as a listener
```
mFenceManager = new StorableFenceManager(this);
mFenceManager.setListener(this);
```

### Create a fence

#### Location fence

The StorableLocationFence is representing a [LocationFence](https://developers.google.com/android/reference/com/google/android/gms/awareness/fence/LocationFence) in the Awareness API.

You can create it with:

```
StorableLocationFence.entering((double)latitude, (double)longitude, (double)radius);
```
or
```
StorableLocationFence.exiting((double)latitude, (double)longitude, (double)radius);
```
or
```
StorableLocationFence.in((double)latitude, (double)longitude, (double)radius, (long)long dwellTimeMillis);
```

#### Activity fence

The StorableActivityFence is representing a [DetectedActivityFence](https://developers.google.com/android/reference/com/google/android/gms/awareness/fence/DetectedActivityFence) in the Awareness API.

You can create it with:

```
StorableActivityFence.starting((@ActivityType)activity1, (@ActivityType)activity2);
```
or
```
StorableActivityFence.stopping((@ActivityType)activity1, (@ActivityType)activity2);
```
or
```
StorableActivityFence.during((@ActivityType)activity1, (@ActivityType)activity2);
```

#### Time fence

The StorableTimeFence is representing a [TimeFence](https://developers.google.com/android/reference/com/google/android/gms/awareness/fence/TimeFence) in the Awareness API.

You can create it with:

```
StorableTimeFence.inInterval((long)startTimeMillis, (long)stopTimeMillis);
```
or
```
StorableTimeFence.inDailyInterval((TimeZone)timeZone, (long)startTimeOfDayMillis, (long)stopTimeOfDayMillis);
```
or
```
StorableTimeFence.inIntervalOfDay(TimeFence.DAY_OF_WEEK_MONDAY, (TimeZone)timeZone, (long)startTimeOfDayMillis, (long)stopTimeOfDayMillis);
```
or
```
StorableTimeFence.inTimeInterval(TimeFence.TIME_INTERVAL_WEEKDAY);
```
or
```
StorableTimeFence.aroundTimeInstant(TimeFence.TIME_INSTANT_SUNRISE, (long)startOffsetMillis, (long)stopOffsetMillis);
```

#### Headphone fence

The StorableHeadphoneFence is representing a [HeadphoneFence](https://developers.google.com/android/reference/com/google/android/gms/awareness/fence/HeadphoneFence) in the Awareness API.

You can create it with:

```
StorableHeadphoneFence.during(state);
```
or
```
StorableHeadphoneFence.pluggingIn();
```
or
```
StorableHeadphoneFence.unplugging();
```

#### Meta fence

You can combine fences between them to create complex meta fences. 

You can create it with:

```
StorableFence.and((Collection<StorableFence>)fences);
```
or
```
StorableFence.or((Collection<StorableFence>)fences);
```
or
```
StorableFence.not((StorableFence)fence);
```

### Add a fence to the StorableFenceManager

Once the fence is created, you'll have to add it to the StorableFenceManager. This action will also add it, as soon as possible, to the Play Services through the Awareness API.

**Before calling addFence with a StorableLocationFence or a META fence containing a StorableLocationFence, you should check if you the permission `ACCESS_FINE_LOCATION` is granted.**

```
mGeofenceManager.addFence(uniqueId, resultFence, additionalData, receiverClassName);
```

*receiverClassName* is the name of the class that will be called when the fence is triggered by Android. It should inherits from IntentService.<br/>
*additionalData* is an HashMap<String, Object> which provides additional data. The values should be of the following types: String, Long, Integer, Double or Boolean.

After this call, the *fenceAddStatus* callback will be called to inform you about the status of the Fence.

## Run the example

To run the example, you just have to add your own Google API key.
To do that, go to the [Google API console](https://console.developers.google.com/iam-admin/projects), create a project if not done already.

Then, open your project and go to Library. Enable (click on the API and click on Enable) the Google Maps API and Awareness API.
After that, go to Credentials and create one (Create credentials -> API key -> Android key).

Finally, replace the missing API key in the [example's manifest](https://github.com/djavan-bertrand/JCVD/tree/master/example/src/main/AndroidManifest.xml).

You can now install the app on your device, see the map and add fences.

## Questions

Feel free to ask your questions to [@Djava7](https://twitter.com/Djava7).<br/>
You can also [open an issue on Github](https://github.com/djavan-bertrand/JCVD/issues/new).

## TODOs

Here is a list of known TODOs. If you think an improvement is missing, feel free to [open an issue](https://github.com/djavan-bertrand/JCVD/issues/new).

* Improve demo
* Support BeaconFence
