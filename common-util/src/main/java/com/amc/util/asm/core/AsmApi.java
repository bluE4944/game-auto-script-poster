package com.amc.util.asm.core;

import org.objectweb.asm.Type;

/**
 * @author AMC
 */
public class AsmApi {

    protected static String replaceText(String text) {
        return text.replace("/", ".");
    }

    protected static String parseTypeText(String text) {
        Type type = Type.getType(text);
        return type.getClassName();
    }

    protected static String getGenericType(String className, String genericName) {
        return className + "<" + genericName + ">";
    }

}
