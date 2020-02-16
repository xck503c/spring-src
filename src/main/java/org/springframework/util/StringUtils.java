package org.springframework.util;

public class StringUtils {

    //从名字可以看出是是否有长度，空格也算
    public static boolean hasLength(CharSequence str){
        return str!=null && str.length()>0;
    }

    public static boolean hasLength(String str){
        return hasLength((CharSequence)str); //复用？
    }

    /**
     * 将inString中的oldPattern替换成newPattern
     * newPattern - 允许为空字符串
     */
    public static String replace(String inString, String oldPattern, String newPattern){
        if(hasLength(inString) && hasLength(oldPattern) && newPattern!=null){
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            //定位第一个为oldPattern的位置
            int index = inString.indexOf(oldPattern);
            //循环定位
            for(int patLen = oldPattern.length(); index>=0; index = inString.indexOf(oldPattern, pos)){
                sb.append(inString.substring(pos, index)); //一段段拼接
                sb.append(newPattern);
                pos = index + patLen;
            }
            sb.append(inString.substring(pos));
            return sb.toString();
        }else{
            return inString; //没有就直接返回
        }
    }
}
