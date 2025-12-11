/**
 * Olyan renderer, ami az element fát jeleníti meg DOM objektumokat generálva a böngészőben, dinamikusan módosítva a DOM
 * objektumkat ahogy az element fa módodul.
 * <p>
 * Ez a renderer csak böngészőben fut, JVM-ben nem. Javascriptre fordul.
 * <p>
 * A modult használó program input, timer, network stb. eseményekre reagálva az element fát módosítja.
 */
package ui11.platform.dom;