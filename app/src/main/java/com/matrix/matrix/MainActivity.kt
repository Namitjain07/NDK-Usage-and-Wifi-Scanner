package com.matrix.matrix

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    
    private lateinit var editTextRowsA: EditText
    private lateinit var editTextColsA: EditText
    private lateinit var editTextRowsB: EditText
    private lateinit var editTextColsB: EditText
    private lateinit var buttonCreateMatrices: Button
    private lateinit var recyclerViewMatrixA: RecyclerView
    private lateinit var recyclerViewMatrixB: RecyclerView
    private lateinit var recyclerViewResult: RecyclerView
    private lateinit var radioGroupOperation: RadioGroup
    private lateinit var buttonCalculate: Button
    
    // Card references for animation
    private lateinit var cardMatrixA: MaterialCardView
    private lateinit var cardMatrixB: MaterialCardView
    private lateinit var cardOperations: MaterialCardView
    private lateinit var cardResult: MaterialCardView
    
    private var adapterA: MatrixAdapter? = null
    private var adapterB: MatrixAdapter? = null
    private var resultAdapter: MatrixAdapter? = null
    
    private val matrixCalculator = MatrixCalculator()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Follow system dark mode setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        setupInitialState()
    }
    
    private fun initViews() {
        // Dimension inputs
        editTextRowsA = findViewById(R.id.editTextRowsA)
        editTextColsA = findViewById(R.id.editTextColsA)
        editTextRowsB = findViewById(R.id.editTextRowsB)
        editTextColsB = findViewById(R.id.editTextColsB)
        
        // Buttons
        buttonCreateMatrices = findViewById(R.id.buttonCreateMatrices)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        
        // RecyclerViews
        recyclerViewMatrixA = findViewById(R.id.recyclerViewMatrixA)
        recyclerViewMatrixB = findViewById(R.id.recyclerViewMatrixB)
        recyclerViewResult = findViewById(R.id.recyclerViewResult)
        
        // Operation selection
        radioGroupOperation = findViewById(R.id.radioGroupOperation)
        
        // Cards
        cardMatrixA = findViewById(R.id.cardMatrixA)
        cardMatrixB = findViewById(R.id.cardMatrixB)
        cardOperations = findViewById(R.id.cardOperations)
        cardResult = findViewById(R.id.cardResult)
        
        // Configure RecyclerViews
        recyclerViewMatrixA.layoutManager = LinearLayoutManager(this)
        recyclerViewMatrixB.layoutManager = LinearLayoutManager(this)
        recyclerViewResult.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupInitialState() {
        // Set initial dimensions for easier start
        editTextRowsA.setText("2")
        editTextColsA.setText("2")
        editTextRowsB.setText("2")
        editTextColsB.setText("2")
        
        // Hide matrix cards initially
        cardMatrixA.visibility = View.GONE
        cardMatrixB.visibility = View.GONE
        cardOperations.visibility = View.GONE
        cardResult.visibility = View.GONE
        
        // Ensure the "Add" operation is selected by default
        radioGroupOperation.check(R.id.radioButtonAdd)
    }
    
    private fun setupListeners() {
        buttonCreateMatrices.setOnClickListener {
            createMatrices()
        }
        
        buttonCalculate.setOnClickListener {
            calculateResult()
        }
        
        // Add listener to enforce compatible dimensions for matrix operations
        radioGroupOperation.setOnCheckedChangeListener { _, checkedId ->
            validateDimensions(checkedId)
        }
    }
    
    private fun createMatrices() {
        try {
            val rowsA = editTextRowsA.text.toString().toIntOrNull() ?: 0
            val colsA = editTextColsA.text.toString().toIntOrNull() ?: 0
            val rowsB = editTextRowsB.text.toString().toIntOrNull() ?: 0
            val colsB = editTextColsB.text.toString().toIntOrNull() ?: 0
            
            if (rowsA <= 0 || colsA <= 0 || rowsB <= 0 || colsB <= 0) {
                Toast.makeText(this, "All dimensions must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (rowsA > 10 || colsA > 10 || rowsB > 10 || colsB > 10) {
                Toast.makeText(this, "Maximum dimension is 10", Toast.LENGTH_SHORT).show()
                return
            }
            
            adapterA = MatrixAdapter(this, rowsA, colsA)
            adapterB = MatrixAdapter(this, rowsB, colsB)
            
            recyclerViewMatrixA.adapter = adapterA
            recyclerViewMatrixB.adapter = adapterB
            
            // Clear result
            resultAdapter = null
            recyclerViewResult.adapter = null
            cardResult.visibility = View.GONE
            
            // Show matrix cards with animation
            animateCardsIn()
            
            // Validate dimensions for the selected operation
            validateDimensions(radioGroupOperation.checkedRadioButtonId)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating matrices: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun animateCardsIn() {
        // Animate matrix cards in sequence
        cardMatrixA.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).withEndAction {
                cardMatrixB.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(300).withEndAction {
                        cardOperations.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate().alpha(1f).setDuration(300)
                        }
                    }
                }
            }
        }
    }
    
    private fun validateDimensions(operationId: Int) {
        if (adapterA == null || adapterB == null) return
        
        val rowsA = editTextRowsA.text.toString().toIntOrNull() ?: 0
        val colsA = editTextColsA.text.toString().toIntOrNull() ?: 0
        val rowsB = editTextRowsB.text.toString().toIntOrNull() ?: 0
        val colsB = editTextColsB.text.toString().toIntOrNull() ?: 0
        
        var warning = ""
        
        when (operationId) {
            R.id.radioButtonAdd, R.id.radioButtonSubtract -> {
                if (rowsA != rowsB || colsA != colsB) {
                    warning = "For addition/subtraction, matrices must have the same dimensions"
                }
            }
            R.id.radioButtonMultiply -> {
                if (colsA != rowsB) {
                    warning = "For multiplication, columns of A must equal rows of B"
                }
            }
            R.id.radioButtonDivide -> {
                if (rowsB != colsB) {
                    warning = "For division, matrix B must be square"
                } else if (colsA != rowsB) {
                    warning = "For division, columns of A must equal rows of B"
                }
            }
        }
        
        if (warning.isNotEmpty()) {
            Toast.makeText(this, warning, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun calculateResult() {
        if (adapterA == null || adapterB == null) {
            Toast.makeText(this, "Please create matrices first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val matrixA = adapterA!!.getMatrixData()
        val matrixB = adapterB!!.getMatrixData()
        
        // Verify matrices have values entered
        if (areMatricesEmpty(matrixA) || areMatricesEmpty(matrixB)) {
            Toast.makeText(this, "Please enter values in both matrices", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Get the selected operation ID
            val selectedId = radioGroupOperation.checkedRadioButtonId
            
            // Default to addition if nothing is selected (though this shouldn't happen)
            val resultMatrix = when (selectedId) {
                R.id.radioButtonAdd -> matrixCalculator.addMatrices(matrixA, matrixB)
                R.id.radioButtonSubtract -> matrixCalculator.subtractMatrices(matrixA, matrixB)
                R.id.radioButtonMultiply -> matrixCalculator.multiplyMatrices(matrixA, matrixB)
                R.id.radioButtonDivide -> matrixCalculator.divideMatrices(matrixA, matrixB)
                else -> {
                    Toast.makeText(this, "No operation selected, defaulting to addition", Toast.LENGTH_SHORT).show()
                    matrixCalculator.addMatrices(matrixA, matrixB)
                }
            }
            
            resultAdapter = MatrixAdapter(this, resultMatrix.size, resultMatrix[0].size, false)
            resultAdapter!!.setMatrixData(resultMatrix)
            recyclerViewResult.adapter = resultAdapter
            
            // Show result card with animation
            cardResult.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300)
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Calculation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    // Helper to check if matrices have been populated
    private fun areMatricesEmpty(matrix: Array<DoubleArray>): Boolean {
        var allZeros = true
        for (row in matrix) {
            for (value in row) {
                if (value != 0.0) {
                    allZeros = false
                    break
                }
            }
            if (!allZeros) break
        }
        return allZeros
    }
}