# KIMG
### This android app claims to be the best image editing assistant. There are a lot of features you can experience.
### (!) For the project to work, you need to integrate OpenCV.
#### How to integrate OpenCV module into the project:
* Watch this video: [click](https://www.youtube.com/watch?v=pRo4K3IyOW4) (00:00 - 05:28)
* Do everything as shown in the video
* Open **build.gradle.kts (Module :app)** 
* Change android -> compileOptions ```sourceCompatibility = JavaVersion.VERSION_17``` and ```targetCompatibility = JavaVersion.VERSION_17```
* Change android -> kotlinOptions ```jvmTarget = "17"```
* Open **build.gradle (Module :openCV)**
* Change android -> compileOptions ```sourceCompatibility JavaVersion.VERSION_17``` and ```targetCompatibility JavaVersion.VERSION_17```
* Sync the project
