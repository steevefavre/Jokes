package com.example.myjokes.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Trace
import com.example.myjokes.tflite.Classifier.Recognition


import android.util.Log;
import com.example.myjokes.Globals

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


class TFLiteObjectDetectionAPIModel : Classifier {

    private var isModelQuantized = false

    // Config values.
    private var inputSize = 0

    // Pre-allocated buffers.
    private val labels: Vector<String> = Vector<String>()
    private lateinit var intValues: IntArray

    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private lateinit var outputLocations: Array<Array<FloatArray>>

    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private lateinit var outputClasses: Array<FloatArray>

    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private lateinit var outputScores: Array<FloatArray>

    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private lateinit var numDetections: FloatArray

    private var imgData: ByteBuffer? = null

    private var tfLite: Interpreter? = null

    private fun TFLiteObjectDetectionAPIModel() {}


    companion object{

        // Only return this many results.
        private val NUM_DETECTIONS = 10

        // Float model
        private val IMAGE_MEAN = 127.5f
        private val IMAGE_STD = 127.5f

        // Number of threads in the java app
        private val NUM_THREADS = 4

        /** Memory-map the model file in Assets.  */
        @Throws(IOException::class)
        private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
            val fileDescriptor = assets.openFd(modelFilename)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel: FileChannel = inputStream.getChannel()
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }


        /**
         * Initializes a native TensorFlow session for classifying images.
         *
         * @param assetManager The asset manager to be used to load assets.
         * @param modelFilename The filepath of the model GraphDef protocol buffer.
         * @param labelFilename The filepath of label file for classes.
         * @param inputSize The size of image input
         * @param isQuantized Boolean representing model is quantized or not
         */
        @Throws(IOException::class)
        fun create(
            assetManager: AssetManager,
            modelFilename: String,
            labelFilename: String?,
            inputSize: Int,
            isQuantized: Boolean
        ): Classifier? {
            val d : TFLiteObjectDetectionAPIModel = TFLiteObjectDetectionAPIModel()
            val modelFile: MappedByteBuffer = loadModelFile(assetManager, modelFilename)
            val metadata = MetadataExtractor(modelFile)
            val br = BufferedReader(InputStreamReader(metadata.getAssociatedFile(labelFilename)))
            var line = br.readLine()
            while (line != null) {

                Log.i(Globals.TAGLOG, line)
                d.labels.add(line)
                line = br.readLine()

            }
            br.close()
            d.inputSize = inputSize
            try {
                val options = Interpreter.Options()
                options.setNumThreads(NUM_THREADS)
                d.tfLite = Interpreter(loadModelFile(assetManager, modelFilename), options)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            d.isModelQuantized = isQuantized
            // Pre-allocate buffers.
            val numBytesPerChannel: Int
            numBytesPerChannel = if (isQuantized) {
                1 // Quantized
            } else {
                4 // Floating point
            }
            d.imgData =
                ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel)
            d.imgData?.order(ByteOrder.nativeOrder())
            d.intValues = IntArray(d.inputSize * d.inputSize)
            d.outputLocations = Array(1) {
                Array(NUM_DETECTIONS) {
                    FloatArray(
                        4
                    )
                }
            }
            d.outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
            d.outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
            d.numDetections = FloatArray(1)
            return d
        }
    }



    override fun recognizeImage(bitmap: Bitmap?): kotlin.collections.List<Recognition?>? {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")
        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.

        bitmap?.let { imgBitmap->
            imgBitmap.getPixels(intValues, 0, imgBitmap.width, 0, 0, imgBitmap.width, imgBitmap.height)
            imgData?.let {data->
                data.rewind()
                for (i in 0 until inputSize) {
                    for (j in 0 until inputSize) {
                        val pixelValue = intValues[i * inputSize + j]
                        if (isModelQuantized) {
                            // Quantized model
                            data.put((pixelValue shr 16 and 0xFF).toByte())
                            data.put((pixelValue shr 8 and 0xFF).toByte())
                            data.put((pixelValue and 0xFF).toByte())
                        } else { // Float model
                            data.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                            data.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                            data.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                        }
                    }
                }

            }
        }



        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        outputLocations = Array(1) {
            Array(NUM_DETECTIONS) {
                FloatArray(
                    4
                )
            }
        }
        outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        numDetections = FloatArray(1)
        val inputArray = arrayOf<Any?>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        tfLite?.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()

        // Show the best detections.
        // after scaling them back to the input size.
        // You need to use the number of detections from the output and not the NUM_DETECTONS variable
        // declared on top
        // because on some models, they don't always output the same total number of detections
        // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
        // If you don't use the output's numDetections, you'll get nonsensical data
        val numDetectionsOutput = Math.min(
            NUM_DETECTIONS,
            numDetections[0].toInt()
        ) // cast from float to integer, use min for safety
        val recognitions: ArrayList<Recognition> = ArrayList(numDetectionsOutput)
        for (i in 0 until numDetectionsOutput) {
            val detection = RectF(
                outputLocations[0][i][1] * inputSize,
                outputLocations[0][i][0] * inputSize,
                outputLocations[0][i][3] * inputSize,
                outputLocations[0][i][2] * inputSize
            )
            recognitions.add(
                Recognition(
                    "" + i, labels.get(outputClasses[0][i].toInt()), outputScores[0][i], detection
                )
            )
        }
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun enableStatLogging(logStats: Boolean) {}

    override fun getStatString(): String? {
        return ""
    }

    override fun close() {}

    override fun setNumThreads(num_threads: Int) {
        if (tfLite != null) tfLite?.setNumThreads(num_threads)
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        //if (tfLite != null) tfLite.setUseNNAPI(isChecked);
    }

}