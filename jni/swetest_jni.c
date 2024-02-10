/*
 * Author    Yura Krymlov
 * Created   2024-02
 * Version   2.10.03
 */
 
#include "swephexp.h"
#include "swetest_jni.h"

JNIEXPORT jint JNICALL Java_swisseph_SweTest_swe_1test_1main(JNIEnv *env, jclass swetest, jobjectArray jargv) {
	//jargv is a Java array of Java strings
    int32 argc = (*env)->GetArrayLength(env, jargv);
	char **argv = (char **)malloc(argc);
	
	for (int i=0; i < argc; ++i) 
	{
		jstring javaString = (jstring) (*env)->GetObjectArrayElement(env, jargv, i);
		const char *nativeString = (*env)->GetStringUTFChars(env, javaString, 0);

		argv[i] = strdup(nativeString);

		(*env)->ReleaseStringUTFChars(env, javaString, nativeString);
		(*env)->DeleteLocalRef(env, javaString);
	}
	
    return swe_test_main(argc, argv);
}
