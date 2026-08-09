/*
 * Author    Yura Krymlov
 * Created   2024-02
 * Version   2.10.03
 */
 
#include "swephexp.h"
#include "swetest_jni.h"
//#include <android/log.h>

#define GET_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    jboolean IS_COPY = JNI_FALSE;                                   \
    const char* CCSTRING = (NULL != JSTRING) ? (*env)->GetStringUTFChars(env, JSTRING, &IS_COPY) : NULL;

#define RLZ_STRING_UTF_CHARS(IS_COPY, JSTRING, CCSTRING)\
    if (JNI_TRUE == IS_COPY && NULL != JSTRING) (*env)->ReleaseStringUTFChars(env, JSTRING, CCSTRING);

/* The command line lands here, and it is easily longer than AS_MAXCH: the app appends
 * -edir<absolute path>, which on Android is ~55 characters on its own. strcpy() into a
 * 256 byte stack buffer was a stack overflow waiting for a long command. */
#define SWE_ARGS_SIZE 4096

#define CPY_CSTRING_TO_CHARS(CCSTRING, CHARS)          \
    char CHARS[SWE_ARGS_SIZE];                         \
    if (NULL == CCSTRING) *CHARS = '\0';               \
    else {                                             \
        strncpy(CHARS, CCSTRING, SWE_ARGS_SIZE - 1);   \
        CHARS[SWE_ARGS_SIZE - 1] = '\0';               \
    }

void addToBuilder1(JNIEnv *env, char *chArray, jobject builder) {
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


char SWE_TMP[SWE_TMP_SIZE] = "";
char SWE_OUT[SWE_OUT_SIZE] = "";

static size_t swe_out_len = 0;

void swe_out_reset(void) {
    swe_out_len = 0;
    SWE_OUT[0] = '\0';
    SWE_TMP[0] = '\0';
}

void swe_out_append(const char *text) {
    static const char TRUNCATED[] = "\n...[output truncated]\n";

    if (NULL == text) return;
    if (swe_out_len >= SWE_OUT_SIZE - sizeof(TRUNCATED)) return;   /* already marked */

    size_t room = SWE_OUT_SIZE - sizeof(TRUNCATED) - swe_out_len;
    size_t len = strlen(text);

    if (len > room) {
        memcpy(SWE_OUT + swe_out_len, text, room);
        swe_out_len += room;
        memcpy(SWE_OUT + swe_out_len, TRUNCATED, sizeof(TRUNCATED));
        swe_out_len += sizeof(TRUNCATED) - 1;
    } else {
        memcpy(SWE_OUT + swe_out_len, text, len + 1);
        swe_out_len += len;
    }
}

//#define JNI_LOG(...) __android_log_print(ANDROID_LOG_INFO,"SWE-TEST",__VA_ARGS__)


JNIEXPORT jint JNICALL Java_swisseph_SwissephTest_swe_1test_1main(JNIEnv *env, jclass swetest, jstring jargs, jobject sout) {
	GET_STRING_UTF_CHARS(isCopy, jargs, cargs)
	
	//JNI_LOG("%s", cargs);
	
	char ** argv  = NULL;
	CPY_CSTRING_TO_CHARS(cargs, args)
	char *p    = strtok (args, " ");
	int n_spaces = 0;

	while (p) {
	  argv = realloc(argv, sizeof (char*) * ++n_spaces);
	  argv[n_spaces - 1] = p;
	  //JNI_LOG("%s", p);
	  p = strtok(NULL, " ");
	}
	
	argv = realloc(argv, sizeof (char*) * (n_spaces+1));
	argv[n_spaces] = 0;
	
	swe_out_reset();
	
	int32 ret = swe_test_main(n_spaces, argv);
	
	free(argv);
	RLZ_STRING_UTF_CHARS(isCopy, jargs, cargs)
	
	addToBuilder1(env, SWE_OUT, sout);
	//JNI_LOG("%s", SWE_OUT);
	
    swe_test_init();

    return ret;
}
