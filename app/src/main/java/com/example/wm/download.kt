package com.example.wm
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream


class download(context: Context, workerParameters: WorkerParameters):
    Worker(context, workerParameters){

    private var notificationCounter = MainActivity.Companion.counter
    private var id = notificationCounter*100
    private val channelId = "channel $id"

    var filecount = 0

    override fun doWork(): Result {
        showNotification("File downloading from firebase")
        downloadFile()
        //SystemClock.sleep(10000)
        //showNotification("$filecount files inserted")
        return Result.success()
    }

    private fun downloadFile(){
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://my-firebase-project-57a96.appspot.com/")

        // Create a reference to "file"
        val mountainsRef = storageRef.child("newfile.zip")
        val localFile = File.createTempFile("work", "zip")


        mountainsRef.getFile(localFile).addOnSuccessListener {
            // File downloaded successfully

            // creating database object
            val dbHelper = DatabaseHelper(applicationContext)
            val db = dbHelper.writableDatabase

            // extracting zip file
            val zipInputStream = ZipInputStream(FileInputStream(localFile))
            var zipEntry = zipInputStream.nextEntry

            while (zipEntry != null) {
                // Process the contents of the zip entry
                val fileExtension = zipEntry.name.substringAfterLast(".")
                val contentType: String = when (fileExtension) {
                    "txt" -> "text/plain"
                    "html" -> "text/html"
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "zip" -> "application/zip"
                    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                    "pdf" -> "application/pdf"
                    else -> "application/octet-stream"
                }

                // Create a temporary file to store the contents of the entry
                val tempFile = File.createTempFile("temp", ".$fileExtension")
                val fileOutputStream = FileOutputStream(tempFile)

                // Read the contents of the entry and write it to the temporary file
                val buffer = ByteArray(zipEntry.size.toInt())
                var count = zipInputStream.read(buffer)
                while (count != -1) {
                    fileOutputStream.write(buffer, 0, count)
                    count = zipInputStream.read(buffer)
                }

                // Close the output stream and do something with the temporary file
                fileOutputStream.close()
                val values = ContentValues().apply {
                    put("file_name", tempFile.name)
                    put("file_path", tempFile.path)
                }
                val id = db?.insert("files_table", null, values)
                if(id!=-1L) {
                    // file inserted successfully
                    filecount++
                }
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.close()


        }.addOnFailureListener {
            // Handle failure
        }
    }

    private fun showNotification(string: String){
       val intent = Intent(applicationContext,MainActivity::class.java).apply { flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}

       val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(androidx.core.R.drawable.notification_icon_background) //ic_launcher_foreground
            .setContentTitle("Workmanager : File $notificationCounter")
            .setContentText(string)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "Workmanager"
            val channelDescription = "Background processing using workmanager"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description= channelDescription }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)

        }
        with(NotificationManagerCompat.from(applicationContext)){
            notify(id,notification.build())
        }
        id ++
    }


}

private const val DATABASE_NAME = "my_database.db"
private const val DATABASE_VERSION = 1
private const val SQL_CREATE_FILES_TABLE = """
        CREATE TABLE files_table (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        file_name TEXT,
        file_path TEXT
        )"""
private const val SQL_DROP_FILES_TABLE = "DROP TABLE IF EXISTS files_table"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private var instance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }



    override fun onCreate(db: SQLiteDatabase) {
        // create tables
        db.execSQL(SQL_CREATE_FILES_TABLE)
    }



    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // upgrade tables
        db.execSQL(SQL_DROP_FILES_TABLE)
        onCreate(db)
    }


}


