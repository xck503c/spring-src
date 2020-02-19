package org.springframework.util;

import com.sun.xml.internal.ws.api.policy.PolicyResolverFactory;

import java.util.*;

public class StringUtils {

    private static final String FOLDER_SEPARATOR = "/";

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String TOP_PATH = ".."; //上级目录

    private static final String CURRENT_PATH = "."; //当前目录



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

    /**
     * 将inString中的charsToDelete全部剔除，采用的方式就是遍历定位
     */
    public static String deleteAny(String inString, String charsToDelete){
        if(hasLength(inString) && hasLength(charsToDelete)){
            StringBuilder sb = new StringBuilder();

            for(int i=0; i<inString.length(); i++){
                char c = inString.charAt(i);
                if(charsToDelete.indexOf(c) == -1){
                    sb.append(c);
                }
            }
            return sb.toString();
        }else {
            return inString;
        }
    }

    /**
     * 清除路径中多余的..或.符号，进行一个抵消计算
     */
    public static String cleanPath(String path){
        if(path == null){
            return null;
        }else {
            //将windows路径中的\\替换成/
            String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

            /**
             * Strip prefix from path to analyze, to not treat it as part of the first path element.
             * This is necessary to correctly parse paths like "file:core/../core/io/Resource.class",
             * where the ".." should just strip the first "core" directory while keeping
             * the "file:" prefix.
             * 翻译：去除path中的前缀，比如说：file:core/../core/io/Resource.class，这种带有..的路径，
             * 我们会去除第一个core，而保留file:前缀
             *
             * 观察下面的代码，发现：
             * 1. 提取前缀file:
             * 2. 去除多余的.或..
             */
            int prefixIndex = pathToUse.indexOf(":");
            String prefix = "";
            if(prefixIndex != -1){ //如果存在:就截取前缀
                prefix = pathToUse.substring(0, prefixIndex+1);
                if(prefix.contains(FOLDER_SEPARATOR)){ //存在/，这个前缀就不合法了吧
                    prefix = "";
                }else{
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }
            if(pathToUse.startsWith(FOLDER_SEPARATOR)){ //若开头为/，则移动到前缀中
                prefix = prefix + FOLDER_SEPARATOR;
                pathToUse = pathToUse.substring(1);
            }

            //用/分割
            String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
            List<String> pathElements = new LinkedList<>();
            int tops = 0;

            //例如：root/../a/b/./c/d/../e/../../dst/a.html
            //==> a/b/dst/a.html
            for(int i=pathArray.length-1; i>=0; i--){
                String element = pathArray[i];
                if(!CURRENT_PATH.equals(element)){ //如果.的就忽略
                    if(TOP_PATH.equals(element)){ //遇到上级目录就累加
                        tops++;
                    } else if(tops > 0){ //非上级目录，但是它的后面有上级目录，就抵消
                        tops--;
                    } else {
                        pathElements.add(0, element); //不断插入头部
                    }
                }
            }

            //如果上级目录太多了，就全部插入到最前面
            //例如：root/../../a.txt
            //==>。../a.txt
            for(int i = 0; i < tops; ++i) {
                pathElements.add(0, TOP_PATH);
            }
            //最后将这些全部拼接里返回
            return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
        }
    }

    public static String[] toStringArray(Collection<String> collection){
        return collection == null ? null : collection.toArray(new String[collection.size()]);
    }

    /**
     * 将str用分隔符分割，然后放到String数组中返回，这种方式和split比起来如何？
     */
    public static String[] delimitedListToStringArray(String str, String delimited){
        return delimitedListToStringArray(str, delimited, null);
    }

    /**
     * 分割的时候，顺带删除指定字符串
     */
    public static String[] delimitedListToStringArray(String str, String delimited, String charsToDelete){
        if(str == null){
            return new String[0];
        }else if(delimited == null){ //不用分割了
            return new String[]{str};
        }else{
            List<String> result = new ArrayList<>();
            int pos;
            if("".equals(delimited)){ //分割符为空字符串那就一个个遍历删除charsToDelete
                for(pos=0; pos<str.length(); pos++){
                    result.add(deleteAny(str.substring(pos, pos+1), charsToDelete));
                }
            }else{ //如果不是，那就需要遍历递增定位分隔符，然后分割删除charsToDelete
                int delPos;
                for(pos=0; (delPos=str.indexOf(delimited, pos))!=-1; pos = delPos+delimited.length()){
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }

                //最后如果pos后面没有分隔符了，就会跳出循环，这里保证可以最后这部分可以删除指定字符串
                if (str.length() > 0 && pos <= str.length()) {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }
            return toStringArray(result);
        }
    }

    /**
     * 针对集合coll，进行累加拼接
     * 前缀+集合元素+后缀+分隔符...
     * @param coll
     * @param delim 分隔符
     * @param prefix 前缀
     * @param suffix 后缀
     * @return
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        if (CollectionUtils.isEmpty(coll)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator it = coll.iterator();

            while(it.hasNext()) {
                sb.append(prefix).append(it.next()).append(suffix);
                if (it.hasNext()) {
                    sb.append(delim); //这个写法不错
                }
            }

            return sb.toString();
        }
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim){
        return collectionToDelimitedString(coll, delim, "", "");
    }
}
