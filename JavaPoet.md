JavaPoet
========

..................................................Java提供的..................................................

基本信息获取：PackageElement、TypeElement、VariableElement  --  extends Element

Element: 表示程序元素。<br>
1. 可以使用该对象获取类型：asType()
2. 返回元素的简单名称：getSimpleName()
3. 返回元素的上一级元素：getEnclosingElement()
4. 返回元素内的所有元素：getEnclosedElements() - 包括直接声明的字段，方法，构造函数和成员类型

PackageElement: 表示包程序元素，提供了有关该软件包及其成员的信息的访问。<br>
1. 获取包的全称：getQualifiedName();
2. 获取包的简称：getSimpleName();

TypeElement：表示一个类或接口程序元素（枚举是类、注释是接口）。<br>
1. 获取元素的全称：getQualifiedName();
2. 获取元素的简称：getSimpleName();
3. 获取直接超类：getSuperclass();
4. 获取元素的形式类型参数：getTypeParameters();

VariableElement：表示一个字段。<br>
1. 获取此元素的包围元素（如果是属性，则获取到的是类名）：getEnclosingElement();
2. 获取元素的简称：getSimpleName();

ElementFilter - 获取元素详细信息工具类<br>
1. 获取元素中的构造方法列表：<br>
constructorsIn(Iterable<? extends Element> elements)<br>
constructorsIn(Set<? extends Element> elements)
2. 获取元素中的方法列表：（不包括构造方法）<br>
methodsIn(Iterable<? extends Element> elements)<br>
methodsIn(Set<? extends Element> elements)
3. 获取元素中的字段（属性）列表：<br>
fieldsIn(Iterable<? extends Element> elements)<br>
fieldsIn(Set<? extends Element> elements)
4. 获取元素中的包列表：(可能只对文件Element有用)<br>
packagesIn(Iterable<? extends Element> elements)<br>
packagesIn(Set<? extends Element> elements)
5. 获取元素中的类型列表：<br>
(如果是文件则可以获取到文件中有几个类，如果是类则可以获取到有几个内部类)<br>
typesIn(Iterable<? extends Element> elements)<br>
typesIn(Set<? extends Element> elements)

..................................................JavaPoet提供的..................................................

###### TypeName
定义了Java中的所有类型

###### ClassName
1. 定义了类名，也可以用来定义ClassName（主要是定义ClassName）
2. ClassName.get(String packageName, String simpleName, String... simpleNames);<br>
第一个参数是包名，后面的参数是拼接在包名后的类名，如：<br>
ClassName.get("java.lang", "String"); - 定义的类型就是java.lang.String<br>
ClassName.get("java.lang", "String", "SSS"); - 定义的类型就是java.lang.String.SSS
3. ClassName.bestGuess(String className)方法也可获取类型，填入类型的全称;
4. 获取包名：packageName();
5. 获取类名简称：simpleName();

###### 生成组合类型（如List<String>）：
使用ParameterizedTypeName.get(ClassName rawType, TypeName... typeArguments);如：
```java
ParameterizedTypeName.get(ClassName.get("java.util", "List"), ClassName.get("java.lang", "String"));
```

**创建java代码主要涉及的类为：TypeSpec、MethodSpec、FieldSpec、AnnotationSpec。**

###### TypeSpec
TypeSpec用来生成类、接口或者枚举。最终都是由TypeSpec.Builder调用build()方法生成。

***字段***
1. annotations: 添加到类上的注解
2. name: 类名
3. fieldSpecs: 类中所有字段的集合
4. methodSpecs: 类中所有方法的集合
5. modifiers: 类所有限定词的集合
6. superclass：类继承的类名
7. superinterfaces：类实现的接口集合
8. typeSpecs：类中所有类的集合

***方法***
1. 是否含有指定限定词：hansModifier(Modifier modifier);
2. 生成类的Builder：<br>
classBuilder(ClassName className)；生成的类名为ClassName的简称，不含包名;<br>
classBuilder(String name)；生成的类名为传入的name;
3. 生成接口的Builder:（同上）<br>
interfaceBuilder(ClassName className);<br>
interfaceBuilder(String name);
4. 生成枚举的Builder：（同上）<br>
enumBuilder(ClassName className);<br>
enumBuilder(String name);
5. 生成注解的Builder：<br>
annotationBuilder(ClassName className);<br>
annotationBuilder(String name);
6. 生成内部类的Builder：<br>
anonymousClassBuilder(CodeBlock typeArguments);<br>
anonymousClassBuilder(String typeArgumentsFormat, Object... args);
7. TypeSpec.Builder：（参考参考文档）<br>
添加类：addType(...);<br>
添加注解：addAnnotation(...);<br>
添加方法：addMethod(...);<br>
添加字段：addField(...);<br>
添加继承的类：superClass(...);<br>
添加实现的接口：addSuperinterface(...);<br>
生成类对象：build();

###### MethodSpec
用来生成方法

***字段***
1. name：方法名
2. annotations：获取添加到方法上的所有注解集合
3. code：方法体中的代码
4. exceptions: 方法中的异常集合
5. modifiers：方法的限定词集合
6. parameters：方法的参数集合（详见ParameterSpec）
7. returnType：方法的返回类型
8. varargs：方法是否含可变参数

***方法***
1. 生成构造函数Builder：constructorBuilder();
2. 是否含有指定限定词：hasModifier(Modifier modifier);
3. 是否为构造函数：isConstrctor();
4. 生成方法Builder：methodBuilder(String name);
5. MethodSpec.Builder:（参考参考文档）<br>
添加注解：addAnnotation(...);<br>
添加代码：addCode(...);<br>
添加异常：addException(...);<br>
添加限定词：addModifiers(...);<br>
添加参数：addParameter(...);<br>
添加代码语句：addStatement(...);<br>
开始循环：beginControlFlow(...);<br>
结束循环: endControlFlow();<br>
返回类型：returns(...);

###### FieldSpec
用来生成字段、属性

***字段***
1. annotations: 添加到字段上的注解
2. modifiers：字段的限定词集合
3. name: 字段的名称
4. type：字段的类型

***方法***
1. 生成字段的Builder：builder(TypeName type, String name, Modifier... modifiers);
2. 是否含有指定限定词：hasModifier(Modifier modifier);
3. FieldSpec.Builder: （参考参考文档）<br>
添加注解：addAnnotation(...);<br>
添加限定词：addModifiers(...);<br>
初始化时赋值：initializer(...);<br>
生成FieldSpec：build();