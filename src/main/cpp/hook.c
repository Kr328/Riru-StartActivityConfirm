#include "hook.h"

#include <stdio.h>
#include <string.h>
#include <string.h>
#include <signal.h>

#define ELMLEN(a) (sizeof(a)/sizeof(*a))

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
    riru_utils_init_module("display_cutout_mode");
	riru_utils_replace_native_functions(native_replace_list ,ELMLEN(native_replace_list));

	return 0;
}

// Logic Implemention
static int       class_loaded = 0;
static jmethodID java_inject_method = NULL;
static jclass    java_inject_class  = NULL;

static int android_runtime_start_reg_replaced(JNIEnv *env) {
	int result = android_runtime_start_reg_original(env);

	if ( !class_loaded ) {
		if ((java_inject_class = (*env)->FindClass(env,"com/github/kr328/dcm/Injector")) != NULL ) {
		    java_inject_method = (*env)->GetStaticMethodID(env ,java_inject_class ,"inject" ,"()V");
		}
		else
		    LOGE("Find Class failure.");

		class_loaded = 1;
	}

	return result;
}

void on_post_fork_system_server(JNIEnv *env) {
    if ( java_inject_method == NULL ) {
        LOGE(TAG ,"Inject method find failure.");
        return ;
    }

    (*env)->CallStaticVoidMethod(env ,java_inject_class ,java_inject_method);
}