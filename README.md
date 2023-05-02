# Background Task Using Workmanager
# Download the APK file named "WorkManager-Latest"
# Click on the "Run background task" button
# A notification will pop up with instance count
# In order to view the number of files inserted follow the below steps
     1) In your mobile, turn on usb debugging mode in developer option
     2) In android studio,go to device file explorer->data->com.example.wm
     3) In database folder, download "my_database.db" file
     4) With the help of DB Browser software check the file count by executing "select count(*) from files_table"
     5) The total number of files is 1177
