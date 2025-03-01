package com.amc.util.common.model;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Modifier;

@Getter
@Setter
public class BaseMetaData {

    /**
     * 访问标志
     */
    private Integer access;


    public boolean isPublic() {
        return Modifier.isPublic(access);
    }
    public boolean isStatic() {
        return Modifier.isStatic(access);
    }
    public boolean isAbstract() {
        return Modifier.isAbstract(access);
    }
    public boolean isFinal() {
        return Modifier.isFinal(access);
    }
    public boolean isInterface() {
        return Modifier.isInterface(access);
    }
    public boolean isSynchronized() {
        return Modifier.isSynchronized(access);
    }

    public static boolean isBridge(int access) {
        return (access & 0x00000040) != 0;
    }

}
