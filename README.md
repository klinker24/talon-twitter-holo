### Notice

This was the old version of Talon for Twitter. I have since open-sourced the Material Design version of the app. All future contributions will go to that version. This classic version should be considered "deprecated", but the project will remain up, for anyone that wants to look through it. 

The new version of Talon, can be found here: https://github.com/klinker24/talon-twitter-material

# Talon for Twitter (Classic) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Talon%20for%20Twitter-brightgreen.svg?style=flat)](http://android-arsenal.com/details/3/1067)

![Main Drawer](https://raw.githubusercontent.com/klinker24/Talon-for-Twitter/master/Other/Promo%20Stuff/Graphics/Classic/Final%20Promos/Feature%20graphic.png)

This is the complete version of the Twitter client that I created for Android. It is 100% open source, the only thing that you will have to do plug in is your own API keys.

I made the majority of this app when I was 19 years old, with one high school java class and half of a college one, and you can tell at some places. I know it isn't the prettiest code that you have ever seen, but it works, and it works pretty well! So don't judge too hard on the code, it definitely got better over time :)

As of now, it doesn't have too many comments throughout it, most of it is pretty easy to understand if you just dig for awhile, but as time goes on, I will go through and attempt to comment more of it.


### What you can use this for

There are some pretty neat things in this app, not just twitter related either. Someone looking at this can get the full rundown of things like:

1. theme engine
2. windowed advances
3. clickable links
4. emoji support
5. simple and extendable wearable app
6. plenty of other gems that Jacob and I spent a lot of time on.


Feel free to use this as a resource for those kinds of thing, that's one of the reasons I wanted to open source it in the first place. Lots to learn from a big project like this, and I think that is pretty important with the ever changing world of Android.

I ask that you do not redistribute this application for your own gain though. If you make a build and want to share it with people, that's great, that's what this should be for. Do not charge them for that build though. I have spent a hard year creating this project and this is what I ask if you choose to make it for yourself.


### Compiling Talon

Please don't try to compile it as an ANT build. I beg you, just use Android Studio or IntelliJ and compile it with Gradle. It will make your life so much easier and I will not be answering questions about dependencies and compiling for Eclipse. We spent a long time changing all of our projects over to Gradle and I want it to help some people.

To compile it:

1. Check out the project with a `git clone <clone URL>`
2. You can compile it from the command line by CDing into the folder then `./gradlew assembleDebug`
3. Import it to IntelliJ or Android Studio by going to File -> Import Project... then selecting the *build.gradle* file in the root of the project

To get your Twitter API key, go through these steps:

1. sign in on their developer site (https://apps.twitter.com/)
2. Click *Create New App* You will have to implement a callback url.
3. Choose a name, description, and website. These are all required and unique to your app, but it makes no difference what you call them. Anything will work here.
4. For the callback URL, you can do anything you like, but to have it work out of the box, use: *http://www.talonforandroid.com*
  * If you want a different one (stressing that it really DOES NOT matter..) then change it in the LoginActivity under com.klinker.android.twitter.ui.setup
5. Read and accept their *Rules of the Road*, then *Create your Twitter Application*
6. After it is created, you can change the icon and add some other info from the settings page.
7. You NEED to go to the *Permissions* page of the app and select the *Read, Write and Access direct messages* option, or else you won't be able to do anything but view your timeline.

##### Adding API Keys to the app

In the `.gitignore` file, I have ignored `secrets.properties` which should contain your keys. Go ahead, copy the `secrets.properties.sample` to `secrets.properties` and fill in the keys in it.

This allows me to keep the keys out of source control, but still build the app without any hassle.

##### Providing a Signing Configuration

For release builds, add your keystore simply as `keystore` to the root of the project, then add a `keystore.properties` file to the root with (no quotation marks around these strings!):

```
KEY_SIGNATURE=xxxx
KEY_ALIAS=xxxx
```

### Pull Requests

One of the reasons that I decided to open source this wasn't just because people would be able to learn from it. I also need help. There are somethings that I just don't know how to do any better. I don't have experience or knowledge yet to understand what is going wrong with them or why they randomly fail for some people.

I have done the absolute best I can with this app, but the more minds working on it, the better. Chances are if you are here and actually reading the readme, you have far more experience programming than me anyways and know how things can be improved.


### Issues

If you think something could be done better, then tell me. I am not saying that I will agree with you on it or that it will ever be the way you think it should be, but there is no hurt in asking.


### Wrap Up

There isn't to much more I have to say about this. I have put a ton of time and effort into this project and I truly hope that this helps someone out there. Take the leap, try something you never have before, see what you can learn from me and my mistakes.

Let me know if you have questions and I will answer them to the best of my ability.

Thanks and have fun with Talon!


Luke Klinker (Klinker Apps Lead Developer)




---

## License

    Copyright 2014 Luke Klinker

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
