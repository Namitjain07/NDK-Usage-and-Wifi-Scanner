#ifndef MATRIX_OPERATIONS_H
#define MATRIX_OPERATIONS_H

#include <vector>
#include <stdexcept>
#include <string>

class Matrix {
private:
    std::vector<std::vector<double>> data;
    int rows;
    int cols;

public:
    Matrix(int rows, int cols);
    Matrix(const std::vector<std::vector<double>>& data);
    
    int getRows() const;
    int getCols() const;
    
    void setElement(int row, int col, double value);
    double getElement(int row, int col) const;
    
    Matrix add(const Matrix& other) const;
    Matrix subtract(const Matrix& other) const;
    Matrix multiply(const Matrix& other) const;
    Matrix divide(const Matrix& other) const; // Divide by B = A * (B^-1)
    
    Matrix inverse() const;
    double determinant() const;
    Matrix adjoint() const;
    
    std::string toString() const;
};

#endif // MATRIX_OPERATIONS_H
