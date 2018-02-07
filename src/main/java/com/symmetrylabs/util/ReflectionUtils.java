package com.symmetrylabs.util;

import heronarts.lx.LXPattern;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static processing.core.PApplet.println;


public final class ReflectionUtils {

    public static void replaceAllFields(Object start, List oldObjects, List newObjects) {
        Set duplicateCheck = new HashSet();
        LinkedList traversal = new LinkedList();
        duplicateCheck.add(start);
        traversal.add(start);
        ListIterator iter = traversal.listIterator(0);
        while (iter.hasNext()) {
            Object obj = iter.next();
            Class objClass = obj.getClass();
            // println("objClass.getName(): "+objClass.getName());
            while (objClass != null) {
                for (Field field : objClass.getDeclaredFields()) {
                    // println("field: "+field);
                    field.setAccessible(true);
                    try {
                        Class fieldType = field.getType();
                        Object fieldObj = field.get(obj);
                        if (fieldObj == null) continue;
                        if (fieldType.isAssignableFrom(LXPattern.class)) {
                            if (fieldObj != null) {
                                int index = oldObjects.indexOf(fieldObj);
                                if (index != -1) {
                                    Object newObj = newObjects.get(index);
                                    setField(field, obj, newObj);
                                }
                            }
                        } else if (fieldType.getPackage() == null || fieldType.getPackage().getName().startsWith("heronarts")) {
                            if (!duplicateCheck.contains(fieldObj)) {
                                duplicateCheck.add(fieldObj);
                                iter.add(fieldObj);
                                iter.previous();
                            }
                        } else if (List.class.isAssignableFrom(fieldType)) {
                            List list = (List) fieldObj;
                            for (int i = 0; i < list.size(); i++) {
                                Object o = list.get(i);
                                if (o == null) continue;
                                if (o instanceof LXPattern) {
                                    int index = oldObjects.indexOf(fieldObj);
                                    if (index != -1) {
                                        Object newObj = newObjects.get(index);
                                        list.set(i, newObj);
                                    }
                                } else if (o.getClass().getPackage() == null || o.getClass()
                                    .getPackage()
                                    .getName()
                                    .startsWith("heronarts")) {
                                    if (!duplicateCheck.contains(o)) {
                                        duplicateCheck.add(o);
                                        iter.add(o);
                                        iter.previous();
                                    }
                                }
                            }
                        } else if (fieldType.isArray()) {
                            Object[] array = (Object[]) fieldObj;
                            for (int i = 0; i < array.length; i++) {
                                Object o = array[i];
                                if (o == null) continue;
                                if (o instanceof LXPattern) {
                                    int index = oldObjects.indexOf(fieldObj);
                                    if (index != -1) {
                                        Object newObj = newObjects.get(index);
                                        array[i] = newObj;
                                    }
                                } else if (o.getClass().getPackage() == null || o.getClass()
                                    .getPackage()
                                    .getName()
                                    .startsWith("heronarts")) {
                                    if (!duplicateCheck.contains(o)) {
                                        duplicateCheck.add(o);
                                        iter.add(o);
                                        iter.previous();
                                    }
                                }
                            }
                        }
                    } catch (IllegalAccessException e) {
                        println("e: " + e);
                        continue;
                    }
                }
                objClass = objClass.getSuperclass();
            }
        }
    }

    public static void swapObjects(Object original, Object replacement) {
        // println("original.getClass().getName(): "+original.getClass().getName());
        Class<?> originalClass = original.getClass();
        Class<?> replacementClass = replacement.getClass();
        while (originalClass != null && replacementClass != null) {
            for (Field oldField : originalClass.getDeclaredFields()) {
                // println("oldField: "+oldField);
                setField(oldField, replacementClass, original, replacement);
            }
            originalClass = originalClass.getSuperclass();
            replacementClass = replacementClass.getSuperclass();
        }
    }

    public static void setField(Field originalField, Class replacementClass, Object original, Object replacement) {
        try {
            Field newField = replacementClass.getDeclaredField(originalField.getName());

            if (!newField.getType().isAssignableFrom(originalField.getType())) return;

            newField.setAccessible(true);
            originalField.setAccessible(true);

            // ignore final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(newField, newField.getModifiers() & ~Modifier.FINAL);

            newField.set(replacement, originalField.get(original));
        } catch (NoSuchFieldException e) {
            println("setField: " + e);
            return;
        } catch (IllegalAccessException e) {
            println("setField: " + e);
            return;
        }
    }

    public static void setField(Field field, Object owner, Object newValue) {
        try {
            field.setAccessible(true);

            // ignore final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(owner, newValue);
        } catch (NoSuchFieldException e) {
            return;
        } catch (IllegalAccessException e) {
            println("e: " + e);
            return;
        }
    }
}
