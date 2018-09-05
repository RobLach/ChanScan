# ChanScan

![ChanScan Screenshots](https://i.imgur.com/HFlJSes.jpg)

## Features

* Thread image archiver (mass downloader)* Built-in proprietary animated GIF viewer
* One hand designed user interface 
* Captcha compatible posting support
* Image gallery generator and viewer
* Personal Bookmark Support
* Multiple language character support (Roman, Japanese, Russian)
* Imageboard optimized image caching
* Modifiable list of imageboards

## Development

ChanScan started as a simultaneous exploration of Android app development and web scraping that was eventually commercially released. 

The primary technical approach was to santize html into valid XML and parse it using an XML parsing library. Focusing the browser specifically on Futaba-style imageboards allowed the app to exploit the assumptions in formating to properly scrape the necesasry information. Furthermore many imageboards shared which software they were built on top of which meant that targeting the most popular packages greatly increased the capability of the application.

Furthermore, to deal with Android's limited Animated GIF rendering capacity, a proprietary renderer was created; a method of capturing and re-injecting Captcha challenges also had to be implemented to allow for posting; and the app also have the ability to load the list of accesible boards from an externally linked XML file which allowed for users to add their favorite boards by simply modifying and hosting the list on their own.

For More Information visit [Rob Lach's site](https://roblach.com/#chanscan).
