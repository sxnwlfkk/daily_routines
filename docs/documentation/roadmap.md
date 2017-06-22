# Roadmap

## Minimum viable app v1.0

I can work on the app Fridays and maybe weekends a couple of hours. Max. weekly output 10 hours. I think, it should take around 40-50 hours to achieve this level. Plus probably testing.

Estimated time at 2017.02.10: 50 hours

### Data persistence
* App should be able to create new routines
  * Create and store routine items
* Delete existing routines
  * Delete routine items also

### Add new routine
* Set routine name
* Set routine item number
  * Generate that many item input field
* Save routine

### General UI
* App should display stored routines
  * Name
  * Lenght
  * Item count
* Clicking on a list item brings up the Statistics view

### Statistics UI
* Users should be able to view routines statistics
  * Items
    * Set time for the item
* Start button progresses the app to the Clock view

### Timer UI
* Timer UI shows before screenlock
* User can cancel routine without affecting statistics
* App shows a coundown clock widget with the item's remaining time and the carry time
* Clock behaviour
  * If item time goes to 0, carry time takes over
* User can continue to the next routine item
  * If time left in the previous item, it gets added to the carry time
  * If carry time progressed to negative in the previous item carry time gets redistributed between the remaining items according to time left in them
* User can go back to the previous item with the carry time, but the previous item time will be zero
