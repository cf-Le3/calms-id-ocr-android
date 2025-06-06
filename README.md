A list of known issues and other noteworthy information.

## Orientation changes
Currently, only the StartActivity has layout changes enabled.
An implementation that has layout changes enabled with appropriate instance state handling can be found in the reorientation branch, but has not been merged into master yet.

## Inaccurate results when photo is captured in landscape mode
It was observed that text was not properly recognized when the photo is captured when the phone was held in portrait mode.
This could be related to the MainActivity (where the CameraX instance is implemented) being locked to portrait mode.
Please test the implementation in the reorientation branch to see if it exhibits the intended behavior.

## Holographic layers interfere with results accuracy

## The state of key information extraction
