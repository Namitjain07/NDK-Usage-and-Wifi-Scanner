package com.matrix.matrix

class MatrixCalculator {

    init {
        System.loadLibrary("matrix-lib")
    }

    // Native methods
    external fun addMatricesNative(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>,
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int
    ): Array<DoubleArray>

    external fun subtractMatricesNative(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>,
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int
    ): Array<DoubleArray>

    external fun multiplyMatricesNative(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>,
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int
    ): Array<DoubleArray>

    external fun divideMatricesNative(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>,
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int
    ): Array<DoubleArray>

    // Wrapper methods for better error handling
    fun addMatrices(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>): Array<DoubleArray> {
        if (matrixA.isEmpty() || matrixB.isEmpty()) {
            throw IllegalArgumentException("Matrices cannot be empty")
        }
        
        val rowsA = matrixA.size
        val colsA = matrixA[0].size
        val rowsB = matrixB.size
        val colsB = matrixB[0].size
        
        if (rowsA != rowsB || colsA != colsB) {
            throw IllegalArgumentException("Matrices must have the same dimensions for addition")
        }
        
        return addMatricesNative(matrixA, matrixB, rowsA, colsA, rowsB, colsB)
    }
    
    fun subtractMatrices(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>): Array<DoubleArray> {
        if (matrixA.isEmpty() || matrixB.isEmpty()) {
            throw IllegalArgumentException("Matrices cannot be empty")
        }
        
        val rowsA = matrixA.size
        val colsA = matrixA[0].size
        val rowsB = matrixB.size
        val colsB = matrixB[0].size
        
        if (rowsA != rowsB || colsA != colsB) {
            throw IllegalArgumentException("Matrices must have the same dimensions for subtraction")
        }
        
        return subtractMatricesNative(matrixA, matrixB, rowsA, colsA, rowsB, colsB)
    }
    
    fun multiplyMatrices(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>): Array<DoubleArray> {
        if (matrixA.isEmpty() || matrixB.isEmpty()) {
            throw IllegalArgumentException("Matrices cannot be empty")
        }
        
        val rowsA = matrixA.size
        val colsA = matrixA[0].size
        val rowsB = matrixB.size
        val colsB = matrixB[0].size
        
        if (colsA != rowsB) {
            throw IllegalArgumentException("Matrix A columns must equal Matrix B rows for multiplication")
        }
        
        return multiplyMatricesNative(matrixA, matrixB, rowsA, colsA, rowsB, colsB)
    }
    
    fun divideMatrices(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>): Array<DoubleArray> {
        if (matrixA.isEmpty() || matrixB.isEmpty()) {
            throw IllegalArgumentException("Matrices cannot be empty")
        }
        
        val rowsA = matrixA.size
        val colsA = matrixA[0].size
        val rowsB = matrixB.size
        val colsB = matrixB[0].size
        
        if (rowsB != colsB) {
            throw IllegalArgumentException("Matrix B must be square for division")
        }
        
        if (colsA != rowsB) {
            throw IllegalArgumentException("Matrix A columns must equal Matrix B rows for division")
        }
        
        return divideMatricesNative(matrixA, matrixB, rowsA, colsA, rowsB, colsB)
    }
}
