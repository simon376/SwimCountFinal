# SwimCount
This the final submission made for the elective course App-Programming @ OTH Regensburg
This was my first completed app.
It's purpose is to count swum laps in a charity event.

The app uses the following key concepts:
- MVVM architectural design pattern (Model-View-ViewModel)
- Room database for locally stored user data
- LiveData to observe changes to the user data in the database
- using a RESTful web interface to upload data regularly
- Repository for single point of accessing Room db or web data
- recurring data uploads using WorkManager

The app has the following features:
- show list of swimmers currently in the swimming pool in a RecyclerView
- laps can easily be counted using buttons, data is persisted in a Room database and observed using LiveData
- you can view additional information, in a profile view when clicking on the user
- users can be searched online and added to the offline "cache" (database) by typing the ID or using a QR code
- changed data (swum laps) is automatically uploaded regularly
