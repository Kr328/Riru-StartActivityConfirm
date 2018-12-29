#include <stdio.h>
#include <jni.h>
#include <dlfcn.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <android/log.h>

#include "hook.h"
#include "log.h"

//#define DEX_PATH               "/data/local/tmp/injector.jar"
#define DEX_PATH "/system/framework/boot-display-cutout-mode.jar"

__attribute__((visibility("default")))
void onModuleLoaded() {
    char buffer[4096];
    char *p = NULL;

    strcpy(buffer,(p = getenv("CLASSPATH")) ? p : "");
    strcat(buffer,":" DEX_PATH);
    setenv("CLASSPATH",buffer,1);

    hook_install();
}

__attribute__((visibility("default")))
int nativeForkSystemServerPost(JNIEnv *env, jclass clazz, jint res) {
	if (res ==  0) {
		LOGE("Injecting SystemServer");
		on_post_fork_system_server(env);
	}
	return 0;
}