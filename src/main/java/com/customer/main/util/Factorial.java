package com.customer.main.util;

/**
 * Simple factorial computation (iterative and recursive).
 */
public final class Factorial {

    /**
     * Factorial using loop. Handles n = 0 (returns 1). Throws for n &lt; 0.
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Factorial using recursion.
     */
    public static long factorialRecursive(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        if (n <= 1) {
            return 1;
        }
        return n * factorialRecursive(n - 1);
    }

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        System.out.println("factorial(" + n + ") = " + factorial(n));
        System.out.println("factorialRecursive(" + n + ") = " + factorialRecursive(n));
    }
}
