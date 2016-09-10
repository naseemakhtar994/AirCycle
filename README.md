# AirCycle 

Binds Android Activity lifecycle callbacks to Activity fields annotated with `@AirCycle`. 

Fields annotated with `@AirCycle` that are defined in an Activity will receive lifecycle callbacks of the enclosing Activity. 
The Activity does NOT need to implement any interface or extend any specific base class.

The field itself also does NOT need to implement any interface or extend any specific base class.

The binding class is generated in compile time using Java annotation processing, NO reflection is used at runtime.


## Usage

```java
public class MyActivity extends AppCompatActivity {

    @AirCycle LifecycleListener myListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyActivityAirCycle.bind(this); // generated by the AirCycle annotation processor
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        myListener = new LifecycleListener();
    }
}
```

```java
public class LifecycleListener {

    // no arguments 
    public void onCreate() {
        // The bound MyActivity was created
    }

    // can also pass the Activity
    public void onCreate(MyActivity activity) {
        // The bound MyActivity was created
    }

    // or just a Bundle 
    public void onCreate(Bundle bundle) {
        // The bound MyActivity was created
    }

    // ...

    // ActivityLifecycleCallbacks method naming is also supported
    public void onActivityDestroyed(AppCompatActivity activity, Bundle bundle) {
        // The bound MyActivity was destroyed
    }

    // can pass the base Activity as well
    public void onDestroy(AppCompatActivity activity) {
        // The bound MyActivity was destroyed
    }
    
    // arguments order does not matter in listener methods
    public void onDestroy(Bundle bundle, AppCompatActivity activity) {
        // The bound MyActivity was destroyed
    }
}
```

AirCycle annotation processor will generate a binding class for the Activity, named &lt;YourActivity&gt;AirCycle with a static `bind` method.

You **MUST** call `MyActivityAirCycle.bind(this)` BEFORE calling `super.onCreate(savedInstanceState)`, otherwise the bound listener will NOT receive the first onCreate call for the Activity.

## Activity lifecycle callbacks

The following [Activity lifecycle callbacks](https://developer.android.com/reference/android/app/Application.ActivityLifecycleCallbacks.html) are supported:

- onCreate() (with optional *Bundle savedInstanceState* argument)
- onStart()
- onResume()
- onPause()
- onStop()
- onSaveInstanceState() (with optional *Bundle outState* argument)
- onDestroy()


## Listener

Fields annotated with `@AirCycle` can be plain Java classes or interfaces (POJOs) that have public lifecycle callback methods. 
Method name defines the bound Activity lifecycle callback.

 lifecycle callback   | listener method      | alternative listener method  | optional Bundle arg?
----------------------|----------------------|------------------------------|--------------------------
onCreate()            |onCreate()            |onActivityCreated()           | &#10003;
onStart()             |onStart()             |onActivityStarted()           | -
onResume()            |onResume()            |onActivityResumed()           | -
onPause()             |onPause()             |onActivityPaused()            | -
onStop()              |onStop()              |onActivityStopped()           | -
onSaveInstanceState() |onSaveInstanceState() |onActivitySaveInstanceState() | &#10003;
onDestroy()           |onDestroy()           |onActivityDestroyed()         | -

**ALL** listener methods can optionally have the bound Activity instance as the method argument.
Unlike method names, argument names do NOT matter.
 

### Built-in listeners
The following listener interfaces are bundled with the library: 

- [ActivityAirCycle](aircycle-api/src/main/java/com/github/simonpercic/aircycle/ActivityAirCycle.java)
- [ActivityBundleAirCycle](aircycle-api/src/main/java/com/github/simonpercic/aircycle/ActivityBundleAirCycle.java)
- [ActivityPassAirCycle](aircycle-api/src/main/java/com/github/simonpercic/aircycle/ActivityPassAirCycle.java)

### ActivityLifecycleCallbacks
Since its method names are also supported by AirCycle, you can also use [Application.ActivityLifecycleCallbacks](https://developer.android.com/reference/android/app/Application.ActivityLifecycleCallbacks.html) as a bound listener.

### Custom listeners
Any class or interface that has at least one listener method can be bound as an AirCycle listener.

[Custom listener](example/src/main/java/com/github/simonpercic/example/aircycle/activity/a7/CustomListener.java) from the example app:
```java
public class CustomListener {

    public void onCreate() {
        
    }

    public void onCreate(CustomListenerActivity activity) {
        
    }

    public void onCreate(Bundle bundle) {
        
    }

    public void onSaveInstanceState(AppCompatActivity activity, Bundle bundle) {
        
    }

    public void onActivitySaveInstanceState(Bundle bundle, Activity activity) {
        
    }
}
```

#### Multiple methods
As seen above, multiple methods are supported for the same lifecycle callback (either overriden with different arguments or using an alternative supported name).
The order and naming of arguments is NOT important.
No arguments are necessary for any lifecycle method.


## Multiple listeners
A single Activity can have multiple fields bound to its lifecycle.

[Multiple listeners Activity](example/src/main/java/com/github/simonpercic/example/aircycle/activity/a6/MultipleListenersActivity.java) from the example app:

```java
public class MultipleListenersActivity extends AppCompatActivity {

    @AirCycle ActivityAirCycleLogger airCycleLogger;
    @AirCycle final ActivityBundleAirCycleLogger bundleAirCycleLogger = new ActivityBundleAirCycleLogger();
    
    // ...
}
```


## How does it work?
AirCycle generates one binding class per-Activity containing 1 or more `@AirCycle` annotated field(s). 
Upon calling `bind` on a generated class passing the Activity instance, [Application.ActivityLifecycleCallbacks](https://developer.android.com/reference/android/app/Application.ActivityLifecycleCallbacks.html) are registered on the Activity's app.
These are automatically unregistered for the Activity once onDestroy is called.

The listener callbacks are invoked only if the listener instance is not null and are dispatched only to their respective bound listener instances.


## ProTip
Annotate your Dagger injected presenters to automatically bind them to the Activity lifecycle, i.e.
```java
@AirCycle @Inject MyPresenter presenter;
```


## Reflection-less
AirCycle uses compile time annotation processing to generate Activity binding classes, NO reflection is used at runtime.


## ProGuard
Since all classes are generated in compile time and no reflection is used in runtime, you can safely use AirCycle with ProGuard.


## Include in your project

```groovy
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  compile 'TODO'
  apt 'TODO'
}
```

## Inspiration and motivation
Inspired by [SoundCloud's](https://developers.soundcloud.com/blog/category/mobile) [LightCycle](https://github.com/soundcloud/lightcycle) library. Kudos to those guys.

Compared to SoundCloud's library, AirCycle supports passing of Activity lifecycle callbacks without the need to extend from a specific base Activity.
Additionally, the listener classes are completely flexible, without the need to extend from any class or interface. 
This enables more flexibility when developing and further promotes composition over inheritance.

The cost of this flexibility is only supporting Activity lifecycle callbacks included in [Application.ActivityLifecycleCallbacks](https://developer.android.com/reference/android/app/Application.ActivityLifecycleCallbacks.html) (since API level 14) and no support for Fragment lifecycle callbacks.


## Change Log
See [CHANGELOG.md](CHANGELOG.md)


## License
Open source, distributed under the MIT License. See [LICENSE](LICENSE) for details.
