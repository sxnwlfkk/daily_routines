# Features

### Data persistence
* App should be able to create new routines
  * Create and store routine items
  * Delete routine items
  * Edit routine items
* Delete existing routines
* Edit existing routines
* Reorder the routines list

### Add/edit new routine
* Set routine name
* Set routine settings specified below
* Set routine items
  * Dynamically add item field if needed
  * Reorder set items

### General UI
* App should display stored routines
  * Name
  * Lenght
  * Item count
  * Optimal start time
  * Wakeup/alarm set
* Group routines
  * with submenus
  * or with headers and visual separators

### Statistics UI
* Users should be able to view routines statistics
  * Times used
  * Avg time users take to finish
  * Items
    * Set time for the item
    * Average time used on individual item
    * Colored green (or similar cold color) if avg time is less then the one set
    * Colored red (or similar warm color) if avg time is more then the one set
    * Indicate if item is not used
  * Routine settings
    * Require wakeup?
    * Reorder items
    * Should it end on a specific time?
    * Should it end after elapsed time?
    * Neither of the above

### Timer UI
* Timer UI shows before screenlock if set in settings
* User can start routine timer from main view or from statistics view
* User can finish routine early, while retaining usage statistics
* User can cancel routine without affecting statistics
* App shows a coundown clock widget with the item's remaining time and the carry time
* Clock behaviour
  * If item time goes to 0, carry time takes over
  * User can take time from carry time to the main clock (30 sec. intervals?)
    * Can cause carry time to go into negative
* User can continue to the next routine item
  * If time left in the previous item, it gets added to the carry time
  * If carry time progressed to negative in the previous item carry time gets redistributed between the remaining items according to time left in them
* User can go back to the previous item with the carry time, but the previous item time will be zero
* If set, the app should display ending view after time elapsed or if the clock hits specified time

### App settings
* Custom alarm sound
* Backup to cloud
  * Use Dropbox API
  * Backup to User memory
* Change color scheme
  * Day
  * Night
* Show clock before screen lock
