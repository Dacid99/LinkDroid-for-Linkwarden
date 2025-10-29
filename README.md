> **!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!**
> 
> **Googles plans to mandate developer verification are threating the existance of the free distribution of apps on android.**
> 
> **If these plans come into effect [it will be the end of the f-droid store](https://f-droid.org/de/2025/09/29/google-developer-registration-decree.html)!**
> 
> **Please go to the page of the KeepAndroidOpen organization](https://keepandroidopen.org/) to see what you can do to prevent this from becoming a reality.**
> 
> *Should google win, I will not consent to relinquishing my personal information to them so this app will not be available on google verified devices any more.*
>
> **!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!**

# LinkDroid - Android App for Linkwarden

<img src="app/src/main/res/mipmap-xxxhdpi/ic_linkdroid.webp" float="left" width="60" alt="The linkdroid logo">

LinkDroid is a native Android application for the [Linkwarden](https://github.com/linkwarden/linkwarden) link management system, providing a seamless mobile experience for managing your links.

© 2023 - 2024 by Daniel Brendel  
© 2024 - by David Aderbauer  

![F-Droid Version](https://img.shields.io/f-droid/v/com.sbv.linkdroid)
![Downloads last month](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fgithub.com%2Fkitswas%2Ffdroid-metrics-dashboard%2Fraw%2Frefs%2Fheads%2Fmain%2Fprocessed%2Fmonthly%2Fcom.sbv.linkdroid.json&query=%24.total_downloads&logo=fdroid&label=Downloads%20last%20month)
![Translation status](https://img.shields.io/weblate/progress/linkdroid-for-linkwarden)

Released under the GPLv3, formerly under the MIT license

This repository is a fork of Daniel Brendel's Android app for HortusFox (https://github.com/danielbrendel/hortusfox-app-android), adapted for Linkwarden.

## Features

- Native Android interface for Linkwarden
- Easy link sharing and uploading
- Material Design 3 user interface
- Connection testing capability
- Dark mode support
- Responsive design for phones and tablets
- Built-in web view for seamless browsing
- Quick settings access through drawer interface

## Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="50">](https://f-droid.org/packages/com.sbv.linkdroid/)

Or get it directly from the [releases](https://gitlab.com/Dacid99/linkdroid-for-linkwarden/-/releases).

## System Requirements

- Android 5.0+ (minimum)  ->  will be bumped to 8.1 from the next release for new features!
- Android 13+ (recommended for best experience)
- Internet connection for syncing with Linkwarden server

## Configuration

1. Install the app
2. Open the settings drawer
3. Enter your Linkwarden server URL
4. Add your authentication token
5. Test the connection
6. Start using the app!

## Translation Support

We're actively looking for translators to help make LinkDroid accessible in more languages, particularly those already supported by Linkwarden:

- Italian (it)
- French (fr)
- Chinese (zh)

### How to Contribute Translations

*via weblate*

Go to [the project's weblate page](https://hosted.weblate.org/projects/linkdroid-for-linkwarden/) and add missing translations.

Feel free to add languages if one is missing! 

*via gitlab/github*

1. Fork this repository
2. Create a new directory in `app/src/main/res` named `values-{language-code}`
3. Copy `app/src/main/res/values/strings.xml` to the new directory
4. Translate the strings in the file
5. Submit a pull request (GitHub) or merge request (GitLab)

## Privacy & Security

- No tracking or analytics
- Local-first approach
- Direct connection to your Linkwarden instance
- No third-party services required

## Contributors

A big THANK YOU to everyone who helped with this project!

- Joe Mild 
  [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/d0dg3r)
  [![GitLab](https://img.shields.io/badge/GitLab-FCA121?style=flat&logo=gitlab&logoColor=white)](https://gitlab.com/d0dg3r)
  [![Website](https://img.shields.io/badge/Website-0076D6?style=flat&logo=internet-explorer&logoColor=white)](https://devops-geek.net/)

New contributors please read the [guidelines](https://gitlab.com/Dacid99/linkdroid-for-linkwarden/-/blob/main/CONTRIBUTING.md?ref_type=heads) to make life easier for everyone.

## Development

This app is built using:
- Native Android development with Java
- Material Design 3 components
- AndroidX libraries
- Modern Android architecture patterns

## License

This project is licensed under the GPLv3 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Original HortusFox app by Daniel Brendel
- Linkwarden team for the amazing link management system
- All contributors and translators

## Support and donating

I started this project to solve my own linkwarden on android issues and to give back to the open-source community.

If you want to support apps like LinkDroid please consider [donating to the f-droid project](https://f-droid.org/donate/), without them I would not have been able to distribute the app so easily and safely.

THANKYOU!