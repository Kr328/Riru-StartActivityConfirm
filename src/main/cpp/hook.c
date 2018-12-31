#include "hook.h"

#include <stdio.h>
#include <string.h>
#include <string.h>
#include <signal.h>

#define ELMLEN(a) (sizeof(a)/sizeof(*a))

static int call_inject_application(JNIEnv *env);
static int call_inject_system(JNIEnv *env);
static int system_server = 0;

// Patch Android Framework
static int android_runtime_start_reg_replaced(JNIEnv *env);
static int (*android_runtime_start_reg_original)(JNIEnv *env);

static riru_utils_native_replace_t native_replace_list[] = {
	{".*android_runtime.*" ,
	"_ZN7android14AndroidRuntime8startRegEP7_JNIEnv" ,
	(void*)&android_runtime_start_reg_replaced ,
	(void**)&android_runtime_start_reg_original}
};

int hook_install() {
    riru_utils_init_module("start_activity_confirm");
	riru_utils_replace_native_functions(native_replace_list ,ELMLEN(native_replace_list));

	return 0;
}

static jlong android_os_binder_clear_calling_identity_replaced(JNIEnv *env ,jobject thiz);
static jlong (*android_os_binder_clear_calling_identity_original)(JNIEnv *env ,jobject thiz);

static riru_utils_jni_replace_method_t jni_replace_list[] = {
    {"android/os/Binder" ,
    "clearCallingIdentity" ,
    "()J" ,
    (void*)&android_os_binder_clear_calling_identity_replaced ,
    (void**)&android_os_binder_clear_calling_identity_original}
};

static jlong android_os_binder_clear_calling_identity_replaced(JNIEnv *env ,jobject thiz) {
    static int prevent_next = 0;

    if ( !prevent_next )
        if ( system_server )
            prevent_next = call_inject_system(env);
        else
            prevent_next = call_inject_application(env);

    return android_os_binder_clear_calling_identity_original(env ,thiz);
}

// Logic Implemention
static int       class_loaded = 0;
static jmethodID java_inject_system_server_method = NULL;
static jmethodID java_inject_application_method = NULL;
static jclass    java_inject_class = NULL;

static int android_runtime_start_reg_replaced(JNIEnv *env) {
	int result = android_runtime_start_reg_original(env);

	if ( !class_loaded ) {
		if ((java_inject_class = (*env)->FindClass(env,"com/github/kr328/sac/Injector")) != NULL ) {
		    java_inject_system_server_method = (*env)->GetStaticMethodID(env ,java_inject_class ,"injectSystem" ,"()I");
		    java_inject_application_method = (*env)->GetStaticMethodID(env ,java_inject_class ,"injectApplication" ,"()I");
		}
		else
		    LOGE("Find Class failure.");

		class_loaded = 1;
	}

	return result;
}

static int call_inject_application(JNIEnv *env) {
    return (*env)->CallStaticIntMethod(env ,java_inject_class ,java_inject_application_method);
}

static int call_inject_system(JNIEnv *env) {
    return (*env)->CallStaticIntMethod(env ,java_inject_class ,java_inject_system_server_method);
}

void on_post_fork_system_server(JNIEnv *env) {
    system_server = 1;
    riru_utils_replace_jni_methods(jni_replace_list ,ELMLEN(jni_replace_list) ,env);
}

void on_post_fork_application(JNIEnv *env ,const char *package) {
    system_server = 0;
    riru_utils_replace_jni_methods(jni_replace_list ,ELMLEN(jni_replace_list) ,env);
}