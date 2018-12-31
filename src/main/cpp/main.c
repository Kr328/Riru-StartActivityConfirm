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
#define DEX_PATH "/system/framework/boot-start-activity-confirm.jar"
#define WHITELIST_PATH "/system/etc/sac/source_whitelist/"

static const char *package_name = NULL;

const char *parse_package_name(JNIEnv *env, jstring appDataDir) {
    if (!appDataDir)
        return 0;

    const char *app_data_dir = (*env)->GetStringUTFChars(env ,appDataDir, NULL);

    int user = 0;
    static char _package_name[256];
    if (sscanf(app_data_dir, "/data/%*[^/]/%d/%s", &user, _package_name) != 2) {
        if (sscanf(app_data_dir, "/data/%*[^/]/%s", _package_name) != 1) {
            _package_name[0] = '\0';
            LOGW("can't parse %s", app_data_dir);
            return NULL;
        }
    }

    (*env)->ReleaseStringUTFChars(env ,appDataDir, app_data_dir);

    return _package_name;
}

int match_whitelist(int uid ,const char *package) {
    char buffer[1024];

    sprintf(buffer ,WHITELIST_PATH "/package.%s" ,package);
    if ( access(buffer ,F_OK) == 0 )
        return 1;

    sprintf(buffer ,WHITELIST_PATH "/uid.%d" ,uid);
    if ( access(buffer ,F_OK) == 0 )
        return 1;

    return 0;
}

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

__attribute__((visibility("default"))) void nativeForkAndSpecializePre(JNIEnv *env, jclass clazz,
                                                                       jint _uid, jint gid,
                                                                       jintArray gids,
                                                                       jint runtime_flags,
                                                                       jobjectArray rlimits,
                                                                       jint _mount_external,
                                                                       jstring se_info,
                                                                       jstring se_name,
                                                                       jintArray fdsToClose,
                                                                       jintArray fdsToIgnore,
                                                                       jboolean is_child_zygote,
                                                                       jstring instructionSet,
                                                                       jstring appDataDir) {
    package_name = parse_package_name(env ,appDataDir);

    if ( !package_name || match_whitelist(_uid ,package_name) ) {
        LOGI("Skip %s" ,package_name);
        package_name = NULL;
    }
}

__attribute__((visibility("default")))
int nativeForkAndSpecializePost(JNIEnv *env, jclass clazz, jint res) {
    if ( res || !package_name )
        return 0;

    on_post_fork_application(env ,package_name);

    return 0;
}
