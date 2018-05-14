---
LiveData Overview
---

LiveData是一个可观察的数据持有者类。与常规的可观察性不同，LiveData是对生命周期感知的，这意味着它遵从其他应用程序组件的生命周期，比如Activity、Fragment或Service。这种感知确保LiveData只更新处于活动生命周期状态的应用程序组件观察者。

> **注**：向项目中导入`LiveData`组件，参考[向项目中添加组件](https://developer.android.google.cn/topic/libraries/architecture/adding-components.html)

LiveData作为一个观察者，它由observer类表示，如果它的生命周期处于`STARTED`或`RESUMED`状态，则表示处于活动状态。`LiveData`只通知活跃的观察者进行更新。非活动的观察者不会被通知更新。

您可以注册一个与实现了`LifecycleOwner`接口的对象相配对的观察者。这种关系可以在当对应的`Lifecycle`对象的状态变为`DESTROYED`时删除观察者。这对于`Activity`和`Fragment`尤其有用，因为它们可以安全地观察到LiveData对象，而不用担心内存泄漏。当它们的生命周期被销毁时，它们就会立即被取消订阅。

更多关于怎样使用`LiveData`，参考**处理LiveData对象**

**使用LiveData的优点**：

使用`LiveData`有以下优点：

* **确保UI和数据状态相匹配**
<br>LiveData遵循观察者模式。当生命周期状态发生变化时，`LiveData`会通知`Observer`对象。您可以在这些`Observer`对象中合并更新UI的代码。你的观察者可以在每次发生改变时更新UI而不是在每次应用程序数据发生改变时才更新UI。

* **没有内存泄露**
<br>观察者会和`Lifecycle`对象绑定，然后会在其关联的生命周期被销毁后进行清理。

* **不会因为活动停止而崩溃**
<br>如果观察者的生命周期是不活动的（例如在回退栈中的活动），那么它就不会接收到任何`LiveData`事件。

* **不再需要手动处理生命周期**
<br>UI组件只观察相关数据，不会停止或恢复观察。因为在进行观察时它知道相关的生命周期状态的变化，所以`LiveData`会自动管理所有。

* **总是最新的数据**
<br>如果一个生命周期变得不活动，它将会在再次激活时接收最新的数据。例如，后台的某个活动在返回到前台后会接收最新的数据。

* **适配配置更改**
<br>如果一个`Activity`或`Fragment`因为配置更改而重建时（如设备旋转），它会立即接收到最新的可用数据。

* **共享资源**
<br>您可以使用单例模式来扩展`LiveData`对象来包装系统服务，以便它们可以在您的应用程序中共享。`LiveData`对象连接到系统服务一次，然后任何需要该资源的观察者都只需要观察`LiveData`对象。有关更多信息，请参见 **扩展LiveData**。

### 处理LiveData对象

遵循以下步骤来处理`LiveData`对象：

1. 创建一个`LiveData`的实例来持有某种类型的数据。这通常在`ViewModel`类中进行。

2. 创建一个定义了`onChanged()`方法的`Observer`对象，该方法控制在`LiveData`对象的数据更改时所发生的情况。通常在UI控制器中创建一个`Observer`对象，比如`Activity`或`Fragment`。

3. 使用`observe()`方法将`Observer`对象附加到`LiveData`对象上。`observe()`方法带有一个`LifecycleOwner`对象。这就 `Observer`对象就订阅了`LiveData`对象，这样它就会被通知更改。通常在UI控制器中附加Observer对象，比如`Activity`或`Fragment`。

> **注**：你可以使用`observeForever(observer)`方法注册一个没有关联的`LifecycleOwner`对象的观察者。在这种情况下，观察者被认为总是活跃的，因此总是会被告知修改。你可以调用`removeObserver(Observer)`方法删除这些观察者。

当你更新存储在LiveData对象中的值时，只要附加的`LifecycleOwner`处于活动状态，它就会触发所有注册的观察者。

`LiveData`允许UI控制器的观察者订阅更新。当`LiveData`对象所持有的数据发生变化时，UI会自动响应更新。

**创建LiveData对象**

`LiveData`是一个可以与任何数据一起使用的包装器，包括实现`Collections`的对象，如`List`。`LiveData`对象通常存储在`ViewModel`对象中，并通过getter方法访问，如下例所示:

```java
public class NameViewModel extends ViewModel {

// Create a LiveData with a String
private MutableLiveData<String> mCurrentName;

    public MutableLiveData<String> getCurrentName() {
        if (mCurrentName == null) {
            mCurrentName = new MutableLiveData<String>();
        }
        return mCurrentName;
    }

// Rest of the ViewModel...
}
```

初始化时，`LiveData`中的数据没有赋值。

> **注**：确保在`ViewModel`对象中存储更新UI的`LiveData`对象，而不是`Activity`或`Fragment`，原因如下:
> 
> * 避免使`Activity`和`Fragment`臃肿。现在这些UI控制器负责显示数据，但不负责保存数据状态。
> 
> * 将`LiveData`实例与特定的`Activity`或`Fragment`实例解耦，来允许`LiveData`对象在配置更改时存活。

您可以在[ViewModel指南](https://developer.android.google.cn/topic/libraries/architecture/viewmodel.html)中阅读更多关于`ViewModel`类的好处和用法。

** 观察LiveData对象

在大多数情况下，开始观察`LiveData`应该在应用程序组件的`onCreate()`方法中，原因如下:

* 确保系统不会从`Activity`或`Fragment`的`onResume()`方法中进行冗余的调用。

* 确保`Activity`或`Fragment`具有可以在激活时尽快用于可以显示的数据。一旦应用程序组件处于`STARTED`状态，它将从它所观察的`LiveData`对象中接收最近的值。这只会发生在被观察的`LiveData`对象已经被赋值的情况下。

一般来说，`LiveData`只在数据更改时才会提供更新，并且只提供给出于活动状态的观察者。但有一个例外，当观察者从非活动状态变为活动状态时，也会收到更新。此外，如果观察者第二次从非活动状态变为活动状态，那么它只会接收到自上次激活后值发生变化的更新。

下面的示例代码演示了如何开始观察`LiveData`对象:

```java
public class NameActivity extends AppCompatActivity {

    private NameViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Other code to setup the activity...

        // Get the ViewModel.
        mModel = ViewModelProviders.of(this).get(NameViewModel.class);


        // Create the observer which updates the UI.
        final Observer<String> nameObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                // Update the UI, in this case, a TextView.
                mNameTextView.setText(newName);
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mModel.getCurrentName().observe(this, nameObserver);
    }
}
```

在把`nameObserver`当做参数调用`observe()`方法之后，`onChanged()`立即被调用以提供当前存储在`mCurrentName`中的最新值。如果`LiveData`对象没有设置`mCurrentName`的值，那么`onChanged()`就不会被调用。

**更新LiveData对象**

`LiveData`没有公开的方法来更新存储的数据。`MutableLiveData`对外暴露`setValue(T)`和`postValue(T)`方法，如果需要编辑存储在`LiveData`对象中的值，则必须使用这些方法。通常在`ViewModel`中使用`MutableLiveData`，然后`ViewModel`只向观察者公开不可变的`LiveData`对象。

在建立了观察者关系之后，您就可以更新`LiveData`对象的值，如下例所示，当用户点击一个按钮时，它会触发所有观察者:

```java
mButton.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        String anotherName = "John Doe";
        mModel.getCurrentName().setValue(anotherName);
    }
});
```

在示例中调用`setValue(T)`会导致观察者调用`onChanged()`方法。该示例显示了按下按钮触发，但是有很多种原因可以调用`setValue()`或`postValue()`来更新`mName`，包括响应网络请求或完成数据库加载;在所有情况下，对`setValue()`或`postValue()`的调用会触发观察者并更新UI。

> **注**：在主线程中你必须调用`setValue(T)`方法更新LiveData对象。如果代码是在一个工作线程中执行的，那么你可以使用`postValue(T)`方法来更新`LiveData`对象。

**和Room一起使用LiveData**

`Room`持久化库支持可观察的查询，该查询返回`LiveData`对象。可观察查询是作为数据库访问对象(DAO)的一部分编写的。

当数据库更新时，`Room`会生成所有必需的代码来更新`LiveData`对象。生成的代码在需要时在后台线程上异步运行。此模式有助于将显示在UI中的数据与存储在数据库中的数据进行同步。您可以在[Room持久化库指南](https://developer.android.google.cn/topic/libraries/architecture/room.html)中了解更多`Room`和`DAOs`。

### 扩展LiveData

如果观察者的生命周期是在`STARTED`状态或`RESUMED`状态中，那么`LiveData`就认为一个观察者处于活动状态，下面的示例代码说明了如何扩展`LiveData`类:

```java
public class StockLiveData extends LiveData<BigDecimal> {
    private StockManager mStockManager;

    private SimplePriceListener mListener = new SimplePriceListener() {
        @Override
        public void onPriceChanged(BigDecimal price) {
            setValue(price);
        }
    };

    public StockLiveData(String symbol) {
        mStockManager = new StockManager(symbol);
    }

    @Override
    protected void onActive() {
        mStockManager.requestPriceUpdates(mListener);
    }

    @Override
    protected void onInactive() {
        mStockManager.removeUpdates(mListener);
    }
}
```

本例中价格监听器的实现包括以下重要方法:

* 当`LiveData`对象有一个活跃状态的观察者时，会调用`onActive()`方法。这意味着您需要从这个方法开始观察股票价格的更新。

* 当`LiveData`对象没有任何一个活跃状态的观察者时，调用`onInactive()`方法。由于没有观察者在听，所以没有理由保持与`StockManager`服务的联系。

* `setValue(T)`方法更新`LiveData`实例的值，并将更改通知到任何活跃的观察者。

你可以向下面一样使用`StockLiveData`类：

```java
public class MyFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LiveData<BigDecimal> myPriceListener = ...;
        myPriceListener.observe(this, price -> {
            // Update the UI.
        });
    }
}
```

`observe()`方法把`Fragment`对象作为第一个参传入，这是`LIfecycleOwner`的一个实例数。这样做意味着该观察者绑定到与所有者关联的`Lifecycle`对象，这意味着:

* 如果`Lifecycle`对象不在活跃状态，即使值发生了改变观察者页不会被调用。

* 在`Lifecycle`对象被销毁后，观察者也会自动地被移除。

事实上`LiveData`对象是生命周期感知的，这意味着您可以在多个`Activity`、`Fragment`和`Service`之间共享它们。为了保持示例的简单性，可以向下面一样将`LiveData`类作为一个单例来实现:

```java
public class StockLiveData extends LiveData<BigDecimal> {
    private static StockLiveData sInstance;
    private StockManager mStockManager;

    private SimplePriceListener mListener = new SimplePriceListener() {
        @Override
        public void onPriceChanged(BigDecimal price) {
            setValue(price);
        }
    };

    @MainThread
    public static StockLiveData get(String symbol) {
        if (sInstance == null) {
            sInstance = new StockLiveData(symbol);
        }
        return sInstance;
    }

    private StockLiveData(String symbol) {
        mStockManager = new StockManager(symbol);
    }

    @Override
    protected void onActive() {
        mStockManager.requestPriceUpdates(mListener);
    }

    @Override
    protected void onInactive() {
        mStockManager.removeUpdates(mListener);
    }
}
```

然后在`Fragment`中使用：

```java
public class MyFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        StockLiveData.get(getActivity()).observe(this, price -> {
            // Update the UI.
        });
    }
}
```

多个`Fragment`和`Activity`可以观察`MyPriceListener`实例。`LiveData`只会在其中一个或多个是可见并且活跃时连接到系统服务。

### LiveData变换

如果你想要在把变化发送给观察者之前改变存储在`LiveData`中的值，或者你可能只需要根据另一个实例的值返回一个不同的`LiveData`实例。`Lifecycle`包中提供了`Transformations`类来支持这些场景。

`Transformations.map()`

对存储在`LiveData`对象中的值应用一个函数，并将结果传播到下游。

```java
LiveData<User> userLiveData = ...;
LiveData<String> userName = Transformations.map(userLiveData, user -> {
    user.name + " " + user.lastName
});
```

`Transformation.switchMap()`

和`map()`类似，将函数应用到存储在`LiveData`对象中的值，并将结果发送到下游。传入`switchMap()`的函数必须返回一个`LiveData`对象，如下例所示:

```java
private LiveData<User> getUser(String id) {
  ...;
}

LiveData<String> userId = ...;
LiveData<User> user = Transformations.switchMap(userId, id -> getUser(id) );
```

您可以使用转换方法在观察者的生命周期中传递信息。除非一个观察者正在观察返回的`LiveData`对象，否则不会进行转换计算。因为这些转换是延迟计算的，所以生命周期相关的行为是隐式传递的，不需要额外的显式调用或依赖项。

如果您认为在`ViewModel`对象中需要一个`Lifecycle`对象，那么转换可能是较好的解决方案。例如，假设您有一个接受地址并返回该地址的邮政编码的UI组件。您可以按照下面的示例代码来实现这个组件的ViewModel:

```java
class MyViewModel extends ViewModel {
    private final PostalCodeRepository repository;
    public MyViewModel(PostalCodeRepository repository) {
       this.repository = repository;
    }

    private LiveData<String> getPostalCode(String address) {
       // DON'T DO THIS
       return repository.getPostCode(address);
    }
}
```

然后，UI组件需要在每次调用getPostalCode()时从之前的LiveData对象中注销，并注册到新实例。此外，如果UI组件重建，它将再次调用`repository.getPostCode()`方法，而不是使用之前的调用结果。

相反，您可以将邮政编码查找实现作为地址输入的转换，如下面的示例所示。

```java
class MyViewModel extends ViewModel {
    private final PostalCodeRepository repository;
    private final MutableLiveData<String> addressInput = new MutableLiveData();
    public final LiveData<String> postalCode =
            Transformations.switchMap(addressInput, (address) -> {
                return repository.getPostCode(address);
             });

  public MyViewModel(PostalCodeRepository repository) {
      this.repository = repository
  }

  private void setInput(String address) {
      addressInput.setValue(address);
  }
}
```

在这种情况下，`postalCode`字段是public和final的，因为字段从不更改。`postalCode`字段被定义为`addressInput`的一个转换，这意味着当`addressInput`更改时将调用`repository.getPostCode()`方法。This is true if there is an active observer, if there are no active observers at the time repository.getPostCode() is called, no calculations are made until an observer is added.（没太明白怎么翻译。。。意思应该是：如果在调用`repository.getPostCode()`方法时没有活跃的观察者，直到添加观察者之前都不会进行任何计算）

该机制允许较低级别的应用程序创建基于需求的延迟计算的`LiveData`对象。`ViewModel`对象可以很容易地获得对`LiveData`对象的引用，然后在它们之上定义转换规则。

**创建新的转换**

有很多不同的特定转换可能在你的APP中有用，但它们不是默认提供的。要实现您自己的转换，您可以使用`MediatorLiveData`类，该类监听其他`LiveData`对象并处理由它们发出的事件。`MediatorLiveData`正确地将其状态传播到源`LiveData`对象。要了解更多关于此模式的信息，请参阅[转换](https://developer.android.google.cn/reference/android/arch/lifecycle/Transformations.html)类的参考文档。

### 合并多个LiveData源

`MediatorLiveData`是`LiveData`的一个子类，它允许您合并多个`LiveData`源。当任何原始的`LiveData`源对象发生变化时，就会触发`MediatorLiveData`对象的观察者。

例如，如果您的UI中有一个可以从本地数据库或网络进行更新的`LiveData`对象，那么您可以将以下源添加到`MediatorLiveData`对象:

* 与存储在数据库中的数据相关联的`LiveData`对象。

* 与从网络访问的数据相关联的`LiveData`对象。

您的活动只需要观察`MediatorLiveData`对象，以便从两个源接收更新。有关详细的示例，请参见[应用程序架构指南](https://developer.android.google.cn/topic/libraries/architecture/guide.html)中的[附录:公开网络状态](https://developer.android.google.cn/topic/libraries/architecture/guide.html#addendum)部分。