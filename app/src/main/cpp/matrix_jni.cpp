#include <jni.h>
#include <string>
#include "MatrixOperations.h"

// Convert jdoubleArray to C++ vector
std::vector<std::vector<double>> convertJavaMatrixToCpp(JNIEnv* env, jobjectArray javaMatrix, int rows, int cols) {
    std::vector<std::vector<double>> cppMatrix(rows, std::vector<double>(cols));
    
    for (int i = 0; i < rows; i++) {
        jdoubleArray row = (jdoubleArray)env->GetObjectArrayElement(javaMatrix, i);
        jdouble* elements = env->GetDoubleArrayElements(row, nullptr);
        
        for (int j = 0; j < cols; j++) {
            cppMatrix[i][j] = elements[j];
        }
        
        env->ReleaseDoubleArrayElements(row, elements, JNI_ABORT);
        env->DeleteLocalRef(row);
    }
    
    return cppMatrix;
}

// Convert C++ Matrix to Java double[][]
jobjectArray convertCppMatrixToJava(JNIEnv* env, const Matrix& matrix) {
    int rows = matrix.getRows();
    int cols = matrix.getCols();
    
    // Create Java array of array (double[][])
    jclass doubleArrayClass = env->FindClass("[D");
    jobjectArray result = env->NewObjectArray(rows, doubleArrayClass, nullptr);
    
    for (int i = 0; i < rows; i++) {
        jdoubleArray row = env->NewDoubleArray(cols);
        jdouble* elements = new jdouble[cols];
        
        for (int j = 0; j < cols; j++) {
            elements[j] = matrix.getElement(i, j);
        }
        
        env->SetDoubleArrayRegion(row, 0, cols, elements);
        env->SetObjectArrayElement(result, i, row);
        
        delete[] elements;
        env->DeleteLocalRef(row);
    }
    
    return result;
}

// JNI function implementations
extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_com_matrix_matrix_MatrixCalculator_addMatricesNative(
        JNIEnv* env, jobject /* this */, jobjectArray matrixA, jobjectArray matrixB,
        jint rowsA, jint colsA, jint rowsB, jint colsB) {
    
    try {
        std::vector<std::vector<double>> cppMatrixA = convertJavaMatrixToCpp(env, matrixA, rowsA, colsA);
        std::vector<std::vector<double>> cppMatrixB = convertJavaMatrixToCpp(env, matrixB, rowsB, colsB);
        
        Matrix a(cppMatrixA);
        Matrix b(cppMatrixB);
        
        Matrix result = a.add(b);
        return convertCppMatrixToJava(env, result);
    } catch (const std::exception& e) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, e.what());
        return nullptr;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_matrix_matrix_MatrixCalculator_subtractMatricesNative(
        JNIEnv* env, jobject /* this */, jobjectArray matrixA, jobjectArray matrixB,
        jint rowsA, jint colsA, jint rowsB, jint colsB) {
    
    try {
        std::vector<std::vector<double>> cppMatrixA = convertJavaMatrixToCpp(env, matrixA, rowsA, colsA);
        std::vector<std::vector<double>> cppMatrixB = convertJavaMatrixToCpp(env, matrixB, rowsB, colsB);
        
        Matrix a(cppMatrixA);
        Matrix b(cppMatrixB);
        
        Matrix result = a.subtract(b);
        return convertCppMatrixToJava(env, result);
    } catch (const std::exception& e) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, e.what());
        return nullptr;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_matrix_matrix_MatrixCalculator_multiplyMatricesNative(
        JNIEnv* env, jobject /* this */, jobjectArray matrixA, jobjectArray matrixB,
        jint rowsA, jint colsA, jint rowsB, jint colsB) {
    
    try {
        std::vector<std::vector<double>> cppMatrixA = convertJavaMatrixToCpp(env, matrixA, rowsA, colsA);
        std::vector<std::vector<double>> cppMatrixB = convertJavaMatrixToCpp(env, matrixB, rowsB, colsB);
        
        Matrix a(cppMatrixA);
        Matrix b(cppMatrixB);
        
        Matrix result = a.multiply(b);
        return convertCppMatrixToJava(env, result);
    } catch (const std::exception& e) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, e.what());
        return nullptr;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_matrix_matrix_MatrixCalculator_divideMatricesNative(
        JNIEnv* env, jobject /* this */, jobjectArray matrixA, jobjectArray matrixB,
        jint rowsA, jint colsA, jint rowsB, jint colsB) {
    
    try {
        std::vector<std::vector<double>> cppMatrixA = convertJavaMatrixToCpp(env, matrixA, rowsA, colsA);
        std::vector<std::vector<double>> cppMatrixB = convertJavaMatrixToCpp(env, matrixB, rowsB, colsB);
        
        Matrix a(cppMatrixA);
        Matrix b(cppMatrixB);
        
        Matrix result = a.divide(b);
        return convertCppMatrixToJava(env, result);
    } catch (const std::exception& e) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, e.what());
        return nullptr;
    }
}

} // extern "C"
