# Android编译打包
由于Android平台的特殊性，所以一切都是为了更小的空间占用、更快的响应速度和最优的显示效果。

参考：
<br>
https://blog.csdn.net/luoshengyang/article/details/8738877
https://blog.csdn.net/luoshengyang/article/details/8744683
### Android应用程序资源
#### assets：
保存原始文件，可以以任何方式来进行组织，最终会被原封不动地打包在apk文件中。如果要访问就需要指定文件名来访问。
```java
getAssets().open("fileName");
```
#### res：
大多数文件都会被编译且赋予资源ID，之后通过ID来访问。

animaotr、anim、color、drawable、minmap、layout、menu、raw、values、xml

drawable：可能会被优化 - 一个不需要多余256色的真彩色PNG文件可能会被转换成一个只有8位调色板的PNG面板，这样就可以无损压缩图片，以减少所占用的内存资源。

raw：和assets类似，也是原封不动地打包在apk中，不过会被赋予资源ID，之后可以通过ID来访问

```java
getResources().openRawResource(R.raw.filename);
```
**注**
除了raw类型和Bitmap文件的drawable类型资源外，其余文件均为文本格式的xml文件，在打包过程中会被编译成二进制格式的xml文件。

应用程序资源的组织方式：

![image](http://img.my.csdn.net/uploads/201303/31/1364719879_5001.jpg)

在应用程序资源目录中寻找最合适资源算法：

![image](http://img.my.csdn.net/uploads/201303/31/1364720803_2359.png)

###### step1：消除与设备配置冲突的drawable目录
###### step2：从MMC开始（MCC - MNC - language...)，选择一个资源组织维度来过滤从step2筛选后剩下的目录
###### step3：检查Step2选择的维度是否有对应的资源目录。如果没有就返回到step2继续处理，如果有，就执行step4
###### step4: 消除不包含有step2所选择的资源维度的目录
###### step5: 继续执行step2、3、3，直到找到一个最匹配的资源目录为止。（即剩下最后一个目录为止）
#### 小结
1. 除了assets和res/raw资源原封不动地打包进apk之外，其他的资源都会被编译或处理（图片）
2. 除了assets资源之外，其他的资源都会被赋予一个资源ID
3. 打包工具负责编译和打包资源，编译完成之后会生成一个resources.arsc文件和一个R.java文件，前者保存的是一个资源索引表，后者定义了各个资源的ID常量
4. AndroidManifest.xml也会被编译成二进制的xml文件，然后再打包到apk中
5. 应用程序在运行时通过Assetmanager、资源ID或者文件名来访问资源
6. 二进制格式的XML文件占用空间更小。这是由于所有XML元素的标签、属性名称、属性值和内容所涉及到的字符串都会被统一收集到一个字符串资源池中去，并且会去重。有了这个字符串资源池，原来使用字符串的地方就会被替换成一个索引到字符串资源池的整数值，从而可以减少文件的大小。
7. 二进制格式的XML文件解析速度更快。这是由于二进制格式的XML元素里面不再包含有字符串值，因此就避免了进行字符串解析，从而提高速度。
#### 资源索引表
Android资源打包工具在编译前，会创建一个资源表（使用ResourceTable对象描述），保存所有资源的信息。之后通过该内容生成资源索引文件resources.arsc
#### 资源编译打包过程
###### 一、解析AndroidManifest.xml
获取编译资源的应用程序的包名称，根据包名称就可以创建资源表了（ResourceTable对象）
###### 二、添加被引用资源包
1. Android系统定义了一套通用资源，这些资源可以被应用程序引用，如：布局文件中的`2. android:orientation="vertical"`，这个`vertical`就是定义在系统资源包中的一个值。<br>
3. 在编译应用程序的资源的时候，至少涉及到两个包：被引用的系统资源包、正在编译的应用程序资源包。一个包通过资源ID引用其他包的资源。资源ID是一个4字节的无符号整数，其中最高字节表示Package ID，次高字节表示Type ID，最低两字节表示Entry ID。<br>
4. Package ID相当于一个命名空间，限定资源的来源。Android系统当前定义了两个资源命令控件，一个是系统资源命令空间（0x01），另一个是应用程序资源命令空间(0x7f），所有位于这两个值之间的ID都是合法的，之外的ID都是非法的。
5. Type ID是指资源的类型ID，animator、anim、color、drawable、layout、menu、raw、string和xml等若干种，每一种都会被赋予一个ID
6. Entry ID是指每一个资源在其所属的资源类型中所出现的次序，不同类型的Entry ID可以是相同的。
###### 三、收集资源文件
###### 四、将收集到的资源文件增加到资源表
这步收集到资源表中的资源是不包括values类型的资源的，这类资源需要经过编译后才会添加到资源表中
###### 五、编译values类资源
###### 六、给Bag资源分配ID
values类资源除了string外还有很多其他类型的资源，这些资源会给自己定义一些专用的值，这些带有专用值的资源就称为Bag资源。如Android提供的`android:orientation`属性的取值范围为{`vertical`、`horizontal`}，就相当于定义了两个Bag。<br>
在继续编译其他非values资源前，需要给之前收集到的Bag资源分配资源ID，因为可能会被其他非values资源引用到
###### 七、编译Xml资源文件
###### 八、生成资源符号
###### 九、生成资源索引表
1. 收集类型字符串
如："drawable"、"layout"、"string"、"id"<br>
收集是按照Package来收集的，即：当前编译的应用程序资源有几个Package，就有几组对应的类型字符串，每一组类型字符串保存在其所属的Package中。
2. 收集资源项名称字符串
也是按照Package收集的
3. 收集资源项值字符串
当前所有参与编译的Package的资源项值字符串都会被统一收集在一起
4. 生成Package数据块
参与编译的每一个Package的资源项元信息都写在一块独立的数据上

**注**
Android资源中有一种资源类型为Public，定义在res/values/public.xml中
```java
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <public type="string" name="string3" id="0x7f040001"/>
</resources>
```
意思是：告诉Android资源打包工具aapt，将类型为string的资源string3的ID固定为0x7f040001<br>
作用：当我们将自己定义的资源导出来给第三方应用程序使用时，为了保证以后修改这些导出资源时，仍然保证第三方应用程序的兼容性，就需要给那些导出资源一个固定的资源ID。因为每当Android资源打包工具aapt重新编译被修改过的资源时，都会重新给这些资源赋予ID，就可能造成同一个资源在两次不同的编译中被赋予不同的ID，就会给第三方应用程序代码麻烦，因为后者一般是假设一个ID对应的永远是同一个资源的。<br>
因此，当我们将自己定义的资源导出来给第三方应用程序使用时，就需要通过public.xml文件将导出来的资源的ID固定下来。
###### 十、编译AndroidManifest.xml文件
在应用程序的所有资源项都编译完成后，再编译AndroidManifest文件，因为后者可能会引用到前者。编译后，aapt还会验证AndroidManifest文件的完整性和正确性。
###### 十一、生成R.java文件
第八步中已经将所有的资源型和对应的资源ID都收集起来了，这里只需将它们写入到指定的R.java文件中去就可以了，每一个资源类型在R.java中都有一个对应的内部类：
```java
public final class R {  
    ......  
  
    public static final class layout {  
        public static final int main=0x7f030000;  
        public static final int sub=0x7f030001;  
    }  
  
    ......  
}  
```
###### 十二、打包APK文件
1. assets目录
2. res目录，但不包括res/values目录，因为res/values目录下的资源文件的内容经过编译后都直接写入到资源项索引表中去了
3. 资源项索引文件resources.arsc<br>
除此之外还包括AndroidManifest.xml以及应用程序代码文件classes.dex，还有描述应用程序的签名信息文件。

*APK解压后目录*

![image](https://github.com/WRainbow/Bed-Of-ScreenShot/blob/master/ScreenShot/APK%E8%A7%A3%E5%8E%8B.png)

### 打包过程
1. AndroidSDK提供的aapt.exe生成R.java
2. AndroidSDK提供的aidl.exe把.aild转为.java文件
3. JDK提供的javac.exe编译.java类文件生成class文件
4. AndroidSDK提供的dx.bat命令行脚本生成classes.dex文件
5. AndroidSDK提供的aapt.exe生成资源包文件
6. AndroidSDK提供的apkbuilder.bat生成未签名的安装文件
7. 使用JDK的jarsigner.exe进行签名

*APK打包流程图*

![image](https://github.com/WRainbow/Bed-Of-ScreenShot/blob/master/ScreenShot/APK%E6%89%93%E5%8C%85%E6%B5%81%E7%A8%8B.png)