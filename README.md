# Social-Distancing-App
A simple app and standard to broadcast your phone's current location and play a warning if another device running any compatible app is broadcasting that it's an unsafe distance away from you during the COVID-19 crisis


## What's Different
 While there are several other social distancing apps available, I created this one to target a few key features:
  - Minimal dependencies
  - No data collection on users
  - Compatibility with other apps

## How to Contribute
 - Issues: Pick an issue, create a fork, commit the patch and make a pull request. Please keep these changes small to expedite the review process
 - Bug Report: Create a new issue if you've discovered a bug. Provide as much detail as possible including (if applicable) steps to reproduce, phone data and Android version number
 - Forks/Compatible Apps: Feel free to fork this project or simply adopt its standardized messaging system into your own. I have no method for porting to iOS personally but have tried to make it as painless as possible to make the systems remain compatible
  
  Although the project is currently only available as an Android app, by keeping dependencies to an absolute minimum any phone with a GPS and access to the [Google Nearby Messaging API](https://developers.google.com/nearby/messages/overview) (compatible with both Android and iOS) can implement a compatible app so long as it follows the same standards
 
 ## The "standard" message
  The project's standardized message's forumla is simply to publish a string with the following format: 
  
  ```STD-COVID-SD <latitude> <longitude> <messages>>```
  
  Where spaces can be used to parse the message into a 3+ object array with STD-COVID-SD simply serving as a unique string to ensure the message was intended for another social distancing app and the other 2 items being used to compare against the current phone's location and play an alert if the threshold has been met. Any strings after this are optional to help provide additional data. These will need to be standardized at a later date


## Privacy Protection
 By using the Nearby Messaging API, only phones in radio range (approx. 100m at the most) will have any contact or access to the messages you broadcast. This also doesn't require users to register for any accounts nor does the project have any capabilities or interest in collecting user data
