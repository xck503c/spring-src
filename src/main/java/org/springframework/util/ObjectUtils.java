package org.springframework.util;

import java.util.Arrays;

public class ObjectUtils {

    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;

    //判断两个实例是否相同
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null && o2 != null) {
            if (o1.equals(o2)) { //调用实例中自己的equals方法
                return true;
            }else {
                //如果是数组类型就需要特殊判断。几种基本类型
                if (o1.getClass().isArray() && o2.getClass().isArray()) {
                    if (o1 instanceof Object[] && o2 instanceof Object[]) {
                        return Arrays.equals((Object[])o1, (Object[])o2);
                    }

                    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
                        return Arrays.equals((boolean[])o1, (boolean[])o2);
                    }

                    if (o1 instanceof byte[] && o2 instanceof byte[]) {
                        return Arrays.equals((byte[])o1, (byte[])o2);
                    }

                    if (o1 instanceof char[] && o2 instanceof char[]) {
                        return Arrays.equals((char[])o1, (char[])o2);
                    }

                    if (o1 instanceof double[] && o2 instanceof double[]) {
                        return Arrays.equals((double[])o1, (double[])o2);
                    }

                    if (o1 instanceof float[] && o2 instanceof float[]) {
                        return Arrays.equals((float[])o1, (float[])o2);
                    }

                    if (o1 instanceof int[] && o2 instanceof int[]) {
                        return Arrays.equals((int[])o1, (int[])o2);
                    }

                    if (o1 instanceof long[] && o2 instanceof long[]) {
                        return Arrays.equals((long[])o1, (long[])o2);
                    }

                    if (o1 instanceof short[] && o2 instanceof short[]) {
                        return Arrays.equals((short[])o1, (short[])o2);
                    }
                }

                return false;
            }
        }else {
            return false;
        }
    }

    /**
     * 获取对象的哈希值，如果是数组，那就一个个遍历累加
     * @param obj
     * @return
     */
    public static int nullSafeHashCode(Object obj) {
        if(obj == null){
            return 0;
        }
        if(obj.getClass().isArray()){
            if (obj instanceof Object[]) {
                return nullSafeHashCode((Object[]) obj);
            }
            if (obj instanceof boolean[]) {
                return nullSafeHashCode((boolean[]) obj);
            }
            if (obj instanceof byte[]) {
                return nullSafeHashCode((byte[]) obj);
            }
            if (obj instanceof char[]) {
                return nullSafeHashCode((char[]) obj);
            }
            if (obj instanceof double[]) {
                return nullSafeHashCode((double[]) obj);
            }
            if (obj instanceof float[]) {
                return nullSafeHashCode((float[]) obj);
            }
            if (obj instanceof int[]) {
                return nullSafeHashCode((int[]) obj);
            }
            if (obj instanceof long[]) {
                return nullSafeHashCode((long[]) obj);
            }
            if (obj instanceof short[]) {
                return nullSafeHashCode((short[]) obj);
            }
        }
        return obj.hashCode();
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(Object[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (Object element : array) {
            hash = MULTIPLIER * hash + nullSafeHashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(boolean[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (boolean element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(byte[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (byte element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(char[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (char element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(double[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (double element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (float element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (int element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(long[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (long element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(short[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (short element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return the same value as {@link Boolean#hashCode()}}.
     * @see Boolean#hashCode()
     */
    public static int hashCode(boolean bool) {
        return (bool ? 1231 : 1237);
    }

    /**
     * Return the same value as {@link Double#hashCode()}}.
     * @see Double#hashCode()
     */
    public static int hashCode(double dbl) {
        return hashCode(Double.doubleToLongBits(dbl));
    }

    /**
     * Return the same value as {@link Float#hashCode()}}.
     * @see Float#hashCode()
     */
    public static int hashCode(float flt) {
        return Float.floatToIntBits(flt);
    }

    /**
     * Return the same value as {@link Long#hashCode()}}.
     * @see Long#hashCode()
     */
    public static int hashCode(long lng) {
        return (int) (lng ^ (lng >>> 32));
    }

    public static String identityToString(Object obj) {
        return obj == null ? "" : obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }

    public static String getIdentityHexString(Object obj){
        return Integer.toHexString(System.identityHashCode(obj));
    }

    public static void main(String[] args) {
        double d = 123.456;
        long l = Double.doubleToLongBits(d);
        System.out.println(l);
    }
}
