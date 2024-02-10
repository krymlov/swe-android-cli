/*
 * Author    Yura Krymlov
 * Created   2024-02
 * Version   2.10.03
 */
 
#include "swephexp.h"
#include "swetest_jni.h"
#include <android/log.h>

#define  LOG_TAG    "SWE-TEST-MAIN"
#define  iPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  wPRINTF(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)

#define JNI_RELEASE (ERR >= retc ? JNI_ABORT : JNI_OK)

#define DEFINE_CHAR_SERR char serr[AS_MAXCH];
#define ERR_BUILDER_APPEND_IF_ERR if (ERR >= retc && NULL != errBuilder) appendToBuilder(env, serr, errBuilder);
#define ERR_BUILDER_APPEND_IF_SERR if (*serr != '\0' && NULL != errBuilder) appendToBuilder(env, serr, errBuilder);

#define GET_DOUBLE_ARRAY_ELEMENTS(IS_COPY, JDOUBLE_ARRAY, ELEMENTS)\
    jboolean IS_COPY = JNI_FALSE;                                              \
    jdouble* ELEMENTS = (NULL != JDOUBLE_ARRAY) ? (*env)->GetDoubleArrayElements(env, JDOUBLE_ARRAY, &IS_COPY) : NULL;

#define RLZ_DOUBLE_ARRAY_ELEMENTS_OK(IS_COPY, JDOUBLE_ARRAY, ELEMENTS)\
    if (JNI_TRUE == IS_COPY && NULL != JDOUBLE_ARRAY) (*env)->ReleaseDoubleArrayElements(env, JDOUBLE_ARRAY, ELEMENTS, JNI_OK);

#define RLZ_DOUBLE_ARRAY_ELEMENTS(IS_COPY, JDOUBLE_ARRAY, ELEMENTS)\
    if (JNI_TRUE == IS_COPY && NULL != JDOUBLE_ARRAY) (*env)->ReleaseDoubleArrayElements(env, JDOUBLE_ARRAY, ELEMENTS, JNI_RELEASE);

#define GET_INT_ARRAY_ELEMENTS(IS_COPY, JINT_ARRAY, ELEMENTS) \
    jboolean IS_COPY = JNI_FALSE;                                         \
    jint * ELEMENTS = (NULL != JINT_ARRAY) ? (*env)->GetIntArrayElements(env, JINT_ARRAY, &IS_COPY) : NULL;

#define RLZ_INT_ARRAY_ELEMENTS_OK(IS_COPY, JINT_ARRAY, ELEMENTS)\
    if (JNI_TRUE == IS_COPY && NULL != JINT_ARRAY) (*env)->ReleaseIntArrayElements(env, JINT_ARRAY, ELEMENTS, JNI_OK);

#define GET_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    jboolean IS_COPY = JNI_FALSE;                                   \
    const char* CCSTRING = (NULL != JSTRING) ? (*env)->GetStringUTFChars(env, JSTRING, &IS_COPY) : NULL;

#define RLZ_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    if (JNI_TRUE == IS_COPY && NULL != JSTRING) (*env)->ReleaseStringUTFChars(env, JSTRING, CCSTRING);

#define CPY_CSTRING_TO_CHARS(CCSTRING, CHARS)    \
    char CHARS[AS_MAXCH];                        \
    if (NULL == CCSTRING) *CHARS = '\0';         \
    else strcpy(CHARS, CCSTRING);

#define BUILDER_APPEND_IF_DIFF(BUILDER, CCSTRING, CHARS)    \
    if (NULL != BUILDER && NULL != CCSTRING && 0 != strcmp(CCSTRING, CHARS)) { \
        emptyBuilder(env, BUILDER);                         \
        appendToBuilder(env, CHARS, BUILDER);               \
    }

JNIEXPORT jint JNICALL Java_swisseph_SweTest_swe_1test_1main(JNIEnv *env, jclass swetest, jstring jargs, jint jargc, jobject jout) {
	GET_STRING_UTF_CHARS(isCopy, jargs, cargs)
	iPRINTF("START: %s", cargs);
	
	char ** argv  = NULL;
	CPY_CSTRING_TO_CHARS(cargs, args)
	char *p    = strtok (args, " ");
	int n_spaces = 0;

	while (p) {
	  argv = realloc (argv, sizeof (char*) * ++n_spaces);
	  argv[n_spaces - 1] = p;
	  iPRINTF("%s", p);
	  p = strtok(NULL, " ");
	}
	
	argv = realloc (argv, sizeof (char*) * (n_spaces+1));
	argv[n_spaces] = 0;
	
	int32 ret = swe_test_main(jargc, argv, jout);
	
	free (argv);
	RLZ_STRING_UTF_CHARS(isCopy, jargs, cargs)
	
	iPRINTF("END: %i", ret);
	
    return ret;
}
