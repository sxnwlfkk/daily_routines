---
title: How to reorder main routine list?
author: sxnwlfkk
layout: modified
permalink: :title.html
date: 2017-06-23
---

__How to reorder main routine list?__

Reordering the main routine list is not something, which is explicitly supported right now. The default is the order, that you created them. I understand, that in some cases you might want to change this. Fortunately there is a way, which is sadly just a hack, not a real solution for the moment.

Basically you have to use the _cloning_ functionality of the routines. When you clone a routine, it gets copied to a new database row, which means it gets added to the bottom of the list. If you want to move something, clone and delete the original ones -- don't worry,  it won't change anything, they are perfect clones -- until you are happy with the order.

_This is a feature, which might get added in the future, but no promises there._
