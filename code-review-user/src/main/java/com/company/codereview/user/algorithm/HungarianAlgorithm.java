package com.company.codereview.user.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Hungarian Algorithm implementation for assignment optimization
 */
@Slf4j
public class HungarianAlgorithm {
    
    private double[][] costMatrix;
    private int n;
    private int[] rowIndices;
    private int[] colIndices;
    private boolean[] rowCovered;
    private boolean[] colCovered;
    
    public HungarianAlgorithm(double[][] costMatrix) {
        this.costMatrix = costMatrix;
        this.n = costMatrix.length;
        this.rowIndices = new int[n];
        this.colIndices = new int[n];
        this.rowCovered = new boolean[n];
        this.colCovered = new boolean[n];
    }
    
    /**
     * Solve the assignment problem using Hungarian algorithm
     * @param costMatrix the cost matrix
     * @return array of assignments [row] = column
     */
    public static int[] solve(double[][] costMatrix) {
        HungarianAlgorithm algorithm = new HungarianAlgorithm(costMatrix);
        return algorithm.solveInternal();
    }
    
    /**
     * Solve the assignment problem using Hungarian algorithm
     * @return array of assignments [row] = column
     */
    public int[] solveInternal() {
        // Step 1: Reduce the cost matrix
        reduceMatrix();
        
        // Step 2: Find minimum number of lines to cover all zeros
        while (!isOptimal()) {
            findMinimumLines();
            updateMatrix();
        }
        
        // Step 3: Extract the optimal assignment
        return extractAssignment();
    }
    
    private void reduceMatrix() {
        // Row reduction
        for (int i = 0; i < n; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                min = Math.min(min, costMatrix[i][j]);
            }
            for (int j = 0; j < n; j++) {
                costMatrix[i][j] -= min;
            }
        }
        
        // Column reduction
        for (int j = 0; j < n; j++) {
            double min = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                min = Math.min(min, costMatrix[i][j]);
            }
            for (int i = 0; i < n; i++) {
                costMatrix[i][j] -= min;
            }
        }
    }
    
    private boolean isOptimal() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (costMatrix[i][j] == 0 && !rowCovered[i] && !colCovered[j]) {
                    rowIndices[i] = j;
                    colIndices[j] = i;
                    rowCovered[i] = true;
                    colCovered[j] = true;
                    count++;
                }
            }
        }
        return count == n;
    }
    
    private void findMinimumLines() {
        Arrays.fill(rowCovered, false);
        Arrays.fill(colCovered, false);
        
        // Mark all rows having no assignment
        for (int i = 0; i < n; i++) {
            if (rowIndices[i] == -1) {
                rowCovered[i] = true;
            }
        }
        
        boolean changed;
        do {
            changed = false;
            
            // Mark all columns having zeros in marked rows
            for (int i = 0; i < n; i++) {
                if (rowCovered[i]) {
                    for (int j = 0; j < n; j++) {
                        if (costMatrix[i][j] == 0 && !colCovered[j]) {
                            colCovered[j] = true;
                            changed = true;
                        }
                    }
                }
            }
            
            // Mark all rows having assignments in marked columns
            for (int j = 0; j < n; j++) {
                if (colCovered[j]) {
                    int i = colIndices[j];
                    if (i != -1 && !rowCovered[i]) {
                        rowCovered[i] = true;
                        changed = true;
                    }
                }
            }
        } while (changed);
    }
    
    private void updateMatrix() {
        // Find minimum uncovered value
        double min = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!rowCovered[i] && !colCovered[j]) {
                    min = Math.min(min, costMatrix[i][j]);
                }
            }
        }
        
        // Subtract from uncovered elements and add to doubly covered elements
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!rowCovered[i] && !colCovered[j]) {
                    costMatrix[i][j] -= min;
                } else if (rowCovered[i] && colCovered[j]) {
                    costMatrix[i][j] += min;
                }
            }
        }
    }
    
    private int[] extractAssignment() {
        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (costMatrix[i][j] == 0 && assignment[i] == -1) {
                    assignment[i] = j;
                    break;
                }
            }
        }
        
        return assignment;
    }
    
    /**
     * Assignment result class
     */
    @Data
    public static class Assignment {
        private int reviewerIndex;
        private int revieweeIndex;
        private double cost;
        
        public Assignment(int reviewerIndex, int revieweeIndex, double cost) {
            this.reviewerIndex = reviewerIndex;
            this.revieweeIndex = revieweeIndex;
            this.cost = cost;
        }
    }
}
