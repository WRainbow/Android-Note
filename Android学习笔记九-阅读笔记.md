Android学习笔记九（Android开发艺术探索）：

# Activity的启动模式：

#### standard: 标准模式（默认模式）  
每次启动一个Activity都会重新创建一个新的实例，不管这个实例是否已经存在。`onCreate`、`onStart`、`onResume`方法都会被调用，一种典型的多实例实现（一个任务栈中可以有多个实例，每个实例也可以属于不同的任务栈）。

#### singleTop：栈顶复用模式

Intent的flag为：`FLAG_ACTIVITY_SINGLE_TOP`

如果新Activity已经位于任务栈的栈顶，那么此Activity不会被重新创建，同时它的`onNewIntent`方法会被回调，通过此方法的参数可以取出当前请求的信息。Activity的`onCreate`、`onStart`不会被系统调用（因为没有发生改变）。

#### singleTask：栈内复用模式

Intent的flag为：`FLAG_ACTIVITY_NEW_TASK`

一种单实例模式。只要Activity在一个栈中存在，那么多次启动此Activity都不会重新创建实例。singleTask默认具有clearTop效果，启动已存在Activity D时会导致栈内所有在D上面的Activity全部出栈

#### singleInstance：单实例模式

一种加强的singleTask模式。除了具有singleTask的所有特性外，Activity只能单独的位于一个任务栈中

##### 注：
1. 在AndroidMenifest中指定Activity启动模式无法直接为Activity设定`FLAG_ACTIVITY_CLEAR_TOP`标识
使用Intent设置标志位指定Activity启动模式无法直接为Activity指定singleInstace模式

2. 常用Activity的flag：

 1> `FLAG_ACTIVITY_NEW_TASK`：为Activity指定“singleTask”启动模式

 2> `FLAG_ACTIVITY_SINGLE_TOP`：为Activity指定“singleTop”启动模式

 3> `FLAG_ACTIVITY_CLEAR_TOP`：由此标记位的Activity，当它启动时，在同一个任务栈中所有位于它上面的Activity都要出栈，一般和FLAG_ACTIVITY_NEW_TASK一起使用

 4> `FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`：具有这个标记的Activity不会出现在历史Activity列表中。当不希望用户通过历史列表回到Activity时使用。等同于在xml中指定Activity的属性`android:excledeFromRecents = "true"`.

# IntentFilter的匹配规则

### action的匹配规则

一个过滤规则中可以有多个action， 那么只要Intent中的action能够和过滤规则中的任何一个action相同即可匹配成功。区分大小写。

### category的匹配规则

1. Intent中如果含有category，那么所有的category都必须和过滤规则中的其中一个category相同。（Intent中可以没有category）

2. Intent不设置category时，系统会默认为Intent加上`“android.intent.category.DEFAULT”`这个而category。
3. 为了activity能够接收隐式调用，必须在intent-filter中指定`“android.intent.category.DEFAULT”`这个category。

### data的匹配规则

###### data组成：由mimeType和URI组成。

`mimeType`：媒体类型。如image/png、image/\*、vide/\*、audio/\*....  
`URI`：`scheme://host:port/path|pathPrefix|pathPattern`

###### 说明：
1. 参数：  
1> `secheme`：URI的模式，如http、file、content等，如果没有指定scheme，则整个URI无效  
2> `host`：URI的主机名，如www.baidu.com、com.srainbow等，如果没有指定host，则整个URI无效  
3> `port`：URI的端口号，如80、8080等，可以不指定  
4> `path`：表示完整路径信息，可不指定  
5> `pathPattern`：表示完整路径信息，可以包含通配符“\*”，可不指定  
6> `pathPrefix`：表示路径的前缀信息，可不指定  

2. URI的默认值为`content`和`file`。即：manifest文件中虽然没有指定URI，但是Intent中的URI部分的schema必须为content或者file才行  
即：`intent.setDataAndType(Uri.parse("content://abc"), "image/\*");`其中abc为任意字符串

3. Intent指定完整data时，必须要调用`setDataAndType`方法，不能先调用`setData`再调用`setType`，因为这两个方法会彼此清除对方的值。

4. 通过隐式方式启动Activity时可进行判断避免报错(在找不到匹配的Activity时会返回null)  
1> 采用PackageManager的`resolveActivity`方法  
2> 常用Intent的`resolveActivity`方法  
3> PackageManager还提供`queryIntentActivities`方法，返回所有成功匹配的Activity信息。方法如下：  

		public abstract List<Resolveinfo> queryIntentActivities(Intent intent, int flags);
		public abstract Resolveinfo resolveActivity(Intent intent, int flags);

 注：上面两个方法的flags参数要使用`MATCH_DEFAULT_ONLY`标记位，用于仅仅匹配在intent-filter中声明了默认category的Activity。

# Android中的序列化接口

### Serializable与Parcelable

###### 使用：  
1. 在使用内存时，使用Parcelable接口（Serializable接口会产生大量临时变量， 引起频繁GC）  
2. 数据存储或网络传输时，使用Serializable接口（Parcelable接口并不是一个通用的序列化机制）

###### Serializable:

1. 为了保证最大限度的恢复数据，应该手动指定serialVersionUID。
2. 静态成员属于类不属于对象，所以不参与序列化过程。

 ###### 注：
有时在更改原来类的成员（如id)为`static`时，在运行程序发现还是会读出已经设为静态的id的值。这是因为在同一个机器同一进程中，jvm已经把之前的id加载进来了，所以获取的是加载好的id。如果换一个进程进行读取，就不会有id的值了
3. 使用`transient`关键字标记的成员变量不会参与序列化过程。

###### Parcelable：

1. 方法：  
 `createFromParcel(Parcel in)`	//从序列化后的对象中创建原始对象  

 `newArray(int size)`	//创建指定长度的原始对象数组

 `User(Parcel in)`//从序列化后的对象中创建原始对象，User为当前类名

 `writeToParcel(Parcel out, int flags)`	//当前对象写入序列化结构中  
 flags：`PARCELABLE_WRITE_RETURN_VALUE flags`为0或1，几乎所有情况下都为0，为1时标识当前对象需要作为返回值返回，不能立即释放资源

 `describeContents`	//返回当前对象的内容描述，含有文件描述符时返回1，其余返回0，几乎所有情况都返回0  
 flags：`CONTENTS_FILE_DESCRIPTOR`

2.  说明：  
1> 序列化由`writeToParcel`方法完成（最终是通过Parcel中的一系列write方法完成）  
2> 反序列化由`Creator`方法完成。内部表明了如何创建序列化对象和数组，并通过Parcel中的一系列`read`方法完成反序列化。  
3> 内容描述功能由`describeContents`方法完成，几乎所有情况下都返回0，仅当当前对象中存在文件描述符时返回1。  
4> 当序列化中含有另一个可序列化对象时，反序列化过程需要传递当前线程的上下文类加载器，否则报无法找到类的错。
（传递当前上下文类加载器：`Thread.urrentThread().getContextClassLoader() `);

# Service的使用

#### 生命周期：

1. `startService`：`onCreate`--`onStartCommand`  
2. `stopService`：`onDestroy`  
3. `bindService`：`onCreate--onBind`  
4. `unbindService`: `onUnBind--onDestroy`  
5. 先调用`startService`再调用`bindService`：`onCreate`--`onStartCommand`--`onBind`(`onCreaate`方法只执行一次)
###### 注：
 这里如果先调用`stopService`方法无法停止服务；再调用`unbindService`方法时直接执行`onUnBind`--`onDestroy`
 如果先调用`unbindService`方法会执行`onUnBind`方法，再调用`stopService`方法时执行`onDestroy`方法，停止服务

6. 先调用`bindService`再调用`startServic`e：`onCreate`--`onBind`--`onStartCommand`(`onCreate`方法只执行一次)，且调用`unbindService`会执行`onUnBind`方法，
再调用`stopService`方法会调用`onDestroy`方法停止服务
###### 注：
这里如果先调用`stopService`方法无法停止服务；再调用`unbindService`方法时直接执行`onUnBind`--`onDestroy`如果先调用`unbindServcie`方法会执行`onUnBind`方法，再调用`stopService`方法时执行`onDestroy`方法，停止服务（同第5点）

# AIDL传递数据
#### AIDL文件支持的数据类型

1. 基本数据类型（`int`, `char`, `long`, `boolean`, `double`等)
2. `String` 和 `CharSequence`
3. `List`(只支持`ArrayList`，且元素都必须被AIDL文件支持)
4. `Map`（只支持`HashMap`，且元素都必须被AIDL文件支持，包括key和value)
5. `Parcelable`(所有实现了`Parcelable`接口的对象)
6. `AIDL`(所有的AIDL接口本身也可以在AIDL文件中使用)

#### 使用Messenger（底层实现是AIDL）

Messenger对AIDL做了封装，可以更加便捷地进行进程间通信。同时，由于一次只处理一个请求， 服务端不同考虑线程同步的问题；（服务端不存在并发执行的情形）

###### 实现Messenger步骤：
服务端进程：
1. 创建一个Service来处理客户端的连接请求
2. 创建一个Handler并通过它来创建一个Messenger对象
3. 在Service中的onBind中返回这个Messenger对象底层的Binder即可

客户端进程：

1. 绑定服务端的Service
2. 绑定成功后用服务端返回的IBinder对象创建一个Messenger，通过这个Me	ssenger向服务端发送消息（消息类型为Message对象）
3. 如果服务端需要回应客户端，则像服务端一样再创建一个Handler并创建一个新的Messenger，并把这个Messenger对象通过Message的
replyTo参数传递给服务端，服务端通过这个replyTo参数就可以回应客户端

***服务端代码：***

![MessengerService](E:\Study Document\Android开发艺术探索\阅读截图\MessengerService.png)

***客户端代码：***（测试结束时记得解绑Service）

![MessengerClient](E:\Study Document\Android开发艺术探索\阅读截图\MessengerClient.png)

如果需要客户端接收
