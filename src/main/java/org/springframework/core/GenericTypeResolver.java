package org.springframework.core;

import com.TestApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import sun.jvm.hotspot.utilities.GenericArray;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 原文百度翻译：帮助类，根据给定的类型变量来解析泛型，主要用于在框架中使用，解析方法参数类型，即使它们是泛型声明的。
 */
public class GenericTypeResolver {

    /**
     * 针对给定的目标类和泛型实现接口来解析泛型变量的具体类型；
     * 这里假设检测目标是实现genericIfc的
     * @param clazz 要检测的目标类
     * @param genericIfc the generic interface or superclass to resolve the type argument from
     * @return
     */
    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericIfc){
        Class<?>[] typeArgs = resolveTypeArguments(clazz, genericIfc);
        if (typeArgs == null) {
            return null;
        }
        if (typeArgs.length != 1) {
            throw new IllegalArgumentException("Expected 1 type argument on generic interface [" +
                    genericIfc.getName() + "] but found " + typeArgs.length);
        }
        return typeArgs[0];
    }

    /**
     * 1. 解析参数化类型，也就是提取给定类的泛型具体类型；
     * 2. 这里假定要解析的class实现了泛型接口，并且声明了具体的参数类型
     * @param clazz
     * @param genericIfc the generic interface or superclass to resolve the type argument from
     * @return
     */
    public static Class<?>[] resolveTypeArguments(Class<?> clazz, Class<?> genericIfc){
        return doResolveTypeArguments(clazz, clazz, genericIfc);
    }

    /**
     * 拿到classToIntrospect父泛型接口或者父泛型类的所有泛型变量具体类型
     *
     * 该方法我是从GenericApplicationListenerAdapter的supportsEventType跟踪进来的，以该例子为说明：
     * 1. 这个supportsEventType方法是为了判断给定的监听器是否支持给定的事件类型
     * 2. 判断的方法就是通过获取该类的父泛型接口或者父父泛型接口的泛型变量的具体类型，只要找到了和该事件类型一样的Class
     * 就算成功
     * 3. 这个事件类型可能是个接口也可以是个类，所以需要分开判断，而针对该例来说，是一个事件最原始接口
     * 4. 这个事件类型可能是是该Class的上上级，所以需要递归遍历
     *
     * @param ownerClass
     * @param classToIntrospect 要解析的Class
     * @param genericIfc  限定Class
     * @return
     */
    private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Class<?> classToIntrospect, Class<?> genericIfc) {
        while (classToIntrospect != null){
            if(genericIfc.isInterface()){ //限定类型是接口
                //那就拿到要解析Class的父泛型接口来，获取泛型变量的具体类型
                Type[] ifcs = classToIntrospect.getGenericInterfaces();
                for (Type ifc : ifcs) {
                    Class<?>[] result = doResolveTypeArguments(ownerClass, ifc, genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
            }else{ //否则那就去拿父泛型类
                try {
                    Class<?>[] result = doResolveTypeArguments(ownerClass, classToIntrospect.getGenericSuperclass(), genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
                catch (MalformedParameterizedTypeException ex) { //看注释好像是说泛型实例无法被实例化？
                    // from getGenericSuperclass() - return null to skip further superclass traversal
                    return null;
                }
            }
            //拿不到就递归往上
            classToIntrospect = classToIntrospect.getSuperclass();
        }
        return null;
    }

        /**
         * 功能：提取ifc的泛型变量的具体类型，或者提取ifc父接口或者父类的泛型变量具体类型；
         * 限定条件：要提取泛型实例的原始类型==指定Class类型
         * 如：要提取是：List<String> 的 List == 指定Class，也是List；
         *
         * 1. 若提取Type类型是泛型实例，且，则该泛型实例的原始类型，等于，指定Class类型，则直接提取泛型变量具体类型；
         * (1)若是指定类型的子类，则需要拿到它的父泛型接口或者父泛型类进行来提取；
         * 2. 若不是泛型实例，说明本身就是原始类型，那肯定不行，若以再尝试判断是否是指定类型的子类，同上；
         * 3. 若什么都提取不到，那就没办法了，返回null；
         * @param ownerClass
         * @param ifc 要提取的类型
         * @param genericIfc 限定Class
         * @return
         */
    public static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Type ifc, Class<?> genericIfc){
        //判断是否是泛型实例，形如List<String>
        if(ifc instanceof ParameterizedType){
            ParameterizedType paramIfc = (ParameterizedType) ifc;
            Type rawType = paramIfc.getRawType(); //原始类型，形如List
            //限定条件<=genericIfc
            if(genericIfc.equals(rawType)){
                Type[] typeArgs = paramIfc.getActualTypeArguments(); //String
                Class<?>[] result = new Class[typeArgs.length];
                for(int i=0; i<typeArgs.length; i++){
                    Type arg = typeArgs[i];
                    result[i] = extractClass(ownerClass, arg); //提取arg对应的原始类型Class
                }
                return result;
            }else if(genericIfc.isAssignableFrom((Class)rawType)){
                //找到它的父泛型接口或者父泛型类，继续来
                return doResolveTypeArguments(ownerClass, (Class) rawType, genericIfc);
            }
        }else if(ifc != null && genericIfc.isAssignableFrom((Class)ifc)){
            //不是泛型实例，那就转化为原始类型比较: 父Class.isAssignableFrom(子Class)->true
            return doResolveTypeArguments(ownerClass, (Class) ifc, genericIfc);
        }
        return null;
    }

    /**
     * 提取给定Type中对应的Class类型
     * @param ownerClass
     * @param arg 需要查找Class的Type
     * @return
     */
    private static Class<?> extractClass(Class<?> ownerClass, Type arg){
        //如果是泛型实例，则需要提取原始类型，重入该方法
        if(arg instanceof ParameterizedType){
            return extractClass(ownerClass, ((ParameterizedType)arg).getRawType());
        }else if(arg instanceof GenericArrayType){ //泛型数组，形如List<String>[]
            GenericArrayType gat = (GenericArrayType) arg;
            Type gt = gat.getGenericComponentType(); //形如：List<String>
            Class<?> componentClass = extractClass(ownerClass, gt);
            return Array.newInstance(componentClass, 0).getClass(); //数组Class，[Ljava.util.ArrayList
        }else if(arg instanceof TypeVariable){ //泛型变量，T，K
            TypeVariable tv = (TypeVariable) arg;

        }
        return (arg instanceof Class)? (Class) arg : Object.class;
    }

    /**
     * 文章：https://www.jianshu.com/p/da21b3a59b47，泛型相关接口等术语定义
     * 1. ParameterizedType：文中称之为泛型实例，如Map<String, String>是泛型Map<K,V>的一个实例
     * 2. GenericDeclaration：文中称为可以声明为泛型的实体，如Class，Method，Construct等，只有
     * 实现了该接口的实体，才能声明泛型变量；
     * 3. TypeVariable<D extends GenericDeclaration>：文中称为泛型变量，也就是T，K，V
     * 4. GenericArrayType：文中称为泛型数组，形如A<T>[]，T[]
     * 5. WildcardType：文中称为泛型参数表达式，如?，? extends A，？ super B等
     *
     * 文章：https://www.jianshu.com/p/4cbe2e46e707，Type及其子接口的来历
     * 文中讲了Type等来历，这里只说我感兴趣地方：
     * 1. Type里只有一个方法，它统一了原始类型和与泛型有关等类型。
     * 2. Java的泛型会在编译后被擦除，即class文件中不会有泛型相关信息，具体可以反编译看看；
     * 3. 看了下面的文章，有点明白了什么叫统一了原始类型和泛型。引入泛型后，扩充了数据类型，上面说的各种type。
     * 而擦除之后，这些类型没了，而为了可以统一就引入了Type做为他们的共同父接口，也就是多态；
     *
     * 那么擦除的原因是什么？为了兼容以前版本，什么意思？知乎中的一个回答，很棒，这里自己小结一下：
     * https://www.zhihu.com/question/28665443
     * 1. 首先导致使用伪泛型的原因是兼容性取舍，而不是实现不了；
     * 2. 兼容性，就是可以让java 5之前的程序在新版本的上还能正常运行，如下面的代码，List中有两种不同类型，
     * 这种情况下，如果将其泛型化肯定不行：
     * ArrayList things = new ArrayList();
     * things.add("a");
     * things.add(Integer.valueOf(42));
     * 所以就有了两种思路：
     * 1. 第一种，以前的有的就不动，新加一套泛型化版本；
     * 2. 第二种，把需要泛型化的原地泛型化，但是为了上面的代码在新版本中还可以继续使用，
     * 就引入了原始类型(raw type)的概念，该类虽然被原地泛型化，但是也可以当作非泛型使用，其实这么说，我突然想起来
     * 以前写代码的时候用List list = new ArrayList();也是可以使用的，原来是这样；
     * (1) ArrayList - 原始类型
     * (2) ArrayList<E> ，ArrayList<String>，ArrayList<?>
     *
     * 下面这样的代码也要保证可以运行：
     * List<String> closeGeneric = new ArrayList<>();
     * List<Integer> IntegercloseGeneric = new ArrayList<>();
     * List rawList = closeGeneric;
     * List rawList = IntegercloseGeneric;
     * 说明raw type需要是泛型的超类型，所以要支持最直接的方法就是擦除，如果是这样说的话，我就比较容易理解为啥
     * 要擦除了；
     *
     * 文中有个很好笑的地方，就是原始类型和泛型的交互，说是来不及完成就不支持了，hh；
     * 不过这是16年写的，到现在也没支持吧，好像；
     * 干货很多，不过我现在关心的兼容性疑问解决了，剩下有时间慢慢看作者贴的传送门，偷懒；
     *
     */
    public static void main(String[] args) {
//        List rawList = new ArrayList();
//        rawList.add("123");
//        rawList.add(123);
//        System.out.println(rawList);
//        List<String> closeGeneric = new ArrayList<>();
//        rawList = closeGeneric;
//        System.out.println(rawList);

        TestApplicationListener listener = new TestApplicationListener();
        Class<?> targetClass = listener.getClass();
        Class<?> genericIfc = ApplicationEvent.class;

        //获取所有实现接口
        Type[] types = targetClass.getGenericInterfaces();
        for (Type type : types){
            System.out.println(targetClass.getSimpleName() + "实现接口: " + type.getTypeName());
            System.out.println(type.getTypeName() + "是否实现泛型实例接口：" + (type instanceof ParameterizedType));
            if(type instanceof ParameterizedType){
                //interface org.springframework.context.ApplicationListener
                //获取泛型实例中的原始类型，就是不包含泛型参数的类型
                System.out.println(type.getTypeName() + "的原始类型: "+((ParameterizedType)type).getRawType());
                System.out.println("原始类型是否是Class类型: "+(((ParameterizedType)type).getRawType() instanceof Class));
                Type[] types1 = ((ParameterizedType)type).getActualTypeArguments();
                for(Type type1 : types1){
                    //com.TestApplicationEvent
                    System.out.println(type.getTypeName() + "泛型类型是: " + type1.getTypeName());
                    System.out.println(type1.getTypeName() + "泛型变量: " + (type1 instanceof TypeVariable));
                }
            }
            System.out.println();
        }

        //TestApplicationListener实现接口: org.springframework.context.ApplicationListener<com.TestApplicationEvent>
        //org.springframework.context.ApplicationListener<com.TestApplicationEvent>是否实现泛型实例接口：true
        //org.springframework.context.ApplicationListener<com.TestApplicationEvent>的原始类型: interface org.springframework.context.ApplicationListener
        //原始类型是否是Class类型: true
        //org.springframework.context.ApplicationListener<com.TestApplicationEvent>泛型类型是: com.TestApplicationEvent
        //com.TestApplicationEvent泛型变量: false
        //
        //TestApplicationListener实现接口: java.io.Serializable
        //java.io.Serializable是否实现泛型实例接口：false

        //[Ljava.util.ArrayList
        List<String>[] l = new ArrayList[0];
        System.out.println(l.getClass());

        //class com.Test
        System.out.println(targetClass.getSuperclass());

        //com.Test<java.lang.Object> 会将泛型列出来
        System.out.println(targetClass.getGenericSuperclass());
    }
}
