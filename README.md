<<<<<<< HEAD
# NDK-Usage-and-Wifi-Scanner
=======
<<<<<<< HEAD
# NDK-Usage-and-Wifi-Scanner
=======
# Matrix Calculator for Android

A native Android application for performing advanced matrix operations, featuring an elegant Material Design 3 interface and optimized C++ performance via JNI.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technical Architecture](#technical-architecture)
- [Project Structure](#project-structure)
- [User Interface Components](#user-interface-components)
- [Usage Guide](#usage-guide)
- [Implementation Details](#implementation-details)
  - [JNI Integration](#jni-integration)
  - [C++ Matrix Operations](#c-matrix-operations)
  - [UI Adaptation](#ui-adaptation)
- [System Requirements](#system-requirements)
- [Development Setup](#development-setup)
- [Build Configuration](#build-configuration)
- [License](#license)

---

## Overview

**Matrix Calculator** is a powerful yet user-friendly Android application for performing a variety of matrix operations. It combines a clean, modern UI based on **Material Design 3** principles with native C++ code to ensure highly efficient and optimized computations, making it ideal for both casual users and advanced learners.

---

## Features

- **Dynamic Matrix Creation**: Easily create matrices of any size.
- **Core Operations**:
  - Matrix Addition
  - Matrix Subtraction
  - Matrix Multiplication
  - Matrix Division (implemented via matrix inversion and multiplication)
- **Native Performance**: Heavy computations are offloaded to optimized C++ code using the Java Native Interface (JNI).
- **Elegant and Responsive UI**:
  - Material Design 3 compliant
  - Smooth transitions and animations
- **Theme Adaptation**:
  - Automatically switches between Light and Dark modes based on system settings.
- **Input Validation**:
  - Ensures matrices meet dimensional requirements before proceeding with operations.

---

## Technical Architecture

### Front-End (Android / Kotlin)

- Developed using the latest Android best practices.
- Uses Material 3 components for a consistent and modern design.
- RecyclerView-based dynamic matrix input.
- Robust state management with input validation and UI feedback.
- Animations for smooth UI transitions.

### Back-End (Native C++ via JNI)

- Matrix operations (addition, subtraction, multiplication, inversion) are implemented in native C++ for maximum efficiency.
- JNI acts as a bridge to communicate between the Kotlin frontend and C++ backend.
- CMake is used for managing the native build configuration.

---

## Project Structure

```plaintext
matrix/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/matrix/matrix/
│   │   │   │   ├── MainActivity.kt              # Entry point and main logic
│   │   │   │   ├── MatrixAdapter.kt             # RecyclerView adapter for dynamic input
│   │   │   │   └── MatrixCalculator.kt          # JNI interface for matrix operations
│   │   │   ├── cpp/
│   │   │   │   ├── MatrixOperations.h           # C++ class and method declarations
│   │   │   │   ├── MatrixOperations.cpp         # C++ matrix operation implementations
│   │   │   │   ├── matrix_jni.cpp               # JNI bridge logic
│   │   │   │   └── CMakeLists.txt               # Native build configuration
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml        # Main activity layout
│   │   │   │   │   ├── item_matrix_row.xml      # Layout for a single matrix row
│   │   │   │   │   └── item_result_cell.xml     # Layout for result cells
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── edit_text_border.xml     # Styling for text inputs
│   │   │   │   │   ├── edit_text_rounded.xml
│   │   │   │   │   ├── radio_selector.xml       # Styling for operation selector
│   │   │   │   │   └── result_cell_background.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml               # Color resources
│   │   │   │   │   ├── themes.xml               # Light theme styles
│   │   │   │   │   └── strings.xml              # App string resources
│   │   │   │   └── values-night/
│   │   │   │       ├── colors.xml               # Dark theme colors
│   │   │   │       └── themes.xml               # Dark theme styles
│   │   │   └── AndroidManifest.xml              # App permissions and configuration
│   │   └── test/                                # Unit tests
│   └── build.gradle                             # App-level Gradle build configuration
└── build.gradle                                 # Project-level Gradle build configuration
```

---

## User Interface Components

The UI consists of modular, card-based components for a clean and intuitive experience:

| Component | Purpose |
| :-------- | :------ |
| **Dimensions Card** | Input fields for matrix dimensions (rows and columns) |
| **Matrix A Card** | Input grid for Matrix A |
| **Matrix B Card** | Input grid for Matrix B |
| **Operations Card** | Radio buttons to select matrix operation (Add, Subtract, Multiply, Divide) |
| **Result Card** | Displays the computed result matrix |

---

## Usage Guide

1. **Set Matrix Dimensions**:
   - Input the desired number of rows and columns for Matrix A and Matrix B.
   - Tap **"Create Matrices"** to generate the input fields.

2. **Enter Matrix Data**:
   - Fill in the input fields with numerical values.
   - All fields must be populated before proceeding.

3. **Choose an Operation**:
   - Select the desired operation (Addition, Subtraction, Multiplication, Division).

4. **Compute the Result**:
   - Tap the **"Calculate"** button.
   - The result will be displayed dynamically within the Result Card.

---

## Implementation Details

### JNI Integration

The native library is loaded at runtime, and Kotlin calls are mapped to C++ functions via JNI:

```kotlin
// Native method declarations
external fun addMatricesNative(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>, 
                               rowsA: Int, colsA: Int, rowsB: Int, colsB: Int): Array<DoubleArray>

external fun subtractMatricesNative(matrixA: Array<DoubleArray>, matrixB: Array<DoubleArray>, 
                                    rowsA: Int, colsA: Int, rowsB: Int, colsB: Int): Array<DoubleArray>

// Initialize the native library
companion object {
    init {
        System.loadLibrary("matrix-lib")
    }
}
```

### C++ Matrix Operations

Efficient C++ implementations handle the core mathematical logic:

```cpp
// Matrix addition
Matrix Matrix::add(const Matrix& other) const {
    // Implementation of matrix addition
}

// Matrix multiplication
Matrix Matrix::multiply(const Matrix& other) const {
    // Implementation of matrix multiplication
}

// Matrix inversion
Matrix Matrix::inverse() const {
    // Implementation of matrix inversion
}
```

- All operations are optimized for speed and low memory footprint.

### UI Adaptation

- Fully responsive layouts optimized for phones and tablets.
- **Light Theme**: Bright background with dark text.
- **Dark Theme**: Dark background with light text.
- Smooth UI transitions between themes without app restart.

---

## System Requirements

- **Android Version**: 6.0 (API level 24) or higher
- **Storage**: At least 50 MB of free space
- **Development Environment**: Android Studio (recommended version 2023.1 or newer)

---

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/matrix-calculator-android.git
   ```
2. Open the project in Android Studio.
3. Let Gradle sync and download necessary dependencies.
4. Build and run the app on a physical device or emulator.

---

## Build Configuration

Native C++ code is built using CMake integrated with Gradle:

```gradle
externalNativeBuild {
    cmake {
        path "src/main/cpp/CMakeLists.txt"
        version "3.22.1"
    }
}
```

---
>>>>>>> 731a9fa (Initial commit)
>>>>>>> b481c5b (Initial commit)
