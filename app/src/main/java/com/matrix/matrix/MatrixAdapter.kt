package com.matrix.matrix

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MatrixAdapter(
    private val context: Context,
    private val rows: Int,
    private val cols: Int,
    private val isEditable: Boolean = true
) : RecyclerView.Adapter<MatrixAdapter.MatrixRowViewHolder>() {

    // Initialize with empty strings instead of zeros for better UX
    private val matrixData: Array<DoubleArray> = Array(rows) { DoubleArray(cols) }
    private val textValues: Array<Array<String>> = Array(rows) { Array(cols) { "" } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixRowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matrix_row, parent, false)
        return MatrixRowViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatrixRowViewHolder, position: Int) {
        holder.rowLayout.removeAllViews()
        
        for (col in 0 until cols) {
            if (isEditable) {
                val cellView = LayoutInflater.from(context)
                    .inflate(R.layout.item_matrix_cell, holder.rowLayout, false) as EditText
                
                // Set the existing value or empty string
                cellView.setText(textValues[position][col])
                
                cellView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    
                    override fun afterTextChanged(s: Editable?) {
                        val text = s?.toString() ?: ""
                        textValues[position][col] = text
                        
                        try {
                            if (text.isNotEmpty()) {
                                matrixData[position][col] = text.toDouble()
                            } else {
                                // Default to 0 if empty for calculations
                                matrixData[position][col] = 0.0
                            }
                        } catch (e: Exception) {
                            // Keep previous value if parsing fails
                        }
                    }
                })
                
                // Give focus to first cell when creating the matrix
                if (position == 0 && col == 0) {
                    cellView.requestFocus()
                }
                
                holder.rowLayout.addView(cellView)
            } else {
                val cellView = LayoutInflater.from(context)
                    .inflate(R.layout.item_result_cell, holder.rowLayout, false) as TextView
                
                cellView.text = String.format("%.2f", matrixData[position][col])
                holder.rowLayout.addView(cellView)
            }
        }
    }

    override fun getItemCount(): Int = rows

    fun getMatrixData(): Array<DoubleArray> = matrixData

    fun setMatrixData(data: Array<DoubleArray>) {
        for (i in data.indices) {
            for (j in data[i].indices) {
                if (i < matrixData.size && j < matrixData[i].size) {
                    matrixData[i][j] = data[i][j]
                    textValues[i][j] = data[i][j].toString()
                }
            }
        }
        notifyDataSetChanged()
    }

    class MatrixRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rowLayout: LinearLayout = itemView.findViewById(R.id.linearLayoutRow)
    }
}
