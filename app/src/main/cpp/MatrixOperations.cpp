#include "MatrixOperations.h"
#include <sstream>
#include <iomanip>
#include <cmath>

Matrix::Matrix(int rows, int cols) : rows(rows), cols(cols) {
    data.resize(rows, std::vector<double>(cols, 0.0));
}

Matrix::Matrix(const std::vector<std::vector<double>>& data) : data(data) {
    rows = data.size();
    cols = (rows > 0) ? data[0].size() : 0;
}

int Matrix::getRows() const {
    return rows;
}

int Matrix::getCols() const {
    return cols;
}

void Matrix::setElement(int row, int col, double value) {
    if (row < 0 || row >= rows || col < 0 || col >= cols) {
        throw std::out_of_range("Matrix indices out of range");
    }
    data[row][col] = value;
}

double Matrix::getElement(int row, int col) const {
    if (row < 0 || row >= rows || col < 0 || col >= cols) {
        throw std::out_of_range("Matrix indices out of range");
    }
    return data[row][col];
}

Matrix Matrix::add(const Matrix& other) const {
    if (rows != other.rows || cols != other.cols) {
        throw std::invalid_argument("Matrices dimensions don't match for addition");
    }
    
    Matrix result(rows, cols);
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result.data[i][j] = data[i][j] + other.data[i][j];
        }
    }
    return result;
}

Matrix Matrix::subtract(const Matrix& other) const {
    if (rows != other.rows || cols != other.cols) {
        throw std::invalid_argument("Matrices dimensions don't match for subtraction");
    }
    
    Matrix result(rows, cols);
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result.data[i][j] = data[i][j] - other.data[i][j];
        }
    }
    return result;
}

Matrix Matrix::multiply(const Matrix& other) const {
    if (cols != other.rows) {
        throw std::invalid_argument("Matrix dimensions are not compatible for multiplication");
    }
    
    Matrix result(rows, other.cols);
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < other.cols; j++) {
            for (int k = 0; k < cols; k++) {
                result.data[i][j] += data[i][k] * other.data[k][j];
            }
        }
    }
    return result;
}

double Matrix::determinant() const {
    if (rows != cols) {
        throw std::invalid_argument("Determinant requires a square matrix");
    }
    
    if (rows == 1) {
        return data[0][0];
    }
    
    if (rows == 2) {
        return data[0][0] * data[1][1] - data[0][1] * data[1][0];
    }
    
    double det = 0;
    for (int j = 0; j < cols; j++) {
        std::vector<std::vector<double>> subMatrix(rows - 1, std::vector<double>(cols - 1));
        
        for (int i = 1; i < rows; i++) {
            int subCol = 0;
            for (int k = 0; k < cols; k++) {
                if (k != j) {
                    subMatrix[i-1][subCol++] = data[i][k];
                }
            }
        }
        
        Matrix minor(subMatrix);
        int sign = (j % 2 == 0) ? 1 : -1;
        det += sign * data[0][j] * minor.determinant();
    }
    
    return det;
}

Matrix Matrix::adjoint() const {
    if (rows != cols) {
        throw std::invalid_argument("Adjoint requires a square matrix");
    }
    
    Matrix result(rows, cols);
    
    if (rows == 1) {
        result.data[0][0] = 1;
        return result;
    }
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            std::vector<std::vector<double>> subMatrix(rows - 1, std::vector<double>(cols - 1));
            
            int subRow = 0;
            for (int r = 0; r < rows; r++) {
                if (r != i) {
                    int subCol = 0;
                    for (int c = 0; c < cols; c++) {
                        if (c != j) {
                            subMatrix[subRow][subCol++] = data[r][c];
                        }
                    }
                    subRow++;
                }
            }
            
            Matrix minor(subMatrix);
            int sign = ((i + j) % 2 == 0) ? 1 : -1;
            result.data[j][i] = sign * minor.determinant();  // Note: j,i for transpose
        }
    }
    
    return result;
}

Matrix Matrix::inverse() const {
    double det = determinant();
    if (std::abs(det) < 1e-10) {
        throw std::invalid_argument("Matrix is singular, cannot find inverse");
    }
    
    Matrix adj = adjoint();
    Matrix inverse(rows, cols);
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            inverse.data[i][j] = adj.data[i][j] / det;
        }
    }
    
    return inverse;
}

Matrix Matrix::divide(const Matrix& other) const {
    return multiply(other.inverse());
}

std::string Matrix::toString() const {
    std::stringstream ss;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            ss << std::fixed << std::setprecision(2) << data[i][j];
            if (j < cols - 1) ss << " ";
        }
        if (i < rows - 1) ss << "\n";
    }
    return ss.str();
}
