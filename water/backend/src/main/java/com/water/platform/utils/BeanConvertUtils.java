package com.water.platform.utils;

import org.springframework.beans.BeanUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 类描述: 转换对象属性工具
 */
public class BeanConvertUtils extends BeanUtils {


    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
        return convertTo(source, targetSupplier, null);
    }

    /**
     * 转换对象
     *
     * @param source         源对象
     * @param targetSupplier 目标对象供应方
     * @param callBack       回调方法
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @return 目标对象
     */
    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (null == source || null == targetSupplier) {
            return null;
        }

        T target = targetSupplier.get();
        copyProperties(source, target);
        if (callBack != null) {
            callBack.callBack(source, target);
        }
        return target;
    }

    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
        return convertListTo(sources, targetSupplier, null);
    }


    /**
     * 转换对象（浅拷贝）
     *
     * @param sources        源对象list
     * @param targetSupplier 目标对象供应方
     * @param callBack       回调方法
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @return 目标对象list
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (null == sources || null == targetSupplier) {
            return null;
        }

        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T target = targetSupplier.get();
            copyProperties(source, target);
            if (callBack != null) {
                callBack.callBack(source, target);
            }
            list.add(target);
        }
        return list;
    }

//    /**
//     * 链上对象转到普通对象（浅拷贝）
//     *
//     * @param sources        源对象list
//     * @param targetSupplier 目标对象供应方
//     * @param <S>            源对象类型
//     * @param <T>            目标对象类型
//     * @return 目标对象list
//     */
//    public static <S, T> List<T> chainConvertListTo(List<S> sources, Class<?> sourceClass, Supplier<T> targetSupplier) {
//        if (null == sources || null == targetSupplier) {
//            return null;
//        }
//
//        List<T> list = new ArrayList<>(sources.size());
//        for (S source : sources) {
//            T target = targetSupplier.get();
//            ChainCopyUtils.copyProperties(source, sourceClass, target);
//
//            list.add(target);
//        }
//        return list;
//    }

    /**
     * 回调接口
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    @FunctionalInterface
    public interface ConvertCallBack<S, T> {
        void callBack(S t, T s);
    }

    /**
     * 将map中的值拷贝到bean中 （bean不允许有@Accessors(chain = true)，否则抛出空指针）
     * @param source  属性来源map
     * @param target  目标Bean
     */
    public static void copyMapToBean(Map<String,Object> source, Object target) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor prop: properties) {
            String key = prop.getName();
            if(source.containsKey(key) && source.get(key) != null){
                Object value = source.get(key);
                Method setMethod = prop.getWriteMethod();
                setMethod.invoke(target,value);
            }
        }
    }

    /**
     * 将bean中的属性存入到map中
     * @param source  来源bean
     * @param target 目标map
     */
    public static void copyBeanToMap(Object source, Map<String,Object>  target) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        //1.获取bean信息
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        if(properties != null && properties.length>0){
            for (PropertyDescriptor prop :properties) {
                //2.得到属性名
                String name = prop.getName();
                //3.过滤class属性
                if(!"class".equals(name)){
                    //4.得到属性的get方法
                    Method getter = prop.getReadMethod();
                    //5.获取属性值
                    Object value = getter.invoke(source);
                    //6.放入map中
                    if(value != null ){
                        target.put(name,value);
                    }
                }
            }
        }
    }
}
