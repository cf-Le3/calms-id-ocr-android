A list of known issues and other noteworthy information.

## Orientation changes
Currently, only the StartActivity has layout changes enabled.<br/>
An implementation that has layout changes enabled with appropriate instance state handling can be found in the reorientation branch, but has not been merged into master yet.

## Inaccurate results when photo is captured in landscape mode
It was observed that text was not properly recognized when the photo is captured when the phone was held in portrait mode.<br/>
This could be related to the MainActivity (where the CameraX instance is implemented) being locked to portrait mode.<br/>
Please test the implementation in the reorientation branch to see if it exhibits the intended behavior.

## Holographic layers interfere with results accuracy
When working with a document/card that has a holographic layer, please ensure that there are no reflections obscuring the text.<br/>

## The state of key information extraction
The application uses simple regular expressions to extract the document ID, which can be a 12 digit Malaysian NRIC (with approrpiately placed hyphens) or a passport number.<br/>
The regular expressions used are hard-coded in the Constants.java file. There is no current way for an end-user to customize the desired format for this field.<br/>
The passport number regular expression has been generalized to account for as many viable international passport numbers as possible, but is not comprehensive.
- Passport number is at least three characters long and at most nine characters long.
- Passport number can contain only digits.
- Passport number can be prefixed and/or suffixed with upper-case letters but must contain only digits in between. 
