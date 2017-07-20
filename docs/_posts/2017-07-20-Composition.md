---
title: Composition
author: sxnwlfkk
layout: modified
permalink: :title.html
date: 2017-07-20
---

Composition
===========

I have added a new feature, which enables you to compose routines. I think it's a good idea generally, to try to create smaller routines, and build them into bigger ones later.

With the compose feature, you can add an other routine in the editor menu. You can change it's position in the big routine, but you can't edit it here -- you'll have to go to the imported routine's profile. Also, you can import a routine, which is composed of several other routines, and some original items. Except you can't import a routine, which has a reference to the currently edited routine (because of infinite recursion loop reasons).

Here is a simple example, how I would use composition. Let's make a morning excerise routine:

1. Warm up, 2 min
2. Do some yoga, 5 min
3. Kettlebell swings, 5 min
4. Stretch, 2 min

Then you can add this to your weekday morning and weekend morning routines, without duplicating anything. This has the added benefit, that the accumulated average information is in one central place, the excercise routine. Also, if you decide, that you want to do pushups instead of yoga, you need to edit only one routine, and the change will be effective on all your morning routines.
