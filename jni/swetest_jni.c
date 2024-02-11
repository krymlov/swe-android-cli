/*
 * Author    Yura Krymlov
 * Created   2024-02
 * Version   2.10.03
 */
 
#include "swephexp.h"
#include "swetest_jni.h"
#include <android/log.h>

#define GET_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    jboolean IS_COPY = JNI_FALSE;                                   \
    const char* CCSTRING = (NULL != JSTRING) ? (*env)->GetStringUTFChars(env, JSTRING, &IS_COPY) : NULL;

#define RLZ_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    if (JNI_TRUE == IS_COPY && NULL != JSTRING) (*env)->ReleaseStringUTFChars(env, JSTRING, CCSTRING);

#define CPY_CSTRING_TO_CHARS(CCSTRING, CHARS)    \
    char CHARS[AS_MAXCH];                        \
    if (NULL == CCSTRING) *CHARS = '\0';         \
    else strcpy(CHARS, CCSTRING);

void addToBuilder(JNIEnv *env, char *chArray, jobject builder) {
    if (NULL == builder) return;

    // Obtain the Java StringBuilder class handle
    jclass clazz = (*env)->GetObjectClass(env, builder);

    // Obtain the method ID for the StringBuilder append method
    jmethodID mid = (*env)->GetMethodID(env, clazz, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");

    // If this method does not exist then return.
    if (mid == 0) return;

    // Create a new Java String object for the given char Array
    jstring jArray = (*env)->NewStringUTF(env, chArray);

    // Call the StringBuilder object's append method
    (*env)->CallObjectMethod(env, builder, mid, jArray);
}

char SWE_OUT[1024 * 1024] = "";
char SWE_TMP[1024] = "";

#define JNI_LOG(...)  __android_log_print(ANDROID_LOG_INFO,"SWE-TEST",__VA_ARGS__)


JNIEXPORT jint JNICALL Java_swisseph_SwephExp_swe_1test_1main(JNIEnv *env, jclass swetest, jstring jargs, jint jargc, jobject sout) {
	GET_STRING_UTF_CHARS(isCopy, jargs, cargs)
	JNI_LOG("START: %s", cargs);
	
	char ** argv  = NULL;
	CPY_CSTRING_TO_CHARS(cargs, args)
	char *p    = strtok (args, " ");
	int n_spaces = 0;

	while (p) {
	  argv = realloc (argv, sizeof (char*) * ++n_spaces);
	  argv[n_spaces - 1] = p;
	  JNI_LOG("%s", p);
	  p = strtok(NULL, " ");
	}
	
	argv = realloc (argv, sizeof (char*) * (n_spaces+1));
	argv[n_spaces] = 0;
	
	int32 ret = swe_test_main(jargc, argv);
	
	free (argv);
	RLZ_STRING_UTF_CHARS(isCopy, jargs, cargs)
	
	JNI_LOG("END: %i => %s", ret, SWE_OUT);
	addToBuilder(env, SWE_OUT, sout);
	
    return ret;
}
