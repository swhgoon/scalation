
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.0
 *  @date    Wed Aug 26 18:41:26 EDT 2009
 *  @see     LICENSE (MIT style license file).
 */

package scalation.linalgebra_gen

import math.abs

import scalation.linalgebra.VectorD
import scalation.util.Error

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** Convenience definitions for commonly used types of matrices.
 */
object Matrices
{
    type MatrixI = MatrixN [Int]
    type MatrixL = MatrixN [Long]
    type MatrixF = MatrixN [Float]
//  type MatrixD = MatrixN [Double]    // see linalgebra package for efficient impl
//  type MatrixC = MatrixN [Complex]   // see linalgebra package for efficient impl

} // Matrices object


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The MatrixN class stores and operates on Numeric Matrices of various sizes
 *  and types.  The element type may be any subtype of Numeric.
 *  @param d1  the first/row dimension
 *  @param d2  the second/column dimension
 *  @param v   the 2D array used to store matrix elements
 */
class MatrixN [T: ClassManifest: Numeric] (val d1: Int,
                                           val d2: Int,
                                   private var v:  Array [Array [T]] = null)
      extends Matrix [T] with Error with Serializable
{
    import Vectors.VectorI

    lazy val dim1 = d1
    lazy val dim2 = d2

    if (v == null) {
        v = Array.ofDim [T] (dim1, dim2)
    } else if (dim1 != v.length || dim2 != v(0).length) {
        flaw ("constructor", "dimensions are wrong")
    } // if

    /** Import Numeric evidence (gets nu val from superclass)
     */
    val nu = implicitly [Numeric [T]]
    import nu._

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a dim1 by dim1 square matrix.
     *  @param dim1  the row and column dimension
     */
    def this (dim1: Int) { this (dim1, dim1) }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a dim1 by dim2 matrix and assign each element the value x.
     *  @param dim1  the row dimension
     *  @param dim2  the column dimesion
     *  @param x     the scalar value to assign
     */
    def this (dim1: Int, dim2: Int, x: T)
    {
        this (dim1, dim2)                          // invoke primary constructor
        for (i <- range1; j <- range2) v(i)(j) = x
    } // constructor

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a dim1 by dim1 square matrix with x assigned on the diagonal
     *  and y assigned off the diagonal.  To obtain an identity matrix, let x = 1
     *  and y = 0.
     *  @param dim1  the row and column dimension
     *  @param x     the scalar value to assign on the diagonal
     *  @param y     the scalar value to assign off the diagonal
     */
    def this (dim1: Int, x: T, y: T)
    {
        this (dim1, dim1)                          // invoke primary constructor
        for (i <- range1; j <- range1) v(i)(j) = if (i == j) x else y
    } // constructor

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a matrix and assign values from array of arrays u.
     *  @param u  the 2D array of values to assign
     */
    def this (u: Array [Array [T]]) { this (u.length, u(0).length, u) }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a matrix from repeated values.
     *  @param dim  the (row, column) dimensions
     *  @param u    the repeated values
     */
    def this (dim: Tuple2 [Int, Int], u: T*)
    {
        this (dim._1, dim._2)                      // invoke primary constructor
        for (i <- range1; j <- range2) v(i)(j) = u(i * dim2 + j)
    } // constructor

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a matrix and assign values from array of vectors u.
     *  @param u  the 2D array of values to assign
     */
    def this (u: Array [VectorN [T]])
    {
        this (u.length, u(0).dim)                  // invoke primary constructor
        for (i <- range1; j <- range2) v(i)(j) = u(i)(j)
    } // constructor

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a matrix and assign values from matrix u.
     *  @param u  the matrix of values to assign
     */
    def this (u: MatrixN [T])
    {
        this (u.d1, u.d2)                        // invoke primary constructor
        for (i <- range1; j <- range2) v(i)(j) = u.v(i)(j)
    } // constructor

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get this matrix's element at the i,j-th index position. 
     *  @param i  the row index
     *  @param j  the column index
     */
    def apply (i: Int, j: Int): T = v(i)(j)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get this matrix's vector at the i-th index position (i-th row).
     *  @param i  the row index
     */
    def apply (i: Int): VectorN [T] = new VectorN [T] (v(i))

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set this matrix's element at the i,j-th index position to the scalar x.
     *  @param i  the row index
     *  @param j  the column index
     *  @param x  the scalar value to assign
     */
    def update (i: Int, j: Int, x: T) { v(i)(j) = x }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set this matrix's row at the i-th index position to the vector u.
     *  @param i  the row index
     *  @param u  the vector value to assign
     */
    def update (i: Int, u: VectorN [T]) { v(i) = u() }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set this matrix's row at the i-th index position to the vector u.
     *  @param i  the row index
     *  @param j  the starting column index
     *  @param u  the vector value to assign
     */
    def set (i: Int, j: Int, u: VectorN [T])
    {
        for (k <- 0 until u.dim) v(i)(j + k) = u(k)
    } // set

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice this matrix row-wise from to end.
     *  @param from  the start row of the slice (inclusive)
     *  @param end   the end row of the slice (exclusive)
     */
    def slice (from: Int, end: Int): MatrixN [T] =
    {
        new MatrixN [T] (end - from, dim2, v.slice (from, end))
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice this matrix row-wise r_from to r_end and column-wise c_from to c_end.
     *  @param r_from  the start of the row slice
     *  @param r_end   the end of the row slice
     *  @param c_from  the start of the column slice
     *  @param c_end   the end of the column slice
     */
    def slice (r_from: Int, r_end: Int, c_from: Int, c_end: Int): MatrixN [T] = 
    {
        val c = new MatrixN [T] (r_end - r_from, c_end - c_from)
        for (i <- c.range1; j <- c.range2) c.v(i)(j) = v(i + r_from)(j + c_from)
        c
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice this matrix excluding the given row and column.
     *  @param row  the row to exclude
     *  @param col  the column to exclude
     */
    def sliceExclude (row: Int, col: Int): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1 - 1, dim2 - 1)
        for (i <- range1 if i != row) for (j <- range2 if j != col) {
            c.v(i - oneIf (i > row))(j - oneIf (j > col)) = v(i)(j)
        } // for
        c
    } // sliceExclude

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get row 'r' from the matrix, returning it as a vector.
     *  @param r     the row to extract from the matrix
     *  @param from  the position to start extracting from
     */
    def row (r: Int, from: Int = 0): VectorN [T] =
    {
        val u = new VectorN [T] (dim2 - from)
        for (j <- from until dim2) u(j-from) = v(r)(j)
        u
    } // row

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select rows from this matrix according a basis. 
     *  @param basis  the row index positions (e.g., (0, 2, 5))
     */
    def selectRows (basis: VectorI): MatrixN [T] =
    {
        val c = new MatrixN [T] (basis.dim)
        for (i <- c.range1) c.setRow (i, col(basis(i)))
        c
    } // selectCols

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get column 'c' from the matrix, returning it as a vector.
     *  @param c     the column to extract from the matrix
     *  @param from  the position to start extracting from
     */
    def col (c: Int, from: Int = 0): VectorN [T] =
    {
        val u = new VectorN [T] (dim1 - from)
        for (i <- from until dim1) u(i-from) = v(i)(c)
        u
    } // col

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select columns from this matrix according a basis. 
     *  @param basis  the column index positions (e.g., (0, 2, 5))
     */
    def selectCols (basis: VectorI): MatrixN [T] =
    {
        val c = new MatrixN [T] (basis.dim)
        for (j <- c.range1) c.setCol (j, col(basis(j)))
        c
    } // selectCols

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate this matrix and vector b.
     *  @param b  the vector to be concatenated as the new last row in matrix
     */
    def ++ (b: VectorN [T]): MatrixN [T] =
    {
        if (b.dim != dim2) flaw ("++", "vector does not match row dimension")
        val c = new MatrixN [T] (dim1 + 1, dim2)
        for (i <- c.range1) c(i) = if (i < dim1) this(i) else b
        c
    } // ++

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Transpose this matrix (rows => columns).
     */
    def t: MatrixN [T] =
    {
        val b = new MatrixN [T] (dim2, dim1)
        for (i <- b.range1; j <- b.range2) b.v(i)(j) = v(j)(i)
        b
    } // t

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add this matrix and matrix b.
     *  @param b  the matrix to add (requires leDimensions)
     */
    def + (b: MatrixN [T]): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) + b.v(i)(j)
        c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place this matrix and matrix b.
     *  @param b  the matrix to add (requires leDimensions)
     */
    def += (b: MatrixN [T])
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) + b.v(i)(j)
    } // +=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add this matrix and scalar s.
     *  @param s  the scalar to add
     */
    def + (s: T): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) + s
        c
    } // +
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place this matrix and scalar s.
     *  @param s  the scalar to add
     */
    def += (s: T)
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) + s
    } // +=
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From this matrix subtract matrix b.
     *  @param b  the matrix to subtract (requires leDimensions)
     */
    def - (b: MatrixN [T]): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) - b.v(i)(j)
        c
    } // -
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From this matrix subtract in-place matrix b.
     *  @param b  the matrix to subtract (requires leDimensions)
     */
    def -= (b: MatrixN [T])
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) - b.v(i)(j)
    } // -=
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From this matrix subtract scalar s.
     *  @param s  the scalar to subtract
     */
    def - (s: T): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- c.range1; j <- c.range2) c.v(i)(j) = v(i)(j) - s
        c
    } // -
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From this matrix subtract in-place scalar s.
     *  @param s  the scalar to subtract
     */
    def -= (s: T)
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) - s
    } // -=
 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by matrix b (concise solution).
     *  @param b  the matrix to multiply by (requires sameCrossDimensions)
     *
    def * (b: Matrix [T]): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, b.dim2)
        for (i <- range1; j <- c.range2) c.v(i)(j) = row(i) dot b.col(j)
        c
    } // *
     */

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by matrix b (efficient solution).
     *  @param b  the matrix to multiply by (requires sameCrossDimensions)
     */
    def * (b: MatrixN [T]): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, b.dim2)
        for (i <- range1; j <- c.range2) {
            var sum = zero
            for (k <- range2) sum += v(i)(k) * b.v(k)(j)
            c.v(i)(j) = sum
        } // for
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place this matrix by matrix b.  If b and this reference the
     *  same matrix (b == this), a copy of the this matrix is made.
     *  @param b  the matrix to multiply by (requires square and sameCrossDimensions)
     */
    def *= (b: MatrixN [T])
    {
        val c = if (b == this) new MatrixN [T] (this) else b
        for (i <- range1) {
            val row_i = new VectorN [T] (row(i))          // save so not overwritten
            for (j <- range1) {
                var sum = zero
                for (k <- range1) sum += row_i(k) * b.v(k)(j)
                v(i)(j) = sum
            } // for
        } // for
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by vector b (concise solution).
     *  @param b  the vector to multiply by
     *
    def * (b: VectorN [T]): VectorN [T] =
    {
        val c = new VectorN [T] (dim1)
        for (i <- range1) c(i) = row(i) dot b
        c
    } // *
     */

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by vector b (efficient solution).
     *  @param b  the vector to multiply by
     */
    def * (b: VectorN [T]): VectorN [T] =
    {
        val c = new VectorN [T] (dim1)
        for (i <- range1) {
            var sum = zero
            for (k <- range2) sum += v(i)(k) * b(k)
            c(i) = sum
        } // for
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by vector b (efficient solution).
     *  @param b  the vector to multiply by
     */
    def * (b: VectorD): VectorD =
    {
        val c = new VectorD (dim1)
        for (i <- range1) {
            var sum = 0.
            for (k <- range2) sum += v(i)(k).asInstanceOf [Double] * b(k)
            c(i) = sum
        } // for
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by scalar s.
     *  @param s  the scalar to multiply by
     */
    def * (s: T): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) * s
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place this matrix by scalar s.
     *  @param s  the scalar to multiply by
     */
    def *= (s: T)
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) * s
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply this matrix by vector b to produce another matrix (a_ij * b_j)
     *  @param b  the vector to multiply by
     */
    def ** (b: VectorN [T]): MatrixN [T] =
    {
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) * b(j)
        c
    } // **

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place this matrix by vector b to produce another matrix (a_ij * b_j)
     *  @param b  the vector to multiply by
     */
    def **= (b: VectorN [T])
    {
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) * b(j)
    } // **=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide this matrix by scalar s.
     *  @param s  the scalar to divide by
     */
    def / (s: T) (implicit fr: Fractional [T]): MatrixN [T] =
    {
        import fr._
        val c = new MatrixN [T] (dim1, dim2)
        for (i <- range1; j <- range2) c.v(i)(j) = v(i)(j) / s
        c
    } // /

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide in-place this matrix by scalar s.
     *  @param s  the scalar to divide by
     */
    def /= (s: T) (implicit fr: Fractional [T])
    {
        import fr._
        for (i <- range1; j <- range2) v(i)(j) = v(i)(j) / s
    } // /=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Raise this matrix to the pth power (for some integer p >= 2).
     *  Caveat: should be replace by a divide and conquer algorithm.
     *  @param p  the power to raise this matrix to
     */
    def ~^ (p: Int): MatrixN [T] =
    {
        if (p < 2)      flaw ("~^", "p must be an integer >= 2")
        if (! isSquare) flaw ("~^", "only defined on square matrices")
        val c = new MatrixN [T] (dim1, dim1)
        for (i <- range1; j <- range1) {
            var sum = zero
            for (k <- range1) sum += v(i)(k) * v(k)(j)
            c.v(i)(j) = sum
        } // for
        if (p > 2) c ~^ (p-1) else c
    } // ~^

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the maximum element in this matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def max (e: Int = dim1): T =
    {
        var x = v(0).max
        for (i <- 1 until e if v(i).max > x) x = v(i).max
        x
    } // max

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the minimum element in this matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def min (e: Int = dim1): T =
    {
        var x = v(0).min
        for (i <- 1 until e if v(i).min < x) x = v(i).min
        x
    } // min

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Decompose this matrix into the product of upper and lower triangular
     *  matrices (l, u) using the LU Decomposition algorithm.  This version uses
     *  no partial pivoting.
     */
    def lud_npp (implicit fr: Fractional [T]): Tuple2 [MatrixN [T], MatrixN [T]] =
    {
        import fr._
        val l = new MatrixN [T] (dim1, dim2)   // lower triangular matrix
        val u = new MatrixN [T] (this)         // upper triangular matrix (a copy of this)

        for (i <- u.range1) {
            val pivot = u(i, i)
            if (pivot == 0.) flaw ("lud_npp", "use lud since you have a zero pivot")
            l(i, i) = one
            for (j <- i + 1 until u.dim2) l(i, j) = zero
            for (k <- i + 1 until u.dim1) {
                val mul = u(k, i) / pivot
                l(k, i) = mul
                for (j <- u.range2) u(k, j) = u(k, j) - mul * u(i, j)
            } // for
        } // for
        Tuple2 (l, u)
    } // lud_npp

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Decompose this matrix into the product of lower and upper triangular
     *  matrices (l, u) using the LU Decomposition algorithm.  This version uses
     *  partial pivoting.
     */
    def lud (implicit fr: Fractional [T]): Tuple2 [MatrixN [T], MatrixN [T]] =
    {
        import fr._
        val l = new MatrixN [T] (dim1, dim2)  // lower triangular matrix
        val u = new MatrixN [T] (this)        // upper triangular matrix (a copy of this)

        for (i <- u.range1) {
            var pivot = u(i, i)
            if (pivot == 0.) {
                val k = partialPivoting (u, i)   // find the maxiumum element below pivot
                swap (u, i, k, i)                // swap rows i and k from column k
                pivot = u(i, i)                  // reset the pivot
            } // if
            l(i, i) = one
            for (j <- i + 1 until u.dim2) l(i, j) = zero
            for (k <- i + 1 until u.dim1) {
                val mul = u(k, i) / pivot
                l(k, i) = mul
                for (j <- u.range2) u(k, j) = u(k, j) - mul * u(i, j)
            } // for
        } // for
        Tuple2 (l, u)
    } // lud

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Decompose in-place this matrix into the product of lower and upper triangular
     *  matrices (l, u) using the LU Decomposition algorithm.  This version uses
     *  partial pivoting.
     */
    def lud_ip (implicit fr: Fractional [T]): Tuple2 [MatrixN [T], MatrixN [T]] =
    {
        import fr._
        val l = new MatrixN [T] (dim1, dim2)  // lower triangular matrix
        val u = this                          // upper triangular matrix (this)

        for (i <- u.range1) {
            var pivot = u(i, i)
            if (pivot == 0.) {
                val k = partialPivoting (u, i)   // find the maxiumum element below pivot
                swap (u, i, k, i)                // swap rows i and k from column k
                pivot = u(i, i)                  // reset the pivot
            } // if
            l(i, i) = one
            for (j <- i + 1 until u.dim2) l(i, j) = zero
            for (k <- i + 1 until u.dim1) {
                val mul = u(k, i) / pivot
                l(k, i) = mul
                for (j <- u.range2) u(k, j) = u(k, j) - mul * u(i, j)
            } // for
        } // for
        Tuple2 (l, u)
    } // lud_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use partial pivoting to find a maximal non-zero pivot and return its row
     *  index, i.e., find the maximum element (k, i) below the pivot (i, i).
     *  @param a  the matrix to perform partial pivoting on
     *  @param i  the row and column index for the current pivot
     */
    private def partialPivoting (a: MatrixN [T], i: Int) (implicit fr: Fractional [T]): Int =
    {
        import fr._
        var max  = a(i, i)   // initially set to the pivot
        var kMax = i         // initially the pivot row

        for (k <- i + 1 until a.dim1 if nu.abs (a(k, i)) > max) {
            max  = nu.abs (a(k, i))
            kMax = k
        } // for
        if (kMax == i) flaw ("partialPivoting", "unable to find a non-zero pivot")
        kMax
    } // partialPivoting

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Swap the elements in rows i and k starting from column col.
     *  @param a    the matrix containing the rows to swap
     *  @param i    the higher row  (e.g., contains a zero pivot)
     *  @param k    the lower row (e.g., contains max element below pivot)
     *  @param col  the starting column for the swap
     */
    private def swap (a: MatrixN [T], i: Int, k: Int, col: Int)
    {
        for (j <- col until a.dim2) {
            val tmp = a(k, j); a(k, j) = a(i, j); a(i, j) = tmp
        } // for
    } // swap

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for x in the equation l*u*x = b (see lud above).
     *  @param l  the lower triangular matrix
     *  @param u  the upper triangular matrix
     *  @param b  the constant vector
     */
    def solve (l: Matrix [T], u: Matrix [T], b: VectorN [T])
        (implicit fr: Fractional [T]): VectorN [T] =
    {
        import fr._
        val y  = new VectorN [T] (l.dim2)
        for (k <- 0 until y.dim) {                   // solve for y in l*y = b
            y(k) = b(k) - (l(k) dot y)
        } // for

        val x = new VectorN [T] (u.dim2)
        for (k <- x.dim - 1 to 0 by -1) {            // solve for x in u*x = y
            x(k) = (y(k) - (u(k) dot x)) / u(k, k)
        } // for
        x
    } // solve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for x in the equation l*u*x = b (see lud above).
     *  @param lu  the lower and upper triangular matrices
     *  @param b   the constant vector
     */
    def solve (lu: Tuple2 [Matrix [T], Matrix [T]], b: VectorN [T])
        (implicit fr: Fractional [T]): VectorN [T] =
    {
       solve (lu._1, lu._2, b)
    } // solve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for x in the equation a*x = b where a is this matrix (see lud above).
     *  @param b  the constant vector.
     */
    def solve (b: VectorN [T]) (implicit fr: Fractional [T]): VectorN [T] =
    {
        solve (lud, b)
    } // solve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Combine this matrix with matrix b, placing them along the diagonal and
     *  filling in the bottom left and top right regions with zeroes; [this, b].
     *  @param b  the matrix to combine with this matrix
     */
    def diag (b: MatrixN [T]): MatrixN [T] =
    {
        val m = dim1 + b.dim1
        val n = dim2 + b.dim2
        val c = new MatrixN [T] (m, n)

        for (i <- 0 until m; j <- 0 until n) {
            c.v(i)(j) = if (i <  dim1 && j <  dim2) v(i)(j)
                   else if (i >= dim1 && j >= dim2) b.v(i-dim1)(j-dim2)
                      else                          zero
        } // for
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Form a matrix [Ip, this, Iq] where Ir is a r by r identity matrix, by
     *  positioning the three matrices Ip, this and Iq along the diagonal.
     *  @param p  the size of identity matrix Ip
     *  @param q  the size of identity matrix Iq
     */
    def diag (p: Int, q: Int): MatrixN [T] =
    {
        if (! isSymmetric) flaw ("diag", "this matrix must be symmetric")
        val n = dim1 + p + q 
        val c = new MatrixN [T] (n, n)

        for (i <- 0 until n; j <- 0 until n) {
            c.v(i)(j) = if (i < p || i > p + dim1) if (i == j) one else zero
                        else                       v(i-p)(j-p)
        } // for
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the main diagonal of this matrix.  Assumes dim2 >= dim1.
     */
    def getDiag (): VectorN [T] =
    {
        val c = new VectorN [T] (dim1)
        for (i <- range1) c(i) = v(i)(i)
        c
    } // getDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert this matrix (requires a squareMatrix) and does not use partial pivoting.
     */
    def inverse_npp (implicit fr: Fractional [T]): MatrixN [T] =
    {
        import fr._
        val b = new MatrixN [T] (this)              // copy this matrix into b
        val c = new MatrixN [T] (dim1, one, zero)   // let c represent the augmentation

        for (i <- b.range1) {
            val pivot = b.v(i)(i)
            if (pivot == 0.) flaw ("inverse_npp", "use inverse since you have a zero pivot")
            for (j <- b.range2) {
                b.v(i)(j) /= pivot
                c.v(i)(j) /= pivot
            } // for
            for (k <- 0 until b.dim1 if k != i) {
                val mul = b.v(k)(i)
                for (j <- b.range2) {
                    b.v(k)(j) -= mul * b.v(i)(j)
                    c.v(k)(j) -= mul * c.v(i)(j)
                } // for
            } // for
        } // for
        c
    } // inverse_npp

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert this matrix (requires a squareMatrix) and use partial pivoting.
     */
    def inverse (implicit fr: Fractional [T]): MatrixN [T] =
    {
        import fr._
        val b = new MatrixN [T] (this)              // copy this matrix into b
        val c = new MatrixN [T] (dim1, one, zero)   // let c represent the augmentation

        for (i <- b.range1) {
            var pivot = b.v(i)(i)
            if (pivot == 0.) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                swap (b, i, k, i)               // in b, swap rows i and k from column i
                swap (c, i, k, 0)               // in c, swap rows i and k from column 0
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- b.range2) {
                b.v(i)(j) /= pivot
                c.v(i)(j) /= pivot
            } // for
            for (k <- 0 until dim1 if k != i) {
                val mul = b.v(k)(i)
                for (j <- b.range2) {
                    b.v(k)(j) -= mul * b.v(i)(j)
                    c.v(k)(j) -= mul * c.v(i)(j)
                } // for
            } // for
        } // for
        c
    } // inverse

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert in-place this matrix (requires a squareMatrix) and uses partial pivoting.
     */
    def inverse_ip (implicit fr: Fractional [T]): MatrixN [T] =
    {
        import fr._
        val b = this                                // use this matrix for b
        val c = new MatrixN [T] (dim1, one, zero)   // let c represent the augmentation

        for (i <- b.range1) {
            var pivot = b.v(i)(i)
            if (pivot == 0.) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                swap (b, i, k, i)               // in b, swap rows i and k from column i
                swap (c, i, k, 0)               // in c, swap rows i and k from column 0
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- b.range2) {
                b.v(i)(j) /= pivot
                c.v(i)(j) /= pivot
            } // for
            for (k <- 0 until dim1 if k != i) {
                val mul = b.v(k)(i)
                for (j <- b.range2) {
                    b.v(k)(j) -= mul * b.v(i)(j)
                    c.v(k)(j) -= mul * c.v(i)(j)
                } // for
            } // for
        } // for
        c
    } // inverse_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction on this matrix to make the left part embed an
     *  identity matrix.  A constraint on this m by n matrix is that n >= m.
     */
    def reduce (implicit fr: Fractional [T]): MatrixN [T] =
    {
        import fr._
        if (dim2 < dim1) flaw ("reduce", "requires n (columns) >= m (rows)")
        val b = new MatrixN [T] (this)    // copy this matrix into b

        for (i <- b.range1) {
            var pivot = b.v(i)(i)
            if (pivot == 0.) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                swap (b, i, k, i)               // in b, swap rows i and k from column i
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- b.range2) {
                b.v(i)(j) /= pivot
            } // for
            for (k <- 0 until dim1 if k != i) {
                val mul = b.v(k)(i)
                for (j <- b.range2) b.v(k)(j) -= mul * b.v(i)(j)
            } // for
        } // for
        b
    } // reduce

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction in-place on this matrix to make the left part
     *  embed an identity matrix.  A constraint on this m by n matrix is that n >= m.
     */
    def reduce_ip (implicit fr: Fractional [T])
    {
        import fr._
        if (dim2 < dim1) flaw ("reduce", "requires n (columns) >= m (rows)")
        val b = this         // use this matrix for b

        for (i <- b.range1) {
            var pivot = b.v(i)(i)
            if (pivot == 0.) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                swap (b, i, k, i)               // in b, swap rows i and k from column i
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- b.range2) {
                b.v(i)(j) /= pivot
            } // for
            for (k <- 0 until dim1 if k != i) {
                val mul = b.v(k)(i)
                for (j <- b.range2) b.v(k)(j) -= mul * b.v(i)(j)
            } // for
        } // for
    } // reduce_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the (right) nullspace of this m by n matrix (requires n = m + 1)
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.  The nullspace of matrix a is "this vector v
     *  times any scalar s", i.e., s*v*a = 0.  The left nullspace of matrix a is
     *  the same as the right nullspace of a.t (a transpose).
     */
    def nullspace (implicit fr: Fractional [T]): VectorN [T] =
    {
        if (dim2 != dim1 + 1) flaw ("nullspace", "requires n (columns) = m (rows) + 1")
        reduce.col(dim2 - 1) * negate (one) ++ one
    } // nullspace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the (right) nullspace in-place of this m by n matrix (requires n = m + 1)
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.  The nullspace of matrix a is "this vector v
     *  times any scalar s", i.e., s*v*a = 0.  The left nullspace of matrix a is
     *  the same as the right nullspace of a.t (a transpose).
     */
    def nullspace_ip (implicit fr: Fractional [T]): VectorN [T] =
    {
        if (dim2 != dim1 + 1) flaw ("nullspace", "requires n (columns) = m (rows) + 1")
        reduce_ip
        col(dim2 - 1) * negate (one) ++ one
    } // nullspace_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the trace of this matrix, i.e., the sum of the elements on the
     *  main diagonal.  Should also equal the sum of the eigenvalues.
     *  @see Eigen.scala
     */
    def trace: T =
    {
        if ( ! isSquare) flaw ("trace", "trace only works on square matrices")
        var sum = zero
        for (i <- range1) sum += v(i)(i)
        sum
    } // trace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of this matrix, i.e., the sum of its elements.
     */
    def sum: T =
    {
        var sum = zero
        for (i <- range1; j <- range2) sum += v(i)(j)
        sum
    } // sum

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of the lower triangular region of this matrix.
     */
    def sumLower: T =
    {
        var sum = zero
        for (i <- range1; j <- 0 until i) sum += v(i)(j)
        sum
    } // sumLower

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the determinant of this matrix.  The value of the determinant
     *  indicates, among other things, whether there is a unique solution to a
     *  system of linear equations (a nonzero determinant).
     */
    def det: T =
    {
        if ( ! isSquare) flaw ("det", "determinant only works on square matrices")
        var sum = zero
        var b: MatrixN [T] = null
        for (j <- range2) {
            b = sliceExclude (0, j)   // the submatrix that excludes row 0 and column j
            if (j % 2 == 0) {
                sum = sum + v(0)(j) * (if (b.dim1 == 1) b.v(0)(0) else b.det)
            } else {
                sum = sum - v(0)(j) * (if (b.dim1 == 1) b.v(0)(0) else b.det)
            } // if
        } // for 
        sum
    } // det

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Check whether this matrix is rectangular (all rows have the same number
     *  of columns).
     */
    def isRectangular: Boolean =
    {
        for (i <- range1 if v(i).length != dim2) return false
        true
    } // isRectangular

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Check whether this matrix is nonnegative (has no negative elements).
     */
    def isNonnegative: Boolean =
    {
        for (i <- range1; j <- range2 if v(i)(j) < zero) return false
        true
    } // isNonegative

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert this matrix to a string.
     */
    override def toString: String = 
    {
        var sb = new StringBuilder ("\nMatrixN(")
        for (i <- range1) {
            sb.append (this(i).toString)
            sb.append (if (i < dim1 - 1) ",\n\t" else ")")
        } // for
        sb.mkString
    } // toString
  
} // MatrixN class


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The MatrixN companion object provides operations for MatrixN that don't require
 *  'this' (like static methods in Java).
 */
object MatrixN
{
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the outer product (a matrix) of vector a and vector b.
     *  @param a  the first vector
     *  @param b  the second vector
     */
    def outer [T: ClassManifest: Numeric] (a: VectorN [T], b: VectorN [T]): Matrix [T] =
    {
        val nu = implicitly [Numeric [T]]             // import Numeric evidence
        import nu._
        val c = new MatrixN [T] (a.dim, b.dim)
        for (i <- 0 until a.dim; j <- 0 until b.dim) c(i, j) = a(i) * b(j)
        c
    } // outer

} // MatrixN companion object


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The MatrixNTest object tests the operations provided by MatrixN.
 */
object MatrixNTest extends App
{
    for (l <- 1 to 4) {
        println ("\n\tTest MatrixN on integer matrices of dim " + l)
        val a = new MatrixN [Int] (l, l)
        val b = new MatrixN [Int] (l, l)
        a.set (2)
        b.set (3)
        println ("a + b = " + (a + b))
        println ("a - b = " + (a - b))
        println ("a * b = " + (a * b))
        println ("a * 4 = " + (a * 4))

        println ("\n\tTest MatrixN on real matrices of dim " + l)
        val x = new MatrixN [Double] (l, l)
        val y = new MatrixN [Double] (l, l)
        x.set (2.)
        y.set (3.)
        println ("x + y  = " + (x + y))
        println ("x - y  = " + (x - y))
        println ("x * y  = " + (x * y))
        println ("x * 4. = " + (x * 4.))
    } // for

    println ("\n\tTest MatrixN on additional operations")

    val z = new MatrixN [Double] (2, 2)
    z.set (Array (Array (1., 2.), Array (3., 2.)))
    val b = new VectorN [Double] (8., 7.)
    val lu  = z.lud
    val lu2 = z.lud_npp

    println ("z         = " + z)
    println ("z.t       = " + z.t)
    println ("z.lud     = " + lu)
    println ("z.lud_npp = " + lu2)
    println ("z.solve   = " + z.solve (lu._1, lu._2, b))
    println ("z.inverse = " + z.inverse)
    println ("z.inv * b = " + z.inverse * b)
    println ("z.det     = " + z.det)
    println ("z         = " + z)
    z *= z                             // in-place matrix multiplication
    println ("z squared = " + z)

    val w = new MatrixN [Double] (2, 3)
    w.set (Array (Array (2., 3., 5.), Array (-4., 2., 3.)))
    val v = new MatrixN [Double] (3, 2)
    v.set (Array (Array (2., -4.), Array (3., 2.), Array (5., 3.)))

    println ("w         = " + w)
    println ("v         = " + v)
    println ("w.reduce  = " + w.reduce)

    println ("right:    w.nullspace = " + w.nullspace)
    println ("check right nullspace = " + w * w.nullspace)

    println ("left:   v.t.nullspace = " + v.t.nullspace)
    println ("check left  nullspace = " + v.t.nullspace * v)

    for (row <- z) println ("row = " + row.deep)

} // MatrixNTest object

