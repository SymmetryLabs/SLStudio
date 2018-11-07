/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_symmetrylabs_slstudio_ui_gdx_UI */

#ifndef _Included_com_symmetrylabs_slstudio_ui_gdx_UI
#define _Included_com_symmetrylabs_slstudio_ui_gdx_UI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    init
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_init
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    newFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_newFrame
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    render
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_render
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    shutdown
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_shutdown
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    setNextWindowDefaults
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowDefaults
  (JNIEnv *, jclass, jint, jint, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    setNextWindowDefaultToCursor
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowDefaultToCursor
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    setNextWindowContentSize
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowContentSize
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    begin
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_begin
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    beginClosable
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginClosable
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    end
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_end
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    sameLine
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_sameLine
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    columnsStart
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_columnsStart
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    nextColumn
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_nextColumn
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    columnsEnd
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_columnsEnd
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    separator
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_separator
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    spacing
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_spacing
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    beginChild
 * Signature: (Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginChild
  (JNIEnv *, jclass, jstring, jboolean, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    endChild
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_endChild
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    beginGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginGroup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    endGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_endGroup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    text
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_text
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    button
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_button
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    checkbox
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_checkbox
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    selectable
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_selectable
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    inputText
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_inputText
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    colorPicker
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_colorPicker
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    sliderFloat
 * Signature: (Ljava/lang/String;FFFZ)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_sliderFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    sliderInt
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_sliderInt
  (JNIEnv *, jclass, jstring, jint, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    combo
 * Signature: (Ljava/lang/String;I[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_combo
  (JNIEnv *, jclass, jstring, jint, jobjectArray);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    beginMainMenuBar
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    endMainMenuBar
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_endMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    beginMenu
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginMenu
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    endMenu
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_endMenu
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    menuItem
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_menuItem
  (JNIEnv *, jclass, jstring, jstring, jboolean, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    treeNode
 * Signature: (Ljava/lang/String;ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_treeNode
  (JNIEnv *, jclass, jstring, jint, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    treePop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_treePop
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    isItemClicked
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_isItemClicked
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    isItemActive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_isItemActive
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    getFrameRate
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_getFrameRate
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    wantCaptureKeyboard
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureKeyboard
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    wantCaptureMouse
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureMouse
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    keyDown
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyDown
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    keyUp
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyUp
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    addInputCharacter
 * Signature: (C)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_addInputCharacter
  (JNIEnv *, jclass, jchar);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    scrolled
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_scrolled
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_gdx_UI
 * Method:    showDemoWindow
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_showDemoWindow
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
