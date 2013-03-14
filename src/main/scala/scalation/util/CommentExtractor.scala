
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.0
 *  @date    Sun Sep  9 20:37:38 EDT 2012
 *  @see     LICENSE (MIT style license file).
 *  run: scala -cp classes scalation.util.CommentExtractor < Build.scala | hunspell -L
 */

package scalation.util

import util.matching.Regex

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** This object is used to extract comments from source code (for example to send
 *  it to a spell checker.  It reads from standard input and writes to standard
 *  output.
 *  @see http://ostermiller.org/findcomment.html
 */
object CommentExtractor extends App
{
    private val pat1      = """/**"""      // comment start pattern
    private val pat2      = """*/"""       // comment end pattern
    private val pat3      = """@param"""   // @param tag
    private var line      = ""
    private var inComment = false
    private var cont      = true

    do {
        line = readLine
        if (line == null) {
            cont = false
        } else {
            if (line.indexOf (pat1) != -1) inComment = true
            if (inComment) println (line.replace (pat3, ""))
            if (inComment && line.indexOf (pat2) != -1) inComment = false
        } // if
    } while (cont)

} // CommentExtractor object

