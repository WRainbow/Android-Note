---
使用可感知生命周期的组件处理生命周期
---

生命周期感知组件在响应另一个组件(例如Activity和Fragment)的生命周期状态的变化时执行操作。这些组件可以帮助您生成更有组织的、通常更轻量级的代码，这样更容易维护。

常见的模式是在Activity和Fragment的生命周期方法中实现依赖组件的操作。然而，这种模式导致了代码的组织不完善和错误的扩散。通过使用生命周期感知组件，您可以将依赖组件的代码从生命周期方法中转移到组件本身中。

`android.arch.lifecycle`包下提供了类和接口，让您构建生命周期感知组件——这些组件可以根据活动或片段的当前生命周期状态自动调整其行为。

> 想要导入`android.arch.lifecycle`包到项目中，可参考[向项目中添加组件](https://developer.android.google.cn/topic/libraries/architecture/adding-components)

Android框架中定义的大多数应用程序组件都有生命周期。生命周期由操作系统或运行在进程中的框架代码来管理。它们是Android工作的核心，你的应用程序必须遵从它们。不这样做可能会触发内存泄漏，甚至应用程序崩溃。

假设有用来在屏幕上显示设备位置的Activity。常见的实现可能如下所示:

```java
class MyLocationListener {
    public MyLocationListener(Context context, Callback callback) {
        // ...
    }

    void start() {
        // connect to system location service
    }

    void stop() {
        // disconnect from system location service
    }
}


class MyActivity extends AppCompatActivity {
    private MyLocationListener myLocationListener;

    @Override
    public void onCreate(...) {
        myLocationListener = new MyLocationListener(this, (location) -> {
            // update UI
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        myLocationListener.start();
        // manage other components that need to respond
        // to the activity lifecycle
    }

    @Override
    public void onStop() {
        super.onStop();
        myLocationListener.stop();
        // manage other components that need to respond
        // to the activity lifecycle
    }
}
```

尽管这个示例看起来很好，但在一个真正的应用程序中，你最终会有过多的调用管理UI方法和其他组件以响应生命周期的当前状态。管理多个组件在生命周期方法中放置了大量的代码，例如`onStart()`和`onStop()`，会使得它们难以维护。

此外，不能保证组件在Activity或Fragment停止之前启动。尤其是如果我们需要执行长时间运行的操作，比如onStart()中的一些配置检查。这可能导致紊乱，像onStop()方法在onStart()之前完成，这会使组件的存活时间超过所需时间。

```java
class MyActivity extends AppCompatActivity {
    private MyLocationListener myLocationListener;

    public void onCreate(...) {
        myLocationListener = new MyLocationListener(this, location -> {
            // update UI
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Util.checkUserStatus(result -> {
            // what if this callback is invoked AFTER activity is stopped?
            if (result) {
                myLocationListener.start();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        myLocationListener.stop();
    }
}
```

`android.arch.lifecycle`包提供了帮助您以一种弹性和隔离的方式解决这些问题的类和接口。

### Lifecycle

`Lifecycle`是一个类，它包含一个组件的生命周期状态的信息(比如一个`Activity`或一个`Fragment`)，并允许其他对象观察这个状态。

`Lifecycle`使用两个主要方面来跟踪其关联组件的生命周期状态:

**事件**
> 生命周期事件从框架和生命周期类中发送。这些事件映射到Activity和Fragment中的回调事件。

**状态**
> 通过Lifecycle对象来追踪组件的当前状态

![lifecycle-states.png](https://developer.android.google.cn/images/topic/libraries/architecture/lifecycle-states.png)

将状态视为图的节点，将事件视为这些节点之间的边。

类可以通过向其方法添加注释来监视组件的生命周期状态。然后，您可以通过调用`Lifecycle`类中的`addObserver()`方法来添加一个观察者并传入观察者的实例，如下面的例子所示:

```java
public class MyObserver implements LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connectListener() {
        ...
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnectListener() {
        ...
    }
}

myLifecycleOwner.getLifecycle().addObserver(new MyObserver());
```

在上面的例子中，`myLifecycleOwner`对象实现了`LifecycleOwner`接口，下面的部分中对这个接口进行了解释。

### LifecycleOwner

`LifecycleOwner`是一个只有方法的接口，它表示类拥有一个`Lifecycle`。它有一个必须实现的方法`getLifecycle()`。如果你试图管理整个`Application`的生命周期，请参见[ProcessLifecycleOwner](https://developer.android.google.cn/reference/android/arch/lifecycle/ProcessLifecycleOwner.html)。

该接口抽象了单个类(例如`Fragment`和`AppCompatActivity`)的生命周期的所有权，并允许编写与它们一起工作的组件。任何自定义`Application`类都可以实现`LifecycleOwner`接口。

实现生命周期的组件与实现生命周期的组件无缝地工作，因为所有者可以提供一个生命周期供一个观察者来注册观察。

对于位置跟踪示例来说，我们可以使`MyLocationListener`类实现`LifecycleObserver`，然后在Activity生命周期的onCreate()方法中初始化它。这允许MyLocationListener类实现自给自足，即在`MyLocationListener`中声明了对生命周期状态变化做出反应的逻辑，而不是在活动中声明。让单个组件存储它们自己的逻辑使活动和片段逻辑更易于管理。

```java
class MyActivity extends AppCompatActivity {
    private MyLocationListener myLocationListener;

    public void onCreate(...) {
        myLocationListener = new MyLocationListener(this, getLifecycle(), location -> {
            // update UI
        });
        Util.checkUserStatus(result -> {
            if (result) {
                myLocationListener.enable();
            }
        });
  }
}
```

一个常见的用例是，如果`Lifecycle`不处于良好状态，则避免调用某些回调。例如，如果一个回调是在Activity状态保存后运行一个Fragment事务，它将触发一个崩溃，那么我们永远不会调用那个回调。

为了简化这个用例，`Lifecycle`类允许其他对象查询当前状态。

```java
class MyLocationListener implements LifecycleObserver {
    private boolean enabled = false;
    public MyLocationListener(Context context, Lifecycle lifecycle, Callback callback) {
       ...
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void start() {
        if (enabled) {
           // connect
        }
    }

    public void enable() {
        enabled = true;
        if (lifecycle.getCurrentState().isAtLeast(STARTED)) {
            // connect if not connected
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void stop() {
        // disconnect if connected
    }
}
```

通过这个实现，我们的`LocationListener`类是完全对生命周期感知的。如果我们在从另一个Activity或Fragment中使用`LocationListener`，我们只需要初始化它。所有的组装(setup)和拆卸(teardown)操作都由类本身管理。

如果一个库提供了需要使用Android生命周期的类，那么我们建议您使用生命周期感知组件。您的库可以轻松地集成这些组件，而无需在客户端手动的对生命周期管理。

**实现自定义的`LifecycleOwner`**

在`Support Library 26.1.0`中的Fragment和Activity已经实现了LifecycleOwner接口。

如果你有一个自定义的类而且想要创建一个`LifecycleOwner`，你可以使用`LifecycleRegistry`类，但是你需要将事件转发到该类，如下面的代码示例所示:

```java
public class MyActivity extends Activity implements LifecycleOwner {
    private LifecycleRegistry mLifecycleRegistry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}
```

### 生命周期感知组件的最佳实践

* 保持UI控制器(Activity和Fragment)尽可能精简。他们不应该试图获取他们自己的数据，而是使用`ViewModel`来实现这一点，并且观察一个`LiveData`对象，以将更改反映到View上。

* 尝试编写数据驱动的UI, UI控制器的职责是在数据更改时更新视图，或者将用户操作更新到ViewModel。

* 将数据逻辑放在`ViewModel`类中。`ViewModel`应该作为UI控制器和其他应用程序之间的连接器。但获取数据并不是`ViewModel`的职责(例如，从网络中获取数据)。`ViewModel`应该调用适当的组件来获取数据，然后将结果返回给UI控制器。

* 使用`Data Binding`来维护视图和UI控制器之间的简洁。这使您可以使您的View更具声明性，并且让在Activity和Fragment中的更新代码最小化。如果您喜欢使用Java这样做，可以使用像`Butter Knife`这样的库来避免样板代码，并有更好的抽象。

* 如果您的UI很复杂，可以考虑创建一个`presenter`类来处理UI的更改。这可能比较费力，但它可以使您的UI组件更容易测试。

* 避免在`ViewModel`中引用`View`或`Activity`上下文对象。如果`ViewModel`超过了Activity(在配置更改的情况下)，那么`Activity`就会发生泄漏，进而垃圾收集器不会正确地处理它。

### 生命周期感知组件的用例

生命周期感知组件可以使您在各种情况下更容易地管理生命周期。比如:

* 在粗略和精确的位置更新之间切换。当你的定位应用程序可见时，使用生命周期感知组件来启用精确的位置更新，而当应用程序在后台时，切换到粗略的更新。`LiveData`是一个生命周期感知组件，可以在当你的位置改变时，应用程序自动更新用户界面。

* 停止和启动视频缓冲。使用生命周期感知组件尽快的启动视频缓冲，但一直延迟到应用程序完全启动后才开始播放。当应用程序被销毁时，你还可以使用生命周期感知组件来中断缓冲。

* 启动和停止网络连接。当应用程序在前台时，使用生命周期感知组件来激活网络数据的实时更新(流)，当应用程序进入后台时也会自动暂停。

* 暂停和恢复动画绘制。当应用程序在后台运行时，使用生命周期感知组件来暂停动画绘制，当应用程序在前台后恢复绘制。

### 停止事件的处理

当一个`Lifecycle`属于一个`AppCompatActivity`或`Fragment`时，`Lifecycle`的状态会变为`CREATED`；而当`AppCompatActivity`或`Fragment`的`onSaveInstanceState()`被调用时，会发送`ON_STOP`事件。

当一个`Fragment`或`AppCompatActivity`的状态通过`onSaveInstanceState()`保存时，直到ON_START被调用，它的UI都会被认为是不可变的。在保存状态后尝试修改UI可能会导致应用程序的状态不一致，这就是为什么在保存了状态后执行`FragmentTrasaction`，`FragmentManager`会抛出一个异常。有关详细信息,参阅[commit()](https://developer.android.google.cn/reference/android/support/v4/app/FragmentTransaction#commit())。

如果与观察者的相联系的`Lifecycle`的状态至少不是`STARTED`，LiveData就可以避免通过调用它的观察者来避免这种边缘情况。内部，在决定调用它的观察者之前会调用`isAtLeast()`方法。

不幸的是，`AppCompatActivity`的`onStop()`方法是在`onSaveInstanceState()`之后调用的，这留下了一个不允许UI状态更改，但是`Lifecycle`还没有被改为`CREATED`状态的间隙。

为了防止这个问题，`Lifecycle`类在beta2和更低版本会在不发送事件的情况下把状态标记为`CREATED`，这样即使在系统调用`onStop()`之前事件不会被发送，任何检查当前状态的代码也都会得到真实的值。

不幸的是，这个解决方式有两个主要的问题：

* 在API23及以下，实际上，即使一部分被另一个Activity覆盖，Android系统也会保存Activity的状态。换句话说，Android系统调用`onSaveInstanceState()`，但它并不一定调用onStop()。这可能产生一个较长的时间间隔，这个时间内，即使它的UI状态不能被修改，观察者也会认为生命周期是活动的。

* Any class that wants to expose a similar behavior to the LiveData class has to implement the workaround provided by Lifecycle version beta 2 and lower.(翻译？任何希望向`LiveData`类公开类似行为的类都必须实现`Lifecycle`版本beta 2和更低版本提供的解决方案。)

> **注意** 为了使这个流程更简单，并提供与旧版本更好的兼容性，从版本1.0.0-rc1开始，`Lifecycle`对象被标记为`CREATED`，`ON_STOP`在调用`onSaveInstanceState()`时被发送，而无需等待`onStop()`方法的调用。这不大可能影响您的代码，但是您需要注意的是，它与API级别26和以下的Activity类中的调用顺序不匹配。